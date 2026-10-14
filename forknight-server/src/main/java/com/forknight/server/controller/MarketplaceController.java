package com.forknight.server.controller;

import com.forknight.common.dto.PagedResponse;
import com.forknight.common.dto.market.RatingCreateRequest;
import com.forknight.common.dto.market.RatingDTO;
import com.forknight.common.dto.market.SnippetCreateRequest;
import com.forknight.common.dto.market.SnippetDTO;
import com.forknight.common.dto.market.SnippetPurchaseResponse;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.MarketplaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/snippets")
    public ResponseEntity<SnippetDTO> createSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SnippetCreateRequest request
    ) {
        SnippetDTO snippet = marketplaceService.createSnippet(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(snippet);
    }

    @GetMapping("/snippets")
    public ResponseEntity<PagedResponse<SnippetDTO>> searchSnippets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = userPrincipal != null ? userPrincipal.id() : null;
        Page<SnippetDTO> paged = marketplaceService.searchSnippets(userId, language, maxPrice, search, PageRequest.of(page, size));
        return ResponseEntity.ok(new PagedResponse<>(
                paged.getContent(),
                paged.getNumber(),
                paged.getSize(),
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.isLast()
        ));
    }

    @GetMapping("/snippets/{id}")
    public ResponseEntity<SnippetDTO> getSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        Long userId = userPrincipal != null ? userPrincipal.id() : null;
        return ResponseEntity.ok(marketplaceService.getSnippetById(userId, id));
    }

    @PostMapping("/snippets/{id}/purchase")
    public ResponseEntity<SnippetPurchaseResponse> purchaseSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        SnippetPurchaseResponse response = marketplaceService.purchaseSnippet(userPrincipal.id(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/repos/{id}/purchase")
    public ResponseEntity<SnippetPurchaseResponse> purchaseRepo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        SnippetPurchaseResponse response = marketplaceService.purchaseRepo(userPrincipal.id(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/snippets/{id}/ratings")
    public ResponseEntity<RatingDTO> rateSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody RatingCreateRequest request
    ) {
        RatingDTO rating = marketplaceService.rateSnippet(userPrincipal.id(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @GetMapping("/snippets/{id}/ratings")
    public ResponseEntity<List<RatingDTO>> getRatings(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.getSnippetRatings(id));
    }
    @GetMapping("/transactions")
    public ResponseEntity<List<com.forknight.common.dto.market.TransactionDTO>> getTransactionHistory(@AuthenticationPrincipal com.forknight.server.security.UserPrincipal userPrincipal) {
        return ResponseEntity.ok(marketplaceService.getTransactionHistory(userPrincipal.getId()));
    }
    @GetMapping("/balance")
    public ResponseEntity<java.math.BigDecimal> getMyBalance(@AuthenticationPrincipal com.forknight.server.security.UserPrincipal userPrincipal) {
        return ResponseEntity.ok(marketplaceService.getMyBalance(userPrincipal.getId()));
    }
}


