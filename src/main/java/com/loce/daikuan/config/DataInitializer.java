package com.loce.daikuan.config;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;
import com.loce.daikuan.repository.CustomerRepository;
import com.loce.daikuan.repository.UserRepository;
import com.loce.daikuan.util.RandomDataGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                      CustomerRepository customerRepository, 
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 创建管理员用户
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setName("管理员");
                admin.setPhone("13800138000");
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                
                // 创建5个随机用户
                List<User> employees = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    User employee = new User();
                    String name = RandomDataGenerator.generateRandomName();
                    employee.setUsername("employee" + i);
                    employee.setPassword(passwordEncoder.encode("employee123"));
                    employee.setName(name);
                    employee.setPhone(RandomDataGenerator.generateRandomPhone());
                    employee.setRole(User.Role.EMPLOYEE);
                    userRepository.save(employee);
                    employees.add(employee);
                }
                
                // 创建20个随机客户
                for (int i = 1; i <= 20; i++) {
                    Customer customer = new Customer();
                    customer.setName(RandomDataGenerator.generateRandomName());
                    customer.setPhone(RandomDataGenerator.generateRandomPhone());
                    customer.setHousingFund(RandomDataGenerator.generateRandomHousingFund());
                    customer.setBusinessLicense(RandomDataGenerator.generateRandomBusinessLicense());
                    customer.setSalary(RandomDataGenerator.generateRandomSalary());
                    customer.setProperty(RandomDataGenerator.generateRandomProperty());
                    customer.setCarProperty(RandomDataGenerator.generateRandomCarProperty());
                    customer.setRemarks(RandomDataGenerator.generateRandomRemarks());
                    customer.setNextFollowUpDate(RandomDataGenerator.generateRandomFollowUpDate());
                    customer.setProcessStatus(RandomDataGenerator.generateRandomProcessStatus());
                    customer.setUser(employees.get(i % employees.size()));
                    customer.setCreatedAt(LocalDate.now());
                    customer.setUpdatedAt(LocalDate.now());
                    customerRepository.save(customer);
                }
            }
        };
    }
}