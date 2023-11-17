package com.yk.Motivation.domain.article.controller;


import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.article.service.ArticleService;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.board.service.BoardService;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/usr/article")
@RequiredArgsConstructor
@Validated
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;
    private final Rq rq;

    @GetMapping("/{boardCode}/list")
    public String showList(
            Model model,
            @PathVariable String boardCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "all") String kwType
    ) {
        Board board = boardService.findByCode(boardCode).get();

        model.addAttribute("board", board);

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Article> articlePage = articleService.findByKw(board, kwType, kw, pageable);
        model.addAttribute("articlePage", articlePage);

        return "usr/article/list";
    }

    @GetMapping("/listByTag/{tagContent}")
    public String showListByTag(
            Model model,
            @NotBlank @PathVariable String tagContent,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Article> articlePage = articleService.findByTag(tagContent, pageable);
        model.addAttribute("articlePage", articlePage);

        return "usr/article/listByTag";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{boardCode}/write")
    public String showWrite(
            Model model,
            @PathVariable String boardCode
    ) {
        Board board = boardService.findByCode(boardCode).get();

        model.addAttribute("board", board);

        return "usr/article/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardCode}/write")
    public String write(
            Model model,
            @PathVariable String boardCode,
            @Valid ArticleWriteForm writeForm
    ) {
        Board board = boardService.findByCode(boardCode).get();

        RsData<Article> rsData = articleService.write(board, rq.getMember(), writeForm.getSubject(), writeForm.getTagsStr(), writeForm.getBody(), writeForm.getBodyHtml());

        if (Ut.file.exists(writeForm.getAttachment__1()))
            articleService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__1(), 1);
        if (Ut.file.exists(writeForm.getAttachment__2()))
            articleService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/article/%s/detail/%d".formatted(board.getCode(), rsData.getData().getId()), rsData);
    }

    @AllArgsConstructor
    @Getter
    public static class ArticleWriteForm {
        @NotBlank
        @Length(min = 2)
        private String subject;
        private String tagsStr;
        @NotBlank
        private String body;
        @NotBlank
        private String bodyHtml;
        private MultipartFile attachment__1;
        private MultipartFile attachment__2;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{boardCode}/modify/{id}")
    public String showModify(
            Model model,
            @PathVariable String boardCode,
            @PathVariable long id
    ) {
        Board board = boardService.findByCode(boardCode).get();
        Article article = articleService.findById(id).get();

        Map<String, GenFile> filesMap = articleService.findGenFilesMapKeyByFileNo(article, "common", "attachment");

        model.addAttribute("board", board);
        model.addAttribute("article", article);
        model.addAttribute("filesMap", filesMap);


        return "usr/article/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardCode}/modify/{id}")
    public String modify(
            Model model,
            @PathVariable String boardCode,
            @PathVariable long id,
            @Valid ArticleModifyForm modifyForm
    ) {
        Board board = boardService.findByCode(boardCode).get();
        Article article = articleService.findById(id).get();

        RsData<Article> rsData = articleService.modify(article, modifyForm.getSubject(), modifyForm.getTagsStr(), modifyForm.getBody(), modifyForm.getBodyHtml());

        if (modifyForm.attachmentRemove__1)
            articleService.removeAttachmentFile(rsData.getData(), 1);

        if (modifyForm.attachmentRemove__2)
            articleService.removeAttachmentFile(rsData.getData(), 2);

        if (Ut.file.exists(modifyForm.getAttachment__1()))
            articleService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__1(), 1);
        if (Ut.file.exists(modifyForm.getAttachment__1()))
            articleService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/article/%s/detail/%d".formatted(board.getCode(), rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class ArticleModifyForm {
        @NotBlank
        @Length(min = 2)
        private String subject;
        private String tagsStr;
        @NotBlank
        private String body;
        @NotBlank
        private String bodyHtml;
        private MultipartFile attachment__1;
        private MultipartFile attachment__2;
        private boolean attachmentRemove__1;
        private boolean attachmentRemove__2;
    }

    @GetMapping("/{boardCode}/detail/{id}")
    public String showDetail(
            Model model,
            @PathVariable String boardCode,
            @PathVariable long id
    ) {
        Board board = boardService.findByCode(boardCode).get();
        Article article = articleService.findById(id).get();

        Map<String, GenFile> filesMap = articleService.findGenFilesMapKeyByFileNo(article, "common", "attachment");

        model.addAttribute("board", board);
        model.addAttribute("article", article);
        model.addAttribute("filesMap", filesMap);

        return "usr/article/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardCode}/remove/{id}")
    public String remove(
            @PathVariable String boardCode,
            @PathVariable long id
    ) {
        Board board = boardService.findByCode(boardCode).get();
        Article article = articleService.findById(id).get();
        RsData<?> rsData = articleService.remove(article);

        return rq.redirectOrBack("/usr/article/%s/list".formatted(board.getCode()), rsData);
    }

    public boolean assertActorCanWrite() {
        String boardCode = rq.getPathVariable(2);
        Board board = boardService.findByCode(boardCode).get();
        articleService.checkActorCanWrite(rq.getMember(), board)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanModify() {
        long articleId = rq.getPathVariableAsLong(4);
        Article article = articleService.findById(articleId).get();

        articleService.checkActorCanModify(rq.getMember(), article)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanRemove() {
        long articleId = rq.getPathVariableAsLong(4);
        Article article = articleService.findById(articleId).get();

        articleService.checkActorCanRemove(rq.getMember(), article)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;

    }
}