package com.missoft.pms.service;

import com.missoft.pms.entity.Organ;
import com.missoft.pms.dto.UserWithRoleDTO;
import com.missoft.pms.repository.OrganRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构服务类
 * 提供机构管理的业务逻辑
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
@Transactional
public class OrganService {
    
    @Autowired
    private OrganRepository organRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取机构树结构
     * 
     * @return 机构树列表
     */
    public List<Map<String, Object>> getOrganTree() {
        List<Organ> allOrgans = organRepository.findAll();
        return buildOrganTree(allOrgans, null);
    }
    
    /**
     * 获取所有机构列表
     * 
     * @return 机构列表
     */
    public List<Organ> getAllOrgans() {
        return organRepository.findAll();
    }
    
    /**
     * 根据ID获取机构
     * 
     * @param organId 机构ID
     * @return 机构对象
     */
    public Optional<Organ> getOrganById(Long organId) {
        return organRepository.findById(organId);
    }
    
    /**
     * 创建机构
     * 
     * @param organ 机构对象
     * @return 创建的机构
     */
    public Organ createOrgan(Organ organ) {
        // 验证机构名称不能为空
        if (!StringUtils.hasText(organ.getOrganName())) {
            throw new IllegalArgumentException("机构名称不能为空");
        }
        
        // 检查机构名称是否已存在
        if (organRepository.existsByOrganName(organ.getOrganName())) {
            throw new IllegalArgumentException("机构名称已存在");
        }
        
        // 设置机构路径
        String path = buildOrganPath(organ.getParentOrganId());
        organ.setPath(path);
        
        return organRepository.save(organ);
    }
    
    /**
     * 更新机构
     * 
     * @param organId 机构ID
     * @param organ 机构对象
     * @return 更新的机构
     */
    public Organ updateOrgan(Long organId, Organ organ) {
        Optional<Organ> existingOrgan = organRepository.findById(organId);
        if (existingOrgan.isEmpty()) {
            throw new IllegalArgumentException("机构不存在");
        }
        
        Organ orgToUpdate = existingOrgan.get();
        
        // 验证机构名称不能为空
        if (!StringUtils.hasText(organ.getOrganName())) {
            throw new IllegalArgumentException("机构名称不能为空");
        }
        
        // 检查机构名称是否已被其他机构使用
        Optional<Organ> organWithSameName = organRepository.findByOrganName(organ.getOrganName());
        if (organWithSameName.isPresent() && !organWithSameName.get().getOrganId().equals(organId)) {
            throw new IllegalArgumentException("机构名称已存在");
        }
        
        // 更新机构信息
        orgToUpdate.setOrganName(organ.getOrganName());
        
        // 如果父机构发生变化，需要更新路径
        if (!Objects.equals(orgToUpdate.getParentOrganId(), organ.getParentOrganId())) {
            // 检查是否会形成循环引用
            if (organ.getParentOrganId() != null && wouldCreateCycle(organId, organ.getParentOrganId())) {
                throw new IllegalArgumentException("不能将机构设置为其子机构的下级");
            }
            
            orgToUpdate.setParentOrganId(organ.getParentOrganId());
            String newPath = buildOrganPath(organ.getParentOrganId());
            orgToUpdate.setPath(newPath);
            
            // 更新所有子机构的路径
            updateChildrenPaths(organId);
        }
        
        return organRepository.save(orgToUpdate);
    }
    
    /**
     * 删除机构（支持级联删除子机构）
     * 
     * @param organId 机构ID
     */
    public void deleteOrgan(Long organId) {
        Optional<Organ> organ = organRepository.findById(organId);
        if (organ.isEmpty()) {
            throw new IllegalArgumentException("机构不存在");
        }
        
        // 检查机构及其所有子机构是否有用户
        Map<String, Object> checkResult = checkOrganAndChildrenHasUsers(organId);
        boolean hasUsers = (Boolean) checkResult.get("hasUsers");
        
        if (hasUsers) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> organUserInfo = (List<Map<String, Object>>) checkResult.get("organUserInfo");
            int totalUsers = (Integer) checkResult.get("totalUsers");
            
            // 构建详细的错误信息
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("无法删除机构，以下机构中存在用户：\n");
            
            for (Map<String, Object> info : organUserInfo) {
                String organName = (String) info.get("organName");
                int userCount = (Integer) info.get("userCount");
                errorMessage.append("- ").append(organName).append("（").append(userCount).append("个用户）\n");
            }
            
            errorMessage.append("总计：").append(totalUsers).append("个用户");
            throw new IllegalArgumentException(errorMessage.toString());
        }
        
        // 如果没有用户，执行级联删除
        deleteOrganCascade(organId);
    }

    /**
     * 级联删除机构及其所有子机构（函数级注释：递归删除指定机构及其所有子机构）
     * @param organId 机构ID
     */
    private void deleteOrganCascade(Long organId) {
        // 先删除所有子机构
        List<Organ> children = organRepository.findByParentOrganId(organId);
        for (Organ child : children) {
            deleteOrganCascade(child.getOrganId());
        }
        
        // 最后删除当前机构
        organRepository.deleteById(organId);
    }
    
    /**
     * 检查机构是否有用户
     * 
     * @param organId 机构ID
     * @return true表示有用户，false表示没有用户
     */
    public boolean hasUsers(Long organId) {
        return !userRepository.findByOrganId(organId).isEmpty();
    }
    
    /**
     * 检查机构是否有子机构
     * 
     * @param organId 机构ID
     * @return true表示有子机构，false表示没有子机构
     */
    public boolean hasChildren(Long organId) {
        return organRepository.hasChildren(organId);
    }
    
    /**
     * 初始化默认机构
     * 
     * @return 默认机构
     */
    public Organ initDefaultOrgan() {
        String defaultOrganName = "管软信息技术（北京）有限公司";
        
        // 检查是否已存在默认机构
        Optional<Organ> existingOrgan = organRepository.findByOrganName(defaultOrganName);
        if (existingOrgan.isPresent()) {
            return existingOrgan.get();
        }
        
        // 创建默认机构
        Organ defaultOrgan = new Organ();
        defaultOrgan.setOrganName(defaultOrganName);
        defaultOrgan.setParentOrganId(null);
        defaultOrgan.setPath("/1");
        
        return organRepository.save(defaultOrgan);
    }
    
    /**
     * 构建机构树
     * 
     * @param allOrgans 所有机构列表
     * @param parentId 父机构ID
     * @return 机构树
     */
    private List<Map<String, Object>> buildOrganTree(List<Organ> allOrgans, Long parentId) {
        return allOrgans.stream()
                .filter(organ -> Objects.equals(organ.getParentOrganId(), parentId))
                .map(organ -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("organId", organ.getOrganId());
                    node.put("organName", organ.getOrganName());
                    node.put("parentOrganId", organ.getParentOrganId());
                    node.put("path", organ.getPath());
                    
                    List<Map<String, Object>> children = buildOrganTree(allOrgans, organ.getOrganId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    
                    return node;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 构建机构路径
     * 
     * @param parentOrganId 父机构ID
     * @return 机构路径
     */
    private String buildOrganPath(Long parentOrganId) {
        if (parentOrganId == null) {
            return "/";
        }
        
        Optional<Organ> parentOrgan = organRepository.findById(parentOrganId);
        if (parentOrgan.isEmpty()) {
            return "/";
        }
        
        String parentPath = parentOrgan.get().getPath();
        if (parentPath == null || parentPath.equals("/")) {
            return "/" + parentOrganId;
        } else {
            return parentPath + "/" + parentOrganId;
        }
    }
    
    /**
     * 检查是否会形成循环引用
     * 
     * @param organId 机构ID
     * @param newParentId 新的父机构ID
     * @return true表示会形成循环，false表示不会
     */
    private boolean wouldCreateCycle(Long organId, Long newParentId) {
        if (newParentId == null) {
            return false;
        }
        
        if (newParentId.equals(organId)) {
            return true;
        }
        
        List<Organ> descendants = organRepository.findAllDescendants(organId);
        return descendants.stream().anyMatch(organ -> organ.getOrganId().equals(newParentId));
    }
    
    /**
     * 更新子机构路径
     * 
     * @param parentOrganId 父机构ID
     */
    private void updateChildrenPaths(Long parentOrganId) {
        List<Organ> children = organRepository.findByParentOrganId(parentOrganId);
        for (Organ child : children) {
            String newPath = buildOrganPath(parentOrganId);
            child.setPath(newPath);
            organRepository.save(child);
            
            // 递归更新子机构的子机构
            updateChildrenPaths(child.getOrganId());
        }
    }
    
    /**
     * 获取指定机构下的用户列表（函数级注释：返回 organId 对应机构下的所有用户）
     * @param organId 机构ID
     * @return 用户列表
     */
    public java.util.List<com.missoft.pms.entity.User> getUsersByOrgan(Long organId) {
        return userRepository.findByOrganId(organId);
    }

    /**
     * 获取指定机构下的用户列表（包含角色信息）（函数级注释：返回 organId 对应机构下的所有用户，包含角色名称）
     * @param organId 机构ID
     * @return 包含角色信息的用户列表
     */
    public java.util.List<UserWithRoleDTO> getUsersWithRoleByOrgan(Long organId) {
        // 获取指定机构下的用户
        java.util.List<com.missoft.pms.entity.User> users = userRepository.findByOrganId(organId);
        
        // 转换为包含角色信息的DTO
        return users.stream()
                .map(user -> userService.convertToUserWithRoleDTO(user))
                .collect(Collectors.toList());
    }

    /**
     * 统计指定机构下的用户数量（函数级注释：返回 organId 对应机构下的用户总数）
     * @param organId 机构ID
     * @return 用户数量
     */
    public long countUsersByOrganId(Long organId) {
        return userRepository.countByOrganId(organId);
    }

    /**
     * 统计指定机构下的子机构数量（函数级注释：返回 organId 对应的直接子机构数量）
     * @param organId 机构ID
     * @return 子机构数量
     */
    public long countChildrenByOrganId(Long organId) {
        return organRepository.countChildren(organId);
    }

    /**
     * 检查机构及其所有子机构是否有用户（函数级注释：递归检查指定机构及其所有子机构是否存在用户）
     * @param organId 机构ID
     * @return 检查结果，包含是否有用户、用户数量、具体信息等
     */
    public Map<String, Object> checkOrganAndChildrenHasUsers(Long organId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> organUserInfo = new ArrayList<>();
        
        // 递归检查机构及其子机构
        boolean hasUsers = checkOrganAndChildrenHasUsersRecursive(organId, organUserInfo);
        
        result.put("hasUsers", hasUsers);
        result.put("organUserInfo", organUserInfo);
        result.put("totalUsers", organUserInfo.stream().mapToInt(info -> (Integer) info.get("userCount")).sum());
        
        return result;
    }

    /**
     * 递归检查机构及其子机构是否有用户（函数级注释：递归遍历机构树，检查每个机构的用户情况）
     * @param organId 机构ID
     * @param organUserInfo 用于收集机构用户信息的列表
     * @return 是否有用户
     */
    private boolean checkOrganAndChildrenHasUsersRecursive(Long organId, List<Map<String, Object>> organUserInfo) {
        boolean hasUsers = false;
        
        // 检查当前机构
        Optional<Organ> organOpt = organRepository.findById(organId);
        if (organOpt.isPresent()) {
            Organ organ = organOpt.get();
            List<com.missoft.pms.entity.User> users = userRepository.findByOrganId(organId);
            
            if (!users.isEmpty()) {
                hasUsers = true;
                Map<String, Object> info = new HashMap<>();
                info.put("organId", organId);
                info.put("organName", organ.getOrganName());
                info.put("userCount", users.size());
                info.put("users", users.stream().map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getUserId());
                    userInfo.put("userName", user.getUserName());
                    userInfo.put("name", user.getName());
                    return userInfo;
                }).collect(Collectors.toList()));
                organUserInfo.add(info);
            }
        }
        
        // 递归检查子机构
        List<Organ> children = organRepository.findByParentOrganId(organId);
        for (Organ child : children) {
            boolean childHasUsers = checkOrganAndChildrenHasUsersRecursive(child.getOrganId(), organUserInfo);
            if (childHasUsers) {
                hasUsers = true;
            }
        }
        
        return hasUsers;
    }

    /**
     * 获取机构及其所有子机构的ID列表（函数级注释：递归获取指定机构及其所有子机构的ID）
     * @param organId 机构ID
     * @return 机构ID列表
     */
    public List<Long> getOrganAndChildrenIds(Long organId) {
        List<Long> organIds = new ArrayList<>();
        getOrganAndChildrenIdsRecursive(organId, organIds);
        return organIds;
    }

    /**
     * 递归获取机构及其子机构的ID（函数级注释：递归遍历机构树，收集所有机构ID）
     * @param organId 机构ID
     * @param organIds 用于收集机构ID的列表
     */
    private void getOrganAndChildrenIdsRecursive(Long organId, List<Long> organIds) {
        organIds.add(organId);
        
        List<Organ> children = organRepository.findByParentOrganId(organId);
        for (Organ child : children) {
            getOrganAndChildrenIdsRecursive(child.getOrganId(), organIds);
        }
    }
}