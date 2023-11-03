package com.example.demo.domain.lecture.service;

import com.example.demo.base.app.AppConfig;
import com.example.demo.base.rsData.RsData;
import com.example.demo.domain.document.service.DocumentService;
import com.example.demo.domain.genFile.entity.GenFile;
import com.example.demo.domain.genFile.service.GenFileService;
import com.example.demo.domain.lecture.entity.Lecture;
import com.example.demo.domain.lecture.repository.LectureRepository;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {
    private final LectureRepository lectureRepository;
    private final DocumentService documentService;
    private final GenFileService genFileService;


    @Transactional
    public RsData<Lecture> write(Member author, String subject, String tagsStr, String body, boolean isPublic) {
        return write(author, subject, tagsStr, body, Ut.markdown.toHtml(body), isPublic);
    }

    @Transactional
    public RsData<Lecture> write(Member producer, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {
        Lecture lecture = Lecture.builder()
                .producer(producer)
                .subject(subject)
                .body(body)
                .bodyHtml(bodyHtml)
                .isPublic(isPublic)
                .build();

        lectureRepository.save(lecture);

        lecture.addTags(tagsStr);

        documentService.updateTempGenFilesToInBody(lecture);

        return new RsData<>("S-1", lecture.getId() + "번 게시물이 생성되었습니다.", lecture);
    }

    @Transactional
    public RsData<Lecture> modify(Lecture lecture, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {

        lecture.modifyTags(tagsStr);

        lecture.setSubject(subject);
        lecture.setBody(body);
        lecture.setBodyHtml(bodyHtml);
        lecture.setPublic(isPublic);

        documentService.updateTempGenFilesToInBody(lecture);

        return new RsData<>("S-1", lecture.getId() + "번 게시물이 수정되었습니다.", lecture);
    }

    public RsData<?> remove(Lecture lecture) {
        findGenFiles(lecture).forEach(genFileService::remove);

        lectureRepository.delete(lecture);

        return new RsData<>("S-1", lecture.getId() + "번 게시물이 삭제되었습니다.", null);
    }








    @Transactional
    public RsData<GenFile> saveAttachmentFile(Lecture lecture, MultipartFile attachmentFile, long fileNo) {
        String attachmentFilePath = Ut.file.toFile(attachmentFile, AppConfig.getTempDirPath());
        return saveAttachmentFile(lecture, attachmentFilePath, fileNo);
    }

    @Transactional
    public RsData<GenFile> saveAttachmentFile(Lecture lecture, String attachmentFile, long fileNo) {
        GenFile genFile = genFileService.save(lecture.getModelName(), lecture.getId(), "common", "attachment", fileNo, attachmentFile);

        return new RsData<>("S-1", genFile.getId() + "번 파일이 생성되었습니다.", genFile);
    }

    @Transactional
    public void removeAttachmentFile(Lecture lecture, long fileNo) {
        genFileService.remove(lecture.getModelName(), lecture.getId(), "common", "attachment", fileNo);
    }

    private List<GenFile> findGenFiles(Lecture lecture) {
        return genFileService.findByRelId(lecture.getModelName(), lecture.getId());
    }

    public Map<String, GenFile> findGenFilesMapKeyByFileNo(Lecture lecture, String typeCode, String type2Code) {
        return genFileService.findGenFilesMapKeyByFileNo(lecture.getModelName(), lecture.getId(), typeCode, type2Code);
    }









    public Page<Lecture> findByKw(String kwType, String kw, Pageable pageable) {
        return lectureRepository.findByKw(kwType, kw, pageable);
    }

    public Optional<Lecture> findById(long id) {
        return lectureRepository.findById(id);
    }

    public Page<Lecture> findByTag(String tagContent, Pageable pageable) {
        return lectureRepository.findByLectureTags_contentAndIsPublicTrue(tagContent, pageable);
    }








    public RsData<?> checkProducerCanModify(Member producer, Lecture lecture) {
        if (producer == null || !producer.equals(lecture.getProducer())) {
            return new RsData<>("F-1", "권한이 없습니다.", null);
        }

        return new RsData<>("S-1", "가능합니다.", null);
    }

    public RsData<?> checkProducerCanRemove(Member producer, Lecture lecture) {
        return checkProducerCanModify(producer, lecture);
    }

}
