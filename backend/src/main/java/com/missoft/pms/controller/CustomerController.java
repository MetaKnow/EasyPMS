package com.missoft.pms.controller;

import com.missoft.pms.entity.Customer;
import com.missoft.pms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户管理控制器
 * 提供客户相关的REST API接口
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 分页查询客户列表
     *
     * @param page         页码（从0开始，默认0）
     * @param size         每页大小（默认10）
     * @param customerName 客户名称（可选）
     * @param contact      联系人（可选）
     * @param province     省份（可选）
     * @param customerRank 客户等级（可选）
     * @param sortBy       排序字段（默认customerId）
     * @param sortDir      排序方向（默认desc）
     * @return 客户分页数据
     */
    @GetMapping
    /**
     * 函数级注释：分页查询客户列表
     * - 支持按销售负责人 `saleLeader` 过滤，满足销售角色仅查看自身数据的要求
     */
    public ResponseEntity<Map<String, Object>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String customerRank,
            @RequestParam(defaultValue = "customerId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long saleLeader) {

        try {
            Page<Customer> customerPage = customerService.getCustomers(
                    page, size, customerName, contact, province, customerRank, sortBy, sortDir, saleLeader);

            Map<String, Object> response = new HashMap<>();
            response.put("customers", customerPage.getContent());
            response.put("currentPage", customerPage.getNumber());
            response.put("totalItems", customerPage.getTotalElements());
            response.put("totalPages", customerPage.getTotalPages());
            response.put("pageSize", customerPage.getSize());
            response.put("hasNext", customerPage.hasNext());
            response.put("hasPrevious", customerPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询客户列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询客户详情
     *
     * @param customerId 客户ID
     * @return 客户详情
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customer);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户不存在");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询客户详情失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新客户
     *
     * @param customer 客户信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.createCustomer(customer);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "客户创建成功");
            response.put("customer", savedCustomer);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新客户信息
     *
     * @param customerId 客户ID
     * @param customer   更新的客户信息
     * @return 更新结果
     */
    @PutMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody Customer customer) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "客户更新成功");
            response.put("customer", updatedCustomer);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除客户
     *
     * @param customerId 客户ID
     * @return 删除结果
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long customerId) {
        try {
            customerService.deleteCustomer(customerId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "客户删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "客户删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除客户
     *
     * @param customerIds 客户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteCustomers(@RequestBody List<Long> customerIds) {
        try {
            int deletedCount = customerService.deleteCustomers(customerIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除成功");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查客户名称是否可用
     *
     * @param customerName 客户名称
     * @param customerId   要排除的客户ID（可选）
     * @return 检查结果
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkCustomerName(
            @RequestParam String customerName,
            @RequestParam(required = false) Long customerId) {
        try {
            boolean available = customerService.isCustomerNameAvailable(customerName, customerId);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "客户名称可用" : "客户名称已存在");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查客户名称失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查联系方式是否可用
     *
     * @param phoneNumber 联系方式
     * @param customerId  要排除的客户ID（可选）
     * @return 检查结果
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhoneNumber(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) Long customerId) {
        try {
            boolean available = customerService.isPhoneNumberAvailable(phoneNumber, customerId);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "联系方式可用" : "联系方式已存在");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查联系方式失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有省份列表
     *
     * @return 省份列表
     */
    @GetMapping("/provinces")
    public ResponseEntity<Map<String, Object>> getAllProvinces() {
        try {
            List<String> provinces = customerService.getAllProvinces();
            Map<String, Object> response = new HashMap<>();
            response.put("provinces", provinces);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取省份列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有客户等级列表
     *
     * @return 客户等级列表
     */
    @GetMapping("/ranks")
    public ResponseEntity<Map<String, Object>> getAllCustomerRanks() {
        try {
            List<String> ranks = customerService.getAllCustomerRanks();
            Map<String, Object> response = new HashMap<>();
            response.put("ranks", ranks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取客户等级列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有客户列表（不分页）
     *
     * @return 客户列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取客户列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取客户统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics() {
        try {
            long totalCount = customerService.getTotalCustomerCount();
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取客户统计信息失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
