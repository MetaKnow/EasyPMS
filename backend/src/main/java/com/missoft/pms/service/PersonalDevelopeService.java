package com.missoft.pms.service;

import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.repository.PersonalDevelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 类级注释：
 * 个性化开发服务，负责创建与查询，并在创建后生成对应的项目-步骤关系。
 */
@Service
@Transactional
public class PersonalDevelopeService {

    @Autowired
    private PersonalDevelopeRepository personalDevelopeRepository;

    @Autowired
    private ProjectSstepRelationService projectSstepRelationService;

    /**
     * 函数级注释：创建个性化开发条目，并生成“个性化功能开发”里程碑对应的步骤关系。
     * @param entity 个性化开发实体，需包含 `projectId` 与 `milestoneId`，`personalDevName` 不为空
     * @return 保存后的实体
     */
    public PersonalDevelope create(PersonalDevelope entity) {
        if (entity == null || entity.getProjectId() == null || entity.getMilestoneId() == null) {
            throw new IllegalArgumentException("缺少项目或里程碑信息，无法创建个性化开发");
        }
        if (!StringUtils.hasText(entity.getPersonalDevName())) {
            throw new IllegalArgumentException("个性化开发名称不能为空");
        }
        PersonalDevelope saved = personalDevelopeRepository.save(entity);
        // 生成对应的个性化开发步骤关系（按产品 + 里程碑筛选）
        projectSstepRelationService.generateRelationsForPersonalDevelope(saved);
        return saved;
    }

    /**
     * 函数级注释：按项目ID查询个性化开发列表
     * @param projectId 项目ID
     * @return 该项目下的个性化开发条目
     */
    @Transactional(readOnly = true)
    public List<PersonalDevelope> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return personalDevelopeRepository.findByProjectId(projectId);
    }
}