package com.loce.daikuan.service;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import com.loce.daikuan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomerService customerService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getAllEmployees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.EMPLOYEE)
                .toList();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // 将用户的所有客户的所属人设置为空
            List<Customer> customers = customerService.getCustomersByUser(user);
            for (Customer customer : customers) {
                customer.setUser(null);
                customerService.saveCustomer(customer);
            }
            userRepository.deleteById(id);
        }
    }
    
    public void deleteUsers(List<Long> userIds) {
        for (Long id : userIds) {
            deleteUser(id);
        }
    }
}