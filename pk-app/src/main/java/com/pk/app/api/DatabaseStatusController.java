package com.pk.app.api;

import com.pk.infra.database.DatabaseConnectionChecker;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pk/v1/platform")
public class DatabaseStatusController {
    private final DatabaseConnectionChecker databaseConnectionChecker;

    public DatabaseStatusController(DatabaseConnectionChecker databaseConnectionChecker) {
        this.databaseConnectionChecker = databaseConnectionChecker;
    }

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
