package com.example.exam_system.examgroup.repository;

import com.example.exam_system.examgroup.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByStudentId(String studentId);

    Optional<GroupMember> findByGroupIdAndStudentId(Long groupId, String studentId);

    boolean existsByGroupIdAndStudentId(Long groupId, String studentId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.groupId = ?1 AND gm.status = 'ACTIVE'")
    Long countActiveMembersByGroupId(Long groupId);

    List<GroupMember> findByStudentIdAndStatus(String studentId, String status);

    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") String studentId);

    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.studentId = :studentId")
    void deleteByGroupIdAndStudentId(@Param("groupId") Long groupId, @Param("studentId") String studentId);

    int countByGroupId(Long groupId);
}
