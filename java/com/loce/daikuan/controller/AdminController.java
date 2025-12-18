package com.loce.daikuan.controller;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import com.loce.daikuan.service.CustomerService;
import com.loce.daikuan.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final CustomerService customerService;
    
    public AdminController(UserService userService, CustomerService customerService) {
        this.userService = userService;
        this.customerService = customerService;
    }
    
    @GetMapping("/users")
    public String users(@AuthenticationPrincipal User currentUser, Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);
        return "admin/users";
    }
    
    @GetMapping("/users/add")
    public String addUserForm(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("currentUser", currentUser);
        return "admin/add-user";
    }
    
    @PostMapping("/users/add")
    public String addUser(@Valid @ModelAttribute("user") User user, 
                         BindingResult result, 
                         @AuthenticationPrincipal User currentUser,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("currentUser", currentUser);
            return "admin/add-user";
        }
        userService.saveUser(user);
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/batch-delete")
    public String batchDeleteUsers(@RequestParam List<Long> userIds, @AuthenticationPrincipal User currentUser) {
        userService.deleteUsers(userIds);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("currentUser", currentUser);
        return "admin/edit-user";
    }
    
    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, 
                         @Valid @ModelAttribute("user") User user, 
                         BindingResult result, 
                         @AuthenticationPrincipal User currentUser,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("currentUser", currentUser);
            return "admin/edit-user";
        }
        User existingUser = userService.getUserById(id);
        if (existingUser != null) {
            // 保留原有密码，如果用户没有输入新密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            }
            user.setId(id);
            userService.saveUser(user);
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/transfer-customer/{id}")
    public String transferCustomerForm(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model) {
        Customer customer = customerService.getCustomerById(id);
        List<User> employees = userService.getAllEmployees();
        model.addAttribute("customer", customer);
        model.addAttribute("employees", employees);
        model.addAttribute("currentUser", currentUser);
        return "admin/transfer-customer";
    }
    
    @PostMapping("/transfer-customer/{id}")
    public String transferCustomer(@PathVariable Long id, 
                                  @RequestParam("newUserId") Long newUserId) {
        User newUser = userService.getUserById(newUserId);
        customerService.transferCustomer(id, newUser);
        return "redirect:/customers/" + id;
    }
}