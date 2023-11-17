package com.yk.Motivation.domain.lesson.controller;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.article.controller.ArticleController;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.service.GenFileService;
import com.yk.Motivation.domain.lecture.controller.LectureController;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.lesson.entity.Lesson;
import com.yk.Motivation.domain.lesson.entity.LessonPlaybackTime;
import com.yk.Motivation.domain.lesson.exception.InvalidAccessAttempError;
import com.yk.Motivation.domain.lesson.service.LessonService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.service.MemberService;
import com.yk.Motivation.domain.order.entity.OrderItem;
import com.yk.Motivation.domain.order.service.OrderService;
import com.yk.Motivation.standard.util.Ut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usr/lesson")
@RequiredArgsConstructor
@Validated
public class LessonController {

    private final LessonService lessonService;
    private final LectureService lectureService;
    private final GenFileService genFileService;
    private final OrderService orderService;
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/list/{id}")
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

        return "usr/lesson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lectureId}/write")
    public String showWrite(
            Model model,
            @PathVariable Long lectureId
    ) {
        Lecture lecture = lectureService.findById(lectureId).get();

        model.addAttribute("lecture", lecture);

        return "usr/lesson/write";
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

        return "usr/lesson/modify";
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

        return "usr/lesson/modifySortNo";
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{lectureId}/modifySortNo")
    public String modifySortNo(
            @PathVariable Long lectureId,
            @RequestParam List<Long> order
    ) {
        RsData<Lecture> modifySortNoRs = lessonService.modifySortNo(lectureId, order);

        return rq.redirectOrBack("/usr/lesson/" + lectureId + "/modify", modifySortNoRs);
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

        return rq.redirectOrBack("/usr/lesson/" + lectureId + "/modify", removeRs);
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

    @PostMapping("/{lessonId}/playbackTime")
    @ResponseBody
    public void receivePlaybackTime(
            @PathVariable Long lessonId,
            @RequestParam("currentTime") double currentTime
            ) {
            Lesson lesson = lessonService.findById(lessonId).get();

            lessonService.saveLessonPlaybackTime(rq.getMember(), lesson, currentTime);
    }




    @PreAuthorize("isAuthenticated()")
    @GetMapping("/hls/{lessonId}")
    public String videoHls(
            Model model,
            @PathVariable Long lessonId
            ) {

        Lesson lesson = lessonService.findById(lessonId).get();
        Member member = memberService.findById(rq.getMember().getId()).get();

        member.getLectures().stream()
                .filter(lecture -> lecture.getId() == lesson.getLecture().getId())
                .findFirst()
                .orElseThrow(() -> new InvalidAccessAttempError("올바르지 않은 접근입니다."));

//        결제 데이터 뒤져보는 코드

//        if( !lesson.getLecture().getProduct().isFree()) {
//
//            Long productId = lesson.getLecture().getProduct().getId();
//            List<OrderItem> orderItemList = orderService.findAllByProductId(productId);
//
//            orderItemList.stream()
//                    .filter(orderItem -> orderItem.getOrder().isPaid()
//                            && !orderItem.getOrder().isRefunded()
//                            && orderItem.getOrder().getBuyer().getId() == rq.getMember().getId())
//                    .findFirst()
//                    .orElseThrow(() -> new InvalidAccessAttempError("올바르지 않은 접근입니다."));
//
//        }

        String masterPlayListPath = getHlsSourcePath(lessonId, "master.m3u8");

        Optional<LessonPlaybackTime> playbackTimeOpt = lessonService.findPlaybackTimeByMember(rq.getMember(), lesson);
        Integer playbackTime = playbackTimeOpt.map(LessonPlaybackTime::getPlaybackTime).orElse(null);

        model.addAttribute("videoUrl", masterPlayListPath);
        model.addAttribute("playbackTime",playbackTime);
        return "usr/lesson/hls";
    }

    private String getHlsSourcePath(long lessonId, String fileName) {
        Lesson lesson = lessonService.findById(lessonId).get();
        GenFile genFile = genFileService.findBy(lesson.getModelName(), lesson.getId(), "common", "lessonVideo", 1).get();

        return "/gen/" + genFile.getFileDir() + "/" + "hls" + "/" + fileName;
    }


}
