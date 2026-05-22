package com.atoook.otsukailist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.PreconditionRequiredException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpPreconditionsTest {

  @Test
  @DisplayName("versionからquoted ETagを生成できること")
  void toVersionEtagQuotesVersion() {
    assertThat(HttpPreconditions.toVersionEtag(7L)).isEqualTo("\"7\"");
  }

  @Test
  @DisplayName("If-Matchのquoted versionを読み取れること")
  void requireIfMatchVersionParsesQuotedVersion() {
    assertThat(HttpPreconditions.requireIfMatchVersion("\"7\"")).isEqualTo(7L);
  }

  @Test
  @DisplayName("If-Matchが未指定の場合は428用例外にすること")
  void requireIfMatchVersionRejectsMissingHeader() {
    assertThatThrownBy(() -> HttpPreconditions.requireIfMatchVersion(null))
        .isInstanceOf(PreconditionRequiredException.class);
  }

  @Test
  @DisplayName("weak ETagは更新前提条件として拒否すること")
  void requireIfMatchVersionRejectsWeakEtag() {
    assertThatThrownBy(() -> HttpPreconditions.requireIfMatchVersion("W/\"7\""))
        .isInstanceOf(BadRequestException.class);
  }
}
