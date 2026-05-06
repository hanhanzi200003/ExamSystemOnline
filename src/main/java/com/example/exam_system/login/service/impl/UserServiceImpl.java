package com.example.exam_system.login.service.impl;

import com.example.exam_system.login.dto.CreateUserDTO;
import com.example.exam_system.login.dto.UpdateUserDTO;
import com.example.exam_system.login.entity.User;
import com.example.exam_system.login.repository.UserRepository;
import com.example.exam_system.login.service.PasswordService;
import com.example.exam_system.login.service.StudentAccountDeletionService;
import com.example.exam_system.login.service.TeacherAccountDeletionService;
import com.example.exam_system.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private StudentAccountDeletionService studentAccountDeletionService;

    @Autowired
    private TeacherAccountDeletionService teacherAccountDeletionService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 基础 CRUD 方法实现
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return findUserByAnyField(username);
    }

    @Override
    public User findByEmail(String email) {
        return findUserByAnyField(email);
    }

    @Override
    public User findByPhone(String phone) {
        return findUserByAnyField(phone);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void deleteByUsername(String username) {
        User user = findByUsername(username);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 业务方法实现
    @Override
    public User registerUser(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }
        String encodedPassword = passwordService.encodePassword(rawPassword);
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        User newUser = new User(username, encodedPassword, userRole);
        return userRepository.save(newUser);
    }

    @Override
    public User registerUser(CreateUserDTO createUserDTO) {
        String encodedPassword = passwordService.encodePassword(createUserDTO.getPassword());
        User.UserRole userRole = User.UserRole.valueOf(createUserDTO.getRole().toUpperCase());
        User newUser;

        switch (createUserDTO.getRegistrationType().toUpperCase()) {
            case "EMAIL":
                if (createUserDTO.getEmail() == null || createUserDTO.getEmail().isEmpty()) {
                    throw new RuntimeException("邮箱不能为空");
                }
                if (userRepository.existsByEmail(createUserDTO.getEmail())) {
                    throw new RuntimeException("该邮箱已被注册");
                }
                newUser = new User(createUserDTO.getEmail(), encodedPassword, userRole, true);
                break;

            case "PHONE":
                if (createUserDTO.getPhone() == null || createUserDTO.getPhone().isEmpty()) {
                    throw new RuntimeException("手机号不能为空");
                }
                if (userRepository.existsByPhone(createUserDTO.getPhone())) {
                    throw new RuntimeException("该手机号已被注册");
                }
                newUser = new User(createUserDTO.getPhone(), encodedPassword, userRole);
                break;

            default:
                throw new RuntimeException("无效的注册类型");
        }

        if (createUserDTO.getNickname() != null && !createUserDTO.getNickname().isEmpty()) {
            newUser.setNickname(createUserDTO.getNickname());
        }
        if (createUserDTO.getStudentStaffId() != null && !createUserDTO.getStudentStaffId().isEmpty()) {
            if (userRepository.existsByStudentStaffId(createUserDTO.getStudentStaffId())) {
                throw new RuntimeException("该学工号已被注册");
            }
            newUser.setStudentStaffId(createUserDTO.getStudentStaffId());
        }

        return userRepository.save(newUser);
    }

    @Override
    public boolean authenticateUser(String input, String rawPassword) {
        User user = findUserByAnyField(input);
        if (user != null) {
            return passwordService.matchesPassword(rawPassword, user.getPassword());
        }
        return false;
    }

    @Override
    public boolean deleteUserAccount(String username, String rawPassword) {
        User user = findByUsername(username);
        if (user == null) return false;
        if (!passwordService.matchesPassword(rawPassword, user.getPassword())) return false;
        try {
            User.UserRole role = user.getRole();
            if (role == User.UserRole.STUDENT) {
                return studentAccountDeletionService.deleteStudentAccount(username, rawPassword, passwordService);
            } else if (role == User.UserRole.TEACHER) {
                return teacherAccountDeletionService.deleteTeacherAccount(username, rawPassword, passwordService);
            } else {
                userRepository.delete(user);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User updateUser(Long id, UpdateUserDTO updateDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            updateUserFields(user, updateDTO);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User updateUserByUsername(String username, UpdateUserDTO updateDTO) {
        User user = findUserByAnyField(username);
        if (user != null) {
            updateUserFields(user, updateDTO);
            return userRepository.save(user);
        }
        return null;
    }

    private void updateUserFields(User user, UpdateUserDTO updateDTO) {
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getStudentStaffId() != null) {
            user.setStudentStaffId(updateDTO.getStudentStaffId());
        }
    }

    // 统一的用户查找方法
    private User findUserByAnyField(String input) {
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

        // 按 11 位手机号查找（兼容旧数据）
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
