package io.pinect.azeron.example.server.azeron.domain;

import org.springframework.data.domain.PageRequest;

public class OffsetLimitPageable extends PageRequest {
    private int offset;

    public OffsetLimitPageable(int offset, int limit) {
        super(offset, limit);
        this.offset = offset;
    }

    @Override
    public long getOffset() {
        return this.offset;
    }
}
