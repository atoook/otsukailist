package com.atoook.otsukailist.service.message;

import lombok.experimental.UtilityClass;

/** サービス層で共有するエラーメッセージ定数。 将来的に国際化が必要になったら MessageSource 等へ移行する。 */
@UtilityClass
public class ErrorMessages {

  public static final String NOT_FOUND = "%s が見つかりません";
  public static final String MEMBER_DUPLICATED = "メンバー名が重複しています: %s";
}
