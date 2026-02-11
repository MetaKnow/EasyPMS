package com.missoft.pms.service;

import com.missoft.pms.dto.CustomerFollowUpDTO;
import com.missoft.pms.entity.CustomerFollowUp;
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

    public Page<CustomerFollowUpDTO> getFollowUps(Long afterServiceProjectId, Pageable pageable) {
        Page<CustomerFollowUp> page = customerFollowUpRepository.findByAfterServiceProjectId(afterServiceProjectId, pageable);
        return page.map(this::toDTO);
    }

    public List<CustomerFollowUpDTO> getFollowUpsAll(Long afterServiceProjectId) {
        return customerFollowUpRepository.findByAfterServiceProjectId(afterServiceProjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CustomerFollowUpDTO createFollowUp(CustomerFollowUpDTO dto) {
        validateDescription(dto);
        CustomerFollowUp entity = new CustomerFollowUp();
        BeanUtils.copyProperties(dto, entity);
        entity.setRecordId(null);
        CustomerFollowUp saved = customerFollowUpRepository.save(entity);
        return toDTO(saved);
    }

    public CustomerFollowUpDTO updateFollowUp(Long id, CustomerFollowUpDTO dto) {
        validateDescription(dto);
        CustomerFollowUp entity = customerFollowUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow up not found"));
        BeanUtils.copyProperties(dto, entity, "recordId", "createTime", "updateTime");
        entity.setRecordId(id);
        CustomerFollowUp saved = customerFollowUpRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteFollowUp(Long id) {
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
}
