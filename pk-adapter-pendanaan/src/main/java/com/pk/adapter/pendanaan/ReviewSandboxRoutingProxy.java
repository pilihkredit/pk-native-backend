package com.pk.adapter.pendanaan;

import com.pk.core.external.LenderInteractionContext;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.review.ReviewSandboxConfigPort;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

final class ReviewSandboxRoutingProxy {
    private ReviewSandboxRoutingProxy() {
    }

    static <T> T wrap(
            Class<T> portType,
            T liveDelegate,
            T reviewDelegate,
            ReviewSandboxConfigPort configPort
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            String mobileNo = resolveMobileNo(args);
            Object target = mobileNo != null && configPort.findEnabledScenario(mobileNo).isPresent()
                    ? reviewDelegate
                    : liveDelegate;
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        };
        return portType.cast(Proxy.newProxyInstance(portType.getClassLoader(), new Class<?>[]{portType}, handler));
    }

    private static String resolveMobileNo(Object[] args) {
        String fromContext = LenderInteractionContext.mobileNo();
        if (fromContext != null && !fromContext.isBlank()) {
            return fromContext;
        }
        if (args == null) {
            return null;
        }
        for (Object argument : args) {
            if (argument instanceof LenderProfileSyncPort.LenderProfileSyncCommand command) {
                return command.mobileNo();
            }
        }
        return null;
    }
}
