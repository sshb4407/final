package com.yk.Motivation.domain.lecture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import static com.yk.Motivation.domain.lecture.entity.QLecture.lecture;

@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Lecture> findByKw(String kwType, String kw, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(lecture.isPublic.eq(true));

        switch (kwType) {
            case "subject" -> builder.and(lecture.subject.containsIgnoreCase(kw));
            case "body" -> builder.and(lecture.body.containsIgnoreCase(kw));
            case "nickname" -> builder.and(lecture.producer.producerName.containsIgnoreCase(kw));
            default -> builder.and(
                    lecture.subject.containsIgnoreCase(kw)
                            .or(lecture.body.containsIgnoreCase(kw))
                            .or(lecture.producer.producerName.containsIgnoreCase(kw))
            );
        }

        JPAQuery<Lecture> lectureQuery = jpaQueryFactory
                .selectDistinct(lecture)
                .from(lecture)
                .where(builder);

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(lecture.getType(), lecture.getMetadata());
            lectureQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        lectureQuery.offset(pageable.getOffset()).limit(pageable.getPageSize());

        JPAQuery<Long> totalQuery = jpaQueryFactory
                .select(lecture.count())
                .from(lecture)
                .where(builder);

        return PageableExecutionUtils.getPage(lectureQuery.fetch(), pageable, totalQuery::fetchOne);
    }
}