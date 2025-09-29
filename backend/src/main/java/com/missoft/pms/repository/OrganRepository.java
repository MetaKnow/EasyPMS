package com.missoft.pms.repository;

import com.missoft.pms.entity.Organ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机构数据访问接口
 * 提供机构相关的数据库操作方法
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface OrganRepository extends JpaRepository<Organ, Long> {
    
    /**
     * 根据机构名称查找机构
     * 
     * @param organName 机构名称
     * @return 机构对象的Optional包装
     */
    Optional<Organ> findByOrganName(String organName);
    
    /**
     * 检查机构名称是否存在
     * 
     * @param organName 机构名称
     * @return true表示存在，false表示不存在
     */
    boolean existsByOrganName(String organName);
    
    /**
     * 根据父机构ID查找子机构列表
     * 
     * @param parentOrganId 父机构ID
     * @return 子机构列表
     */
    List<Organ> findByParentOrganId(Long parentOrganId);
    
    /**
     * 查找所有顶级机构（父机构ID为null）
     * 
     * @return 顶级机构列表
     */
    List<Organ> findByParentOrganIdIsNull();
    
    /**
     * 根据机构路径查找机构
     * 
     * @param path 机构路径
     * @return 机构列表
     */
    List<Organ> findByPathContaining(String path);
    
    /**
     * 检查机构是否有子机构
     * 
     * @param organId 机构ID
     * @return true表示有子机构，false表示没有
     */
    @Query("SELECT COUNT(o) > 0 FROM Organ o WHERE o.parentOrganId = :organId")
    boolean hasChildren(@Param("organId") Long organId);
    
    /**
     * 获取机构的所有子机构（递归）
     * 
     * @param organId 机构ID
     * @return 子机构列表
     */
    @Query("SELECT o FROM Organ o WHERE o.path LIKE CONCAT('%/', :organId, '/%') OR o.path LIKE CONCAT(:organId, '/%') OR o.path LIKE CONCAT('%/', :organId) OR o.organId = :organId")
    List<Organ> findAllDescendants(@Param("organId") Long organId);
    
    /**
     * 根据机构名称模糊查询
     * 
     * @param organName 机构名称关键字
     * @return 机构列表
     */
    @Query("SELECT o FROM Organ o WHERE o.organName LIKE %:organName%")
    List<Organ> findByOrganNameContaining(@Param("organName") String organName);
    
    /**
     * 统计指定机构的子机构数量（函数级注释：返回 organId 对应的直接子机构数量）
     * @param organId 机构ID
     * @return 子机构数量
     */
    @Query("SELECT COUNT(o) FROM Organ o WHERE o.parentOrganId = :organId")
    long countChildren(@Param("organId") Long organId);
}