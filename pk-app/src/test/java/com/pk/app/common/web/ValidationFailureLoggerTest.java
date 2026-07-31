package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

class ValidationFailureLoggerTest {
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(ValidationFailureLogger.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

    @Test
    void logsApiExceptionDetail() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/profile/personal");
        request.addHeader("X-Trace-Id", "trace-device");

        ValidationFailureLogger.logApiException(
                logger,
                new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.deviceNo does not match header X-Device-No"
                ),
                request
        );

        assertThat(logAppender.list)
                .anyMatch(event -> event.getFormattedMessage().contains("trace-device")
                        && event.getFormattedMessage().contains("device.deviceNo does not match header X-Device-No"));
    }
}
