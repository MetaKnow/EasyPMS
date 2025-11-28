package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceProjectDTO;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.CustomerRepository;
import com.missoft.pms.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运维项目服务
 */
@Service
@Transactional
public class AfterServiceProjectService {

    @Autowired
    private AfterServiceProjectRepository afterServiceProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 分页查询运维项目
     */
    public Page<AfterServiceProjectDTO> getProjects(String projectName, String status, Long saleDirector, Pageable pageable) {
        Specification<AfterServiceProject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(projectName)) {
                predicates.add(cb.like(root.get("projectName"), "%" + projectName + "%"));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("serviceState"), status));
            }

            if (saleDirector != null) {
                predicates.add(cb.equal(root.get("saleDirector"), saleDirector));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AfterServiceProject> page = afterServiceProjectRepository.findAll(spec, pageable);
        return page.map(this::convertToDTO);
    }

    /**
     * 获取单个项目
     */
    public AfterServiceProjectDTO getProjectById(Long id) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDTO(project);
    }

    /**
     * 创建项目
     */
    public AfterServiceProjectDTO createProject(AfterServiceProjectDTO dto) {
        AfterServiceProject project = new AfterServiceProject();
        BeanUtils.copyProperties(dto, project);
        project.setProjectId(null); // Ensure new
        AfterServiceProject saved = afterServiceProjectRepository.save(project);
        return convertToDTO(saved);
    }

    /**
     * 更新项目
     */
    public AfterServiceProjectDTO updateProject(Long id, AfterServiceProjectDTO dto) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        BeanUtils.copyProperties(dto, project, "projectId", "createTime", "updateTime");
        project.setProjectId(id);
        
        AfterServiceProject saved = afterServiceProjectRepository.save(project);
        return convertToDTO(saved);
    }

    /**
     * 删除项目
     */
    public void deleteProject(Long id) {
        afterServiceProjectRepository.deleteById(id);
    }

    /**
     * 批量删除项目
     */
    public void batchDeleteProjects(List<Long> ids) {
        afterServiceProjectRepository.deleteAllById(ids);
    }

    /**
     * 实体转DTO
     */
    private AfterServiceProjectDTO convertToDTO(AfterServiceProject entity) {
        AfterServiceProjectDTO dto = new AfterServiceProjectDTO();
        BeanUtils.copyProperties(entity, dto);
        
        if (entity.getSaleDirector() != null) {
            userRepository.findById(entity.getSaleDirector())
                    .ifPresent(user -> dto.setSaleDirectorName(user.getName()));
        }

        if (entity.getCustomerId() != null) {
            customerRepository.findById(entity.getCustomerId())
                    .ifPresent(customer -> dto.setCustomerName(customer.getCustomerName()));
        }
        
        return dto;
    }
}
