package com.example.demo.domain.book.controller;

import com.example.demo.base.rq.Rq;
import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.service.BookService;
import com.example.demo.domain.genFile.entity.GenFile;
import com.example.demo.domain.genFile.service.GenFileService;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.domain.postKeyword.entity.PostKeyword;
import com.example.demo.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/usr/book")
@RequiredArgsConstructor
@Validated
public class BookController {
    private final BookService bookService;
    private final Rq rq;
    private final GenFileService genFileService;
    private final PostService postService;

    @GetMapping("/list")
    public String showList(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "all") String kwType
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Book> bookPage = bookService.findByKw(kwType, kw, true, pageable);
        model.addAttribute("bookPage", bookPage);

        return "usr/book/list";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(
            Model model,
            @PathVariable long id
    ) {
        Book book = bookService.findById(id).get();

        Map<String, GenFile> filesMap = bookService.findGenFilesMapKeyByFileNo(book, "common", "attachment");

        model.addAttribute("book", book);
        model.addAttribute("filesMap", filesMap);

        return "usr/book/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    public String showWrite(Model model) {
        List<PostKeyword> postKeywords = postService.findPostKeywordsByMemberId(rq.getMember());

        model.addAttribute("postKeywords", postKeywords);

        return "usr/book/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String write(
            @Valid BookController.BookWriteForm writeForm
    ) {
        RsData<Book> rsData = bookService.write(rq.getMember(), writeForm.getPostKeywordId(), writeForm.getSubject(), writeForm.getTagsStr(), writeForm.getBody(), writeForm.getBodyHtml(), writeForm.isPublic());

        if (Ut.file.exists(writeForm.getAttachment__1()))
            bookService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__1(), 1);
        if (Ut.file.exists(writeForm.getAttachment__1()))
            bookService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/book/detail/%d".formatted(rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class BookWriteForm {
        private boolean isPublic;
        private long postKeywordId;
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
    @GetMapping("/modify/{id}")
    public String showModify(
            Model model,
            @PathVariable long id
    ) {
        Book book = bookService.findById(id).get();

        Map<String, GenFile> filesMap = bookService.findGenFilesMapKeyByFileNo(book, "common", "attachment");

        model.addAttribute("book", book);
        model.addAttribute("filesMap", filesMap);

        return "usr/book/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(
            @PathVariable long id,
            @Valid BookController.BookModifyForm modifyForm
    ) {
        Book book = bookService.findById(id).get();

        RsData<Book> rsData = bookService.modify(book, modifyForm.getSubject(), modifyForm.getTagsStr(), modifyForm.getBody(), modifyForm.getBodyHtml(), modifyForm.isPublic());

        if (modifyForm.attachmentRemove__1)
            bookService.removeAttachmentFile(rsData.getData(), 1);

        if (modifyForm.attachmentRemove__2)
            bookService.removeAttachmentFile(rsData.getData(), 2);

        if (Ut.file.exists(modifyForm.getAttachment__1()))
            bookService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__1(), 1);
        if (Ut.file.exists(modifyForm.getAttachment__2()))
            bookService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/book/detail/%d".formatted(rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class BookModifyForm {
        @NotBlank
        @Length(min = 2)
        private String subject;
        private String tagsStr;
        @NotBlank
        private String body;
        @NotBlank
        private String bodyHtml;
        private boolean isPublic;
        private MultipartFile attachment__1;
        private MultipartFile attachment__2;
        private boolean attachmentRemove__1;
        private boolean attachmentRemove__2;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove/{id}")
    public String remove(
            @PathVariable long id
    ) {
        Book book = bookService.findById(id).get();

        RsData<?> rsData = bookService.remove(book);

        return rq.redirectOrBack("/usr/book/myList", rsData);
    }

    public boolean assertActorCanWrite() {
        bookService.checkActorCanWrite(rq.getMember())
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanModify() {
        long bookId = rq.getPathVariableAsLong(3);
        Book book = bookService.findById(bookId).get();

        bookService.checkActorCanModify(rq.getMember(), book)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanRemove() {
        long bookId = rq.getPathVariableAsLong(3);
        Book book = bookService.findById(bookId).get();

        bookService.checkActorCanRemove(rq.getMember(), book)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }
}