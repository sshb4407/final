package com.yk.Motivation.base.app;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class AppConfig { // application.yml - custom
    private static String resourcesStaticDirPath; // static/ 절대 경로

    @Getter
    public static String tempDirPath; // c:/temp/motivation/temp

    @Getter
    public static String genFileDirPath; // c:/temp/motivation

    @Getter
    public static String siteName; // 모티베이션

    @Getter
    public static String siteBaseUrl; // ( localhost:8090 -> 도메인으로 변경 )

    @Getter
    public static String lessonDirPath; // c:/temp/motivation/lesson

    @Value("${custom.genFile.dirPath}") // setter
    public void setGenFileDirPath(String genFileDirPath) {
        AppConfig.genFileDirPath = genFileDirPath;
    }

    @Value("${custom.site.name}") // setter
    public void setSiteName(String siteName) {
        AppConfig.siteName = siteName;
    }

    @Value("${custom.site.baseUrl}") // setter
    public void setSiteBaseUrl(String siteBaseUrl) {
        AppConfig.siteBaseUrl = siteBaseUrl;
    }

    @Value("${custom.tempDirPath}") // setter
    public void setTempDirPath(String tempDirPath) {
        AppConfig.tempDirPath = tempDirPath;
    }

    @Value("${custom.lesson.dirPath}")
    public void setLessonDirPath(String lessonDirPath) {
        AppConfig.lessonDirPath = lessonDirPath;
    }


    public static String getResourcesStaticDirPath() { // static/ 디렉토리의 절대경로를 return
        if (resourcesStaticDirPath == null) {
            ClassPathResource resource = new ClassPathResource("static/");
            try {
                resourcesStaticDirPath = resource.getFile().getAbsolutePath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return resourcesStaticDirPath;
    }
}