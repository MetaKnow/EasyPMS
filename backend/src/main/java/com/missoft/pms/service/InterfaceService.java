package com.missoft.pms.service;

import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.service.ProjectSstepRelationService;
import com.missoft.pms.repository.InterfaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 接口管理服务（类级注释：面向接口表的CRUD及关联处理）
 * 负责接口实体的创建、查询与按项目删除等操作。
 */
public class InterfaceService {
    private final InterfaceRepository repository;
    private final ProjectSstepRelationService relationService;

    /**
     * 构造函数
     * @param repository 接口仓库
     * @param relationService 项目步骤关系服务（用于在接口创建后生成四个“接口开发”步骤）
     */
    public InterfaceService(InterfaceRepository repository, ProjectSstepRelationService relationService) {
        this.repository = repository;
        this.relationService = relationService;
    }

    /**
     * 创建接口实体，并在保存成功后为该项目生成四个“接口开发”步骤关系
     * 生成逻辑：按项目的产品(systemName)筛选标准步骤类型为“接口开发”，且所属里程碑为“05完成接口开发集成”
     * @param entity 接口实体
     * @return 保存后的接口实体
     */
    public InterfaceEntity create(InterfaceEntity entity) {
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
        repository.deleteAll(interfaces);
        return interfaces.size();
    }
}