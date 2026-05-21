package com.atoook.otsukailist.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.atoook.otsukailist.config.AppMemberProperties;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListMemberLimitService {
  private static final String MSG_MEMBER_LIMIT_EXCEEDED = "リストに追加できるメンバーは%d人までです";

  private final MemberRepository memberRepo;
  private final AppMemberProperties memberProperties;

  /**
   * Validate that one more member can be added to the list.
   *
   * @param listId target list ID
   */
  public void validateCanAddOne(UUID listId) {
    long currentMemberCount = memberRepo.countByItemListId(listId);
    validateMemberCount(currentMemberCount + 1L);
  }

  /**
   * Validate the initial member count for a newly created list.
   *
   * @param memberCount normalized initial member count
   */
  public void validateInitialMemberCount(long memberCount) {
    validateMemberCount(memberCount);
  }

  private void validateMemberCount(long memberCount) {
    int maxMembersPerList = memberProperties.getMaxMembersPerList();
    if (memberCount > maxMembersPerList) {
      throw new BadRequestException(String.format(MSG_MEMBER_LIMIT_EXCEEDED, maxMembersPerList));
    }
  }
}
