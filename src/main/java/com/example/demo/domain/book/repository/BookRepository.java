package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.repository.BookRepositoryCustom;
import com.example.demo.domain.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    Page<Book> findByBookTags_content(String tagContent, Pageable pageable);
}