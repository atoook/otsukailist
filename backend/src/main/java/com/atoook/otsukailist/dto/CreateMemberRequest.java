package com.atoook.otsukailist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMemberRequest {

    @NotBlank(message = "メンバー名は必須です")
    @Size(max = 80, message = "メンバー名は80文字以下にしてください")
    private String displayName;
}
