package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.atoook.otsukailist.config.AppMemberProperties;
import com.atoook.otsukailist.dto.CreateItemListWithMembersRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandServiceTest {

  @Mock private ItemListRepository itemListRepo;
  @Mock private MemberRepository memberRepo;
  @Mock private ListRevisionService listRevisionService;

  private AppMemberProperties memberProperties;
  private ListCommandService service;

  @BeforeEach
  void setUp() {
    memberProperties = new AppMemberProperties();
    ListMemberLimitService listMemberLimitService =
        new ListMemberLimitService(memberRepo, memberProperties);
    service =
        new ListCommandService(
            itemListRepo, memberRepo, listRevisionService, listMemberLimitService);
  }

  @Test
  @DisplayName("初期メンバー数が上限を超える場合はリスト作成を拒否すること")
  void createListRejectsWhenInitialMembersExceedLimit() {
    memberProperties.setMaxMembersPerList(2);
    CreateItemListWithMembersRequest request =
        CreateItemListWithMembersRequest.builder()
            .name("買い物")
            .memberNames(List.of("太郎", "花子", "次郎"))
            .build();

    assertThatThrownBy(() -> service.createListWithMembers(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("リストに追加できるメンバーは2人までです");
  }
}
