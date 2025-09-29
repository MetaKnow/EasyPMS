package com.missoft.pms.repository;

import com.missoft.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 提供用户相关的数据库操作方法
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param userName 用户名
     * @return 用户对象的Optional包装
     */
    Optional<User> findByUserName(String userName);
    
    /**
     * 检查用户名是否存在
     * 
     * @param userName 用户名
     * @return true表示存在，false表示不存在
     */
    boolean existsByUserName(String userName);
    
    /**
     * 根据用户名和锁定状态查找用户
     * 
     * @param userName 用户名
     * @param locked 锁定状态 (0-正常, 1-锁定)
     * @return 用户对象的Optional包装
     */
    Optional<User> findByUserNameAndLocked(String userName, Integer locked);
    
    /**
     * 根据角色ID查找用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.roleId = :roleId")
    java.util.List<User> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据组织ID查找用户列表
     * 
     * @param organId 组织ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.organId = :organId")
    java.util.List<User> findByOrganId(@Param("organId") Long organId);

    // 分页版本：根据组织ID查找用户列表（函数级注释：用于 /api/users 分页接口）
    Page<User> findByOrganId(@Param("organId") Long organId, Pageable pageable);

    // 复合条件分页查询（函数级注释：支持 organId 与 keyword 可选条件）
    @Query("SELECT u FROM User u WHERE (:organId IS NULL OR u.organId = :organId) AND (" +
           "(:keyword IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<User> searchUsers(@Param("organId") Long organId, @Param("keyword") String keyword, Pageable pageable);

    // 统计指定组织下的用户数量（已存在方法，确保保留）
    @Query("SELECT COUNT(u) FROM User u WHERE u.organId = :organId")
    long countByOrganId(@Param("organId") Long organId);
    
    /**
     * 统计指定角色下的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.roleId = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);
}