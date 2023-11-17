package com.yk.Motivation.domain.rebate.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.rebate.entity.RebateOrderItem;
import com.yk.Motivation.domain.rebate.service.RebateService;
import com.yk.Motivation.standard.util.Ut;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/adm/rebate")
@RequiredArgsConstructor
@Validated
public class RebateController {
    private final RebateService rebateService;
    private final Rq rq;

    @Secured("ROLE_ADMIN")
    @GetMapping("/makeData")
    public String showMakeData() {
        return "adm/rebate/makeData";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/makeData")
    public String makeData(String yearMonth) {
        RsData makeDateRsData = rebateService.makeDate(yearMonth);

        return rq.redirectOrBack("/adm/rebate/rebateOrderItemList?yearMonth=" + yearMonth, makeDateRsData);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/rebateOrderItemList")
    public String showRebateOrderItemList(String yearMonth, Model model) {
        if (yearMonth == null) {
            yearMonth = "2023-11";
        }

        List<RebateOrderItem> items = rebateService.findRebateOrderItemsByPayDateIn(yearMonth);

        model.addAttribute("items", items);
        model.addAttribute("yearMonth", yearMonth);

        return "adm/rebate/rebateOrderItemList";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/rebateOne/{orderItemId}/{yearMonth}")
    public String rebateOne(
            @PathVariable long orderItemId,
            @PathVariable String yearMonth,
            HttpServletRequest req) {
        RsData rebateRsData = rebateService.rebate(orderItemId);

        System.out.println("yearMonth : " + yearMonth);

        return rq.redirectOrBack("/adm/rebate/rebateOrderItemList?yearMonth=" + yearMonth, rebateRsData);
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/rebate/{yearMonth}")
    public String rebate(
            @PathVariable String yearMonth,
            String ids,
            HttpServletRequest req) {

        String[] idsArr = ids.split(",");

        Arrays.stream(idsArr)
                .mapToLong(Long::parseLong)
                .forEach(rebateService::rebate);

        System.out.println("yearMonth : " + yearMonth);

        return rq.redirectOrBack("/adm/rebate/rebateOrderItemList?yearMonth=" + yearMonth, RsData.of("S-1", "%d건의 정산이 처리되었습니다.".formatted(idsArr.length)));
    }
}

