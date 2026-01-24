package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.CreateMemberRequest;
import com.atoook.otsukailist.dto.MemberResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.service.MemberCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/members")
public class MemberCommandController {

    private final MemberCommandService memberCommandService;

    /**
     * Adds a new member to the specified list.
     *
     * @param listId target list identifier
     * @param req    creation request payload
     * @return 201 created member details wrapped in a mutation response
     */
    @PostMapping
    public ResponseEntity<MutationResponse<MemberResponse>> add(
            @PathVariable("listId") UUID listId,
            @Valid @RequestBody CreateMemberRequest req) {
        return ResponseEntity.created(null).body(memberCommandService.addMember(listId, req));
    }

    /**
     * Renames an existing member.
     *
     * @param listId   parent list identifier
     * @param memberId member identifier
     * @param req      rename request payload
     * @return 200 updated member information
     */
    @PatchMapping("/{memberId}")
    public ResponseEntity<MutationResponse<MemberResponse>> rename(
            @PathVariable("listId") UUID listId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody CreateMemberRequest req) {
        return ResponseEntity.ok(memberCommandService.renameMember(listId, memberId, req));
    }

    /**
     * Deletes a member from the list.
     *
     * @param listId   parent list identifier
     * @param memberId member identifier
     * @return 204 deletion result
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> delete(
            @PathVariable("listId") UUID listId, @PathVariable("memberId") UUID memberId) {
        memberCommandService.deleteMember(listId, memberId);
        return ResponseEntity.noContent().build();
    }
}
