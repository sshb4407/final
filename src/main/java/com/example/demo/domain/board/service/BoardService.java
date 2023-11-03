package com.example.demo.domain.board.service;

import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.board.entity.Board;
import com.example.demo.domain.board.repository.BoardRepository;
import com.example.demo.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;

    @Transactional
    public RsData<Board> make(String code, String name, String icon) {
        if (Ut.str.isBlank(icon)) icon = "<i class=\"fa-solid fa-list\"></i>";

        Board board = Board.builder()
                .code(code)
                .name(name)
                .icon(icon)
                .build();

        boardRepository.save(board);

        return new RsData<>("S-1", name = " 게시판이 생성되었습니다.", board);
    }

    public Optional<Board> findByCode(String boardCode) {
        return boardRepository.findByCode(boardCode);
    }
}
