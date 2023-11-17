package com.yk.Motivation.domain.genFile.controller;

import com.yk.Motivation.base.rq.Rq;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.service.GenFileService;
import com.yk.Motivation.standard.util.Ut;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usr/genFile")
@Validated
public class GenFileController {
    private final Rq rq;
    private final GenFileService genFileService;

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable long id, HttpServletRequest request) throws FileNotFoundException {
        GenFile genFile = genFileService.findById(id).get();
        String filePath = genFile.getFilePath();

        Resource resource = new InputStreamResource(new FileInputStream(filePath)); // 파일 경로에서 FileInputStream 객체 생성하고 InputStreamResource 로

        String contentType = request.getServletContext().getMimeType(new File(filePath).getAbsolutePath()); // file 의 MIME 타입 결정 ( text/html , image/jpeg, video/mp4 ...)

        if (contentType == null) contentType = "application/octet-stream"; // MIME 타입 기본값

        String fileName = Ut.url.encode(genFile.getOriginFileName()).replace("%20", " "); // 파일 이름 URL 인코딩 후 %20 을 공백으로

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"") // 파일을 첨부로 다운로드 할 것 (헤더 설정)
                .contentType(MediaType.parseMediaType(contentType)).body(resource); // HTTP 응답의 본문 ( resource 객체의 파일을 클라이언트에게 전송)
    }

    @PostMapping("/temp")
    @ResponseBody
    public RsData<String> temp(@RequestParam("file") MultipartFile file) { // temp_member 저장
        GenFile savedFile = genFileService.saveTempFile(rq.getMember(), file);

        return RsData.of("S-1", "임시 파일이 생성되었습니다.", savedFile.getUrl());
    }

    @Scheduled(cron = "0 0 4 * * ?") // 0초 / 0분 / 매일 오전 4시 / 매일 / 매달 / 요일은 ? (즉, 매일)
    public void removeOldTempFiles() {
        genFileService.removeOldTempFiles(); // temp 경로에 24시간 이상 존재 한 파일 삭제
    }

}