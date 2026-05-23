package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.config.AppMemberProperties;
import com.atoook.otsukailist.dto.CreateMemberRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.PreconditionFailedException;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.Member;
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

  @Test
  @DisplayName("メンバー名変更時にversionが一致しない場合は拒否すること")
  void renameMemberRejectsWhenVersionDoesNotMatch() {
    UUID listId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Member member = member(listId, memberId, 3L);
    CreateMemberRequest request = CreateMemberRequest.builder().displayName("変更後").build();

    when(memberRepo.findByIdAndItemListId(memberId, listId)).thenReturn(Optional.of(member));

    assertThatThrownBy(() -> service.renameMember(listId, memberId, request, 2L))
        .isInstanceOf(PreconditionFailedException.class);

    verify(memberRepo, never()).saveAndFlush(any(Member.class));
    verify(itemListRepo, never()).incrementRevision(listId);
  }

  @Test
  @DisplayName("メンバー削除時にversionが一致しない場合は拒否すること")
  void deleteMemberRejectsWhenVersionDoesNotMatch() {
    UUID listId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Member member = member(listId, memberId, 3L);

    when(memberRepo.findByIdAndItemListId(memberId, listId)).thenReturn(Optional.of(member));

    assertThatThrownBy(() -> service.deleteMember(listId, memberId, 2L))
        .isInstanceOf(PreconditionFailedException.class);

    verify(memberRepo, never()).delete(any(Member.class));
    verify(memberRepo, never()).flush();
    verify(itemListRepo, never()).incrementRevision(listId);
  }

  private static ItemList itemList(UUID listId) {
    ItemList list = new ItemList();
    list.setId(listId);
    list.setName("買い物");
    return list;
  }

  private static Member member(UUID listId, UUID memberId, long version) {
    ItemList list = itemList(listId);
    Member member = new Member();
    member.setId(memberId);
    member.setDisplayName("太郎");
    member.setVersion(version);
    member.setItemList(list);
    return member;
  }
}
