package com.yk.Motivation.domain.article.entity;


import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.document.standard.DocumentHavingTags;
import com.yk.Motivation.domain.document.standard.DocumentTag;
import com.yk.Motivation.standard.util.Ut;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.yk.Motivation.domain.member.entity.Member;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Article extends BaseEntity implements DocumentHavingTags {
    @ManyToOne
    private Member author;

    @ManyToOne
    private Board board;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    @OneToMany(mappedBy = "article", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude
    private Set<ArticleTag> articleTags = new HashSet<>();

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public Set<? extends DocumentTag> _getTags() {
        return articleTags;
    }

    // DocumentHavingTags 의 추상메서드
    // 태그기능을 사용하려면 필요하다.
    @Override
    public DocumentTag _addTag(String tagContent) {
        ArticleTag tag = ArticleTag.builder()
                .author(author)
                .article(this)
                .content(tagContent)
                .build();

        articleTags.add(tag);

        return tag;
    }
}