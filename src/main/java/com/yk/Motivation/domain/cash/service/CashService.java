package com.yk.Motivation.domain.cash.service;

import com.yk.Motivation.domain.cash.entity.CashLog;
import com.yk.Motivation.domain.cash.repository.CashLogRepository;
import com.yk.Motivation.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashService {
    private final CashLogRepository cashLogRepository;

    public CashLog addCash(Member member, long price, String eventType) {
        CashLog cashLog = CashLog.builder()
                .member(member)
                .price(price)
                .eventType(eventType)
                .build();

        cashLogRepository.save(cashLog);

        return cashLog;
    }
}
