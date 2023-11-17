package com.yk.Motivation.domain.product.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.product.entity.Product;
import com.yk.Motivation.domain.product.service.ProductService;
import com.yk.Motivation.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/usr/product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final LectureService lectureService;
    private final Rq rq;
    private final ProductService productService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write/lecture/{id}")
    public String showCreate(
            @PathVariable Long id,
            Model model
    ) {
        Member producer = rq.getMember();

        Lecture lecture = lectureService.findById(id).get();

        model.addAttribute("lecture", lecture);

        return "usr/product/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write/lecture/{id}")
    public String create(
            @PathVariable Long id,
            @Valid ProductWriteForm writeForm
    ) {
        Member producer = rq.getMember();

        Lecture lecture = lectureService.findById(id).get();

        RsData<Lecture> rsData = productService.create(lecture, producer, writeForm.getPrice(), writeForm.isFree());

        return rq.redirectOrBack("/usr/lecture/detail/%d".formatted(lecture.getId()), rsData);
    }

    @Getter
    @Setter
    public static class ProductWriteForm {
        private boolean isFree;
        @NotNull
        private int price;
    }

}
