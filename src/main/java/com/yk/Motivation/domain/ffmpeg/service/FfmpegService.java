package com.yk.Motivation.domain.ffmpeg.service;

import com.yk.Motivation.base.app.AppConfig;
import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FfmpegService {

    @Value("${custom.ffmpeg.path}")
    private String ffmpegPath;

    @Value("${custom.ffprobe.path}")
    private String ffprobePath;

    private static final Logger log = LoggerFactory.getLogger(FfmpegService.class);

    public double videoHlsMake(String inputFilePath, String fileDir) throws IOException {

        String outputFilePath = AppConfig.getGenFileDirPath() + "/" + fileDir + "/" + "hls";

        File outputDir = new File(outputFilePath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
        FFprobe ffprobe = new FFprobe(ffprobePath);

        // 원본 영상의 해상도를 가져옵니다.
        FFmpegProbeResult probeResult = ffprobe.probe(inputFilePath);
        double originalDuration = probeResult.getFormat().duration; // 영상의 길이를 초 단위로
        int originalHeight = getVideoHeight(inputFilePath, ffprobePath);

        CompletableFuture<Void> future480p = CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, outputFilePath, inputFilePath, "480", "480p.m3u8", "-2:480", "1000k", "96k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> log.info("480p 변환 성공"));

        CompletableFuture<Void> future720p = CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, outputFilePath, inputFilePath, "720","720p.m3u8", "-2:720", "2800k", "128k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> log.info("720p 변환 성공"));

        CompletableFuture<Void> future1080p = originalHeight >= 1080 ? CompletableFuture.runAsync(() -> {
            try {
                convertVideo(ffmpeg, ffprobe, outputFilePath, inputFilePath, "1080","1080p.m3u8", "-2:1080", "18000k", "128k");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> log.info("1080p 변환 성공")) : CompletableFuture.completedFuture(null);

        // 모든 작업이 완료되면 종료 메시지를 출력합니다.
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future480p, future720p, future1080p);
        allOf.thenRun(() -> {
            log.info("모든 변환 작업 성공");
            try {
                createMasterPlaylist(outputFilePath, inputFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).join();
        return originalDuration;
    }

    private String convertVideo(FFmpeg ffmpeg, FFprobe ffprobe, String outputFilePath, String inputFilePath, String folderName, String outputFileName, String scale, String bitrateVideo, String bitrateAudio) throws IOException {

        File outputDir = new File(outputFilePath, folderName);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        String outputPath = new File(outputDir, outputFileName).getAbsolutePath();

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFilePath)
                .addOutput(outputPath)
                .addExtraArgs("-c:v", "libx264")
                .addExtraArgs("-c:a", "aac")
                .addExtraArgs("-ar", "48000")
                .addExtraArgs("-filter:v", "scale=" + scale)
                .addExtraArgs("-b:v", bitrateVideo)
                .addExtraArgs("-b:a", bitrateAudio)
                .addExtraArgs("-profile:v", "baseline")
                .addExtraArgs("-level", "3.0")
                .addExtraArgs("-start_number", "0")
                .addExtraArgs("-hls_time", "10")
                .addExtraArgs("-hls_list_size", "0")
                .addExtraArgs("-f", "hls")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        return outputFileName + " 변환 완료";
    }

    private void createMasterPlaylist(String outputFilePath, String inputFilePath) throws IOException {
        // Master Playlist 생성 시작
        StringBuilder masterPlaylistContent = new StringBuilder("#EXTM3U\n");
        masterPlaylistContent.append("#EXT-X-VERSION:3\n");
        masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=854x480\n");
        masterPlaylistContent.append("480/480p.m3u8\n");
        masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=1280x720\n");
        masterPlaylistContent.append("720/720p.m3u8\n");

        // 원본 해상도가 1080 이상일 때만 추가
        if (getVideoHeight(inputFilePath, ffprobePath) >= 1080) {
            masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1920x1080\n");
            masterPlaylistContent.append("1080/1080p.m3u8\n");
        }

        Path path = Paths.get(outputFilePath, "master.m3u8");
        Files.write(path, masterPlaylistContent.toString().getBytes(StandardCharsets.UTF_8));
    }

    private int getVideoHeight(String videoPath, String ffprobePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=height",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath
        );

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String heightString = reader.readLine();
            return Integer.parseInt(heightString.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
