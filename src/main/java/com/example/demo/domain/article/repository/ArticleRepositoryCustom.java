package com.example.demo.domain.article.repository;

import com.example.demo.domain.entity.Article;
import com.example.demo.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {
    Page<Article> findByKw(Board board, String kwType, String kw, Pageable pageable);
}