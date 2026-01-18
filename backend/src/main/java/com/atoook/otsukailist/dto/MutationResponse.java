package com.atoook.otsukailist.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MutationResponse<T> {
    private long revision;
    private T data;
}
