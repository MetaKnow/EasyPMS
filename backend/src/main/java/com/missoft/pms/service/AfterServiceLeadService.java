package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceLeadDTO;
import com.missoft.pms.entity.AfterServiceLead;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.config.UserContextHolder;
import com.missoft.pms.repository.AfterServiceLeadRepository;
import com.missoft.pms.repository.AfterServiceProjectParticipantRepository;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AfterServiceLeadService {

    @Autowired
    private AfterServiceLeadRepository afterServiceLeadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AfterServiceLeadDeliverableFileService afterServiceLeadDeliverableFileService;

    @Autowired
    private AfterServiceProjectRepository afterServiceProjectRepository;

    @Autowired
    private AfterServiceProjectParticipantRepository afterServiceProjectParticipantRepository;

    @Transactional(readOnly = true)
    public Page<AfterServiceLeadDTO> getLeads(Long afterServiceProjectId, Pageable pageable) {
        return getLeads(afterServiceProjectId, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AfterServiceLeadDTO> getLeads(Long afterServiceProjectId, Long operatorUserId, Pageable pageable) {
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        Page<AfterServiceLead> page;
        if (shouldParticipantViewOwnOnly(afterServiceProjectId, resolvedOperatorUserId)) {
            page = afterServiceLeadRepository.findByAfterServiceProjectIdAndCreateUser(afterServiceProjectId, resolvedOperatorUserId, pageable);
        } else {
            page = afterServiceLeadRepository.findByAfterServiceProjectId(afterServiceProjectId, pageable);
        }
        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AfterServiceLeadDTO> getLeadsAll(Long afterServiceProjectId) {
        return getLeadsAll(afterServiceProjectId, null);
    }

    @Transactional(readOnly = true)
    public List<AfterServiceLeadDTO> getLeadsAll(Long afterServiceProjectId, Long operatorUserId) {
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        List<AfterServiceLead> list;
        if (shouldParticipantViewOwnOnly(afterServiceProjectId, resolvedOperatorUserId)) {
            list = afterServiceLeadRepository.findByAfterServiceProjectIdAndCreateUser(afterServiceProjectId, resolvedOperatorUserId);
        } else {
            list = afterServiceLeadRepository.findByAfterServiceProjectId(afterServiceProjectId);
        }
        return list
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AfterServiceLeadDTO createLead(AfterServiceLeadDTO dto) {
        validateLeadDto(dto);
        AfterServiceLead entity = new AfterServiceLead();
        BeanUtils.copyProperties(dto, entity);
        entity.setLeadsId(null);
        Long operatorUserId = resolveOperatorUserId(null);
        if (operatorUserId != null) {
            entity.setCreateUser(operatorUserId);
            entity.setUpdateUser(operatorUserId);
        }
        AfterServiceLead saved = afterServiceLeadRepository.save(entity);
        return toDTO(saved);
    }

    public AfterServiceLeadDTO updateLead(Long id, AfterServiceLeadDTO dto) {
        return updateLead(id, dto, null);
    }

    public AfterServiceLeadDTO updateLead(Long id, AfterServiceLeadDTO dto, Long operatorUserId) {
        validateLeadDto(dto);
        AfterServiceLead entity = afterServiceLeadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        Long originalCreateUser = entity.getCreateUser();
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        BeanUtils.copyProperties(dto, entity, "leadsId", "createTime", "updateTime", "createUser", "updateUser");
        entity.setLeadsId(id);
        entity.setCreateUser(originalCreateUser);
        if (resolvedOperatorUserId != null) {
            entity.setUpdateUser(resolvedOperatorUserId);
        }
        AfterServiceLead saved = afterServiceLeadRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteLead(Long id) {
        deleteLead(id, null);
    }

    public void deleteLead(Long id, Long operatorUserId) {
        AfterServiceLead entity = afterServiceLeadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        afterServiceLeadDeliverableFileService.deleteFilesByLeadId(id);
        afterServiceLeadRepository.deleteById(id);
    }

    private AfterServiceLeadDTO toDTO(AfterServiceLead entity) {
        AfterServiceLeadDTO dto = new AfterServiceLeadDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getLeadsFinder() != null) {
            userRepository.findById(entity.getLeadsFinder())
                    .ifPresent(user -> dto.setLeadsFinderName(user.getName()));
        }
        if (entity.getLeadsId() != null) {
            dto.setHasFiles(afterServiceLeadDeliverableFileService.hasFiles(entity.getLeadsId()));
        }
        return dto;
    }

    private void validateLeadDto(AfterServiceLeadDTO dto) {
        if (dto == null) {
            throw new RuntimeException("线索数据不能为空");
        }
        if (dto.getLeadsSource() == null || dto.getLeadsSource().trim().isEmpty()) {
            throw new RuntimeException("线索来源不能为空");
        }
        if (dto.getLeadsFinder() == null) {
            throw new RuntimeException("线索挖掘人不能为空");
        }
        if (dto.getLeadsDescript() == null || dto.getLeadsDescript().trim().isEmpty()) {
            throw new RuntimeException("线索描述不能为空");
        }
    }

    private Long resolveOperatorUserId(Long preferUserId) {
        if (preferUserId != null) {
            return preferUserId;
        }
        return UserContextHolder.getCurrentUserId();
    }

    private boolean shouldParticipantViewOwnOnly(Long projectId, Long operatorUserId) {
        if (projectId == null || operatorUserId == null) {
            return false;
        }
        if (isSystemAdmin(operatorUserId)) {
            return false;
        }
        AfterServiceProject project = afterServiceProjectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return false;
        }
        boolean isSaleDirector = java.util.Objects.equals(operatorUserId, project.getSaleDirector());
        boolean isServiceDirector = java.util.Objects.equals(operatorUserId, project.getServiceDirector());
        if (isSaleDirector || isServiceDirector) {
            return false;
        }
        return afterServiceProjectParticipantRepository.existsByProjectIdAndUserId(projectId, operatorUserId);
    }

    private void assertParticipantCanModify(AfterServiceLead entity, Long operatorUserId) {
        if (entity == null || operatorUserId == null) {
            return;
        }
        Long projectId = entity.getAfterServiceProjectId();
        if (!shouldParticipantViewOwnOnly(projectId, operatorUserId)) {
            return;
        }
        boolean isCreator = java.util.Objects.equals(operatorUserId, entity.getCreateUser());
        if (!isCreator) {
            throw new RuntimeException("项目参与人仅可编辑或删除本人新增的销售线索");
        }
    }

    private boolean isSystemAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            return false;
        }
        var user = userRepository.findById(operatorUserId).orElse(null);
        if (user == null || user.getUserName() == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(user.getUserName().trim());
    }
}
