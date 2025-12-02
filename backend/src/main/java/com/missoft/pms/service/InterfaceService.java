package com.missoft.pms.service;

import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.service.ProjectSstepRelationService;
import com.missoft.pms.repository.InterfaceRepository;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/**
 * 接口管理服务（类级注释：面向接口表的CRUD及关联处理）
 * 负责接口实体的创建、查询与按项目删除等操作。
 */
public class InterfaceService {
    private final InterfaceRepository repository;
    private final ProjectSstepRelationService relationService;
    private final ProjectSstepRelationRepository relationRepository;
    private final ConstructingProjectRepository constructingProjectRepository;
    private final ConstructMilestoneService constructMilestoneService;

    /**
     * 构造函数
     * @param repository 接口仓库
     * @param relationService 项目步骤关系服务（用于在接口创建后生成四个“接口开发”步骤）
     */
    public InterfaceService(
            InterfaceRepository repository,
            ProjectSstepRelationService relationService,
            ProjectSstepRelationRepository relationRepository,
            ConstructingProjectRepository constructingProjectRepository,
            ConstructMilestoneService constructMilestoneService
    ) {
        this.repository = repository;
        this.relationService = relationService;
        this.relationRepository = relationRepository;
        this.constructingProjectRepository = constructingProjectRepository;
        this.constructMilestoneService = constructMilestoneService;
    }

    /**
     * 创建接口实体，并在保存成功后为该项目生成四个“接口开发”步骤关系
     * 生成逻辑：按项目的产品(systemName)筛选标准步骤类型为“接口开发”，且所属里程碑为“05完成接口开发集成”
     * @param entity 接口实体
     * @return 保存后的接口实体
     */
    public InterfaceEntity create(InterfaceEntity entity) {
        // 函数级注释：禁止在“已完成”项目下新增接口需求
        if (entity == null || entity.getProjectId() == null) {
            throw new IllegalArgumentException("缺少项目ID，无法创建接口");
        }
        ConstructingProject project = constructingProjectRepository.findById(entity.getProjectId()).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在，无法创建接口");
        }
        String state = project.getProjectState();
        if ("已完成".equals(state)) {
            throw new IllegalStateException("已完成项目不可新增接口需求");
        }
        InterfaceEntity saved = repository.save(entity);
        // 接口创建成功后，生成对应的四个步骤关系
        relationService.generateRelationsForInterface(saved);
        return saved;
    }

    public List<InterfaceEntity> listByProject(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    /**
     * 删除指定项目下的所有接口数据（函数级注释：编辑项目取消“接口开发”后的数据清理）
     * 当项目编辑时取消勾选“接口开发”，需要同步清空该项目在 interface 表中的记录。
     * @param projectId 项目ID
     * @return 实际删除的接口数量
     */
    public int deleteInterfacesByProject(Long projectId) {
        if (projectId == null) {
            return 0;
        }
        List<InterfaceEntity> interfaces = repository.findByProjectId(projectId);
        if (interfaces == null || interfaces.isEmpty()) {
            return 0;
        }
        // 先清理与这些接口相关的项目-步骤关系
        for (InterfaceEntity iface : interfaces) {
            Long iid = iface != null ? iface.getInterfaceId() : null;
            if (iid != null) {
                List<ProjectSstepRelation> rels = relationRepository.findByProjectIdAndInterfaceId(projectId, iid);
                if (rels != null && !rels.isEmpty()) {
                    relationRepository.deleteAll(rels);
                }
            }
        }
        // 删除接口记录
        repository.deleteAll(interfaces);
        // 同步项目里程碑集合与工期汇总
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project != null) {
            try {
                constructMilestoneService.syncMilestonesWithProjectSteps(project);
                constructMilestoneService.updateMilestonePeriodsForProject(projectId);
            } catch (Exception ignore) {}
        }
        return interfaces.size();
    }

    /**
     * 函数级注释：按接口ID删除该接口并级联删除其生成的项目-步骤关系，同时刷新项目里程碑集合与工期汇总。
     * 业务语义：前端在接口信息行点击“删除”后，应删除该接口条目以及其对应的“接口开发”步骤集合。
     * @param interfaceId 接口ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteInterfaceById(Long interfaceId) {
        if (interfaceId == null) {
            return false;
        }
        InterfaceEntity iface = repository.findById(interfaceId).orElse(null);
        if (iface == null) {
            return false;
        }
        Long projectId = iface.getProjectId();
        // 函数级注释：禁止在“已完成”项目下删除接口需求
        if (projectId != null) {
            ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
            if (project != null && "已完成".equals(project.getProjectState())) {
                throw new IllegalStateException("已完成项目不可删除接口需求");
            }
        }
        // 删除该接口关联的项目-步骤关系
        if (projectId != null) {
            List<ProjectSstepRelation> rels = relationRepository.findByProjectIdAndInterfaceId(projectId, interfaceId);
            if (rels != null && !rels.isEmpty()) {
                relationRepository.deleteAll(rels);
            }
        }
        // 删除接口记录
        repository.deleteById(interfaceId);
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
