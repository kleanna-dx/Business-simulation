package com.company.precost.common.util;

import com.company.precost.common.exception.BusinessException;
import com.company.precost.common.exception.ErrorCode;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * target_month('YYYY-MM') 처리 유틸.
 */
public final class YearMonthUtils {

    public static final Pattern PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private YearMonthUtils() {
    }

    /** 'YYYY-MM' 형식 유효성 검사 */
    public static boolean isValid(String yearMonth) {
        if (yearMonth == null || !PATTERN.matcher(yearMonth).matches()) {
            return false;
        }
        try {
            YearMonth.parse(yearMonth, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** 검증 후 YearMonth 로 파싱. 실패 시 BusinessException */
    public static YearMonth parse(String yearMonth) {
        if (!isValid(yearMonth)) {
            throw new BusinessException(ErrorCode.INVALID_TARGET_MONTH,
                    "기준월 형식이 올바르지 않습니다: " + yearMonth);
        }
        return YearMonth.parse(yearMonth, FORMATTER);
    }

    /** 전월 'YYYY-MM' 반환 */
    public static String previous(String yearMonth) {
        return parse(yearMonth).minusMonths(1).format(FORMATTER);
    }

    /** 익월 'YYYY-MM' 반환 */
    public static String next(String yearMonth) {
        return parse(yearMonth).plusMonths(1).format(FORMATTER);
    }

    /** N개월 전 목록 (최신순), 대시보드 추이용 */
    public static java.util.List<String> lastMonths(String yearMonth, int count) {
        YearMonth base = parse(yearMonth);
        java.util.List<String> result = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(base.minusMonths(i).format(FORMATTER));
        }
        return result;
    }
}
