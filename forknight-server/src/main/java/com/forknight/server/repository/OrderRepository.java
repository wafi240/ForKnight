package com.forknight.server.repository;

import com.forknight.common.enums.ItemType;
import com.forknight.server.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    boolean existsByBuyerIdAndItemTypeAndItemId(Long buyerId, ItemType itemType, Long itemId);

    List<Order> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}

