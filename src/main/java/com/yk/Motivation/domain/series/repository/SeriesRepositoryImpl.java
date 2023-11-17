package com.yk.Motivation.domain.series.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yk.Motivation.domain.series.entity.Series;
import com.yk.Motivation.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import static com.yk.Motivation.domain.series.entity.QSeries.series;


@RequiredArgsConstructor
public class SeriesRepositoryImpl implements SeriesRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Series> findByKw(String kwType, String kw, boolean isPublic, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(series.isPublic.eq(isPublic));
        return findBy(kwType, kw, pageable, builder);
    }

    @Override
    public Page<Series> findByKw(Member author, String kwType, String kw, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(series.author.eq(author));
        return findBy(kwType, kw, pageable, builder);
    }

    private Page<Series> findBy(String kwType, String kw, Pageable pageable, BooleanBuilder builder) {
        applyKeywordFilter(kwType, kw, builder);

        JPAQuery<Series> seriesQuery = createSeriesQuery(builder);
        applySorting(pageable, seriesQuery);

        seriesQuery.offset(pageable.getOffset()).limit(pageable.getPageSize());

        JPAQuery<Long> totalQuery = createTotalQuery(builder);

        return PageableExecutionUtils.getPage(seriesQuery.fetch(), pageable, totalQuery::fetchOne);
    }

    private void applyKeywordFilter(String kwType, String kw, BooleanBuilder builder) {
        switch (kwType) {
            case "subject" -> builder.and(series.subject.containsIgnoreCase(kw));
            case "body" -> builder.and(series.body.containsIgnoreCase(kw));
            default -> builder.and(
                    series.subject.containsIgnoreCase(kw)
                            .or(series.body.containsIgnoreCase(kw))
            );
        }
    }

    private JPAQuery<Series> createSeriesQuery(BooleanBuilder builder) {
        return jpaQueryFactory
                .selectDistinct(series)
                .from(series)
                .where(builder);
    }

    private void applySorting(Pageable pageable, JPAQuery<Series> seriesQuery) {
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(series.getType(), series.getMetadata());
            seriesQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }
    }

    private JPAQuery<Long> createTotalQuery(BooleanBuilder builder) {
        return jpaQueryFactory
                .select(series.count())
                .from(series)
                .where(builder);
    }
}