package com.loce.daikuan.repository;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByUser(User user);
    List<Customer> findByUserAndNextFollowUpDate(User user, LocalDate date);
    List<Customer> findByNameContainingOrPhoneContaining(String name, String phone);
    Optional<Customer> findByPhone(String phone);
    
    // 查询非空所属人的客户
    List<Customer> findByUserNotNull();
    List<Customer> findByUserNotNullAndUser(User user);
    
    // 分页查询方法
    Page<Customer> findAll(Pageable pageable);
    Page<Customer> findByUser(User user, Pageable pageable);
    Page<Customer> findByNameContainingOrPhoneContaining(String name, String phone, Pageable pageable);
    
    // 分页查询非空所属人的客户
    Page<Customer> findByUserNotNull(Pageable pageable);
    Page<Customer> findByUserNotNullAndUser(User user, Pageable pageable);
    
    // 按所属人ID查询客户
    List<Customer> findByUserId(Long userId);
    Page<Customer> findByUserId(Long userId, Pageable pageable);
    
    // 搜索并按所属人筛选
    List<Customer> findByNameContainingOrPhoneContainingAndUserId(String name, String phone, Long userId);
    Page<Customer> findByNameContainingOrPhoneContainingAndUserId(String name, String phone, Long userId, Pageable pageable);
    
    // 查询空所属人的客户（客户公海）
    List<Customer> findByUserNull();
    Page<Customer> findByUserNull(Pageable pageable);
}