package org.pkaq.sys.dict.service;

import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.sys.dict.cache.DictCacheHelper.Owner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 同一次数据库读取形成的不可变字典快照与身份绑定校验值。
 * @author PKAQ
 * @date 2026-11-01
 */
public record DictSnapshot(String etag, Map<String, Map<String, String>> data) {
    /** 从可信身份、实际来源和有序投影生成校验值，不依赖共享缓存。 */
    static DictSnapshot create(ThreadUser user, List<Owner> owners,
                               Map<String, LinkedHashMap<String, String>> values) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JVM不支持SHA-256", exception);
        }
        append(digest, "dict-snapshot-v1");
        append(digest, String.valueOf(user.getTenantId()));
        append(digest, String.valueOf(user.getUserId()));
        append(digest, String.valueOf(owners.size()));
        owners.forEach(owner -> {
            append(digest, owner.schema());
            append(digest, owner.category());
        });
        append(digest, String.valueOf(values.size()));
        Map<String, Map<String, String>> copy = new LinkedHashMap<>();
        values.forEach((code, items) -> {
            if (code == null || code.isBlank() || items == null) {
                throw new DictSourceException();
            }
            append(digest, code);
            append(digest, String.valueOf(items.size()));
            Map<String, String> labels = new LinkedHashMap<>();
            items.forEach((key, label) -> {
                if (key == null || key.isBlank() || label == null) {
                    throw new DictSourceException();
                }
                append(digest, key);
                append(digest, label);
                labels.put(key, label);
            });
            copy.put(code, Collections.unmodifiableMap(labels));
        });
        return new DictSnapshot("W/\"dict-v1-" + HexFormat.of().formatHex(digest.digest()) + "\"",
                Collections.unmodifiableMap(copy));
    }

    private static void append(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
