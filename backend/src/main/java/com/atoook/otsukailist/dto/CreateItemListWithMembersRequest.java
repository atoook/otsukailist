package com.atoook.otsukailist.dto;

import static com.atoook.otsukailist.config.AppMemberProperties.ABSOLUTE_MAX_MEMBERS_PER_LIST;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

  @NotNull(message = "メンバー一覧は必須です")
  @Size(min = 1, max = ABSOLUTE_MAX_MEMBERS_PER_LIST, message = "メンバーは1〜{max}名で指定してください")
  private List<
          @NotBlank(message = "メンバー名は必須です") @Size(max = 80, message = "メンバー名は80文字以下にしてください") String>
      memberNames;
}
