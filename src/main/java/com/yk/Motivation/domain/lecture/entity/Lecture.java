package com.yk.Motivation.domain.lecture.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.document.standard.DocumentHavingTags;
import com.yk.Motivation.domain.document.standard.DocumentTag;
import com.yk.Motivation.domain.lesson.entity.Lesson;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Lecture extends BaseEntity implements DocumentHavingTags {
    @ManyToOne
    private Member producer;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    private boolean isPublic;

    @OneToOne(fetch = LAZY)
    private Product product;

    private boolean isLessonsReady;

    @OneToMany(mappedBy = "lecture", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude
    private Set<LectureTag> lectureTags = new HashSet<>();

    @OneToMany(mappedBy = "lecture", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude
    @OrderBy("sortNo")
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToMany(mappedBy = "lectures", fetch = LAZY)
    private List<Member> members;

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public Set<? extends DocumentTag> _getTags() {
        return lectureTags;
    }

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public DocumentTag _addTag(String tagContent) {
        LectureTag tag = LectureTag.builder()
                .author(producer)
                .lecture(this)
                .content(tagContent)
                .build();

        lectureTags.add(tag);

        return tag;
    }

    public String getPublicHanName() {
        return isPublic ? "공개" : "비공개";
    }

    public Long getTotalLessonLength() {

        return lessons.stream()
                .mapToLong(Lesson::getLessonLength)
                .sum();
    }

    public Integer getProgressRate(Integer sumPlaybackTime) {
        return (int) (((double) sumPlaybackTime / getTotalLessonLength() ) * 100 );
    }

}