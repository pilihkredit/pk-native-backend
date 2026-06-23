package com.pk.app.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.infra.database.DatabaseConnectionChecker;
import com.pk.infra.database.DatabaseConnectionResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class DatabaseStatusControllerTest {
    @Test
    void returnsWrappedDatabaseStatus() {
        DatabaseConnectionChecker checker = mock(DatabaseConnectionChecker.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-456");
        when(checker.check()).thenReturn(new DatabaseConnectionResult("MySQL", "8.0", 1));

        ApiResponse<DatabaseStatus> response = new DatabaseStatusController(checker).database(request);

        assertThat(response.code()).isEqualTo("000000");
        assertThat(response.traceId()).isEqualTo("trace-456");
        assertThat(response.data().status()).isEqualTo("UP");
        assertThat(response.data().databaseProductName()).isEqualTo("MySQL");
        assertThat(response.data().validationValue()).isEqualTo(1);
    }
}
