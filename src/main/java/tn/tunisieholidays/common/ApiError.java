package tn.tunisieholidays.common;

import java.time.Instant;
import java.util.List;

public record ApiError(int status, String error, String message, List<String> details, Instant timestamp) {

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(status, error, message, details == null || details.isEmpty() ? null : details, Instant.now());
    }
}
