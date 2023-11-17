package com.yk.Motivation.base.initData;

import com.yk.Motivation.base.app.AppConfig;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class All {
    @Bean
    public ApplicationRunner initAll( // ApplicationRunner ( SpringBoot 의 인터페이스 로써 애플리케이션 시작 시 실행 됨)
    ) {
        return args -> {
            new File(AppConfig.getTempDirPath()).mkdirs(); // TempDirPath 경로가 없으면 만들겠다.
        };
    }
}
