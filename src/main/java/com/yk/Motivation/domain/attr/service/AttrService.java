package com.yk.Motivation.domain.attr.service;

import com.yk.Motivation.domain.attr.entity.Attr;
import com.yk.Motivation.domain.attr.repository.AttrRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttrService {
    private final AttrRepository attrRepository;

    // 조회
    public String get(String varName, String defaultValue) {
        Attr attr = findAttr(varName);

        if (attr == null) {
            return defaultValue;
        }

        if (attr.getExpireDate() != null && attr.getExpireDate().compareTo(LocalDateTime.now()) < 0) {
            return defaultValue;
        }

        return attr.getVal();
    }

    private Attr findAttr(String relTypeCode, Long relId, String typeCode, String type2Code) {
        return attrRepository.findByRelTypeCodeAndRelIdAndTypeCodeAndType2Code(relTypeCode, relId, typeCode, type2Code).orElse(null);
    }

    private Attr findAttr(String varName) {
        String[] varNameBits = varName.split("__");
        String relTypeCode = varNameBits[0];
        long relId = Integer.parseInt(varNameBits[1]);
        String typeCode = varNameBits[2];
        String type2Code = varNameBits[3];

        return findAttr(relTypeCode, relId, typeCode, type2Code);
    }

    public long getAsLong(String varName, long defaultValue) {
        String value = get(varName, "");

        if (value.isBlank()) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public boolean getAsBoolean(String varName, boolean defaultValue) {
        String value = get(varName, "");

        if (value.isBlank()) {
            return defaultValue;
        }

        if (value.equals("true")) {
            return true;
        } else return value.equals("1");
    }

    public LocalDateTime getAsLocalDatetime(String varName, LocalDateTime defaultValue) {
        String value = get(varName, "");

        if (value.isBlank()) {
            return defaultValue;
        }

        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
    }

    // 명령

    // String, expireDate 없음
    @Transactional
    public void set(String varName, String value) {
        set(varName, value, null);
    }

    // String, expireDate 있음
    @Transactional
    public void set(String varName, String value, LocalDateTime expireDate) {
        _set(varName, value, expireDate);
    }

    // long, expireDate 없음
    @Transactional
    public void set(String varName, long value) {
        set(varName, String.valueOf(value), null);
    }

    // long, expireDate 있음
    @Transactional
    public void set(String varName, long value, LocalDateTime expireDate) {
        _set(varName, String.valueOf(value), expireDate);
    }

    // boolean, expireDate 없음
    @Transactional
    public void set(String varName, boolean value) {
        set(varName, String.valueOf(value), null);
    }

    // boolean, expireDate 있음
    @Transactional
    public void set(String varName, boolean value, LocalDateTime expireDate) {
        _set(varName, String.valueOf(value), expireDate);
    }

    // LocalDateTime, expireDate 없음
    @Transactional
    public void set(String varName, LocalDateTime value) {
        set(varName, value, null);
    }

    // LocalDateTime, expireDate 있음
    @Transactional
    public void set(String varName, LocalDateTime value, LocalDateTime expireDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        _set(varName, value.format(formatter), expireDate);
    }

    private void _set(String varName, String value, LocalDateTime expireDate) {
        String[] varNameBits = varName.split("__");
        String relTypeCode = varNameBits[0];
        long relId = Long.parseLong(varNameBits[1]);
        String typeCode = varNameBits[2];
        String type2Code = varNameBits[3];

        _set(relTypeCode, relId, typeCode, type2Code, value, expireDate);
    }

    private void _set(String relTypeCode, Long relId, String typeCode, String type2Code, String value, LocalDateTime expireDate) {
        Attr attr = findAttr(relTypeCode, relId, typeCode, type2Code);

        if (attr == null) {
            attr = Attr
                    .builder()
                    .relTypeCode(relTypeCode)
                    .relId(relId)
                    .typeCode(typeCode)
                    .type2Code(type2Code)
                    .build();
        }

        attr.setVal(value);
        attr.setExpireDate(expireDate);

        attrRepository.save(attr);
    }
}