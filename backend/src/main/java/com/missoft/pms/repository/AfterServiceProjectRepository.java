package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 运维项目Repository
 */
@Repository
public interface AfterServiceProjectRepository extends JpaRepository<AfterServiceProject, Long>, JpaSpecificationExecutor<AfterServiceProject> {
    boolean existsByProjectNum(String projectNum);
}
