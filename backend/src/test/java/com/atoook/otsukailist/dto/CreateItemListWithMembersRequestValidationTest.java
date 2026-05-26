package com.atoook.otsukailist.dto;

import static com.atoook.otsukailist.config.AppMemberProperties.ABSOLUTE_MAX_MEMBERS_PER_LIST;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateItemListWithMembersRequestValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldRejectMemberNamesAboveAbsoluteLimit() {
    CreateItemListWithMembersRequest request =
        CreateItemListWithMembersRequest.builder()
            .name("買い物")
            .memberNames(
                IntStream.rangeClosed(0, ABSOLUTE_MAX_MEMBERS_PER_LIST)
                    .mapToObj(index -> "member-" + index)
                    .toList())
            .build();

    var violations = validator.validate(request);

    assertThat(violations)
        .extracting(ConstraintViolation::getPropertyPath)
        .map(Object::toString)
        .contains("memberNames");
  }
}
