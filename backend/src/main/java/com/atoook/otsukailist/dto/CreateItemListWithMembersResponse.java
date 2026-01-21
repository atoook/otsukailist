package com.atoook.otsukailist.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateItemListWithMembersResponse {

    private UUID listId;
    private String name;

    private List<MemberResponse> members;
}
