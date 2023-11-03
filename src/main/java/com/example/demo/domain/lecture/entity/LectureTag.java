package com.example.demo.domain.lecture.entity;

import com.example.demo.domain.lecture.entity.Lecture;
import com.example.demo.base.jpa.baseEntity.BaseEntity;
import com.example.demo.domain.document.standard.DocumentTag;
import com.example.demo.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class LectureTag extends BaseEntity implements DocumentTag {
    @ManyToOne
    private Member author;

    @ManyToOne
    private Lecture lecture;

    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LectureTag that = (LectureTag) o;

        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (lecture != null ? lecture.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}