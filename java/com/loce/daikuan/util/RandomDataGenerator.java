package com.loce.daikuan.util;

import com.loce.daikuan.entity.Customer;
import com.loce.daikuan.entity.User;

import java.time.LocalDate;
import java.util.Random;

public class RandomDataGenerator {
    private static final Random random = new Random();
    private static final String[] FIRST_NAMES = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴", "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"};
    private static final String[] LAST_NAMES = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞"};
    private static final String[] PHONE_PREFIXES = {"138", "139", "137", "136", "135", "134", "159", "158", "157", "150", "151", "152", "188", "187", "182", "183", "184", "178", "177", "176"};
    private static final Customer.ProcessStatus[] PROCESS_STATUSES = Customer.ProcessStatus.values();
    
    public static String generateRandomName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }
    
    public static String generateRandomPhone() {
        String prefix = PHONE_PREFIXES[random.nextInt(PHONE_PREFIXES.length)];
        String suffix = String.format("%08d", random.nextInt(100000000));
        return prefix + suffix;
    }
    
    public static String generateRandomUsername(String name) {
        return name.toLowerCase() + random.nextInt(1000);
    }
    
    public static String generateRandomPassword() {
        return "password" + random.nextInt(10000);
    }
    
    public static String generateRandomHousingFund() {
        return random.nextBoolean() ? "有" : "无";
    }
    
    public static String generateRandomBusinessLicense() {
        return random.nextBoolean() ? "有" : "无";
    }
    
    public static String generateRandomSalary() {
        return String.valueOf(3000 + random.nextInt(15000));
    }
    
    public static String generateRandomProperty() {
        return random.nextBoolean() ? "有" : "无";
    }
    
    public static String generateRandomCarProperty() {
        return random.nextBoolean() ? "有" : "无";
    }
    
    public static String generateRandomRemarks() {
        String[] remarks = {
            "需要贷款买房", "创业资金需求", "资金周转", "购车贷款", "装修贷款", 
            "教育贷款", "旅游贷款", "医疗贷款", "其他用途", "投资需求"
        };
        return remarks[random.nextInt(remarks.length)] + "，金额：" + (random.nextInt(100) + 10) + "万";
    }
    
    public static LocalDate generateRandomFollowUpDate() {
        return LocalDate.now().plusDays(random.nextInt(30));
    }
    
    public static Customer.ProcessStatus generateRandomProcessStatus() {
        return PROCESS_STATUSES[random.nextInt(PROCESS_STATUSES.length)];
    }
}