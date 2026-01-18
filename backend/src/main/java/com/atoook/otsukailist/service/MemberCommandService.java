package com.atoook.otsukailist.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.MemberResponse;
import com.atoook.otsukailist.dto.CreateMemberRequest;
import com.atoook.otsukailist.dto.DeleteMemberResponse;
import com.atoook.otsukailist.model.Member;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.mapper.MemberMapper;
import com.atoook.otsukailist.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final ItemListRepository itemListRepo;
    private final MemberRepository memberRepo;

    private final static String MSG_NOT_FOUND = "%s が見つかりません";

    @Transactional
    public MutationResponse<MemberResponse> addMember(UUID listId, CreateMemberRequest req) {
        ItemList list = itemListRepo.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "リスト")));

        Member member = new Member();
        member.setDisplayName(req.getDisplayName().trim());
        member.setItemList(list);

        Member saved = memberRepo.save(member);

        long revision = incrementAndGetRevision(listId);

        return MutationResponse.<MemberResponse>builder()
                .revision(revision)
                .data(MemberMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public MutationResponse<MemberResponse> renameMember(UUID listId, UUID memberId, CreateMemberRequest req) {
        Member member = memberRepo.findByIdAndItemListId(memberId, listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "メンバー")));

        // Mapperでtrim含めて更新する想定
        MemberMapper.updateEntity(member, req);

        Member saved = memberRepo.save(member);

        long revision = incrementAndGetRevision(listId);

        return MutationResponse.<MemberResponse>builder()
                .revision(revision)
                .data(MemberMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public MutationResponse<DeleteMemberResponse> deleteMember(UUID listId, UUID memberId) {
        Member member = memberRepo.findByIdAndItemListId(memberId, listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "メンバー")));

        memberRepo.delete(member);
        // DB側 FK: item.completed_by_member_id ON DELETE SET NULL が効く

        long revision = incrementAndGetRevision(listId);

        return MutationResponse.<DeleteMemberResponse>builder()
                .revision(revision)
                .data(DeleteMemberResponse.builder().deletedMemberId(memberId).build())
                .build();
    }

    private long incrementAndGetRevision(UUID listId) {
        int updated = itemListRepo.incrementRevision(listId);
        if (updated != 1)
            throw new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "リスト"));
        return itemListRepo.findRevision(listId).orElseThrow();
    }
}
