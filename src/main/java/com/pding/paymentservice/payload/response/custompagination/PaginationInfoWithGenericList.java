package com.pding.paymentservice.payload.response.custompagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Builder
public class PaginationInfoWithGenericList<T> {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private List<T> content;
    private Boolean hasNext;

    public PaginationInfoWithGenericList(int pageNumber, int pageSize, long totalElements, int totalPages, List<T> content) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    public PaginationInfoWithGenericList(int pageNumber, int pageSize, long totalElements, int totalPages, List<T> content, boolean hasNext) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
        this.hasNext = hasNext;
    }
}
