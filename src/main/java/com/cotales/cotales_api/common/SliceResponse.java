package com.cotales.cotales_api.common;

import java.util.List;
import org.springframework.data.domain.Slice;

public record SliceResponse<T>(List<T> content, int page, int size, boolean hasNext) {

    public static <T> SliceResponse<T> of(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(), slice.getNumber(), slice.getSize(), slice.hasNext());
    }
}
