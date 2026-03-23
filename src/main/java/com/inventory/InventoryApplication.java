package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class InventoryApplication {

//     -- admin / admin123
// UPDATE app_users SET password = '$2a$10$N9qo8uLOic5kgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
// WHERE username = 'admin';

// -- manager / 123456
// UPDATE app_users SET password = '$2a$10$slYQmyNdgTY1hcVfla2c9OIoEo8qTJMSFcUFZFJCcVj0oOSS0Tdam'
// WHERE username = 'manager';

// -- staff1 / 123456
// UPDATE app_users SET password = '$2a$10$slYQmyNdgTY1hcVfla2c9OIoEo8qTJMSFcUFZFJCcVj0oOSS0Tdam'
// WHERE username = 'staff1';
    public static void main(String[] args) {
    // sinh ma hash -> copy ma hash that vao lenh sql tren dan vao sql server de cap nhat password cho user
    // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    // System.out.println(encoder.encode("admin123"));
    // System.out.println(encoder.encode("manager123"));
    // System.out.println(encoder.encode("staff123"));
        SpringApplication.run(InventoryApplication.class, args);
    }
}