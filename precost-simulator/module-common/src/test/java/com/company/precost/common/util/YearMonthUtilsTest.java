package com.company.precost.common.util;

import com.company.precost.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("YearMonthUtils - 기준월(YYYY-MM) 유틸 검증")
class YearMonthUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"2026-05", "2026-01", "2026-12", "1999-09"})
    @DisplayName("유효한 YYYY-MM 형식은 통과한다")
    void valid_yearMonth(String input) {
        assertThat(YearMonthUtils.isValid(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-5", "2026-13", "26-05", "2026/05", "2026-00", "abcd-ef", ""})
    @DisplayName("잘못된 형식은 거부한다")
    void invalid_yearMonth(String input) {
        assertThat(YearMonthUtils.isValid(input)).isFalse();
    }

    @Test
    @DisplayName("null 은 isValid=false")
    void null_isInvalid() {
        assertThat(YearMonthUtils.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("parse 실패 시 BusinessException")
    void parse_invalid_throws() {
        assertThatThrownBy(() -> YearMonthUtils.parse("2026-13"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("전월/익월 계산 - 연도 경계 포함")
    void previous_next() {
        assertThat(YearMonthUtils.previous("2026-01")).isEqualTo("2025-12");
        assertThat(YearMonthUtils.next("2026-12")).isEqualTo("2027-01");
        assertThat(YearMonthUtils.previous("2026-05")).isEqualTo("2026-04");
    }

    @Test
    @DisplayName("lastMonths - 최신순 N개월 반환")
    void lastMonths() {
        List<String> months = YearMonthUtils.lastMonths("2026-03", 4);
        assertThat(months).containsExactly("2026-03", "2026-02", "2026-01", "2025-12");
    }
}
