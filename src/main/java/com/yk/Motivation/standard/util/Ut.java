package com.yk.Motivation.standard.util;

import com.yk.Motivation.base.app.AppConfig;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ut {

    public static class markdown {
        // 오직 초기 데이터를 생성하는데만 사용된다.
        // 운영모드에서는 사용되지 않는다.
        public static String toHtml(String body) {
            return "<p>" + body.replace("\r\n", "<br>") + "</p>";
        }
    }
    public static class date {
        public static String getCurrentDateFormatted(String pattern) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(new Date());
        }

        public static int getEndDayOf(int year, int month) {
            String yearMonth = year + "-" + "%02d".formatted(month);

            return getEndDayOf(yearMonth);
        }

        public static int getEndDayOf(String yearMonth) {
            LocalDate convertedDate = LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            convertedDate = convertedDate.withDayOfMonth(
                    convertedDate.getMonth().length(convertedDate.isLeapYear()));

            return convertedDate.getDayOfMonth();
        }

        public static LocalDateTime parse(String pattern, String dateText) {
            return LocalDateTime.parse(dateText, DateTimeFormatter.ofPattern(pattern));
        }

        public static LocalDateTime parse(String dateText) {
            return parse("yyyy-MM-dd HH:mm:ss.SSSSSS", dateText);
        }

    }

    public static class file {
        private static final String ORIGIN_FILE_NAME_SEPARATOR;

        static {
            ORIGIN_FILE_NAME_SEPARATOR = "--originFileName_";
        }
        public static String getOriginFileName(String file) {
            if (file.contains(ORIGIN_FILE_NAME_SEPARATOR)) {
                String[] fileInfos = file.split(ORIGIN_FILE_NAME_SEPARATOR);
                return fileInfos[fileInfos.length - 1];
            }

            return Paths.get(file).getFileName().toString();
        }

        public static String toFile(MultipartFile multipartFile, String tempDirPath) { // temp 저장
            if (multipartFile == null) return "";
            if (multipartFile.isEmpty()) return "";

            String filePath = tempDirPath + "/" + UUID.randomUUID() + ORIGIN_FILE_NAME_SEPARATOR + multipartFile.getOriginalFilename();

            try {
                multipartFile.transferTo(new File(filePath));
            } catch (IOException e) {
                return "";
            }

            return filePath;
        }

        public static void moveFile(String filePath, File file) {
            moveFile(filePath, file.getAbsolutePath());
        }

        public static boolean exists(String file) {
            return new File(file).exists();
        }

        public static boolean exists(MultipartFile file) {
            return file != null && !file.isEmpty();
        }

        public static String tempCopy(String file) {
            String tempPath = AppConfig.getTempDirPath() + "/" + getFileName(file);
            copy(file, tempPath);

            return tempPath;
        }

        private static String getFileName(String file) {
            return Paths.get(file).getFileName().toString();
        }

        private static void copy(String file, String tempDirPath) {
            try {
                Files.copy(Paths.get(file), Paths.get(tempDirPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static class DownloadFileFailException extends RuntimeException {

        }

        private static String getFileExt(File file) {
            Tika tika = new Tika();
            String mimeType = "";

            try {
                mimeType = tika.detect(file);
            } catch (IOException e) {
                return null;
            }

            String ext = mimeType.replace("image/", "");
            ext = ext.replace("jpeg", "jpg");

            return ext.toLowerCase();
        }

        public static String getFileExt(String fileName) {
            int pos = fileName.lastIndexOf(".");

            if (pos == -1) {
                return "";
            }

            return fileName.substring(pos + 1).trim();
        }

        public static String getFileNameFromUrl(String fileUrl) {
            try {
                return Paths.get(new URI(fileUrl).getPath()).getFileName().toString();
            } catch (URISyntaxException e) {
                return "";
            }
        }

        // fileUrl 에서 파일을 다운로드 하여 filePath(outputDir/tempFileName) 에 저장
        public static String downloadFileByHttp(String fileUrl, String outputDir) {
            String originFileName = getFileNameFromUrl(fileUrl); // filename.ext
            String fileExt = getFileExt(originFileName); // ext

            if (fileExt.isEmpty()) {
                fileExt = "tmp";
            }

            new File(outputDir).mkdirs(); // 경로 없으면 생성 (tem Directory)

            String tempFileName = UUID.randomUUID() + ORIGIN_FILE_NAME_SEPARATOR + originFileName + "." + fileExt;
            String filePath = outputDir + "/" + tempFileName;

            // 'try-with-resources' 문을 사용하여 파일 출력 스트림을 생성
            // 이렇게 하면 스트림이 사용 후 자동으로 닫음
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {

                // 주어진 파일 URL에서 입력 스트림을 열어 읽기 가능한 바이트 채널을 생성
                ReadableByteChannel readableByteChannel = Channels.newChannel(new URI(fileUrl).toURL().openStream());

                // FileOutputStream 객체에서 파일 채널을 가져옴
                FileChannel fileChannel = fileOutputStream.getChannel();

                // readableByteChannel에서 데이터를 읽어와 fileChannel을 통해 파일에 기록
                // 시작 위치는 0이며, Long.MAX_VALUE는 최대 복사 가능한 바이트 수를 나타냄
                // 실제로는 EOF(End-Of-File) 또는 파일의 크기에 도달할 때까지 데이터를 복사
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            } catch (Exception e) {
                throw new DownloadFileFailException();
            }

            File file = new File(filePath);

            if (file.length() == 0) {
                throw new DownloadFileFailException();
            }

            if (fileExt.equals("tmp")) {
                String ext = getFileExt(file);

                if (ext == null || ext.isEmpty()) {
                    throw new DownloadFileFailException();
                }

                String newFilePath = filePath.replace(".tmp", "." + ext);
                moveFile(filePath, newFilePath);
                filePath = newFilePath;
            }

            return filePath;
        }

        public static void moveFile(String filePath, String destFilePath) {
            Path file = Paths.get(filePath);
            Path destFile = Paths.get(destFilePath);

            try {
                // file 경로의 파일이 destFile 경로로 이동함
                // destFile 경로에 이미 파일이 존재 한다면 'StandardCopyOption.REPLACE_EXISTING' 에 의해서 덮어 씌워짐
                Files.move(file, destFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {

            }
        }


        public static String getExt(String filename) {
            return Optional.ofNullable(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase())
                    .orElse("");
        }

        public static String getFileExtTypeCodeFromFileExt(String ext) {
            return switch (ext) {
                case "jpeg", "jpg", "gif", "png" -> "img";
                case "mp4", "avi", "mov" -> "video";
                case "mp3" -> "audio";
                default -> "etc";
            };

        }

        public static String getFileExtType2CodeFromFileExt(String ext) {

            return switch (ext) {
                case "jpeg", "jpg" -> "jpg";
                case "gif", "png", "mp4", "mov", "avi", "mp3" -> ext;
                default -> "etc";
            };

        }

        public static void remove(String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }

        public static void removeAll(String filePath) {
            if (filePath == null) {
                return;
            }

            File file = new File(filePath);
            if (file.exists()) {
                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            removeAll(child.getAbsolutePath());
                        }
                    }
                }
                file.delete();
            }
        }
    }

    public static class url {

        public static String encode(String message) {
            String tempReplacement = "TEMP_PLUS";
            message = message.replace("+", tempReplacement);
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return encodedMessage.replace("+", "%20")
                    .replace(tempReplacement, "+");
        }

        public static String modifyQueryParam(String url, String paramName, String paramValue) {
            url = deleteQueryParam(url, paramName);
            url = addQueryParam(url, paramName, paramValue);

            return url;
        }

        public static String addQueryParam(String url, String paramName, String paramValue) {
            if (!url.contains("?")) {
                url += "?";
            }

            if (!url.endsWith("?") && !url.endsWith("&")) {
                url += "&";
            }

            url += paramName + "=" + paramValue;

            return url;
        }

        public static String deleteQueryParam(String url, String paramName) {
            int startPoint = url.indexOf(paramName + "=");
            if (startPoint == -1) return url;

            int endPoint = url.substring(startPoint).indexOf("&");

            if (endPoint == -1) {
                return url.substring(0, startPoint - 1);
            }

            String urlAfter = url.substring(startPoint + endPoint + 1);

            return url.substring(0, startPoint) + urlAfter;
        }

        public static String encodeWithTtl(String s) {
            if (Ut.str.isBlank(s)) return "";

            return withTtl(encode(s));
        }

        public static String withTtl(String msg) {
            return msg + ";ttl=" + new Date().getTime();
        }

        public static String getPath(String refererUrl, String defaultValue) {
            try {
                return new URI(refererUrl).getPath();
            } catch (URISyntaxException e) {
                return defaultValue;
            }
        }

        public static String getQueryParamValue(String url, String paramName, String defaultValue) {
            String[] urlBits = url.split("\\?", 2);

            if (urlBits.length == 1) {
                return defaultValue;
            }

            urlBits = urlBits[1].split("&");

            String param = Arrays.stream(urlBits)
                    .filter(s -> s.startsWith(paramName + "="))
                    .findAny()
                    .orElse(paramName + "=" + defaultValue);

            String value = param.split("=", 2)[1].trim();

            return value.length() > 0 ? value : defaultValue;
        }
    }

    public static class str {
        public static boolean hasLength(String string) {
            return string != null && !string.trim().isEmpty();
        }

        public static boolean isBlank(String string) {
            return !hasLength(string);
        }


        public static String tempPassword(int len) {
            String passwordSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
            StringBuilder password = new StringBuilder();

            for (int x = 0; x < len; x++) {
                int random = (int) (Math.random() * passwordSet.length());
                password.append(passwordSet.charAt(random));
            }

            return password.toString();
        }

        public static String randomNumStr(int i) {
            String passwordSet = "0123456789";
            StringBuilder password = new StringBuilder();

            for (int x = 0; x < i; x++) {
                int random = (int) (Math.random() * passwordSet.length());
                password.append(passwordSet.charAt(random));
            }

            return password.toString();
        }

        public static String replace(String input, String regex, Function<String, String> replacer) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String replacement = replacer.apply(matcher.group(1));
                matcher.appendReplacement(result, replacement);
            }

            matcher.appendTail(result);

            return result.toString();
        }

        public static String ucfirst(String str) {
            if (str == null || str.isEmpty()) return str;

            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }

    public static class thy {
        private static String getFirstStrOrEmpty(List<String> requestParameterValues) {
            return Optional.ofNullable(requestParameterValues)
                    .filter(values -> !values.isEmpty())
                    .map(values -> values.get(0).replace("%20", "").trim())
                    .orElse("");
        }

        public static boolean inputAttributeAutofocus(List<String> requestParameterValues) {
            return !str.hasLength(getFirstStrOrEmpty(requestParameterValues));
        }

        public static boolean isBlank(List<String> requestParameterValues) {
            return !hasText(requestParameterValues);
        }

        public static boolean hasText(List<String> requestParameterValues) {
            return str.hasLength(getFirstStrOrEmpty(requestParameterValues));
        }

        public static String value(List<String> requestParameterValues) {
            return getFirstStrOrEmpty(requestParameterValues);
        }

    }
}