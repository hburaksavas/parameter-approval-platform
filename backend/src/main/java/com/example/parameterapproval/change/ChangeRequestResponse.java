package com.example.parameterapproval.change;

import java.time.Instant;
import java.util.List;

public record ChangeRequestResponse(
        Long id,
        String requestNo,
        String title,
        String description,
        ChangeRequestStatus status,
        String createdBy,
        String createdByName,
        Instant createdAt,
        String decidedBy,
        String decidedByName,
        Instant decidedAt,
        String decisionNote,
        int itemCount,
        List<ChangeItemResponse> items
) { }

