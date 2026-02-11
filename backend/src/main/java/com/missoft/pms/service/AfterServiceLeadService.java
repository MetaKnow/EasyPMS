package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceLeadDTO;
import com.missoft.pms.entity.AfterServiceLead;
import com.missoft.pms.repository.AfterServiceLeadRepository;
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

    @Transactional(readOnly = true)
    public Page<AfterServiceLeadDTO> getLeads(Long afterServiceProjectId, Pageable pageable) {
        Page<AfterServiceLead> page = afterServiceLeadRepository.findByAfterServiceProjectId(afterServiceProjectId, pageable);
        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AfterServiceLeadDTO> getLeadsAll(Long afterServiceProjectId) {
        return afterServiceLeadRepository.findByAfterServiceProjectId(afterServiceProjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AfterServiceLeadDTO createLead(AfterServiceLeadDTO dto) {
        validateLeadDto(dto);
        AfterServiceLead entity = new AfterServiceLead();
        BeanUtils.copyProperties(dto, entity);
        entity.setLeadsId(null);
        AfterServiceLead saved = afterServiceLeadRepository.save(entity);
        return toDTO(saved);
    }

    public AfterServiceLeadDTO updateLead(Long id, AfterServiceLeadDTO dto) {
        validateLeadDto(dto);
        AfterServiceLead entity = afterServiceLeadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        BeanUtils.copyProperties(dto, entity, "leadsId", "createTime", "updateTime");
        entity.setLeadsId(id);
        AfterServiceLead saved = afterServiceLeadRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteLead(Long id) {
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
}
