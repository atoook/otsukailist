package com.atoook.otsukailist.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DeleteMemberResponse {
    private UUID deletedMemberId;
}
