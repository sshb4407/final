package com.yk.Motivation.domain.series.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.SeriesTag.entity.SeriesTag;
import com.yk.Motivation.domain.document.standard.DocumentHavingTags;
import com.yk.Motivation.domain.document.standard.DocumentTag;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.postKeyword.entity.PostKeyword;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Series extends BaseEntity implements DocumentHavingTags {
    @ManyToOne
    private PostKeyword postKeyword;

    @ManyToOne
    private Member author;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    private boolean isPublic;

    @OneToMany(mappedBy = "series", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude
    private Set<SeriesTag> seriesTags = new HashSet<>();

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public Set<? extends DocumentTag> _getTags() {
        return seriesTags;
    }

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public DocumentTag _addTag(String tagContent) {
        SeriesTag tag = SeriesTag.builder()
                .author(author)
                .series(this)
                .content(tagContent)
                .build();

        seriesTags.add(tag);

        return tag;
    }

    public String getPublicHanName() {
        return isPublic ? "공개" : "비공개";
    }

}