package com.atoook.otsukailist.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.atoook.otsukailist.config.AppItemProperties;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListItemLimitService {
  private static final String MSG_ITEM_LIMIT_EXCEEDED = "リストに追加できるアイテムは%d件までです";

  private final ItemRepository itemRepo;
  private final AppItemProperties itemProperties;

  /**
   * Validate that one more item can be added to the list.
   *
   * @param listId target list ID
   */
  public void validateCanAddOne(UUID listId) {
    validateItemCountAfterChange(listId, 1L, 0L);
  }

  /**
   * Validate item count after a generated-item sync.
   *
   * @param listId target list ID
   * @param itemsToAdd number of newly created items
   * @param itemsToDelete number of items deleted by the same operation
   */
  public void validateItemCountAfterChange(UUID listId, long itemsToAdd, long itemsToDelete) {
    long currentItemCount = itemRepo.countByItemListId(listId);
    long nextItemCount = currentItemCount + itemsToAdd - itemsToDelete;
    int maxItemsPerList = itemProperties.getMaxItemsPerList();
    if (nextItemCount > maxItemsPerList) {
      throw new BadRequestException(String.format(MSG_ITEM_LIMIT_EXCEEDED, maxItemsPerList));
    }
  }
}
