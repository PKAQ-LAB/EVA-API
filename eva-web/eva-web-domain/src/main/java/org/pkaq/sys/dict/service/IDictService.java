package org.pkaq.sys.dict.service;

import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.vo.DictViewVo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface IDictService {

    void init();

    Map<String, LinkedHashMap<String, String>> selectDict();

    Map<?, ?> fetchDicts();

    DictViewVo getDict(DictAoeBo bo);

    List<DictViewVo> listDict();

    void delDict(String id);

    void edit(DictAoeBo dictAoeBo);

    boolean checkUnique(DictAoeBo bo);

    void init(Map<String, LinkedHashMap<String, String>> dictMap);

    void reload();

    void reload(Map<String, LinkedHashMap<String, String>> dictMap);
}
