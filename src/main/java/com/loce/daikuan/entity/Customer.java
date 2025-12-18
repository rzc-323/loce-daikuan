package com.loce.daikuan.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "姓名不能为空")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "电话号码不能为空")
    @Column(nullable = false)
    private String phone;
    
    private String housingFund;
    private String businessLicense;
    private String salary;
    private String property;
    private String carProperty;
    private String remarks;
    
    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStatus processStatus;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    public enum ProcessStatus {
        WECHAT("微信沟通"),
        ARRIVED("到访"),
        SIGNED("签约"),
        APPROVED("审批通过"),
        LOANED("放款完成");
        
        private final String chineseName;
        
        ProcessStatus(String chineseName) {
            this.chineseName = chineseName;
        }
        
        public String getChineseName() {
            return chineseName;
        }
    }
}