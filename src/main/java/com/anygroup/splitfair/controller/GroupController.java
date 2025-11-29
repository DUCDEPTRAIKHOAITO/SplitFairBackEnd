package com.anygroup.splitfair.controller;

import com.anygroup.splitfair.dto.GroupDTO;
import com.anygroup.splitfair.dto.UserDTO;
import com.anygroup.splitfair.model.GroupMember;
import com.anygroup.splitfair.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;


    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(
            @RequestBody GroupDTO dto,
            @RequestParam("creatorId") UUID creatorId
    ) {
        GroupDTO created = groupService.createGroup(dto, creatorId);
        return ResponseEntity.ok(created);
    }


    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }


    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable UUID id) {
        GroupDTO group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }


    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<GroupDTO>> getGroupsCreatedByUser(@PathVariable UUID userId) {
        List<GroupDTO> groups = groupService.getGroupsCreatedByUser(userId);
        return ResponseEntity.ok(groups);
    }


    @PostMapping("/{groupId}/members")
    public ResponseEntity<String> addMemberToGroup(
            @PathVariable UUID groupId,
            @RequestParam("userId") UUID userId
    ) {
        groupService.addMemberToGroup(groupId, userId);
        return ResponseEntity.ok("Member added to group successfully");
    }


    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembersByGroup(@PathVariable UUID groupId) {
        List<GroupMember> members = groupService.getMembersByGroup(groupId);
        return ResponseEntity.ok(members);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/search-users")
    public ResponseEntity<List<UserDTO>> searchUsersToAdd(
            @PathVariable UUID groupId,
            @RequestParam("keyword") String keyword
    ) {
        return ResponseEntity.ok(groupService.searchUsersToAdd(groupId, keyword));
    }

}
