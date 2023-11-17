package com.yk.Motivation.domain.order.service;

import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.cart.entity.CartItem;
import com.yk.Motivation.domain.cart.service.CartService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.service.MemberService;
import com.yk.Motivation.domain.order.entity.Order;
import com.yk.Motivation.domain.order.entity.OrderItem;
import com.yk.Motivation.domain.order.repository.OrderItemRepository;
import com.yk.Motivation.domain.order.repository.OrderRepository;
import com.yk.Motivation.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final MemberService memberService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order createFromCart(Member buyer) {
        // 입력된 회원의 장바구니 아이템들을 전부 가져온다.

        // 만약에 특정 장바구니의 상품옵션이 판매불능이면 삭제
        // 만약에 특정 장바구니의 상품옵션이 판매가능이면 주문품목으로 옮긴 후 삭제

        List<CartItem> cartItems = cartService.getItemsByBuyer(buyer);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.isOrderable()) {
                orderItems.add(new OrderItem(product));
            }

            cartService.removeItem(cartItem);
        }

        return create(buyer, orderItems);
    }

    @Transactional
    public Order create(Member buyer, List<OrderItem> orderItems) {
        Order order = Order
                .builder()
                .buyer(buyer)
                .build();

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        // 주문 품목으로 부터 이름을 만든다.
        order.makeName();

        orderRepository.save(order);

        return order;
    }

    @Transactional
    public RsData<Order> refund(Order order, String refundReason) {
        int payPrice = order.getPayPrice();
        memberService.addCash(order.getBuyer(), payPrice, "주문__%d__환불__토스페이먼츠".formatted(order.getId()));

        order.setRefundDone();
        order.setRefundReason(refundReason);
        orderRepository.save(order);

        return RsData.of("S-1", "%d번 주문의 환불이 처리되었습니다.".formatted(order.getId()), order);
    }

    public boolean memberCanSee(Member member, Order order) {
        return member.getId().equals(order.getBuyer().getId());
    }

    @Transactional
    public void payByTossPayments(Order order, String paymentKey) {
        Member buyer = order.getBuyer();
        int payPrice = order.calculatePayPrice();

        memberService.addCash(buyer, payPrice, "주문__%d__충전__토스페이먼츠".formatted(order.getId()));
        memberService.addCash(buyer, payPrice * -1, "주문__%d__사용__토스페이먼츠".formatted(order.getId()));

        order.setPaymentDone();
        order.setPaymentKey(paymentKey);
        orderRepository.save(order);
    }

    public boolean actorCanPayment(Member member, Order order) {
        return memberCanSee(member, order);
    }

    public List<OrderItem> findAllByPayDateBetweenOrderByIdAsc(LocalDateTime fromDate, LocalDateTime toDate) {
        return orderItemRepository.findAllByPayDateBetween(fromDate, toDate);
    }

    public List<OrderItem> findOrderItemByOrderId(long id) {
        return orderItemRepository.findAllByOrderId(id);
    }

    public Optional<Order> findForPrintById(long id) {
        return findById(id);
    }

    private Optional<Order> findById(long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByBuyerIdAndIsPaidTrue(long id) {
        return orderRepository.findByBuyerIdAndIsPaidTrueOrderByIdDesc(id);
    }

    public List<OrderItem> findAllByProductId(long productId) {
        return orderItemRepository.findAllByProductId(productId);
    }

}
