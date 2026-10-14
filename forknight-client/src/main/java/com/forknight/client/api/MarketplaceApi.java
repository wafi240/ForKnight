package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.PagedResponse;
import com.forknight.common.dto.market.RatingCreateRequest;
import com.forknight.common.dto.market.RatingDTO;
import com.forknight.common.dto.market.SnippetCreateRequest;
import com.forknight.common.dto.market.SnippetDTO;
import com.forknight.common.dto.market.SnippetPurchaseResponse;
import com.forknight.common.util.JsonUtil;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MarketplaceApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<PagedResponse<SnippetDTO>> searchSnippets(String language, BigDecimal maxPrice, String search, int page, int size) {
        StringBuilder url = new StringBuilder("/market/snippets?page=").append(page).append("&size=").append(size);
        if (language != null && !language.isBlank() && !language.equalsIgnoreCase("all")) {
            url.append("&language=").append(URLEncoder.encode(language.trim(), StandardCharsets.UTF_8));
        }
        if (maxPrice != null) {
            url.append("&maxPrice=").append(maxPrice);
        }
        if (search != null && !search.isBlank()) {
            url.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }

        return apiClient.get(url.toString(), String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<PagedResponse<SnippetDTO>>() {}));
    }

    public CompletableFuture<SnippetDTO> createSnippet(SnippetCreateRequest request) {
        return apiClient.post("/market/snippets", request, SnippetDTO.class);
    }

    public CompletableFuture<SnippetDTO> getSnippet(Long id) {
        return apiClient.get("/market/snippets/" + id, SnippetDTO.class);
    }

    public CompletableFuture<SnippetPurchaseResponse> purchaseSnippet(Long id) {
        return apiClient.post("/market/snippets/" + id + "/purchase", null, SnippetPurchaseResponse.class);
    }

    public CompletableFuture<RatingDTO> rateSnippet(Long id, RatingCreateRequest request) {
        return apiClient.post("/market/snippets/" + id + "/ratings", request, RatingDTO.class);
    }

    public CompletableFuture<List<RatingDTO>> getRatings(Long id) {
        return apiClient.get("/market/snippets/" + id + "/ratings", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<RatingDTO>>() {}));
    }

    public CompletableFuture<SnippetPurchaseResponse> purchaseRepo(Long id) {
        return apiClient.post("/market/repos/" + id + "/purchase", null, SnippetPurchaseResponse.class);
    }
    public CompletableFuture<java.math.BigDecimal> getMyBalance() {
        return apiClient.get("/market/balance", java.math.BigDecimal.class);
    }

    public CompletableFuture<List<com.forknight.common.dto.market.TransactionDTO>> getTransactionHistory() {
        return apiClient.get("/market/transactions", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<com.forknight.common.dto.market.TransactionDTO>>() {}));
    }
}
