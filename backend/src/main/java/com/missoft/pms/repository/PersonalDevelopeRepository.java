package com.missoft.pms.repository;

import com.missoft.pms.entity.PersonalDevelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 类级注释：
 * 个性化开发仓库接口，提供 `personal_develope` 表的基础CRUD操作。
 */
@Repository
public interface PersonalDevelopeRepository extends JpaRepository<PersonalDevelope, Long> {

    /**
     * 函数级注释：按项目ID查询该项目下的个性化开发列表
     * @param projectId 项目ID
     * @return 个性化开发列表
     */
    List<PersonalDevelope> findByProjectId(Long projectId);
}