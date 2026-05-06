package com.example.exam_system.examgroup.mapper;

import com.example.exam_system.examgroup.dto.GroupCreateDTO;
import com.example.exam_system.examgroup.dto.GroupDTO;
import com.example.exam_system.examgroup.entity.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public Group toEntity(GroupCreateDTO dto, String teacherId, String groupCode) {
        return new Group(groupCode, teacherId, dto.getGroupName(), dto.getDescription());
    }

    public GroupDTO toDTO(Group group, Long memberCount) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setTeacherId(group.getTeacherId());
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setMemberCount(memberCount);
        return dto;
    }
}
