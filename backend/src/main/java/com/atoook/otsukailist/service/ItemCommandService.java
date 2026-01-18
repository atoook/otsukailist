package com.atoook.otsukailist.service;

import java.util.UUID;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.DeleteItemResponse;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCommandService {

    private final ItemRepository itemRepo;
    private final ItemListRepository itemListRepo;
    private final MemberRepository memberRepo;

    private final ListRevisionService listRevisionService;

    private final static String MSG_NOT_FOUND = "%s が見つかりません";
    private final static String MSG_MEMBER_NOT_IN_LIST = "指定された完了者はリストのメンバーではありません";
    private final static String MSG_COMPLETED_BY_NOT_SPECIFIED = "完了者が未指定です";

    /**
     * Item追加（listIdスコープ）
     * - 完了状態を作成時に許可するなら completedByMemberId もDTOに追加するのが整合的
     * - ミニマムなら「作成時は未完了固定」を推奨
     */
    @Transactional
    public MutationResponse<ItemResponse> createItem(UUID listId, CreateItemRequest req) {
        // list存在確認
        ItemList list = itemListRepo.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "リスト")));

        // Entity作成（ミニマム：作成時は未完了固定）
        Item item = new Item();
        item.setName(req.getName().trim());
        item.setCompleted(false);
        item.setCompletedByMemberId(null);
        item.setCompletedAt(null);
        item.setItemList(list);

        Item saved = itemRepo.save(item);

        long revision = listRevisionService.incrementAndGet(listId);

        return MutationResponse.<ItemResponse>builder()
                .revision(revision)
                .data(ItemMapper.toResponse(saved))
                .build();
    }

    /**
     * Item更新（rename / setCompleted）
     */
    @Transactional
    public MutationResponse<ItemResponse> updateItem(UUID listId, UUID itemId, UpdateItemRequest req) {
        Item item = itemRepo.findByIdAndItemListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "アイテム")));

        // rename（Mapperは name のみ更新）
        ItemMapper.updateEntity(item, req);

        // completion（業務ロジック）
        if (req.getCompleted() != null) {
            if (req.getCompleted()) {
                if (req.getCompletedByMemberId() == null) {
                    throw new IllegalArgumentException(MSG_COMPLETED_BY_NOT_SPECIFIED);
                }
                boolean exists = memberRepo.existsByIdAndItemListId(req.getCompletedByMemberId(), listId);
                if (!exists) {
                    throw new IllegalArgumentException(MSG_MEMBER_NOT_IN_LIST);
                }
                item.setCompleted(true);
                item.setCompletedByMemberId(req.getCompletedByMemberId());
                item.setCompletedAt(Instant.now());
            } else {
                item.setCompleted(false);
                item.setCompletedByMemberId(null);
                item.setCompletedAt(null);
            }
        }

        Item saved = itemRepo.save(item);

        long revision = listRevisionService.incrementAndGet(listId);

        return MutationResponse.<ItemResponse>builder()
                .revision(revision)
                .data(ItemMapper.toResponse(saved))
                .build();
    }

    /**
     * Item削除（listIdスコープ）
     */
    @Transactional
    public MutationResponse<DeleteItemResponse> deleteItem(UUID listId, UUID itemId) {
        // listIdスコープで存在確認
        Item item = itemRepo.findByIdAndItemListId(itemId, listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "アイテム")));

        itemRepo.delete(item);

        long revision = listRevisionService.incrementAndGet(listId);

        return MutationResponse.<DeleteItemResponse>builder()
                .revision(revision)
                .data(DeleteItemResponse.builder().deletedItemId(itemId).build())
                .build();
    }

}