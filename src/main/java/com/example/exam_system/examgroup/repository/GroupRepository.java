package com.example.exam_system.examgroup.repository;

import com.example.exam_system.examgroup.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByTeacherId(String teacherId);

    Optional<Group> findByGroupCode(String groupCode);

    boolean existsByGroupCode(String groupCode);

    boolean existsByIdAndTeacherId(Long id, String teacherId);

    Page<Group> findByGroupNameContaining(String groupName, Pageable pageable);
}
