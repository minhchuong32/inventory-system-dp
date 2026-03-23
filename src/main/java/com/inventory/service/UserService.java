package com.inventory.service;

import com.inventory.entity.AppUser;
import org.springframework.data.domain.Page;
import java.util.Optional;

public interface UserService {
    Page<AppUser> findAll(int page, int size);
    Optional<AppUser> findById(Long id);
    AppUser save(AppUser user);
    AppUser update(Long id, AppUser incoming, boolean changePassword);
    void toggleStatus(Long id);
    void deleteById(Long id);
    boolean existsByUsername(String username);
}
