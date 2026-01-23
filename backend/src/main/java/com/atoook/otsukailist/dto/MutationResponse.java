package com.atoook.otsukailist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MutationResponse<T> {
  private long revision;
  private T data;
}
