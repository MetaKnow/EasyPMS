package com.missoft.pms.repository;

import com.missoft.pms.entity.ArchieveSoft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 软件产品仓库接口
 * 提供对archieveSoft表的数据库操作
 */
@Repository
public interface ArchieveSoftRepository extends JpaRepository<ArchieveSoft, Long> {

    /**
     * 根据软件名称查询产品（分页）
     *
     * @param softName 软件名称
     * @param pageable 分页参数
     * @return 产品分页数据
     */
    Page<ArchieveSoft> findBySoftNameContainingIgnoreCase(String softName, Pageable pageable);

    /**
     * 根据软件版本查询产品（分页）
     *
     * @param softVersion 软件版本
     * @param pageable    分页参数
     * @return 产品分页数据
     */
    Page<ArchieveSoft> findBySoftVersionContainingIgnoreCase(String softVersion, Pageable pageable);

    /**
     * 多条件查询产品
     *
     * @param softName    软件名称（可选）
     * @param softVersion 软件版本（可选）
     * @param pageable    分页参数
     * @return 产品分页数据
     */
    @Query("SELECT a FROM ArchieveSoft a WHERE " +
           "(:softName IS NULL OR LOWER(a.softName) LIKE LOWER(CONCAT('%', :softName, '%'))) AND " +
           "(:softVersion IS NULL OR LOWER(a.softVersion) LIKE LOWER(CONCAT('%', :softVersion, '%')))")
    Page<ArchieveSoft> findByMultipleConditions(
            @Param("softName") String softName,
            @Param("softVersion") String softVersion,
            Pageable pageable);

    /**
     * 检查软件名称是否已存在（排除指定ID）
     *
     * @param softName 软件名称
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean existsBySoftNameIgnoreCaseAndSoftIdNot(String softName, Long excludeId);

    /**
     * 检查软件名称是否已存在
     *
     * @param softName 软件名称
     * @return 是否存在
     */
    boolean existsBySoftNameIgnoreCase(String softName);

    /**
     * 获取所有产品列表
     *
     * @return 产品列表
     */
    List<ArchieveSoft> findAllByOrderBySoftNameAsc();

    /**
     * 检查名称与版本组合是否已存在（不区分大小写）
     *
     * @param softName    软件名称
     * @param softVersion 软件版本
     * @return 是否存在
     */
    boolean existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCase(String softName, String softVersion);

    /**
     * 检查名称与版本组合是否已存在（排除指定ID，不区分大小写）
     *
     * @param softName    软件名称
     * @param softVersion 软件版本
     * @param excludeId   需要排除的产品ID
     * @return 是否存在
     */
    boolean existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCaseAndSoftIdNot(String softName, String softVersion, Long excludeId);
}