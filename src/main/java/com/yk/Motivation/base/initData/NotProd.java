package com.yk.Motivation.base.initData;

import com.yk.Motivation.base.app.AppConfig;
import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.article.service.ArticleService;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.board.service.BoardService;
import com.yk.Motivation.domain.lecture.service.LectureService;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.service.MemberService;
import com.yk.Motivation.domain.post.entity.Post;
import com.yk.Motivation.domain.post.service.PostService;
import com.yk.Motivation.domain.postKeyword.entity.PostKeyword;
import com.yk.Motivation.domain.series.service.SeriesService;
import com.yk.Motivation.domain.system.service.SystemService;
import com.yk.Motivation.standard.util.Ut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;


@Configuration
@Profile("!prod")  // 이 구성은 'prod' 프로파일이 활성화되지 않은 경우에만 적용된다.
public class NotProd {

//    @Value("${custom.security.oauth2.client.registration.kakao.devUserOauthId}") // 개발 중 테스트를 위해 사용 했었음.
//    private String kakaoDevUserOAuthId;

    @Autowired
    @Lazy
    private NotProd self;
    @Autowired
    private BoardService boardService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private PostService postService;
    @Autowired
    private LectureService lectureService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private SeriesService seriesService;

    @Bean
    public ApplicationRunner initNotProd() {
        return args -> {
            if (systemService.isNotProdInitDataConfigured() == false) {
                self.work1();
                self.work2();

                systemService.setNotProdInitDataConfigured(true);
            }

//            self.work3();
        };
    }

    @Transactional
    public void work1() {
        Board board1 = boardService.make("notice1", "공지사항", "<i class=\"fa-regular fa-flag\"></i>").getData();
        Board board2 = boardService.make("free1", "자유", "<i class=\"fa-solid fa-face-grin-tears\"></i>").getData();

        Member member1 = memberService.join("admin", "1234", "admin", "admin@test.com", "").getData();
        Member member2 = memberService.join("user1", "1234", "nickname1", "user1@test.com", "").getData();
        Member member3 = memberService.join("user2", "1234", "nickname2", "user2@test.com", "").getData();
        Member member4 = memberService.join("user3", "1234", "nickname3", "user3@test.com", "").getData();

        memberService.setEmailVerified(member1);
        memberService.setEmailVerified(member2);
        memberService.setEmailVerified(member3);

        memberService.beProducer(member1.getId(), "장필우");
        memberService.beProducer(member2.getId(), "고니");

//        Member memberByKakao = memberService.whenSocialLogin("KAKAO", "KAKAO__%s".formatted(kakaoDevUserOAuthId), "홍길동", "");

        Article article1 = articleService.write(board1, member1, "제목 1", "#자바 #HTML", "내용 1").getData();

        Article article2 = articleService.write(board1, member2, "제목 2", "#CSS #HTML #홍길동", "내용 2").getData();
        Article article3 = articleService.write(board2, member1, "제목 3", "#자바의 정석", "내용 3").getData();
        Article article4 = articleService.write(board2, member2, "제목 4", "#홍길동", "내용 4").getData();

        String file1Path = Ut.file.tempCopy(AppConfig.getResourcesStaticDirPath() + "/resource/common/common.css");
        String file2Path = Ut.file.tempCopy(AppConfig.getResourcesStaticDirPath() + "/resource/common/common.js");
        articleService.saveAttachmentFile(article1, file1Path, 1L);
        articleService.saveAttachmentFile(article1, file2Path, 2L);

        Post post1 = postService.write(member1, "제목 1", "#자바 #HTML", "내용 1", true).getData();

        PostKeyword postKeywordHtml = post1.getPostTags()
                .stream()
                .filter(postTag -> postTag.getContent().equals("HTML"))
                .map(postTag -> postTag.getPostKeyword())
                .findFirst()
                .get();

        String file3Path = Ut.file.tempCopy(AppConfig.getResourcesStaticDirPath() + "/resource/common/common.css");
        String file4Path = Ut.file.tempCopy(AppConfig.getResourcesStaticDirPath() + "/resource/common/common.js");
        postService.saveAttachmentFile(post1, file3Path, 1L);
        postService.saveAttachmentFile(post1, file4Path, 2L);

        Post post2 = postService.write(member1, "제목 2", "#CSS #HTML #Python", "내용 2", true).getData();
        Post post3 = postService.write(member1, "제목 3", "#Java #HTML", "내용 3", true).getData();
        Post post4 = postService.write(member2, "제목 4", "#Python #Script", "내용 4", false).getData();
        Post post5 = postService.write(member2, "제목 5", "#Java #JSP", "내용 5", false).getData();
        Post post6 = postService.write(member2, "제목 6", "#CSS #Hungry #Python", "내용 6", true).getData();

        seriesService.write(member1, postKeywordHtml, "HTML 기초", "#HTML #프론트 엔드", "내용 1", Ut.markdown.toHtml("내용 1"), true);

//        lectureService.write(member1, "제목1", "#HTML", "내용1", true);
//        lectureService.write(member1, "제목2", "#HTML", "내용1", true);


    }

    @Transactional
    public void work2() {
        Article article1 = articleService.findById(1L).get();
        articleService.modify(article1, "제목 1 수정", "#자바2 #HTML", "내용 1\n수정", "내용 1<br />수정");
    }


    @Transactional
    public void work3() {

        Board board = boardService.findByCode("free1").get();
        Member member = memberService.findById(2).get();

        IntStream.rangeClosed(1, 100).forEach(i -> {
            articleService.write(board, member, "제목" + i, "dummy", "내용" + i);
        });
    }
}