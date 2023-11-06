package com.example.demo.domain.lesson.controller;

import com.example.demo.base.rq.Rq;
import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.genFile.entity.GenFile;
import com.example.demo.domain.genFile.service.GenFileService;
import com.example.demo.domain.lecture.entity.Lecture;
import com.example.demo.domain.lecture.service.LectureService;
import com.example.demo.domain.lesson.entity.Lesson;
import com.example.demo.domain.lesson.service.LessonService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/templates/usr/lesson")
@RequiredArgsConstructor
@Validated
public class LessonController {

    private final LessonService lessonService;
    private final LectureService lectureService;
    private final GenFileService genFileService;
    private final Rq rq;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lectureId}/write")
    public String showWrite(
            Model model,
            @PathVariable Long lectureId
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();

        model.addAttribute("lecture", lecture);

        return "templates/usr/lesson/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/write")
    @ResponseBody
    public RsData<Long> write(
            @PathVariable Long lectureId,
            @Valid LessonController.LessonWriteForm writeForm
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();

        RsData<Lecture> rsData = lessonService.write(lecture, writeForm.getSubjects(), writeForm.getVideos());

        return RsData.of("S-1", "%d 번 강의가 등록 되었습니다.".formatted(lectureId), lecture.getId());
    }

    @Setter
    @Getter
    public static class LessonWriteForm {
        private List<String> subjects;
        private List<MultipartFile> videos;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lectureId}/modify")
    public String showModify(
            Model model,
            @PathVariable Long lectureId
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();
        List<Lesson> lessons = lecture.getLessons();

        model.addAttribute("lecture", lecture);
        model.addAttribute("lessons", lessons);

        return "templates/usr/lesson/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/modify")
    @ResponseBody
    public RsData<Long> modify(
            @PathVariable Long lectureId,
            @Valid LessonController.LessonModifyForm modifyForm
    ) {

        lessonService.modify(lectureId, modifyForm.getLessonId(), modifyForm.getSubject(), modifyForm.getVideo());

        return RsData.of("S-1", "%d 번 강의가 수정 되었습니다.".formatted(lectureId), lectureId);
    }


    @Getter
    @Setter
    public static class LessonModifyForm {
        private long lessonId;
        private String subject;
        private MultipartFile video;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lectureId}/modifySortNo")
    public String showModifySortNo(
            Model model,
            @PathVariable Long lectureId
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();
        List<Lesson> lessons = lecture.getLessons();

        model.addAttribute("lecture", lecture);
        model.addAttribute("lessons", lessons);

        return "templates/usr/lesson/modifySortNo";
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/modifySortNo")
    public String modifySortNo(
            @PathVariable Long lectureId,
            @RequestParam List<Long> order
    ) {
        RsData<Lecture> modifySortNoRs = lessonService.modifySortNo(lectureId, order);

        return rq.redirectOrBack("/templates/usr/lesson/" + lectureId + "/modify", modifySortNoRs);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/remove/{lessonId}")
    public String remove(
            Model model,
            @PathVariable Long lectureId,
            @PathVariable Long lessonId,
            @Valid LessonController.LessonModifyForm modifyForm
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();

        RsData<Lecture> removeRs = lessonService.remove(lectureId, lessonId);

        List<Lesson> lessons = lecture.getLessons();

        model.addAttribute("lecture", lecture);
        model.addAttribute("lessons", lessons);

        return rq.redirectOrBack("/templates/usr/lesson/" + lectureId + "/modify", removeRs);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/writeAddLesson")
    @ResponseBody
    public RsData<Long> writeAddLesson(
            @PathVariable Long lectureId,
            @Valid LessonController.writeAddLessonForm addLessonForm
    ) {

        Lecture lecture = lectureService.findById(lectureId).get();
        List<Lesson> lessons = lecture.getLessons();

        lessonService.writeAddLesson(lecture, lessons, addLessonForm.getSubject(), addLessonForm.getVideo());

        return RsData.of("S-1", "%d 번 강의가 수정 되었습니다.".formatted(lectureId), lecture.getId());
    }

    @Getter
    @Setter
    public static class writeAddLessonForm {
        private String subject;
        private MultipartFile video;
    }






    @GetMapping("/hls/{lessonId}")
    public String videoHls(
            Model model,
            @PathVariable long lessonId
    ) {

        String masterPlayListPath = getHlsSourcePath(lessonId, "master.m3u8");

        model.addAttribute("videoUrl", masterPlayListPath);
        return "templates/usr/lesson/hls";
    }

    private String getHlsSourcePath(long lessonId, String fileName) {
        Lesson lesson = lessonService.findById(lessonId).get();
        GenFile genFile = genFileService.findBy(lesson.getModelName(), lesson.getId(), "templates/common", "lessonVideo", 1).get();

        return "/gen/" + genFile.getFileDir() + "/" + "hls" + "/" + fileName;
    }


}
