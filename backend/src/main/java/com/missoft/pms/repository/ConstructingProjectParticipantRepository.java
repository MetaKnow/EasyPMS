package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 在建项目参与人员仓库接口
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface ConstructingProjectParticipantRepository extends JpaRepository<ConstructingProjectParticipant, Long> {
    
    /**
     * 根据项目ID查询参与人员列表
     * 
     * @param projectId 项目ID
     * @return 参与人员列表
     */
    List<ConstructingProjectParticipant> findByProjectId(Long projectId);
    
    /**
     * 根据项目ID和用户ID检查是否为项目参与人
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否存在匹配的参与关系
     */
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    
    /**
     * 根据项目ID删除参与人员
     * 
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);
}
