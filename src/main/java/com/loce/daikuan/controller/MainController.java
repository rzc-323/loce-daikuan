package com.loce.daikuan.controller;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import com.loce.daikuan.service.CustomerService;
import com.loce.daikuan.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
    private final CustomerService customerService;
    private final UserService userService;
    
    public MainController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }
    
    @GetMapping("/")
    public String home(@AuthenticationPrincipal User currentUser, Model model) {
        List<Customer> todayFollowUpCustomers;
        if (currentUser.getRole() == User.Role.ADMIN) {
            // 管理员可以查看所有今天需要跟进的客户
            todayFollowUpCustomers = customerService.getAllCustomers().stream()
                    .filter(c -> c.getNextFollowUpDate() != null && c.getNextFollowUpDate().equals(java.time.LocalDate.now()))
                    .toList();
        } else {
            // 普通员工只能查看自己负责的今天需要跟进的客户
            todayFollowUpCustomers = customerService.getTodayFollowUpCustomers(currentUser);
        }
        model.addAttribute("customers", todayFollowUpCustomers);
        model.addAttribute("currentUser", currentUser);
        return "home";
    }
    
    @GetMapping("/customers")
    public String customers(@AuthenticationPrincipal User currentUser, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) Long assigneeId,
                           Model model, HttpSession session) {
        Page<Customer> customers;
        Pageable pageable = PageRequest.of(page, size);
        
        if (currentUser.getRole() == User.Role.ADMIN) {
            // 管理员可以查看所有客户，可以按所属人筛选
            if (assigneeId != null) {
                customers = customerService.getCustomersByUserId(assigneeId, pageable);
            } else {
                customers = customerService.getAllCustomers(pageable);
            }
        } else {
            // 普通员工只能查看自己负责的有所属人的客户
            customers = customerService.getCustomersByUserWithUserNotNull(currentUser, pageable);
        }
        
        // 所有用户都需要用户列表用于转移功能
        model.addAttribute("allUsers", userService.getAllUsers());
        
        model.addAttribute("customers", customers.getContent());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", customers.getTotalPages());
        model.addAttribute("totalItems", customers.getTotalElements());
        model.addAttribute("sizes", Arrays.asList(10, 30, 50, 100, 200));
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("keyword", null); // 添加keyword变量，初始化为null
        // 获取并清除会话中的成功消息
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        return "customers";
    }
    
    @GetMapping("/customers/add")
    public String addCustomerForm(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("processStatuses", Customer.ProcessStatus.values());
        model.addAttribute("currentUser", currentUser);
        return "add-customer";
    }
    
    @PostMapping("/customers/add")
    public String addCustomer(@Valid @ModelAttribute("customer") Customer customer, 
                             BindingResult result, 
                             @AuthenticationPrincipal User currentUser, 
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("processStatuses", Customer.ProcessStatus.values());
            return "add-customer";
        }
        customer.setUser(currentUser);
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }
    
    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model, HttpSession session) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return "redirect:/customers";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("processStatuses", Customer.ProcessStatus.values());
        model.addAttribute("currentUser", currentUser);
        // 获取并清除会话中的成功消息
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        return "customer-detail"; 
    }
    
    @PostMapping("/customers/{id}")
    public String updateCustomer(@PathVariable Long id, 
                                @Valid @ModelAttribute("customer") Customer customer, 
                                BindingResult result, 
                                @AuthenticationPrincipal User currentUser, 
                                Model model, 
                                HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("processStatuses", Customer.ProcessStatus.values());
            model.addAttribute("currentUser", currentUser);
            return "customer-detail";
        }
        Customer existingCustomer = customerService.getCustomerById(id);
        if (existingCustomer != null) {
            // 检查权限：只有管理员或客户的当前负责人可以修改客户信息
            if (currentUser.getRole() != User.Role.ADMIN && 
                (existingCustomer.getUser() == null || !existingCustomer.getUser().getId().equals(currentUser.getId()))) {
                return "redirect:/customers";
            }
            
            customer.setId(id);
            customer.setUser(existingCustomer.getUser());
            customer.setCreatedAt(existingCustomer.getCreatedAt());
            customerService.saveCustomer(customer);
            
            // 添加成功提示信息
            session.setAttribute("successMessage", "客户信息修改成功！");
        }
        return "redirect:/customers/" + id;
    }
    
    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, 
                                @AuthenticationPrincipal User currentUser, 
                                HttpSession session) {
        Customer customer = customerService.getCustomerById(id);
        if (customer != null) {
            // 检查权限：只有管理员或客户的当前负责人可以删除客户
            if (currentUser.getRole() != User.Role.ADMIN && 
                (customer.getUser() == null || !customer.getUser().getId().equals(currentUser.getId()))) {
                return "redirect:/customers";
            }
            customerService.deleteCustomer(id);
            
            // 添加成功提示信息
            session.setAttribute("successMessage", "客户删除成功！");
        }
        return "redirect:/customers";
    }
    
    @GetMapping("/customers/search")
    public String searchCustomers(@RequestParam("keyword") String keyword, 
                                 @AuthenticationPrincipal User currentUser, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) Long assigneeId,
                                 Model model, HttpSession session) {
        List<Customer> filteredCustomers;
        long totalItems;
        Pageable pageable = PageRequest.of(page, size);
        
        if (currentUser.getRole() == User.Role.ADMIN) {
            // 管理员可以搜索所有客户，并可按所属人筛选
            if (assigneeId != null) {
                Page<Customer> customers = customerService.searchCustomersByKeywordAndUserId(keyword, assigneeId, pageable);
                filteredCustomers = customers.getContent();
                totalItems = customers.getTotalElements();
            } else {
                Page<Customer> customers = customerService.searchCustomers(keyword, pageable);
                filteredCustomers = customers.getContent();
                totalItems = customers.getTotalElements();
            }
        } else {
            // 普通员工只能搜索自己负责的有所属人的客户
            List<Customer> allCustomers = customerService.getCustomersByUserWithUserNotNull(currentUser);
            filteredCustomers = allCustomers.stream()
                    .filter(c -> c.getName().contains(keyword) || c.getPhone().contains(keyword))
                    .skip(page * size)
                    .limit(size)
                    .toList();
            totalItems = allCustomers.stream()
                    .filter(c -> c.getName().contains(keyword) || c.getPhone().contains(keyword))
                    .count();
        }
        
        // 所有用户都需要用户列表用于转移功能
        model.addAttribute("allUsers", userService.getAllUsers());
        
        model.addAttribute("customers", filteredCustomers);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("keyword", keyword);
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalItems / size));
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("sizes", Arrays.asList(10, 30, 50, 100, 200));
        // 获取并清除会话中的成功消息
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        return "customers";
    }
    
    // 客户公海页面
    @GetMapping("/customer-pool")
    public String customerPool(@AuthenticationPrincipal User currentUser, 
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model, HttpSession session) {
        // 只有管理员可以访问客户公海
        if (currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/customers";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPool = customerService.getCustomerPool(pageable);
        
        model.addAttribute("customers", customerPool.getContent());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", customerPool.getTotalPages());
        model.addAttribute("totalItems", customerPool.getTotalElements());
        model.addAttribute("sizes", Arrays.asList(10, 30, 50, 100, 200));
        model.addAttribute("allUsers", userService.getAllUsers()); // 用于分配用户的下拉列表
        
        // 获取并清除会话中的成功消息
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        return "customer-pool";
    }
    
    // 分配客户公海中的客户
    @PostMapping("/customer-pool/assign")
    public String assignCustomerFromPool(@RequestParam Long customerId, 
                                       @RequestParam Long userId,
                                       @AuthenticationPrincipal User currentUser,
                                       HttpSession session) {
        // 只有管理员可以分配客户公海中的客户
        if (currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/customers";
        }
        
        User newUser = userService.getUserById(userId);
        if (newUser != null) {
            customerService.transferCustomer(customerId, newUser);
            session.setAttribute("successMessage", "客户分配成功！");
        }
        
        return "redirect:/customer-pool";
    }
    
    // 批量分配客户公海中的客户
    @PostMapping("/customer-pool/batch-assign")
    public String batchAssignCustomersFromPool(@RequestParam List<Long> customerIds, 
                                             @RequestParam Long userId,
                                             @AuthenticationPrincipal User currentUser,
                                             HttpSession session) {
        // 只有管理员可以进行批量分配
        if (currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/customers";
        }
        
        User newUser = userService.getUserById(userId);
        if (newUser != null) {
            customerService.batchTransferCustomers(customerIds, newUser);
            session.setAttribute("successMessage", "客户批量分配成功！");
        }
        
        return "redirect:/customer-pool";
    }
    
    // 批量删除客户公海中的客户
    @PostMapping("/customer-pool/batch-delete")
    public String batchDeleteCustomersFromPool(@RequestParam List<Long> customerIds,
                                              @AuthenticationPrincipal User currentUser,
                                              HttpSession session) {
        // 只有管理员可以进行批量删除
        if (currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/customers";
        }
        
        customerService.batchDeleteCustomers(customerIds);
        session.setAttribute("successMessage", "客户批量删除成功！");
        
        return "redirect:/customer-pool";
    }
    

    
    // 批量转移客户
    @PostMapping("/customers/batch-transfer")
    public String batchTransferCustomers(@RequestParam List<Long> customerIds, 
                                        @RequestParam Long userId,
                                        @AuthenticationPrincipal User currentUser,
                                        HttpSession session) {
        // 验证每个客户的转移权限
        for (Long customerId : customerIds) {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                // 检查权限：只有管理员或客户的当前负责人可以转移客户
                if (currentUser.getRole() != User.Role.ADMIN && 
                    (customer.getUser() == null || !customer.getUser().getId().equals(currentUser.getId()))) {
                    return "redirect:/customers";
                }
            }
        }
        
        User newUser = userService.getUserById(userId);
        if (newUser != null) {
            customerService.batchTransferCustomers(customerIds, newUser);
            session.setAttribute("successMessage", "客户批量转移成功！");
        }
        
        return "redirect:/customers";
    }
    
    // 批量删除客户
    @PostMapping("/customers/batch-delete")
    public String batchDeleteCustomers(@RequestParam List<Long> customerIds,
                                      @AuthenticationPrincipal User currentUser,
                                      HttpSession session) {
        // 验证每个客户的删除权限
        for (Long customerId : customerIds) {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                // 检查权限：只有管理员或客户的当前负责人可以删除客户
                if (currentUser.getRole() != User.Role.ADMIN && 
                    (customer.getUser() == null || !customer.getUser().getId().equals(currentUser.getId()))) {
                    return "redirect:/customers";
                }
            }
        }
        
        // 执行批量删除
        customerService.batchDeleteCustomers(customerIds);
        session.setAttribute("successMessage", "客户批量删除成功！");
        
        return "redirect:/customers";
    }
    
    // 批量转移名单
    @PostMapping("/customers/batch-transfer-list")
    public String batchTransferList(@RequestParam List<Long> customerIds,
                                  @AuthenticationPrincipal User currentUser,
                                  HttpSession session) {
        // 只有管理员可以进行批量转移名单操作
        if (currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/customers";
        }
        
        // TODO: 实现批量转移名单的业务逻辑
        // 这里可以根据实际需求扩展，比如创建名单记录、更新客户状态等
        
        session.setAttribute("successMessage", "客户批量转移名单成功！");
        
        return "redirect:/customers";
    }
}