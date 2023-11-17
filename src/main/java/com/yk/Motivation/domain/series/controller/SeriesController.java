package com.yk.Motivation.domain.series.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.post.entity.Post;
import com.yk.Motivation.domain.series.entity.Series;
import com.yk.Motivation.domain.series.service.SeriesService;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.service.GenFileService;
import com.yk.Motivation.domain.post.service.PostService;
import com.yk.Motivation.domain.postKeyword.entity.PostKeyword;
import com.yk.Motivation.standard.util.Ut;
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
@RequestMapping("/usr/series")
@RequiredArgsConstructor
@Validated
public class SeriesController {
    private final SeriesService seriesService;
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
        Page<Series> seriesPage = seriesService.findByKw(kwType, kw, true, pageable);
        model.addAttribute("seriesPage", seriesPage);

        return "usr/series/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myList")
    public String showMyList(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "all") String kwType
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Series> seriesPage = seriesService.findByKw(rq.getMember(), kwType, kw, pageable);
        model.addAttribute("seriesPage", seriesPage);

        return "usr/series/myList";
    }

    @GetMapping("/listByTag/{tagContent}")
    public String showListByTag(
            Model model,
            @PathVariable String tagContent,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Series> seriesPage = seriesService.findByTag(tagContent, pageable, true);
        model.addAttribute("seriesPage", seriesPage);

        return "usr/series/listByTag";
    }

    @GetMapping("/myListByTag/{tagContent}")
    public String showMyListByTag(
            Model model,
            @PathVariable String tagContent,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Series> seriesPage = seriesService.findByTag(rq.getMember(), tagContent, pageable);
        model.addAttribute("seriesPage", seriesPage);

        return "usr/series/myListByTag";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(
            Model model,
            @PathVariable long id
    ) {
        Series series = seriesService.findById(id).get();

        Map<String, GenFile> filesMap = seriesService.findGenFilesMapKeyByFileNo(series, "common", "attachment");

        model.addAttribute("series", series);
        model.addAttribute("filesMap", filesMap);

        return "usr/series/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    public String showWrite(Model model) {
        List<PostKeyword> postKeywords = postService.findPostKeywordsByMemberId(rq.getMember());

        model.addAttribute("postKeywords", postKeywords);

        return "usr/series/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String write(
            @Valid SeriesController.SeriesWriteForm writeForm
    ) {
        RsData<Series> rsData = seriesService.write(rq.getMember(), writeForm.getPostKeywordId(), writeForm.getSubject(), writeForm.getTagsStr(), writeForm.getBody(), writeForm.getBodyHtml(), writeForm.isPublic());

        if (Ut.file.exists(writeForm.getAttachment__1()))
            seriesService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__1(), 1);
        if (Ut.file.exists(writeForm.getAttachment__1()))
            seriesService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/series/detail/%d".formatted(rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class SeriesWriteForm {
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
        Series series = seriesService.findById(id).get();

        Map<String, GenFile> filesMap = seriesService.findGenFilesMapKeyByFileNo(series, "common", "attachment");
        List<PostKeyword> postKeywords = postService.findPostKeywordsByMemberId(rq.getMember());

        model.addAttribute("postKeywords", postKeywords);
        model.addAttribute("series", series);
        model.addAttribute("filesMap", filesMap);

        return "usr/series/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(
            @PathVariable long id,
            @Valid SeriesController.SeriesModifyForm modifyForm
    ) {
        Series series = seriesService.findById(id).get();

        RsData<Series> rsData = seriesService.modify(series, modifyForm.getPostKeywordId(), modifyForm.getSubject(), modifyForm.getTagsStr(), modifyForm.getBody(), modifyForm.getBodyHtml(), modifyForm.isPublic());

        if (modifyForm.attachmentRemove__1)
            seriesService.removeAttachmentFile(rsData.getData(), 1);

        if (modifyForm.attachmentRemove__2)
            seriesService.removeAttachmentFile(rsData.getData(), 2);

        if (Ut.file.exists(modifyForm.getAttachment__1()))
            seriesService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__1(), 1);
        if (Ut.file.exists(modifyForm.getAttachment__2()))
            seriesService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/series/detail/%d".formatted(rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class SeriesModifyForm {
        private long postKeywordId;
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
        Series series = seriesService.findById(id).get();

        RsData<?> rsData = seriesService.remove(series);

        return rq.redirectOrBack("/usr/series/myList", rsData);
    }

    public boolean assertActorCanWrite() {
        seriesService.checkActorCanWrite(rq.getMember())
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanModify() {
        long seriesId = rq.getPathVariableAsLong(3);
        Series series = seriesService.findById(seriesId).get();

        seriesService.checkActorCanModify(rq.getMember(), series)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }

    public boolean assertActorCanRemove() {
        long seriesId = rq.getPathVariableAsLong(3);
        Series series = seriesService.findById(seriesId).get();

        seriesService.checkActorCanRemove(rq.getMember(), series)
                .optional()
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new AccessDeniedException(rsData.getMsg());
                });

        return true;
    }
}