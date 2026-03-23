package com.inventory.service.impl;

import com.inventory.entity.AppUser;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.AppUserRepository;
import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override @Transactional(readOnly = true)
    public Page<AppUser> findAll(int page, int size) {
        return userRepository.findByDeletedFalse(
            PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @Override @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id).filter(u -> !u.isDeleted());
    }

    @Override
    public AppUser save(AppUser user) {
        if (userRepository.existsByUsernameAndDeletedFalse(user.getUsername()))
            throw new RuntimeException("Tên đăng nhập '" + user.getUsername() + "' đã tồn tại");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public AppUser update(Long id, AppUser incoming, boolean changePassword) {
        AppUser existing = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        existing.setFullName(incoming.getFullName());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setRole(incoming.getRole());
        existing.setStatus(incoming.getStatus());
        if (changePassword && incoming.getPassword() != null && !incoming.getPassword().isBlank())
            existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
        return userRepository.save(existing);
    }

    @Override
    public void toggleStatus(Long id) {
        AppUser user = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        user.setStatus(!Boolean.TRUE.equals(user.getStatus()));
        userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        AppUser user = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        user.softDelete();
        userRepository.save(user);
    }

    @Override @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameAndDeletedFalse(username);
    }
}
