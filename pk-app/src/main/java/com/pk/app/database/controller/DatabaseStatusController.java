package com.pk.app.database.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.database.dto.DatabaseStatus;
import com.pk.infra.database.DatabaseConnectionChecker;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform
 *
 * Database connectivity probes.
 */
@RestController
@RequestMapping("/platform")
public class DatabaseStatusController {
    private final DatabaseConnectionChecker databaseConnectionChecker;

    public DatabaseStatusController(DatabaseConnectionChecker databaseConnectionChecker) {
        this.databaseConnectionChecker = databaseConnectionChecker;
    }

    /** Database status. */
    @GetMapping("/database")
    public ApiResponse<DatabaseStatus> database(HttpServletRequest request) {
        var result = databaseConnectionChecker.check();
        return ApiResponse.success(
                new DatabaseStatus(
                        "UP",
                        result.databaseProductName(),
                        result.databaseProductVersion(),
                        result.validationValue()
                ),
                RequestTrace.resolveTraceId(request)
        );
    }
}
