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

    /**
     * Database Status
     *
     * Runs SELECT 1 against the configured datasource. Requires Bearer access token.
     *
     * @param request servlet request for trace id
     * @return database product info and validation result
     */
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
