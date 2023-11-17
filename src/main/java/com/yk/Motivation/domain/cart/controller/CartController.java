package com.yk.Motivation.domain.cart.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.cart.entity.CartItem;
import com.yk.Motivation.domain.cart.service.CartService;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.product.entity.Product;
import com.yk.Motivation.domain.product.service.ProductService;
import com.yk.Motivation.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/usr/cart")
@RequiredArgsConstructor
@Validated
public class CartController {
    private final CartService cartService;
    private final ProductService productService;
    private final Rq rq;

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public String showItems(Model model) {
        Member buyer = rq.getMember();

        List<CartItem> items = cartService.getItemsByBuyer(buyer);

        model.addAttribute("items", items);

        return "usr/cart/items";
    }

    @PostMapping("/remove")
    @PreAuthorize("isAuthenticated()")
    public String removeItems(String ids) {
        Member buyer = rq.getMember();

        String[] idsArr = ids.split(",");

        Arrays.stream(idsArr)
                .mapToLong(Long::parseLong)
                .forEach(id -> {
                    CartItem cartItem = cartService.findItemById(id).orElse(null);

                    if (cartService.actorCanDelete(buyer, cartItem)) {
                        cartService.removeItem(cartItem);
                    }
                });

        return rq.redirectOrBack("/usr/cart/items", RsData.of("S-1","%d건의 품목을 삭제하였습니다.".formatted(idsArr.length)));
    }

    @GetMapping("/write/lecture/{id}")
    public String write(
            @PathVariable Long id
    ) {

        Product product = productService.findByLectureId(id).get();

        RsData<CartItem> rsData = cartService.addItem(rq.getMember(), product);

        return rq.redirectOrBack("/usr/cart/items", rsData);
    }
}