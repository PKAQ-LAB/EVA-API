package org.pkaq.web.sys.organization.ctrl;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.pkaq.core.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public class OrgCtrlTest extends BaseTest {
    private final String prefix = "/organization";
    @Autowired
    private TestRestTemplate restTemplate;
    // 查询测试
    @Test
    public void listOrg() {
        // 全量查询
        String body = this.restTemplate.getForObject(prefix+"/list", String.class);
        Console.log(body);
    }

    // 按名称查询
    @Test
    public void listOrgByName(){
        // 按名称查询
        String byname = this.restTemplate.getForObject(prefix+"/list?name=统合部", String.class);
        Console.log(byname);
    }
    // 按code查询
    public void listOrgByCode(){
        // 按编码查询
        String bycode = this.restTemplate.getForObject(prefix+"/list?code=T01", String.class);
        Console.log(bycode);
    }
    // 新增/编辑测试
    @Test
    public void editOrg() {
        // 插入测试
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","插入测试");
        jsonObject.put("code","testCode");

        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(jsonObject, setHeader());
        this.restTemplate.postForObject(prefix+"/edit", httpEntity, String.class);
    }

    // 修改测试O
    @Test
    public void updateOrg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "38c1d73b8aae438bbcbdb25de257a6fd");
        jsonObject.put("name","测试修改");

        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(jsonObject, setHeader());
        this.restTemplate.postForObject(prefix+"/edit", httpEntity, String.class);
    }

    /**
     * 删除测试
     */
    @Test
    public void delete(){
        // 测试存在的ID
        // 测试不存在的ID
        String[] ids = {"3a1b818362d248439b54f58b64b4a64a"};
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ids", ids);

        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(jsonObject, setHeader());
        Object obje = this.restTemplate.postForEntity(prefix+"/del", httpEntity, String.class);
        Console.error(obje.toString());
        // 测试参数为空
        HttpEntity<JSONObject> nullEntity = new HttpEntity<>(null, setHeader());
        Object obj = this.restTemplate.postForEntity(prefix+"/del", nullEntity, String.class);
        Console.error(obj.toString());
    }
}