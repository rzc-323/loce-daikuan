package com.loce.daikuan.service;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import com.loce.daikuan.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public List<Customer> getCustomersByUser(User user) {
        return customerRepository.findByUser(user);
    }
    
    public List<Customer> getTodayFollowUpCustomers(User user) {
        return customerRepository.findByUserAndNextFollowUpDate(user, LocalDate.now());
    }
    
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.findByNameContainingOrPhoneContaining(keyword, keyword);
    }
    
    // 获取非空所属人的客户
    public List<Customer> getAllCustomersWithUser() {
        return customerRepository.findByUserNotNull();
    }
    
    public List<Customer> getCustomersByUserWithUserNotNull(User user) {
        return customerRepository.findByUserNotNullAndUser(user);
    }
    
    // 分页服务方法
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
    
    public Page<Customer> getCustomersByUser(User user, Pageable pageable) {
        return customerRepository.findByUser(user, pageable);
    }
    
    public Page<Customer> searchCustomers(String keyword, Pageable pageable) {
        return customerRepository.findByNameContainingOrPhoneContaining(keyword, keyword, pageable);
    }
    
    // 分页获取非空所属人的客户
    public Page<Customer> getAllCustomersWithUser(Pageable pageable) {
        return customerRepository.findByUserNotNull(pageable);
    }
    
    public Page<Customer> getCustomersByUserWithUserNotNull(User user, Pageable pageable) {
        return customerRepository.findByUserNotNullAndUser(user, pageable);
    }
    
    // 按所属人ID查询客户
    public List<Customer> getCustomersByUserId(Long userId) {
        return customerRepository.findByUserId(userId);
    }
    
    public Page<Customer> getCustomersByUserId(Long userId, Pageable pageable) {
        return customerRepository.findByUserId(userId, pageable);
    }
    
    // 搜索并按所属人筛选
    public List<Customer> searchCustomersByKeywordAndUserId(String keyword, Long userId) {
        return customerRepository.findByNameContainingOrPhoneContainingAndUserId(keyword, keyword, userId);
    }
    
    public Page<Customer> searchCustomersByKeywordAndUserId(String keyword, Long userId, Pageable pageable) {
        return customerRepository.findByNameContainingOrPhoneContainingAndUserId(keyword, keyword, userId, pageable);
    }
    
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }
    
    public Customer saveCustomer(Customer customer) {
        LocalDate now = LocalDate.now();
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(now);
        }
        // 设置默认的处理状态
        if (customer.getProcessStatus() == null) {
            customer.setProcessStatus(Customer.ProcessStatus.WECHAT);
        }
        customer.setUpdatedAt(now);
        return customerRepository.save(customer);
    }
    
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    
    public void transferCustomer(Long customerId, User newUser) {
        Customer customer = getCustomerById(customerId);
        if (customer != null) {
            customer.setUser(newUser);
            saveCustomer(customer);
        }
    }
    
    // 批量转移客户
    public void batchTransferCustomers(List<Long> customerIds, User newUser) {
        List<Customer> customers = customerRepository.findAllById(customerIds);
        for (Customer customer : customers) {
            customer.setUser(newUser);
            saveCustomer(customer);
        }
    }
    
    // 批量删除客户
    public void batchDeleteCustomers(List<Long> customerIds) {
        customerRepository.deleteAllById(customerIds);
    }
    
    // 客户公海相关方法
    public List<Customer> getCustomerPool() {
        return customerRepository.findByUserNull();
    }
    
    public Page<Customer> getCustomerPool(Pageable pageable) {
        return customerRepository.findByUserNull(pageable);
    }
}