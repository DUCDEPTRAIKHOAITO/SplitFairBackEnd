package com.anygroup.splitfair.service;

import com.anygroup.splitfair.dto.GroupDTO;
import com.anygroup.splitfair.dto.UserDTO;
import com.anygroup.splitfair.model.GroupMember;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    GroupDTO createGroup(GroupDTO dto, UUID creatorId);
    List<GroupDTO> getAllGroups();
    GroupDTO getGroupById(UUID id);
    void addMemberToGroup(UUID groupId, UUID userId);
    void deleteGroup(UUID id);

    List<GroupDTO> getGroupsCreatedByUser(UUID userId);

    List<GroupMember> getMembersByGroup(UUID groupId);
    List<UserDTO> searchUsersToAdd(UUID groupId, String keyword);
}
