package com.atoook.otsukailist.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.ItemListSnapshotResponse;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.ListMetaItemResponse;
import com.atoook.otsukailist.dto.MemberResponse;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.mapper.MemberMapper;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.service.message.ErrorMessages;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListQueryService {

  private final ItemListRepository itemListRepo;
  private final MemberRepository memberRepo;
  private final ItemRepository itemRepo;

  /**
   * Retrieve the latest snapshot of a list including members and items.
   *
   * @param listId target list ID
   * @return aggregated snapshot response
   */
  @Transactional(readOnly = true)
  public ItemListSnapshotResponse snapshot(UUID listId) {
    ItemList list =
        itemListRepo
            .findById(listId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));

    List<MemberResponse> members =
        memberRepo.findByItemListId(listId).stream().map(MemberMapper::toResponse).toList();

    List<Item> itemEntities = itemRepo.findByItemListIdOrderByDisplayRules(listId);
    List<ItemResponse> items = itemEntities.stream().map(ItemMapper::toResponse).toList();

    Instant lastItemActivityAt =
        itemEntities.stream().map(Item::getUpdatedAt).max(Comparator.naturalOrder()).orElse(null);

    return ItemListSnapshotResponse.builder()
        .listId(list.getId())
        .name(list.getName())
        .revision(list.getRevision())
        .itemCount(itemEntities.size())
        .serverTime(Instant.now())
        .lastItemActivityAt(lastItemActivityAt)
        .members(members)
        .items(items)
        .build();
  }

  /**
   * 複数リストのメタ情報をまとめて取得する。
   *
   * <p>存在しないlistId（削除済みなど）は結果に含まれない。 アイテムが0件のリストは itemCount=0, completeCount=0,
   * lastItemActivityAt=null で返す。
   *
   * @param listIds 取得対象のリストID一覧（最大10件）
   * @return 存在するリストのメタ情報一覧
   */
  @Transactional(readOnly = true)
  public List<ListMetaItemResponse> getListsMeta(List<UUID> listIds) {
    List<ItemList> existingLists = itemListRepo.findAllById(listIds);

    // アイテム集計をバッチ取得し、listId -> projection のマップに変換
    Map<UUID, ItemRepository.ItemSummaryProjection> summaryMap =
        itemRepo.summarizeByListIds(listIds).stream()
            .collect(Collectors.toMap(ItemRepository.ItemSummaryProjection::getListId, p -> p));

    return existingLists.stream()
        .map(
            list -> {
              ItemRepository.ItemSummaryProjection summary = summaryMap.get(list.getId());
              return ListMetaItemResponse.builder()
                  .listId(list.getId())
                  .name(list.getName())
                  .itemCount(summary != null ? summary.getItemCount() : 0L)
                  .completeCount(summary != null ? summary.getCompleteCount() : 0L)
                  .lastItemActivityAt(summary != null ? summary.getLastItemActivityAt() : null)
                  .build();
            })
        .toList();
  }
}
