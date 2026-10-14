package com.forknight.server.service;
import com.forknight.common.enums.PaymentStatus;

import com.forknight.common.dto.market.RatingCreateRequest;
import com.forknight.common.dto.market.RatingDTO;
import com.forknight.common.dto.market.SnippetCreateRequest;
import com.forknight.common.dto.market.SnippetDTO;
import com.forknight.common.dto.market.SnippetPurchaseResponse;
import com.forknight.common.enums.ItemType;
import com.forknight.common.enums.NotificationType;

import com.forknight.server.exception.BadRequestException;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Order;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.model.Snippet;
import com.forknight.server.model.SnippetRating;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.OrderRepository;
import com.forknight.server.repository.SnippetRatingRepository;
import com.forknight.server.repository.SnippetRepository;
import com.forknight.server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class MarketplaceService {

    private final SnippetRepository snippetRepository;
    private final OrderRepository orderRepository;
    private final SnippetRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RepoRepository repoRepository;

    public MarketplaceService(
            SnippetRepository snippetRepository,
            OrderRepository orderRepository,
            SnippetRatingRepository ratingRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            RepoRepository repoRepository
    ) {
        this.snippetRepository = snippetRepository;
        this.orderRepository = orderRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.repoRepository = repoRepository;
    }

    public SnippetDTO createSnippet(Long authorId, SnippetCreateRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Snippet snippet = new Snippet();
        snippet.setAuthor(author);
        snippet.setTitle(request.title());
        snippet.setDescription(request.description());
        snippet.setCodeContent(request.codeContent());
        snippet.setLanguage(request.language() != null ? request.language().toLowerCase() : "java");
        snippet.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
        if (request.tags() != null && !request.tags().isEmpty()) {
            snippet.setTags(String.join(",", request.tags()));
        }

        Snippet saved = snippetRepository.save(snippet);
        return saved.toDTO(true);
    }

    @Transactional(readOnly = true)
    public Page<SnippetDTO> searchSnippets(Long currentUserId, String language, BigDecimal maxPrice, String search, Pageable pageable) {
        Page<Snippet> snippets = snippetRepository.searchSnippets(language, maxPrice, search, pageable);
        return snippets.map(s -> {
            boolean isPurchased = s.getPrice().compareTo(BigDecimal.ZERO) == 0 
                    || (currentUserId != null && (s.getAuthor().getId().equals(currentUserId) 
                    || orderRepository.existsByBuyerIdAndItemTypeAndItemId(currentUserId, ItemType.SNIPPET, s.getId())));
            return s.toDTO(isPurchased);
        });
    }

    @Transactional(readOnly = true)
    public SnippetDTO getSnippetById(Long currentUserId, Long snippetId) {
        Snippet s = snippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", "id", snippetId));

        boolean isPurchased = s.getPrice().compareTo(BigDecimal.ZERO) == 0 
                || (currentUserId != null && (s.getAuthor().getId().equals(currentUserId) 
                || orderRepository.existsByBuyerIdAndItemTypeAndItemId(currentUserId, ItemType.SNIPPET, s.getId())));

        return s.toDTO(isPurchased);
    }

    public SnippetPurchaseResponse purchaseSnippet(Long buyerId, Long snippetId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        Snippet snippet = snippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", "id", snippetId));

        if (snippet.getAuthor().getId().equals(buyerId)) {
            return new SnippetPurchaseResponse(
                    0L, snippet.getId(), BigDecimal.ZERO, "ALREADY_OWNED",
                    snippet.getCodeContent(), "You are the author of this snippet."
            );
        }

        if (orderRepository.existsByBuyerIdAndItemTypeAndItemId(buyerId, ItemType.SNIPPET, snippetId)) {
            return new SnippetPurchaseResponse(
                    0L, snippet.getId(), BigDecimal.ZERO, "ALREADY_PURCHASED",
                    snippet.getCodeContent(), "You have already purchased this snippet."
            );
        }

        BigDecimal price = snippet.getPrice();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            if (buyer.getBalance().compareTo(price) < 0) {
                throw new BadRequestException("Insufficient balance. Your balance: $" + buyer.getBalance() + ", Snippet price: $" + price);
            }
            buyer.setBalance(buyer.getBalance().subtract(price));
            userRepository.save(buyer);

            User seller = snippet.getAuthor();
            seller.setBalance(seller.getBalance().add(price));
            userRepository.save(seller);
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(snippet.getAuthor());
        order.setItemType(ItemType.SNIPPET);
        order.setItemId(snippet.getId());
        order.setAmount(price);
        order.setStatus(PaymentStatus.COMPLETED);
        order.setPaymentMethod("WALLET");
        Order savedOrder = orderRepository.save(order);

        snippet.setSalesCount(snippet.getSalesCount() + 1);
        snippetRepository.save(snippet);

        // Notify seller
        String title = "Snippet Sold! 💰";
        String msg = String.format("@%s purchased your snippet '%s' for $%s!",
                buyer.getUsername(), snippet.getTitle(), price);
        notificationService.createNotification(snippet.getAuthor(), NotificationType.SYSTEM, title, msg, "SNIPPET", snippet.getId());

        return new SnippetPurchaseResponse(
                savedOrder.getId(),
                snippet.getId(),
                price,
                "COMPLETED",
                snippet.getCodeContent(),
                "Purchase completed successfully! Full code unlocked."
        );
    }

    @Transactional
    public com.forknight.common.dto.market.SnippetPurchaseResponse purchaseRepo(Long buyerId, Long repoId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        if (!repo.isForSale()) {
            throw new BadRequestException("This repository is not for sale.");
        }

        if (repo.getOwner().getId().equals(buyerId)) {
            return new com.forknight.common.dto.market.SnippetPurchaseResponse(
                    0L, repo.getId(), BigDecimal.ZERO, "ALREADY_OWNED",
                    "repo", "You are the author of this repository."
            );
        }

        if (orderRepository.existsByBuyerIdAndItemTypeAndItemId(buyerId, ItemType.REPO_LICENSE, repoId)) {
            return new com.forknight.common.dto.market.SnippetPurchaseResponse(
                    0L, repo.getId(), BigDecimal.ZERO, "ALREADY_PURCHASED",
                    "repo", "You have already purchased this repository."
            );
        }

        BigDecimal price = repo.getSalePrice() != null ? repo.getSalePrice() : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            if (buyer.getBalance().compareTo(price) < 0) {
                throw new BadRequestException("Insufficient balance. Your balance: $" + buyer.getBalance() + ", Repo price: $" + price);
            }
            buyer.setBalance(buyer.getBalance().subtract(price));
            userRepository.save(buyer);

            User seller = repo.getOwner();
            seller.setBalance(seller.getBalance().add(price));
            userRepository.save(seller);
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(repo.getOwner());
        order.setItemType(ItemType.REPO_LICENSE);
        order.setItemId(repo.getId());
        order.setAmount(price);
        order.setStatus(PaymentStatus.COMPLETED);
        order.setPaymentMethod("ACCOUNT_BALANCE");
        Order savedOrder = orderRepository.save(order);

        String title = "Repository Sale!";
        String msg = String.format("@%s purchased your repository '%s' for $%s!",
                buyer.getUsername(), repo.getName(), price);
        notificationService.createNotification(repo.getOwner(), NotificationType.SYSTEM, title, msg, "REPO", repo.getId());

        return new com.forknight.common.dto.market.SnippetPurchaseResponse(
                savedOrder.getId(),
                repo.getId(),
                price,
                "COMPLETED",
                "repo_unlocked",
                "Repository purchase completed successfully! You can now browse the files."
        );
    }

    public RatingDTO rateSnippet(Long userId, Long snippetId, RatingCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Snippet snippet = snippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", "id", snippetId));

        if (request.stars() < 1 || request.stars() > 5) {
            throw new BadRequestException("Rating stars must be between 1 and 5");
        }

        SnippetRating rating = ratingRepository.findBySnippetIdAndUserId(snippetId, userId)
                .orElseGet(() -> {
                    SnippetRating r = new SnippetRating();
                    r.setSnippet(snippet);
                    r.setUser(user);
                    return r;
                });

        rating.setStars(request.stars());
        rating.setReview(request.review());
        SnippetRating saved = ratingRepository.save(rating);

        // Recalculate average rating
        List<SnippetRating> allRatings = ratingRepository.findBySnippetIdOrderByCreatedAtDesc(snippetId);
        double avg = allRatings.stream().mapToInt(SnippetRating::getStars).average().orElse(0.0);
        snippet.setRatingAvg(Math.round(avg * 10.0) / 10.0);
        snippet.setRatingCount(allRatings.size());
        snippetRepository.save(snippet);

        return saved.toDTO();
    }

    @Transactional(readOnly = true)
    public List<RatingDTO> getSnippetRatings(Long snippetId) {
        return ratingRepository.findBySnippetIdOrderByCreatedAtDesc(snippetId)
                .stream()
                .map(SnippetRating::toDTO)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<com.forknight.common.dto.market.TransactionDTO> getTransactionHistory(Long userId) {
        List<Order> orders = orderRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
        return orders.stream().map(o -> {
            boolean isBuyer = o.getBuyer().getId().equals(userId);
            String type = isBuyer ? "SPEND" : "EARN";
            String counterparty = isBuyer ? o.getSeller().getUsername() : o.getBuyer().getUsername();
            return new com.forknight.common.dto.market.TransactionDTO(
                    o.getId(),
                    type,
                    o.getAmount(),
                    o.getItemType().name(),
                    o.getItemId(),
                    counterparty,
                    o.getCreatedAt()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal getMyBalance(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getBalance() != null ? user.getBalance() : java.math.BigDecimal.ZERO;
    }
}

