package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructDeliverableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目交付物文件仓库接口
 */
@Repository
public interface ConstructDeliverableFileRepository extends JpaRepository<ConstructDeliverableFile, Long> {

    /**
     * 根据项目ID和交付物ID查询文件列表
     */
    List<ConstructDeliverableFile> findByProjectIdAndDeliverableId(Long projectId, Long deliverableId);

    /**
     * 根据项目ID查询文件列表
     */
    List<ConstructDeliverableFile> findByProjectId(Long projectId);
}