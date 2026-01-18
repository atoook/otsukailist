package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberResponse {

    private UUID id;

    private String displayName;

    private Instant createdAt;

    private Instant updatedAt;
}
