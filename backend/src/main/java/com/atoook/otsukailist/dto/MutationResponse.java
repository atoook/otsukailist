package com.atoook.otsukailist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MutationResponse<T> {
  private long revision;
  private T data;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean changed;
}
