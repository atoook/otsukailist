package com.atoook.otsukailist.api.dto;

public enum ApiErrorCode {
  NOT_FOUND("not_found", "resource not found"),
  BAD_REQUEST("bad_request", "bad request"),
  VALIDATION_ERROR("validation_error", "validation error"),
  CONFLICT("conflict", "conflict"),
  INTERNAL_ERROR("internal_error", "internal server error");

  private final String code;
  private final String defaultMessage;

  ApiErrorCode(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  /**
   * 取得したエラーコード値を返します。
   *
   * @return API で利用するエラーコード
   */
  public String code() {
    return code;
  }

  /**
   * エラーコードに紐づくデフォルトメッセージを返します。
   *
   * @return デフォルトメッセージ
   */
  public String defaultMessage() {
    return defaultMessage;
  }
}
