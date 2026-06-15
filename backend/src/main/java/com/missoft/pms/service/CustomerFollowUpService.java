package com.missoft.pms.service;

import com.missoft.pms.dto.CustomerFollowUpDTO;
import com.missoft.pms.entity.CustomerFollowUp;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.config.UserContextHolder;
import com.missoft.pms.repository.AfterServiceProjectParticipantRepository;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.CustomerFollowUpRepository;
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
public class CustomerFollowUpService {

    @Autowired
    private CustomerFollowUpRepository customerFollowUpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerFollowUpDeliverableFileService customerFollowUpDeliverableFileService;

    @Autowired
    private AfterServiceProjectRepository afterServiceProjectRepository;

    @Autowired
    private AfterServiceProjectParticipantRepository afterServiceProjectParticipantRepository;

    public Page<CustomerFollowUpDTO> getFollowUps(Long afterServiceProjectId, Pageable pageable) {
        return getFollowUps(afterServiceProjectId, null, pageable);
    }

    public Page<CustomerFollowUpDTO> getFollowUps(Long afterServiceProjectId, Long operatorUserId, Pageable pageable) {
        Page<CustomerFollowUp> page = customerFollowUpRepository.findByAfterServiceProjectId(afterServiceProjectId, pageable);
        return page.map(this::toDTO);
    }

    public List<CustomerFollowUpDTO> getFollowUpsAll(Long afterServiceProjectId) {
        return getFollowUpsAll(afterServiceProjectId, null);
    }

    public List<CustomerFollowUpDTO> getFollowUpsAll(Long afterServiceProjectId, Long operatorUserId) {
        return customerFollowUpRepository.findByAfterServiceProjectId(afterServiceProjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CustomerFollowUpDTO createFollowUp(CustomerFollowUpDTO dto) {
        validateDescription(dto);
        CustomerFollowUp entity = new CustomerFollowUp();
        BeanUtils.copyProperties(dto, entity);
        entity.setRecordId(null);
        Long operatorUserId = resolveOperatorUserId(null);
        if (operatorUserId != null) {
            entity.setCreateUser(operatorUserId);
            entity.setUpdateUser(operatorUserId);
        }
        CustomerFollowUp saved = customerFollowUpRepository.save(entity);
        return toDTO(saved);
    }

    public CustomerFollowUpDTO updateFollowUp(Long id, CustomerFollowUpDTO dto) {
        return updateFollowUp(id, dto, null);
    }

    public CustomerFollowUpDTO updateFollowUp(Long id, CustomerFollowUpDTO dto, Long operatorUserId) {
        validateDescription(dto);
        CustomerFollowUp entity = customerFollowUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow up not found"));
        Long originalCreateUser = entity.getCreateUser();
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        BeanUtils.copyProperties(dto, entity, "recordId", "createTime", "updateTime", "createUser", "updateUser");
        entity.setRecordId(id);
        entity.setCreateUser(originalCreateUser);
        if (resolvedOperatorUserId != null) {
            entity.setUpdateUser(resolvedOperatorUserId);
        }
        CustomerFollowUp saved = customerFollowUpRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteFollowUp(Long id) {
        deleteFollowUp(id, null);
    }

    public void deleteFollowUp(Long id, Long operatorUserId) {
        CustomerFollowUp entity = customerFollowUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow up not found"));
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        customerFollowUpDeliverableFileService.deleteFilesByRecordId(id);
        customerFollowUpRepository.deleteById(id);
    }

    private CustomerFollowUpDTO toDTO(CustomerFollowUp entity) {
        CustomerFollowUpDTO dto = new CustomerFollowUpDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getFollowUpPerson() != null) {
            userRepository.findById(entity.getFollowUpPerson())
                    .ifPresent(user -> dto.setFollowUpPersonName(user.getName()));
        }
        if (entity.getRecordId() != null) {
            dto.setHasFiles(customerFollowUpDeliverableFileService.hasFiles(entity.getRecordId()));
        }
        return dto;
    }

    private void validateDescription(CustomerFollowUpDTO dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("回访描述为必填");
        }
    }

    private Long resolveOperatorUserId(Long preferUserId) {
        if (preferUserId != null) {
            return preferUserId;
        }
        return UserContextHolder.getCurrentUserId();
    }

    private void assertParticipantCanModify(CustomerFollowUp entity, Long operatorUserId) {
        if (entity == null || operatorUserId == null) {
            return;
        }
        if (isSystemAdmin(operatorUserId)) {
            return;
        }
        Long projectId = entity.getAfterServiceProjectId();
        if (projectId == null) {
            return;
        }
        AfterServiceProject project = afterServiceProjectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return;
        }
        boolean isSaleDirector = java.util.Objects.equals(operatorUserId, project.getSaleDirector());
        boolean isServiceDirector = java.util.Objects.equals(operatorUserId, project.getServiceDirector());
        if (isSaleDirector || isServiceDirector) {
            return;
        }
        boolean isParticipant = afterServiceProjectParticipantRepository.existsByProjectIdAndUserId(projectId, operatorUserId);
        if (!isParticipant) {
            return;
        }
        boolean isCreator = java.util.Objects.equals(operatorUserId, entity.getCreateUser());
        if (!isCreator) {
            throw new RuntimeException("项目参与人仅可编辑或删除本人新增的回访记录");
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
