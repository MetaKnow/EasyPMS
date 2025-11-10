package com.missoft.pms.service;

import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.repository.PersonalDevelopeRepository;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.repository.ConstructingProjectRepository;
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

    @Autowired
    private com.missoft.pms.repository.ProjectSstepRelationRepository projectSstepRelationRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructMilestoneService constructMilestoneService;

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

    /**
     * 函数级注释：根据项目ID删除该项目下所有个性化开发记录，并清理其关联的项目-步骤关系。
     * 场景：编辑项目时取消勾选“个性化功能开发”，需要同步删除 `personal_develope` 表记录，
     * 以及通过 `personalDevId` 关联的项目-步骤关系，避免遗留孤儿数据导致前端展示异常。
     *
     * @param projectId 项目ID
     * @return 删除的个性化开发记录数量
     */
    public int deleteByProjectId(Long projectId) {
        if (projectId == null) {
            return 0;
        }
        List<PersonalDevelope> list = personalDevelopeRepository.findByProjectId(projectId);
        if (list == null || list.isEmpty()) {
            return 0;
        }

        // 先删除与每个个性化开发项相关联的项目-步骤关系
        for (PersonalDevelope p : list) {
            Long devId = p != null ? p.getPersonalDevId() : null;
            if (devId != null) {
                var rels = projectSstepRelationRepository.findByProjectIdAndPersonalDevId(projectId, devId);
                if (rels != null && !rels.isEmpty()) {
                    projectSstepRelationRepository.deleteAll(rels);
                }
            }
        }

        // 删除个性化开发记录
        personalDevelopeRepository.deleteAll(list);
        // 同步项目里程碑集合与工期汇总
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project != null) {
            try {
                constructMilestoneService.syncMilestonesWithProjectSteps(project);
                constructMilestoneService.updateMilestonePeriodsForProject(projectId);
            } catch (Exception ignore) {}
        }
        return list.size();
    }

    /**
     * 函数级注释：按个性化开发ID删除该项并级联删除其生成的项目-步骤关系，同时刷新项目里程碑集合与工期汇总。
     * 业务语义：前端在个性化需求行点击“删除”后，应删除该需求条目以及其对应的“个性化功能开发”步骤集合。
     * @param personalDevId 个性化开发ID
     * @return 是否删除成功
     */
    public boolean deleteById(Long personalDevId) {
        if (personalDevId == null) {
            return false;
        }
        PersonalDevelope p = personalDevelopeRepository.findById(personalDevId).orElse(null);
        if (p == null) {
            return false;
        }
        Long projectId = p.getProjectId();
        // 删除该个性化开发关联的项目-步骤关系
        if (projectId != null) {
            var rels = projectSstepRelationRepository.findByProjectIdAndPersonalDevId(projectId, personalDevId);
            if (rels != null && !rels.isEmpty()) {
                projectSstepRelationRepository.deleteAll(rels);
            }
        }
        // 删除个性化记录
        personalDevelopeRepository.deleteById(personalDevId);
        // 同步项目里程碑集合与工期汇总
        if (projectId != null) {
            ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
            if (project != null) {
                try {
                    constructMilestoneService.syncMilestonesWithProjectSteps(project);
                    constructMilestoneService.updateMilestonePeriodsForProject(projectId);
                } catch (Exception ignore) {}
            }
        }
        return true;
    }
}