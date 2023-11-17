package com.yk.Motivation.domain.rebate.service;

import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.cash.entity.CashLog;
import com.yk.Motivation.domain.member.service.MemberService;
import com.yk.Motivation.domain.order.entity.OrderItem;
import com.yk.Motivation.domain.order.service.OrderService;
import com.yk.Motivation.domain.product.entity.Product;
import com.yk.Motivation.domain.rebate.entity.RebateOrderItem;
import com.yk.Motivation.domain.rebate.repository.RebateOrderItemRepository;
import com.yk.Motivation.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RebateService {
    private final OrderService orderService;
    private final MemberService memberService;
    private final RebateOrderItemRepository rebateOrderItemRepository;

    @Transactional
    public RsData makeDate(String yearMonth) {
        int monthEndDay = Ut.date.getEndDayOf(yearMonth);

        String fromDateStr = yearMonth + "-01 00:00:00.000000";
        String toDateStr = yearMonth + "-%02d 23:59:59.999999".formatted(monthEndDay);
        LocalDateTime fromDate = Ut.date.parse(fromDateStr);
        LocalDateTime toDate = Ut.date.parse(toDateStr);

        // 정산데이터 생성과 클라이언트의 환불요청이 동시에 발생할 경우를 대비하여
        // 정산데이터는 환불기한이 지난 order (즉, payDate 가 3시간이 초과한 Order) 에 대해서만 행해지도록
        if (toDate.isAfter(LocalDateTime.now())) { 
            toDate = LocalDateTime.now().minusHours(3);
        }

        // 데이터 가져오기
        List<OrderItem> orderItems = orderService.findAllByPayDateBetweenOrderByIdAsc(fromDate, toDate);
        List<RebateOrderItem> oldRebateOrderItems = findRebateOrderItemsByPayDateIn(yearMonth);

        // 변환하기
        Set<Long> oldProductIds = oldRebateOrderItems.stream()
                .map(RebateOrderItem::getOrderItem)
                .map(OrderItem::getId)
                .collect(Collectors.toSet());

        List<RebateOrderItem> rebateOrderItems = orderItems.stream()
                .filter(orderItem -> !oldProductIds.contains(orderItem.getId()))
                .map(this::toRebateOrderItem)
                .collect(Collectors.toList());

        // 저장하기
        rebateOrderItems.forEach(this::makeRebateOrderItem);

        if (rebateOrderItems.isEmpty()) return RsData.of("S-2", "추가된 정산 데이터가 없습니다.");

        return RsData.of("S-1", "정산데이터가 성공적으로 생성되었습니다.");
    }

    @Transactional
    public void makeRebateOrderItem(RebateOrderItem item) {
        RebateOrderItem oldRebateOrderItem = rebateOrderItemRepository.findByOrderItemId(item.getOrderItem().getId()).orElse(null);

        if (oldRebateOrderItem != null) {
            rebateOrderItemRepository.delete(oldRebateOrderItem);
        }

        rebateOrderItemRepository.save(item);
    }

    public RebateOrderItem toRebateOrderItem(OrderItem orderItem) {
        return new RebateOrderItem(orderItem);
    }

    @Transactional
    public RsData rebate(long orderItemId) {
        RebateOrderItem rebateOrderItem = rebateOrderItemRepository.findByOrderItemId(orderItemId).get();

        if (rebateOrderItem.isRebateAvailable() == false) {
            return RsData.of("F-1", "정산을 할 수 없는 상태입니다.");
        }

        int calculateRebatePrice = rebateOrderItem.calculateRebatePrice();

        CashLog cashLog = memberService.addCash(
                rebateOrderItem.getProduct().getProducer(),
                calculateRebatePrice,
                "정산__%d__지급__예치금".formatted(rebateOrderItem.getOrderItem().getId())
        ).getData().getCashLog();

        rebateOrderItem.setRebateDone(cashLog);

        return RsData.of("S-1", "주문품목번호 %d번에 대해서 판매자에게 %s원 정산을 완료하였습니다.".formatted(rebateOrderItem.getOrderItem().getId(), calculateRebatePrice));

    }

    public List<RebateOrderItem> findRebateOrderItemsByPayDateIn(String yearMonth) {
        int monthEndDay = Ut.date.getEndDayOf(yearMonth);

        String fromDateStr = yearMonth + "-01 00:00:00.000000";
        String toDateStr = yearMonth + "-%02d 23:59:59.999999".formatted(monthEndDay);
        LocalDateTime fromDate = Ut.date.parse(fromDateStr);
        LocalDateTime toDate = Ut.date.parse(toDateStr);

        return rebateOrderItemRepository.findAllByPayDateBetweenOrderByIdAsc(fromDate, toDate);
    }
}
