package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.notification.NotificationDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NotificationApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<List<NotificationDTO>> getNotifications() {
        return apiClient.get("/notifications", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<NotificationDTO>>() {}));
    }

    public CompletableFuture<List<NotificationDTO>> getUnreadNotifications() {
        return apiClient.get("/notifications/unread", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<NotificationDTO>>() {}));
    }

    public CompletableFuture<Void> markAsRead(Long id) {
        return apiClient.put("/notifications/" + id + "/read", null, Void.class);
    }

    public CompletableFuture<Void> markAllAsRead() {
        return apiClient.put("/notifications/read-all", null, Void.class);
    }
}
