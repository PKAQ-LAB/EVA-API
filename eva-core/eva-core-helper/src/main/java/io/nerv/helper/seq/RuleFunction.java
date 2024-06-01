package io.nerv.helper.seq;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 自定义序列号规则生成工具
 */
public class RuleFunction {
    private final String tenantId;
    private final SequenceRepository sequenceRepository;

    public RuleFunction(String tenantId, SequenceRepository sequenceRepository) {
        this.tenantId = tenantId;
        this.sequenceRepository = sequenceRepository;
    }

    public static String date(String pattern) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String prefix(String prefix) {
        return prefix != null ? prefix : "";
    }


    public String seq(int start, int length, String resetPeriod) {
        String datePattern = getDatePattern(resetPeriod);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern));
        long seqNum = sequenceRepository.getNextSequence(tenantId, date, resetPeriod);
        return String.format("%0" + length + "d", seqNum);
    }

    private String getDatePattern(String resetPeriod) {
        return switch (resetPeriod) {
            case "D" -> "yyyyMMdd";
            case "M" -> "yyyyMM";
            case "Y" -> "yyyy";
            default -> throw new IllegalArgumentException("Unsupported reset period: " + resetPeriod);
        };
    }
}
