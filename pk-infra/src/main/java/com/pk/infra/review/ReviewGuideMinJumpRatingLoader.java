package com.pk.infra.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;

public class ReviewGuideMinJumpRatingLoader {
    public static final String CONFIG_KEY = "reviewGuide.minJumpRating";
    public static final int DEFAULT_MIN_JUMP_RATING = 4;

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public ReviewGuideMinJumpRatingLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public int load() {
        return appConfigRepository.findByKey(CONFIG_KEY)
                .map(this::parse)
                .orElse(DEFAULT_MIN_JUMP_RATING);
    }

    private int parse(AppConfigRepository.AppConfigRecord record) {
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            int rating;
            if (root != null && root.isNumber()) {
                rating = root.asInt();
            } else if (root != null && root.isTextual()) {
                rating = Integer.parseInt(root.asText().trim());
            } else if (root != null && root.isObject() && root.has("minJumpRating") && root.get("minJumpRating").isNumber()) {
                rating = root.get("minJumpRating").asInt();
            } else {
                return DEFAULT_MIN_JUMP_RATING;
            }
            return rating >= 1 && rating <= 5 ? rating : DEFAULT_MIN_JUMP_RATING;
        } catch (Exception exception) {
            return DEFAULT_MIN_JUMP_RATING;
        }
    }
}
