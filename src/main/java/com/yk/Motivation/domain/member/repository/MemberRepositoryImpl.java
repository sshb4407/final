package com.yk.Motivation.domain.member.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yk.Motivation.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import static com.yk.Motivation.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Member> findByKw(String kwType, String kw, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        switch (kwType) {
            case "username" -> builder.and(member.username.containsIgnoreCase(kw)); // builder.and() - and 연산 ( builder.or())
            case "nickname" -> builder.and(member.nickname.containsIgnoreCase(kw)); // containsIgnoreCase() - 대,소문자 구분 없이 contains filter
            case "email" -> builder.and(member.email.containsIgnoreCase(kw)); // contains -> startsWith, endsWith, like, equals, notEquals, isNull, isNotNull, isEmpty, isNotEmpty 등등...
            default -> builder.and(
                    member.username.containsIgnoreCase(kw)
                            .or(member.nickname.containsIgnoreCase(kw))
                            .or(member.email.containsIgnoreCase(kw))
            );
        }

        JPAQuery<Member> membersQuery = jpaQueryFactory
                .selectDistinct(member)
                .from(member)
                .where(builder);  // builder 의 조건을 통해 where 절을 붙여 query 문 생성

        for (Sort.Order o : pageable.getSort()) { // sorts 를 순회
            PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata()); // path 생성을 위한 도구(PathBuilder) 생성
            membersQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty()))); // OrderSpecifier(order, path) - ex) OrderSpecifier(Order.ASC, "id")
        }

        membersQuery.offset(pageable.getOffset()).limit(pageable.getPageSize()); // query 에 offset 과 limit 적용

        JPAQuery<Long> totalQuery = jpaQueryFactory
                .select(member.count()) // member.count 만
                .from(member)
                .where(builder); // data 의 총 갯수만 구할 query 생성

        return PageableExecutionUtils.getPage(membersQuery.fetch(), pageable, totalQuery::fetchOne);
        // fetch() 시에 쿼리 날라감, fetchOne() 쿼리의 결과로 단일 결과를 가져옴(count 만)
        // PageableExecutionUtils.getPage() - 데이터, 페이지 정보를 함께 포함 하는 Page 객체를 생성 하여 반환
    }
}