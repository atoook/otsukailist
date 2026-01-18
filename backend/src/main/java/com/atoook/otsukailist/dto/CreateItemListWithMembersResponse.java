package com.atoook.otsukailist.dto;

import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemListWithMembersResponse {

    private UUID listId;
    private String name;
    private long revision;

    private List<MemberResponse> members;
}
