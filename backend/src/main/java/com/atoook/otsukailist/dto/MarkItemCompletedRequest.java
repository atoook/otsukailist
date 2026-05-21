package com.atoook.otsukailist.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for marking an item as completed. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkItemCompletedRequest {

  private UUID completedByMemberId;
}
