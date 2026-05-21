package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.config.AppMemberProperties;
import com.atoook.otsukailist.dto.CreateMemberRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

  @Mock private ItemListRepository itemListRepo;
  @Mock private MemberRepository memberRepo;

  private AppMemberProperties memberProperties;
  private MemberCommandService service;

  @BeforeEach
  void setUp() {
    memberProperties = new AppMemberProperties();
    ListMemberLimitService listMemberLimitService =
        new ListMemberLimitService(memberRepo, memberProperties);
    service = new MemberCommandService(itemListRepo, memberRepo, listMemberLimitService);
  }

  @Test
  @DisplayName("メンバー数が上限に達している場合は新規追加を拒否すること")
  void addMemberRejectsWhenListAlreadyReachedMemberLimit() {
    UUID listId = UUID.randomUUID();
    memberProperties.setMaxMembersPerList(20);

    when(itemListRepo.findByIdForUpdate(listId)).thenReturn(Optional.of(itemList(listId)));
    when(memberRepo.countByItemListId(listId)).thenReturn(20L);

    CreateMemberRequest request = CreateMemberRequest.builder().displayName("追加メンバー").build();

    assertThatThrownBy(() -> service.addMember(listId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("リストに追加できるメンバーは20人までです");
  }

  private static ItemList itemList(UUID listId) {
    ItemList list = new ItemList();
    list.setId(listId);
    list.setName("買い物");
    return list;
  }
}
