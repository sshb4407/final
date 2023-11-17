package com.yk.Motivation.domain.lesson.service;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.ffmpeg.service.FfmpegService;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.service.GenFileService;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.lecture.repository.LectureRepository;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.lesson.entity.Lesson;
import com.yk.Motivation.domain.lesson.entity.LessonPlaybackTime;
import com.yk.Motivation.domain.lesson.repository.LessonPlaybackTimeRepository;
import com.yk.Motivation.domain.lesson.repository.LessonRepository;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    @Autowired
    @Lazy
    private LessonService self;

    private final LessonRepository lessonRepository;
    private final LessonPlaybackTimeRepository lessonPlaybackTimeRepository;
    private final LectureService lectureService;
    private final GenFileService genFileService;
    private final FfmpegService ffmpegService;
    private final Rq rq;



    @Transactional
    public RsData<Lecture> write(Lecture lecture, List<String> subjects, List<MultipartFile> videos) {

        if (lecture.isLessonsReady()) {
            lecture.setLessonsReady(false);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < subjects.size(); i++) {

            Lesson lesson = Lesson.builder()
                    .lecture(lecture)
                    .subject(subjects.get(i))
                    .sortNo(i + 1)
                    .build();

            lessonRepository.save(lesson);

            RsData<GenFile> rsData = saveVideoFile(lesson, videos.get(i), 0);

            CompletableFuture<Void> processFuture = CompletableFuture.runAsync(() -> {
                try {
                    double originalDuration = ffmpegService.videoHlsMake(rsData.getData().getFilePath(), rsData.getData().getFileDir());
                    self.setLessonLength(lesson.getId(), originalDuration);
                    self.setLessonReadyTrue(lesson.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            futures.add(processFuture);
        }

//         모든 비디오 처리 작업이 완료될 때까지 기다린 후
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRunAsync(() -> {
                    self.setLessonsReadyTrue(lecture);
                });

        // 응답 바로 return
        return new RsData<>("S-1", lecture.getSubject() + " 강의의 커리큘럼이 생성되었습니다.", lecture);
    }

    @Transactional
    public void modify(long lectureId, long lessonId, String subject, MultipartFile video) {

        Lecture lecture = lectureService.findById(lectureId).get();

        if (lecture.isLessonsReady()) {
            lecture.setLessonsReady(false);
        }

        Lesson lesson = lessonRepository.findById(lessonId).get();

        lesson.setSubject(subject);
        if(lesson.isLessonReady()) {
            lesson.setLessonReady(false);
        }

        if(!video.isEmpty()) {

            removeVideoFile(lesson, 1);

            RsData<GenFile> rsData = saveVideoFile(lesson, video, 0);

            CompletableFuture<Void> processFuture = CompletableFuture.runAsync(() -> {
                try {
                    double originalDuration = ffmpegService.videoHlsMake(rsData.getData().getFilePath(), rsData.getData().getFileDir());
                    self.setLessonLength(lesson.getId(), originalDuration);
                    self.setLessonReadyTrue(lesson.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> {
                self.setLessonsReadyTrue(lecture);
            });
        } else {
            setLessonsReadyTrue(lecture);
            setLessonReadyTrue(lesson.getId());
        }
    }

    @Transactional
    public RsData<Lecture> remove(Long lectureId, Long lessonId) {

        Lecture lecture = lectureService.findById(lectureId).get();
        List<Lesson> lessons = lecture.getLessons();

        Lesson lesson = lessonRepository.findById(lessonId).get();

        Iterator<Lesson> iterator = lessons.iterator();
        while (iterator.hasNext()) {
            Lesson l = iterator.next();
            if (lesson.getSortNo() == l.getSortNo()) {
                iterator.remove();
            } else if (lesson.getSortNo() < l.getSortNo()) {
                l.setSortNo(l.getSortNo() - 1);
            }
        }

        removeVideoFile(lesson, 1);
        lessonRepository.delete(lesson);

        return RsData.of("S-1", "%d 번 강의가 수정되었습니다.".formatted(lecture.getId()), lecture);
    }

    @Transactional
    public void writeAddLesson(Lecture lecture, List<Lesson> lessons, String subject, MultipartFile video) {

        if (lecture.isLessonsReady()) {
            lecture.setLessonsReady(false);
        }

        Lesson lesson = Lesson.builder()
                .lecture(lecture)
                .subject(subject)
                .sortNo(lessons.size()+1)
                .build();

        lessonRepository.save(lesson);

        RsData<GenFile> rsData = saveVideoFile(lesson, video, 0);

        CompletableFuture<Void> processFuture = CompletableFuture.runAsync(() -> {
            try {
                double originalDuration = ffmpegService.videoHlsMake(rsData.getData().getFilePath(), rsData.getData().getFileDir());
                self.setLessonLength(lesson.getId(), originalDuration);
                self.setLessonReadyTrue(lesson.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            self.setLessonsReadyTrue(lecture);
        });
    }

    @Transactional
    public RsData<Lecture> modifySortNo(Long lectureId, List<Long> order) {
        Lecture lecture = lectureService.findById(lectureId).get();

        List<Lesson> lessons = lecture.getLessons();

        for (Lesson lesson : lessons) {
            for (int i = 0; i < order.size(); i++) {

                if(lesson.getId() == order.get(i)) lesson.setSortNo(i+1);
                
            }
        }

        return RsData.of("S-1", "%d 번 강의가 수정되었습니다.".formatted(lecture.getId()), lecture);
    }

    @Transactional
    public void saveLessonPlaybackTime(Member member, Lesson lesson, double currentTime) {

        Optional<LessonPlaybackTime> opPlaybackTime = findPlaybackTimeByMember(member, lesson);

        opPlaybackTime.ifPresentOrElse(
                existingPlaybackTime -> existingPlaybackTime.setPlaybackTime((int)currentTime),
                () -> {
                    LessonPlaybackTime playbackTime = LessonPlaybackTime.builder()
                            .lesson(lesson)
                            .member(member)
                            .lecture(lesson.getLecture())
                            .playbackTime((int)currentTime)
                            .build();

                    lessonPlaybackTimeRepository.save(playbackTime);
                }
        );
    }







    @Transactional
    public void setLessonLength(long lessonId, double originalDuration) {

        Lesson lesson = findById(lessonId).get();

        lesson.setLessonLength((int) originalDuration);
    }

    @Transactional
    public void setLessonReadyTrue(long lessonId){
        Lesson lesson = findById(lessonId).get();

        lesson.setLessonReady(true);
    }

    @Transactional
    public void setLessonsReadyTrue(Lecture lecture) {

        Lecture saveLecture = lectureService.findById(lecture.getId()).get();

        if (!saveLecture.isLessonsReady()) {
            saveLecture.setLessonsReady(true);
        }
    }

    @Transactional
    public RsData<GenFile> saveVideoFile(Lesson lesson, MultipartFile attachmentFile, long fileNo) {
        String attachmentFilePath = Ut.file.toFile(attachmentFile, AppConfig.getTempDirPath());
        return saveVideoFile(lesson, attachmentFilePath, fileNo);
    }

    @Transactional
    public RsData<GenFile> saveVideoFile(Lesson lesson, String attachmentFile, long fileNo) {
        GenFile genFile = genFileService.saveForLesson(lesson.getModelName(), lesson.getId(), "common", "lessonVideo", fileNo, attachmentFile);

        return new RsData<>("S-1", genFile.getId() + "번 파일이 생성되었습니다.", genFile);
    }

    @Transactional
    public void removeVideoFile(Lesson lesson, long fileNo) {
        genFileService.removeLessonVideo(lesson.getModelName(), lesson.getId(), "common", "lessonVideo", fileNo);
    }

    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }

    public List<Lesson> findByLectureId(Long id) {
        return lessonRepository.findByLectureId_Id(id);
    }

    public Optional<LessonPlaybackTime> findPlaybackTimeByMember(Member member, Lesson lesson) {
        return lessonPlaybackTimeRepository.findByMemberIdAndLessonId(member.getId(), lesson.getId());
    }

    public Integer getSumPlaybackTime(Lecture lecture) {
        return sumPlaybackTime(lecture.getId(), rq.getMember().getId());
    }

    public Integer sumPlaybackTime(Long lectureId, Long memberId) {

        List<LessonPlaybackTime> lptList = lessonPlaybackTimeRepository.findByLectureIdAndMemberId(lectureId, memberId);

        return lptList.stream()
                .mapToInt(LessonPlaybackTime::getPlaybackTime)
                .sum();
    }

    public boolean isLessonCompleted(Long lessonId, Integer lessonLength) {
        return lessonPlaybackTimeRepository.findByMemberIdAndLessonId(rq.getMember().getId(), lessonId)
                .map(lessonPlaybackTime -> lessonPlaybackTime.getPlaybackTime() >= lessonLength)
                .orElse(false);
    }

}
