package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 运维项目参与人员仓库接口
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface AfterServiceProjectParticipantRepository extends JpaRepository<AfterServiceProjectParticipant, Long> {
    
    /**
     * 根据项目ID查询参与人员列表
     * 
     * @param projectId 项目ID
     * @return 参与人员列表
     */
    List<AfterServiceProjectParticipant> findByProjectId(Long projectId);
    
    /**
     * 根据项目ID删除参与人员
     * 
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);
}
