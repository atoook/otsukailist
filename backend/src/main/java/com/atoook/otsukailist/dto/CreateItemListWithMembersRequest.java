package com.atoook.otsukailist.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreateItemListWithMembersRequest {

    @NotBlank(message = "リスト名は必須です")
    @Size(max = 100, message = "リスト名は100文字以下にしてください")
    private String name;

    /**
     * 初期メンバー名一覧
     * - UI側で 1人以上を想定するなら min=1
     * - 上限は適当（例: 20）
     */
    @NotNull(message = "メンバー一覧は必須です")
    @Size(min = 1, max = 20, message = "メンバーは1〜20名で指定してください")
    private List<@NotBlank(message = "メンバー名は必須です") @Size(max = 80, message = "メンバー名は80文字以下にしてください") String> memberNames;

}
