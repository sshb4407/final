package com.yk.Motivation.domain.board.repository;

import com.yk.Motivation.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByCode(String boardCode);
}
