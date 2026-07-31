package com.pk.app.config.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.infra.appconfig.AppConfigFacade;
import org.springframework.stereotype.Service;

@Service
public class AppConfigApplicationService {
    private final AppConfigFacade appConfigFacade;

    public AppConfigApplicationService(AppConfigFacade appConfigFacade) {
        this.appConfigFacade = appConfigFacade;
    }

    public JsonNode getByKey(String key) {
        return appConfigFacade.getValueByKey(key);
    }
}
