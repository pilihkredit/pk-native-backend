package com.pk.infra.push;

import com.pk.core.push.PushDeviceTarget;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.FcmPushPort;
import com.pk.core.push.port.PushAudienceRepository;
import com.pk.core.push.port.PushDeviceRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Delivers one published push task to its resolved recipients through FCM. */
public class PushDeliveryFacade {
    private static final Logger log = LoggerFactory.getLogger(PushDeliveryFacade.class);
    private static final int FULL_SCAN_PAGE_SIZE = 500;

    private final FcmPushPort fcmPushPort;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushAudienceRepository pushAudienceRepository;
    private final PushNotificationTaskRepository taskRepository;
    private final PushPayloadAssembler payloadAssembler;
    private final int sendConcurrency;

    public PushDeliveryFacade(
            FcmPushPort fcmPushPort,
            PushDeviceRepository pushDeviceRepository,
            PushAudienceRepository pushAudienceRepository,
            PushNotificationTaskRepository taskRepository,
            PushPayloadAssembler payloadAssembler,
            int sendConcurrency
    ) {
        this.fcmPushPort = fcmPushPort;
        this.pushDeviceRepository = pushDeviceRepository;
        this.pushAudienceRepository = pushAudienceRepository;
        this.taskRepository = taskRepository;
        this.payloadAssembler = payloadAssembler;
        this.sendConcurrency = Math.max(1, sendConcurrency);
    }

    public record DeliveryResult(int totalCount, int successCount, int failureCount) {
    }

    /** Delivers the task; clear_required tasks first mark their targets cleared server-side. */
    public DeliveryResult deliver(PushNotificationTask task) {
        if (task.isClearRequired() && !task.clearTargetIds().isEmpty()) {
            int cleared = taskRepository.markCleared(task.clearTargetIds(), Instant.now());
            log.info("Push clear task {} superseded {} mandatory tasks", task.id(), cleared);
        }
        List<String> tokens = resolveRecipientTokens(task);
        PushPayloadAssembler.FcmTemplate template = payloadAssembler.assemble(task);
        return sendAll(task.id(), tokens, template);
    }

    private List<String> resolveRecipientTokens(PushNotificationTask task) {
        Set<String> tokens = new LinkedHashSet<>();
        if ("upload".equals(task.audienceType())) {
            for (Long userId : pushAudienceRepository.findUserIdsByTaskId(task.id())) {
                for (PushDeviceTarget target : pushDeviceRepository.findTargetsByUserId(userId)) {
                    addToken(tokens, target.fcmToken());
                }
            }
            return List.copyOf(tokens);
        }
        long cursor = 0L;
        while (true) {
            List<PushDeviceTarget> page = pushDeviceRepository.findTargetsAfterId(cursor, FULL_SCAN_PAGE_SIZE);
            if (page.isEmpty()) {
                break;
            }
            for (PushDeviceTarget target : page) {
                addToken(tokens, target.fcmToken());
                cursor = target.id();
            }
        }
        return List.copyOf(tokens);
    }

    private static void addToken(Set<String> tokens, String token) {
        if (token != null && !token.isBlank()) {
            tokens.add(token.trim());
        }
    }

    private DeliveryResult sendAll(long taskId, List<String> tokens, PushPayloadAssembler.FcmTemplate template) {
        if (tokens.isEmpty()) {
            log.warn("Push task {} resolved no recipient tokens", taskId);
            return new DeliveryResult(0, 0, 0);
        }
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(sendConcurrency);
        try {
            List<Future<Void>> futures = new ArrayList<>(tokens.size());
            for (String token : tokens) {
                futures.add(executor.submit(sendOne(token, template, success, failure)));
            }
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Push delivery interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Push delivery failed", exception);
        } finally {
            executor.shutdownNow();
        }
        return new DeliveryResult(tokens.size(), success.get(), failure.get());
    }

    private Callable<Void> sendOne(
            String token,
            PushPayloadAssembler.FcmTemplate template,
            AtomicInteger success,
            AtomicInteger failure
    ) {
        return () -> {
            try {
                FcmPushPort.FcmSendResult result = fcmPushPort.send(new FcmPushPort.FcmSendCommand(
                        token,
                        template.notificationTitle(),
                        template.notificationBody(),
                        template.data()
                ));
                if (result != null && result.success()) {
                    success.incrementAndGet();
                } else {
                    failure.incrementAndGet();
                    log.warn("Push send failed token={} response={}",
                            token,
                            result == null ? "null" : result.rawBody());
                }
            } catch (RuntimeException exception) {
                failure.incrementAndGet();
                log.warn("Push send error token={}: {}", token, exception.getMessage());
            }
            return null;
        };
    }
}
