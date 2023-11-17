package com.yk.Motivation.domain.genFile.service;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.repository.GenFileRepository;
import com.yk.Motivation.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.yk.Motivation.standard.util.Ut;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenFileService {
    private final GenFileRepository genFileRepository;

    // 조회
    public Optional<GenFile> findBy(String relTypeCode, Long relId, String typeCode, String type2Code, long fileNo) {
        return genFileRepository.findByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeAndFileNo(relTypeCode, relId, typeCode, type2Code, fileNo);
    }

    @Transactional
    public GenFile save(String relTypeCode, Long relId, String typeCode, String type2Code, long fileNo, MultipartFile sourceFile) {
        String sourceFilePath = Ut.file.toFile(sourceFile, AppConfig.getTempDirPath());
        return save(relTypeCode, relId, typeCode, type2Code, fileNo, sourceFilePath);
    }

    // 명령
    @Transactional
    public GenFile save(String relTypeCode, Long relId, String typeCode, String type2Code, long fileNo, String sourceFile) {
        if (!Ut.file.exists(sourceFile)) return null;

        // fileNo 가 0 이면, 이 파일은 로직상 무조건 새 파일이다.
        if (fileNo > 0) remove(relTypeCode, relId, typeCode, type2Code, fileNo);

        String originFileName = Ut.file.getOriginFileName(sourceFile); // 확장자 포함한 원본 파일의 이름
        String fileExt = Ut.file.getExt(originFileName); // 파일의 확장자
        String fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt); // img, video, audio 등...
        String fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt); // 파일의 확장자?
        long fileSize = new File(sourceFile).length(); // 파일의 크기를 바이트 단위로
        String fileDir = getCurrentDirName(relTypeCode); // relTypeCode/2023_10_11 ...

        int maxTryCount = 3;

        GenFile genFile = null;

        // fileNo 가 0이면 auto increment 를 해주고 싶다.
        // 그러나 동시에 많은 요청이 발생 하면, fileNo 의 중복이 발생 해버릴 수도 있다.
        // 그래서 세 번 정도는 기회를 주겠다.
        for (int tryCount = 1; tryCount <= maxTryCount; tryCount++) {
            try {
                if (fileNo == 0) fileNo = genNextFileNo(relTypeCode, relId, typeCode, type2Code);

                genFile = GenFile.builder()
                        .relTypeCode(relTypeCode) // 관련 엔티티
                        .relId(relId) // 관련 엔티티의 id(primary key)
                        .typeCode(typeCode) // common ...
                        .type2Code(type2Code) // profileImg ...
                        .fileExtTypeCode(fileExtTypeCode) // 확장자
                        .fileExtType2Code(fileExtType2Code) // 확장자
                        .originFileName(originFileName) // 이름.확장자
                        .fileSize(fileSize) // 파일크기 (바이트)
                        .fileNo(fileNo) // 파일 번호
                        .fileExt(fileExt) // 확장자
                        .fileDir(fileDir) // 파일 위치
                        .build();

                genFileRepository.save(genFile);

                break;
            } catch (Exception ignored) {

            }
        }

        File file = new File(genFile.getFilePath()); // directory 핸들링

        file.getParentFile().mkdirs(); // 없으면 생성

        Ut.file.moveFile(sourceFile, file); // sourceFile 경로의 파일을 file 로 이동
        Ut.file.remove(sourceFile); // sourceFile 경로의 파일 삭제

        return genFile;
    }

    @Transactional
    public GenFile saveForLesson(String relTypeCode, Long relId, String typeCode, String type2Code, long fileNo, String sourceFile) {
        if (!Ut.file.exists(sourceFile)) return null;

        // fileNo 가 0 이면, 이 파일은 로직상 무조건 새 파일이다.
        if (fileNo > 0) remove(relTypeCode, relId, typeCode, type2Code, fileNo);

        String originFileName = Ut.file.getOriginFileName(sourceFile); // 확장자 포함한 원본 파일의 이름
        String fileExt = Ut.file.getExt(originFileName); // 파일의 확장자
        String fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt); // img, video, audio 등...
        String fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt); // 파일의 확장자?
        long fileSize = new File(sourceFile).length(); // 파일의 크기를 바이트 단위로
        String fileDir = getCurrentDirNameForLesson(relTypeCode, relId); // relTypeCode/2023_10_11/1 ...

        int maxTryCount = 3;

        GenFile genFile = null;

        // fileNo 가 0이면 auto increment 를 해주고 싶다.
        // 그러나 동시에 많은 요청이 발생 하면, fileNo 의 중복이 발생 해버릴 수도 있다.
        // 그래서 세 번 정도는 기회를 주겠다.
        for (int tryCount = 1; tryCount <= maxTryCount; tryCount++) {
            try {
                if (fileNo == 0) fileNo = genNextFileNo(relTypeCode, relId, typeCode, type2Code);

                genFile = GenFile.builder()
                        .relTypeCode(relTypeCode) // 관련 엔티티
                        .relId(relId) // 관련 엔티티의 id(primary key)
                        .typeCode(typeCode) // common ...
                        .type2Code(type2Code) // profileImg ...
                        .fileExtTypeCode(fileExtTypeCode) // 확장자
                        .fileExtType2Code(fileExtType2Code) // 확장자
                        .originFileName(originFileName) // 이름.확장자
                        .fileSize(fileSize) // 파일크기 (바이트)
                        .fileNo(fileNo) // 파일 번호
                        .fileExt(fileExt) // 확장자
                        .fileDir(fileDir) // 파일 위치
                        .build();

                genFileRepository.save(genFile);

                break;
            } catch (Exception ignored) {

            }
        }

        File file = new File(genFile.getFilePath()); // directory 핸들링

        file.getParentFile().mkdirs(); // 없으면 생성

        Ut.file.moveFile(sourceFile, file); // sourceFile 경로의 파일을 file 로 이동
        Ut.file.remove(sourceFile); // sourceFile 경로의 파일 삭제

        return genFile;
    }

    private long genNextFileNo(String relTypeCode, Long relId, String typeCode, String type2Code) {
        return genFileRepository
                .findTop1ByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeOrderByFileNoDesc(relTypeCode, relId, typeCode, type2Code)
                .map(genFile -> genFile.getFileNo() + 1)
                .orElse(1L);
    }

    private String getCurrentDirName(String relTypeCode) { // relTypeCode/2023_10_11 ...
        return relTypeCode + "/" + Ut.date.getCurrentDateFormatted("yyyy_MM_dd");
    }

    private String getCurrentDirNameForLesson(String relTypeCode, Long lessonId) { // relTypeCode/2023_10_11/1 ...
        return relTypeCode + "/" + Ut.date.getCurrentDateFormatted("yyyy_MM_dd") + "/" + lessonId;
    }

    public Map<String, GenFile> findGenFilesMapKeyByFileNo(String relTypeCode, long relId, String typeCode, String type2Code) {
        List<GenFile> genFiles = genFileRepository.findByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeOrderByFileNoAsc(relTypeCode, relId, typeCode, type2Code);

        return genFiles
                .stream()
                .collect(Collectors.toMap(
                        genFile -> String.valueOf(genFile.getFileNo()), // key
                        genFile -> genFile // value
                ));
    }

    public Optional<GenFile> findById(long id) {
        return genFileRepository.findById(id);
    }

    @Transactional
    public void remove(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo) {
        findBy(relTypeCode, relId, typeCode, type2Code, fileNo).ifPresent(this::remove);
    }

    @Transactional
    public void remove(GenFile genFile) {
        Ut.file.remove(genFile.getFilePath());
        genFileRepository.delete(genFile);
        genFileRepository.flush();
    }

    @Transactional
    public void removeLessonVideo(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo) {
        findBy(relTypeCode, relId, typeCode, type2Code, fileNo).ifPresent(this::removeLessonVideo);
    }
    @Transactional
    public void removeLessonVideo(GenFile genFile) {
        Ut.file.removeAll(AppConfig.getGenFileDirPath() + "/" + genFile.getFileDir());
        genFileRepository.delete(genFile);
        genFileRepository.flush();
    }



    public List<GenFile> findByRelId(String modelName, Long relId) {
        return genFileRepository.findByRelTypeCodeAndRelId(modelName, relId);
    }
    @Transactional
    public GenFile saveTempFile(Member actor, MultipartFile file) {
            return save("temp_" + actor.getModelName(), actor.getId(), "common", "editorUpload", 0, file);
    }

    @Transactional
    public GenFile tempToFile(String url, BaseEntity entity, String typeCode, String type2Code, long fileNo) {
        String fileName = Ut.file.getFileNameFromUrl(url);
        String fileExt = Ut.file.getFileExt(fileName);

        long genFileId = Long.parseLong(fileName.replace("." + fileExt, ""));
        GenFile tempGenFile = findById(genFileId).get();

        GenFile newGenFile = save(entity.getModelName(), entity.getId(), typeCode, type2Code, fileNo, tempGenFile.getFilePath());

        remove(tempGenFile);

        return newGenFile;
    }

    public void removeOldTempFiles() {
        findOldTempFiles().forEach(this::remove);
    }

    private List<GenFile> findOldTempFiles() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return genFileRepository.findByRelTypeCodeAndCreateDateBefore("temp", oneDayAgo);
    }

}