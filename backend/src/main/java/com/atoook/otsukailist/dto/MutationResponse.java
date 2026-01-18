package com.atoook.otsukailist.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@AllArgsConstructor
@Builder
public class MutationResponse<T> {
    private long revision;
    private T data;
}
