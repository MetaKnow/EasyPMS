package com.missoft.pms.repository;

import com.missoft.pms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 * 提供角色相关的数据库操作方法
 *
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查找角色
     * @param roleName 角色名称
     * @return 角色对象的 Optional 包装
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 检查角色名称是否存在
     * @param roleName 角色名称
     * @return true 表示存在，false 表示不存在
     */
    boolean existsByRoleName(String roleName);
}