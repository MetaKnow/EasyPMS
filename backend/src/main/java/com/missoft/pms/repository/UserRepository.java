package com.missoft.pms.repository;

import com.missoft.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}