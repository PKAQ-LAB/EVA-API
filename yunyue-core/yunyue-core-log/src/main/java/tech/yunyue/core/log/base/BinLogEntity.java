package tech.yunyue.core.log.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class BinLogEntity implements Serializable {
    private String type;
    private String database;
    private String table;
    private Map<String, Object> data;
    private Map<String, Object> old;
}
