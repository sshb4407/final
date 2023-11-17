package com.yk.Motivation.domain.lecture.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.article.controller.ArticleController;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.article.service.ArticleService;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.board.service.BoardService;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.lesson.entity.Lesson;
import com.yk.Motivation.domain.lesson.service.LessonService;
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
@RequestMapping("/usr/lecture")
@RequiredArgsConstructor
@Validated
public class LectureController {
    private final LectureService lectureService;
    private final LessonService lessonService;
    private final Rq rq;

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
        Page<Lecture> lecturePage = lectureService.findByKw(kwType, kw, pageable);
        model.addAttribute("lecturePage", lecturePage);

        return "usr/lecture/list";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(
            Model model,
            @PathVariable long id
    ) {
        Lecture lecture = lectureService.findById(id).get();
        List<Lesson> lessons = lecture.getLessons();

        Map<String, GenFile> filesMap = lectureService.findGenFilesMapKeyByFileNo(lecture, "common", "attachment");

        model.addAttribute("lecture", lecture);
        model.addAttribute("lessons", lessons);
        model.addAttribute("filesMap", filesMap);

        return "usr/lecture/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    public String showWrite() {
        return "usr/lecture/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String write(
            @Valid LectureController.LectureWriteForm writeForm
    ) {
        RsData<Lecture> rsData = lectureService.write(rq.getMember(), writeForm.getSubject(), writeForm.getTagsStr(), writeForm.getBody(), writeForm.getBodyHtml(), writeForm.isPublic());

        if (Ut.file.exists(writeForm.getAttachment__1()))
            lectureService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__1(), 1);
        if (Ut.file.exists(writeForm.getAttachment__2()))
            lectureService.saveAttachmentFile(rsData.getData(), writeForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/lesson/%d/write".formatted(rsData.getData().getId()), rsData);
    }

    @Setter
    @Getter
    public static class LectureWriteForm {
        private boolean isPublic;
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
        Lecture lecture = lectureService.findById(id).get();

        Map<String, GenFile> filesMap = lectureService.findGenFilesMapKeyByFileNo(lecture, "common", "attachment");

        model.addAttribute("lecture", lecture);
        model.addAttribute("filesMap", filesMap);


        return "usr/lecture/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(
            @PathVariable long id,
            @Valid LectureController.LectureModifyForm modifyForm
    ) {
        Lecture lecture = lectureService.findById(id).get();

        RsData<Lecture> rsData = lectureService.modify(lecture, modifyForm.getSubject(), modifyForm.getTagsStr(), modifyForm.getBody(), modifyForm.getBodyHtml(), modifyForm.isPublic());

        if (modifyForm.attachmentRemove__1)
            lectureService.removeAttachmentFile(rsData.getData(), 1);

        if (modifyForm.attachmentRemove__2)
            lectureService.removeAttachmentFile(rsData.getData(), 2);

        if (Ut.file.exists(modifyForm.getAttachment__1()))
            lectureService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__1(), 1);
        if (Ut.file.exists(modifyForm.getAttachment__2()))
            lectureService.saveAttachmentFile(rsData.getData(), modifyForm.getAttachment__2(), 2);

        return rq.redirectOrBack("/usr/lecture/detail/%d".formatted(rsData.getData().getId()), rsData);
    }

    @Getter
    @Setter
    public static class LectureModifyForm {
        private boolean isPublic;
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove/{id}")
    public String remove(
            @PathVariable long id
    ) {
        Lecture lecture = lectureService.findById(id).get();
        RsData<?> rsData = lectureService.remove(lecture);

        return rq.redirectOrBack("/usr/lecture/list", rsData);
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
        Page<Lecture> lecturePage = lectureService.findByTag(tagContent, pageable);
        model.addAttribute("lecturePage", lecturePage);

        return "usr/lecture/listByTag";
    }

}



