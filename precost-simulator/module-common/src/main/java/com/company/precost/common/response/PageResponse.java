package com.company.precost.common.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 응답 표준 형식.
 * Spring Data 의 Page<T> 를 프론트엔드 친화적인 형태로 변환한다.
 *
 * @param <T> 콘텐츠 타입
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;          // 0-base 현재 페이지
    private final int size;          // 페이지 크기
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    private PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    private PageResponse(List<T> content, int page, int size, long totalElements,
                         int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    /** Page<T> 를 그대로 변환 */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page);
    }

    /** Entity Page 를 DTO 로 매핑하면서 변환 */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        List<T> mapped = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(mapped, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
