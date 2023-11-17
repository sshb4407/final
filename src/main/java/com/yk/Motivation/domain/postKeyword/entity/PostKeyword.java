package com.yk.Motivation.domain.postKeyword.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.document.standard.DocumentSortableKeyword;
import com.yk.Motivation.domain.document.standard.DocumentSortableTag;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.postTag.entity.PostTag;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.core.annotation.Order;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class PostKeyword extends BaseEntity implements DocumentSortableKeyword {
    @ManyToOne
    private Member author;
    private String content;

    // 아래 @OneToMany 옵션에 ophanRemoval = true, cascade = CascadeType.ALL 가 없는 이유
    // PostKeyword 는 PostTag 들의 순서만 관리하는 역할을 한다.
    // PostTag 의 삭제와 추가는 Post 엔티티가 관리한다.
    @OneToMany(mappedBy = "postKeyword")
    @Builder.Default
    @ToString.Exclude
    @OrderBy("sortNo ASC")
    private Set<PostTag> postTags = new LinkedHashSet<>();

    @Setter(PRIVATE)
    private long total;

    @Override
    public Set<? extends DocumentSortableTag> _getTags() {
        return postTags;
    }

    @Override
    public boolean __addTag(DocumentSortableTag tag) {
        return postTags.add((PostTag) tag);
    }

    @Override
    public void _setTotal(long total) {
        setTotal(total);
    }
}