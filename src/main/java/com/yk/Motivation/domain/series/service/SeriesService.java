package com.yk.Motivation.domain.series.service;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.base.rsData.RsData;
import com.yk.Motivation.domain.post.entity.Post;
import com.yk.Motivation.domain.series.entity.Series;
import com.yk.Motivation.domain.series.repository.SeriesRepository;
import com.yk.Motivation.domain.document.service.DocumentService;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.genFile.service.GenFileService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.post.service.PostService;
import com.yk.Motivation.domain.postKeyword.entity.PostKeyword;
import com.yk.Motivation.standard.util.Ut;
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
public class SeriesService {
    private final SeriesRepository seriesRepository;
    private final DocumentService documentService;
    private final GenFileService genFileService;
    private final PostService postService;

    public Page<Series> findByKw(String kwType, String kw, boolean isPublic, Pageable pageable) {
        return seriesRepository.findByKw(kwType, kw, isPublic, pageable);
    }

    public Page<Series> findByKw(Member author, String kwType, String kw, Pageable pageable) {
        return seriesRepository.findByKw(author, kwType, kw, pageable);
    }

    @Transactional
    public RsData<Series> write(Member author, long postKeywordId, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {
        return write(author, postService.findKeywordById(postKeywordId).get(), subject, tagsStr, body, bodyHtml, isPublic);
    }

    @Transactional
    public RsData<Series> write(Member author, PostKeyword postKeyword, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {
        Series series = Series.builder()
                .postKeyword(postKeyword)
                .author(author)
                .subject(subject)
                .body(body)
                .bodyHtml(bodyHtml)
                .isPublic(isPublic)
                .build();

        seriesRepository.save(series);

        series.addTags(tagsStr);

        documentService.updateTempGenFilesToInBody(series);

        return new RsData<>("S-1", series.getId() + "번 시리즈가 생성되었습니다.", series);
    }

    @Transactional
    public RsData<Series> modify(Series series, long postKeywordId, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {
        return modify(series, postService.findKeywordById(postKeywordId).get(), subject, tagsStr, body, bodyHtml, isPublic);
    }

    @Transactional
    public RsData<Series> modify(Series series, PostKeyword postKeyword, String subject, String tagsStr, String body, String bodyHtml, boolean isPublic) {

        series.modifyTags(tagsStr);
        series.setPostKeyword(postKeyword);
        series.setSubject(subject);
        series.setBody(body);
        series.setBodyHtml(bodyHtml);
        series.setPublic(isPublic);

        documentService.updateTempGenFilesToInBody(series);

        return new RsData<>("S-1", series.getId() + "번 시리즈가 수정되었습니다.", series);
    }

    public Optional<Series> findById(long id) {
        return seriesRepository.findById(id);
    }

    public Map<String, GenFile> findGenFilesMapKeyByFileNo(Series series, String typeCode, String type2Code) {
        return genFileService.findGenFilesMapKeyByFileNo(series.getModelName(), series.getId(), typeCode, type2Code);
    }

    public RsData<?> checkActorCanModify(Member actor, Series series) {
        if (actor == null || !actor.equals(series.getAuthor())) {
            return new RsData<>("F-1", "권한이 없습니다.", null);
        }

        return new RsData<>("S-1", "가능합니다.", null);
    }

    public RsData<?> checkActorCanRemove(Member actor, Series series) {
        return checkActorCanModify(actor, series);
    }

    public RsData<?> checkActorCanWrite(Member author) {
        return author.isProducer() ? new RsData<>("S-1", "가능합니다.", null) : new RsData<>("F-1", "권한이 없습니다.", null);
    }

    @Transactional
    public RsData<GenFile> saveAttachmentFile(Series series, MultipartFile attachmentFile, long fileNo) {
        String attachmentFilePath = Ut.file.toFile(attachmentFile, AppConfig.getTempDirPath());
        return saveAttachmentFile(series, attachmentFilePath, fileNo);
    }

    @Transactional
    public RsData<GenFile> saveAttachmentFile(Series series, String attachmentFile, long fileNo) {
        GenFile genFile = genFileService.save(series.getModelName(), series.getId(), "common", "attachment", fileNo, attachmentFile);

        return new RsData<>("S-1", genFile.getId() + "번 파일이 생성되었습니다.", genFile);
    }

    @Transactional
    public void removeAttachmentFile(Series series, long fileNo) {
        genFileService.remove(series.getModelName(), series.getId(), "common", "attachment", fileNo);
    }

    public Page<Series> findByTag(String tagContent, Pageable pageable, boolean isPublic) {
        return seriesRepository.findBySeriesTags_contentAndIsPublic(tagContent, pageable, isPublic);
    }

    public Page<Series> findByTag(Member author, String tagContent, Pageable pageable) {
        return seriesRepository.findByAuthorAndSeriesTags_content(author, tagContent, pageable);
    }
    @Transactional
    public RsData<?> remove(Series series) {
        findGenFiles(series).forEach(genFileService::remove);

        seriesRepository.delete(series);

        return new RsData<>("S-1", series.getId() + "번 시리즈가 삭제되었습니다.", null);
    }

    private List<GenFile> findGenFiles(Series series) {
        return genFileService.findByRelId(series.getModelName(), series.getId());
    }
}