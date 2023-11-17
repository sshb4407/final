package com.yk.Motivation.domain.article.repository;

import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {
    Page<Article> findByKw(Board board, String kwType, String kw, Pageable pageable);
}