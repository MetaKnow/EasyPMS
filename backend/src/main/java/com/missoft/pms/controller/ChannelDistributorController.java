package com.missoft.pms.controller;

import com.missoft.pms.entity.ChannelDistributor;
import com.missoft.pms.service.ChannelDistributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道商管理控制器
 * 提供渠道商相关的REST API接口
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/channel-distributors")
@CrossOrigin(origins = "*")
public class ChannelDistributorController {

    @Autowired
    private ChannelDistributorService channelDistributorService;

    /**
     * 分页查询渠道商列表
     *
     * @param page        页码（从0开始，默认0）
     * @param size        每页大小（默认10）
     * @param channelName 渠道名称（可选）
     * @param contactor   联系人（可选）
     * @param phoneNumber 联系方式（可选）
     * @param sortBy      排序字段（默认channelId）
     * @param sortDir     排序方向（默认desc）
     * @return 渠道商分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getChannelDistributors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String channelName,
            @RequestParam(required = false) String contactor,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "channelId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Page<ChannelDistributor> channelDistributorPage = channelDistributorService.getChannelDistributors(
                    page, size, channelName, contactor, phoneNumber, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("content", channelDistributorPage.getContent());
            response.put("totalElements", channelDistributorPage.getTotalElements());
            response.put("totalPages", channelDistributorPage.getTotalPages());
            response.put("currentPage", channelDistributorPage.getNumber());
            response.put("size", channelDistributorPage.getSize());
            response.put("hasNext", channelDistributorPage.hasNext());
            response.put("hasPrevious", channelDistributorPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询渠道商列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询渠道商详情
     *
     * @param channelId 渠道商ID
     * @return 渠道商详情
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<Map<String, Object>> getChannelDistributorById(@PathVariable Long channelId) {
        try {
            ChannelDistributor channelDistributor = channelDistributorService.getChannelDistributorById(channelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", channelDistributor);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询渠道商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新渠道商
     *
     * @param channelDistributor 渠道商信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChannelDistributor(@Valid @RequestBody ChannelDistributor channelDistributor) {
        try {
            ChannelDistributor savedChannelDistributor = channelDistributorService.createChannelDistributor(channelDistributor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", savedChannelDistributor);
            response.put("message", "渠道商创建成功");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "创建渠道商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新渠道商信息
     *
     * @param channelId          渠道商ID
     * @param channelDistributor 更新的渠道商信息
     * @return 更新结果
     */
    @PutMapping("/{channelId}")
    public ResponseEntity<Map<String, Object>> updateChannelDistributor(
            @PathVariable Long channelId, 
            @Valid @RequestBody ChannelDistributor channelDistributor) {
        try {
            ChannelDistributor updatedChannelDistributor = channelDistributorService.updateChannelDistributor(channelId, channelDistributor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", updatedChannelDistributor);
            response.put("message", "渠道商更新成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "更新渠道商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除渠道商
     *
     * @param channelId 渠道商ID
     * @return 删除结果
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Map<String, Object>> deleteChannelDistributor(@PathVariable Long channelId) {
        try {
            channelDistributorService.deleteChannelDistributor(channelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "渠道商删除成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "删除渠道商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除渠道商
     *
     * @param channelIds 渠道商ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteChannelDistributors(@RequestBody List<Long> channelIds) {
        try {
            int deletedCount = channelDistributorService.deleteChannelDistributors(channelIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "成功删除 " + deletedCount + " 个渠道商");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除渠道商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查渠道名称是否可用
     *
     * @param channelName 渠道名称
     * @param channelId   要排除的渠道商ID（可选）
     * @return 检查结果
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkChannelNameAvailability(
            @RequestParam String channelName,
            @RequestParam(required = false) Long channelId) {
        try {
            boolean available = channelDistributorService.isChannelNameAvailable(channelName, channelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "渠道名称可用" : "渠道名称已被使用");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查渠道名称失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取渠道商总数
     *
     * @return 渠道商总数
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTotalCount() {
        try {
            long totalCount = channelDistributorService.getTotalCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询渠道商总数失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有渠道商列表（不分页）
     *
     * @return 渠道商列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllChannelDistributors() {
        try {
            List<ChannelDistributor> channelDistributors = channelDistributorService.getAllChannelDistributors();
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", channelDistributors);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询渠道商列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}