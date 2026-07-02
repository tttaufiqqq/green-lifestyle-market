package com.glm.common.error;

import java.util.List;

/**
 * Error envelope body — maps to docs/error-catalogue.md.
 * Shape: {"error": {"code": "E-XYZ", "message": "...", "details": [...], "errorId": "uuid"}}
 */
public record ApiError(
        String code,
        String message,
        List<Detail> details,
        String errorId
) {
    public record Detail(String field, String issue) {}
}
