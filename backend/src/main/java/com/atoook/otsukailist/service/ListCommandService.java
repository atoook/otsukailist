package com.atoook.otsukailist.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.mapper.MemberMapper;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.Member;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.mapper.ItemListMapper;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.dto.CreateItemListWithMembersRequest;
import com.atoook.otsukailist.dto.CreateItemListWithMembersResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemListRequest;
import com.atoook.otsukailist.service.message.ErrorMessages;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCommandService {

    private final ItemListRepository itemListRepo;
    private final MemberRepository memberRepo;

    private final ListRevisionService listRevisionService;

    /**
     * Create a list with its initial member set.
     *
     * @param req list and member creation request
     * @return response that contains stored entities
     */
    @Transactional
    public MutationResponse<CreateItemListWithMembersResponse> createListWithMembers(
            CreateItemListWithMembersRequest req) {
        String listName = req.getName().trim();

        ItemList list = new ItemList();
        list.setName(listName);
        ItemList savedList = itemListRepo.save(list);

        // 正規化 + 重複チェック（アプリ側で早期に分かりやすく）
        List<String> names = req.getMemberNames().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        Set<String> seen = new HashSet<>();
        for (String n : names) {
            if (!seen.add(n)) {
                throw new BadRequestException(String.format(ErrorMessages.MEMBER_DUPLICATED, n));
            }
        }

        List<Member> members = new ArrayList<>();
        for (String n : names) {
            Member m = new Member();
            m.setDisplayName(n);
            m.setItemList(savedList);
            members.add(m);
        }
        List<Member> savedMembers = memberRepo.saveAll(members);

        CreateItemListWithMembersResponse data = CreateItemListWithMembersResponse.builder()
                .listId(savedList.getId())
                .name(savedList.getName())
                .members(savedMembers.stream().map(MemberMapper::toResponse).toList())
                .build();

        return MutationResponse.<CreateItemListWithMembersResponse>builder()
                .revision(savedList.getRevision())
                .data(data)
                .build();
    }

    /**
     * Rename an existing list and return the latest revision.
     *
     * @param listId identifier of the target list
     * @param req    rename request payload
     * @return renamed list payload with the incremented revision
     */
    @Transactional
    public MutationResponse<ItemListResponse> renameList(UUID listId, UpdateItemListRequest req) {
        ItemList list = itemListRepo.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));

        list.setName(req.getName().trim());

        // list保存（updated_at更新）
        ItemList saved = itemListRepo.save(list);

        long revision = listRevisionService.incrementAndGet(listId);

        return MutationResponse.<ItemListResponse>builder()
                .revision(revision)
                .data(ItemListMapper.toResponse(saved)) // list単体mapper想定（items触らない）
                .build();
    }

}
