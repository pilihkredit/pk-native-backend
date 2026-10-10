package com.pk.infra.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiException;
import com.pk.core.push.PushNotificationTask;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PushPayloadAssemblerTest {
    private final PushPayloadAssembler assembler = new PushPayloadAssembler();

    @Test
    void internalPopupBuildsFullPathWithEncodedParams() {
        var task = task(PushNotificationTask.TYPE_INTERNAL, true, List.of("/profile", "/home"), null);

        var template = assembler.assemble(task);

        assertThat(template.notificationTitle()).isNull();
        assertThat(template.notificationBody()).isNull();
        assertThat(template.data().get("type")).isEqualTo("IN_APP_POPUP");
        assertThat(template.data().get("mandatory")).isEqualTo("true");
        assertThat(template.data().get("popupId")).isEqualTo("1001");
        String path = template.data().get("path");
        assertThat(path).startsWith("/in-app-popup?");
        assertThat(path).contains("title=Loan+approved");
        assertThat(path).contains("button=Lihat");
        assertThat(URLDecoder.decode(path, StandardCharsets.UTF_8))
                .contains("showOn=/profile,/home");
        // Jump target is a standalone data field now, not a path query param.
        assertThat(path).doesNotContain("url=");
        assertThat(template.data().get("popupUrl")).isEqualTo("/profile/loan-history/123");
        assertThat(template.data()).doesNotContainKey("clickUrl");
    }

    @Test
    void pathPrefixIsFixedAndSkipsBlankParams() {
        var task = task(PushNotificationTask.TYPE_INTERNAL, false, List.of(), "/h5/popup");

        var template = assembler.assemble(task);

        assertThat(template.data().get("mandatory")).isEqualTo("false");
        String path = template.data().get("path");
        // Client contract fixes the popup prefix; the backoffice internalUrl is ignored.
        assertThat(path).startsWith("/in-app-popup?");
        assertThat(path).doesNotContain("showOn=");
        assertThat(path).doesNotContain("bannerUrl=");
        assertThat(template.data().get("popupUrl")).isEqualTo("/profile/loan-history/123");
    }

    @Test
    void externalOnlySendsNotificationWithClickUrl() {
        var task = task(PushNotificationTask.TYPE_EXTERNAL, false, List.of(), null);

        var template = assembler.assemble(task);

        assertThat(template.notificationTitle()).isEqualTo("Loan approved");
        assertThat(template.notificationBody()).isEqualTo("Your loan has been approved");
        assertThat(template.data().get("type")).isEqualTo("EXTERNAL_NOTIFY");
        assertThat(template.data().get("clickUrl")).isEqualTo("/repay?from=notify");
        assertThat(template.data()).doesNotContainKey("path");
        assertThat(template.data()).doesNotContainKey("mandatory");
    }

    @Test
    void allCombinesNotificationPopupPathAndClickUrl() {
        var task = task(PushNotificationTask.TYPE_ALL, true, List.of("/profile"), "/h5/popup");

        var template = assembler.assemble(task);

        assertThat(template.notificationTitle()).isEqualTo("Loan approved");
        assertThat(template.data().get("type")).isEqualTo("IN_APP_POPUP");
        assertThat(template.data().get("mandatory")).isEqualTo("true");
        assertThat(template.data().get("path")).startsWith("/in-app-popup?");
        assertThat(template.data().get("clickUrl")).isEqualTo("/repay?from=notify");
    }

    @Test
    void clearRequiredSendsCancelTypeWithPopupIds() {
        var task = new PushNotificationTask(
                1004L, "cancel", "", "", "", PushNotificationTask.TYPE_CLEAR_REQUIRED, false,
                List.of(), null, null, null, null, "all", null, List.of(1001L, 1002L), null,
                PushNotificationTask.STATUS_PUBLISHED, null, 0, 0, 0, null, Instant.now(), null
        );

        var template = assembler.assemble(task);

        assertThat(template.notificationTitle()).isNull();
        assertThat(template.data().get("type")).isEqualTo("IN_APP_POPUP_CANCEL");
        assertThat(template.data().get("popupIds")).isEqualTo("1001,1002");
        assertThat(template.data().get("popupId")).isEqualTo("1004");
    }

    @Test
    void rejectsPayloadOverFcmLimit() {
        String hugeBody = "a".repeat(6000);
        var task = new PushNotificationTask(
                1005L, "t", "title", hugeBody, "btn", PushNotificationTask.TYPE_INTERNAL, false,
                List.of(), null, null, null, null, "all", null, List.of(), null,
                PushNotificationTask.STATUS_PUBLISHED, null, 0, 0, 0, null, Instant.now(), null
        );

        assertThatThrownBy(() -> assembler.assemble(task))
                .isInstanceOf(ApiException.class);
    }

    private static PushNotificationTask task(String type, boolean required, List<String> pages,
            String internalUrl) {
        return new PushNotificationTask(
                1001L,
                "task-name",
                "Loan approved",
                "Your loan has been approved",
                "Lihat",
                type,
                required,
                pages,
                internalUrl,
                "/profile/loan-history/123",
                "/repay?from=notify",
                null,
                "all",
                null,
                List.of(),
                null,
                PushNotificationTask.STATUS_PUBLISHED,
                null,
                0,
                0,
                0,
                null,
                Instant.now(),
                null
        );
    }
}
