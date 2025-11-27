package com.internal.utils.pagination;

import com.internal.feature.master_data.dto.response.ClsCommuneDto;
import lombok.Data;

import java.util.List;

@Data
public class PaginationResponse<T> {
    private List<T> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PaginationResponse(List<T> content, int pageNo, int pageSize, long totalElements) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
}
