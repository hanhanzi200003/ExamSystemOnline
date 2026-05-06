package com.example.exam_system.login.service;

import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        User user = findUserByInput(input);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + input);
        }

        String authority = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private User findUserByInput(String input) {
        // 按用户名查找
        Optional<User> userOptional = userRepository.findByUsername(input);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        // 按邮箱查找
        Optional<User> emailUser = userRepository.findByEmail(input);
        if (emailUser.isPresent()) {
            return emailUser.get();
        }

        // 按手机号查找
        Optional<User> phoneUser = userRepository.findByPhone(input);
        if (phoneUser.isPresent()) {
            return phoneUser.get();
        }

        // 按格式化手机号查找
        if (PHONE_PATTERN.matcher(input).matches()) {
            String formattedPhone = "+86" + input;
            Optional<User> formattedPhoneUser = userRepository.findByUsername(formattedPhone);
            if (formattedPhoneUser.isPresent()) {
                return formattedPhoneUser.get();
            }
        }

        return null;
    }
}
