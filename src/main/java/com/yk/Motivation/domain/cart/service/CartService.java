package com.yk.Motivation.domain.cart.service;

import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.cart.entity.CartItem;
import com.yk.Motivation.domain.cart.repository.CartItemRepository;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartItemRepository cartItemRepository;

    @Transactional
    public RsData<CartItem> addItem(Member buyer, Product product) {
        CartItem oldCartItem = cartItemRepository.findByBuyerIdAndProductId(buyer.getId(), product.getId()).orElse(null);

        if (oldCartItem != null) {
            return new RsData<>("S-2", null, oldCartItem);
        }

        CartItem cartItem = CartItem.builder()
                .buyer(buyer)
                .product(product)
                .build();

        cartItemRepository.save(cartItem);

        return new RsData<>("S-1", "수강 바구니에 추가되었습니다.", cartItem);
    }

    @Transactional
    public boolean removeItem(Member buyer, Product product) {
        CartItem oldCartItem = cartItemRepository.findByBuyerIdAndProductId(buyer.getId(), product.getId()).orElse(null);

        if (oldCartItem != null) {
            cartItemRepository.delete(oldCartItem);
            return true;
        }

        return false;
    }

    public boolean hasItem(Member buyer, Product product) {
        return cartItemRepository.existsByBuyerIdAndProductId(buyer.getId(), product.getId());
    }

    public List<CartItem> getItemsByBuyer(Member buyer) {
        return cartItemRepository.findAllByBuyerId(buyer.getId());
    }

    @Transactional
    public void removeItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    public Optional<CartItem> findItemById(long id) {
        return cartItemRepository.findById(id);
    }

    public Long findItemCountById(Long id) {
        return cartItemRepository.countByBuyerId(id);
    }

    public boolean actorCanDelete(Member buyer, CartItem cartItem) {
        return buyer.getId().equals(cartItem.getBuyer().getId());
    }


}
