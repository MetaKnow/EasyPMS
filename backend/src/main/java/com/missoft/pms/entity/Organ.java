package com.missoft.pms.entity;

import jakarta.persistence.*;

/**
 * 机构实体类
 * 对应数据库中的organ表
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "organ")
public class Organ {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organId")
    private Long organId;
    
    @Column(name = "organName", nullable = false, length = 200)
    private String organName;
    
    @Column(name = "parentOrganId")
    private Long parentOrganId;
    
    @Column(name = "path", length = 500)
    private String path;
    
    /**
     * 默认构造函数
     */
    public Organ() {}
    
    /**
     * 构造函数
     * 
     * @param organName 机构名称
     * @param parentOrganId 父机构ID
     * @param path 机构路径
     */
    public Organ(String organName, Long parentOrganId, String path) {
        this.organName = organName;
        this.parentOrganId = parentOrganId;
        this.path = path;
    }
    
    /**
     * 获取机构ID
     * @return 机构ID
     */
    public Long getOrganId() {
        return organId;
    }
    
    /**
     * 设置机构ID
     * @param organId 机构ID
     */
    public void setOrganId(Long organId) {
        this.organId = organId;
    }
    
    /**
     * 获取机构名称
     * @return 机构名称
     */
    public String getOrganName() {
        return organName;
    }
    
    /**
     * 设置机构名称
     * @param organName 机构名称
     */
    public void setOrganName(String organName) {
        this.organName = organName;
    }
    
    /**
     * 获取父机构ID
     * @return 父机构ID
     */
    public Long getParentOrganId() {
        return parentOrganId;
    }
    
    /**
     * 设置父机构ID
     * @param parentOrganId 父机构ID
     */
    public void setParentOrganId(Long parentOrganId) {
        this.parentOrganId = parentOrganId;
    }
    
    /**
     * 获取机构路径
     * @return 机构路径
     */
    public String getPath() {
        return path;
    }
    
    /**
     * 设置机构路径
     * @param path 机构路径
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return "Organ{" +
                "organId=" + organId +
                ", organName='" + organName + '\'' +
                ", parentOrganId=" + parentOrganId +
                ", path='" + path + '\'' +
                '}';
    }
}