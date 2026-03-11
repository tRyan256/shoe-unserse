package com.su.test;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HttpClientTest {


    /**
     * 娴嬭瘯httpclient鍙戦€乬et鏂瑰紡鐨勮姹?
     */
    @Test
    public void testGET() throws IOException {
        //纭繚鏈嶅姟绔凡缁忓惎鍔?
        System.out.println("璇风‘淇濇湇鍔＄宸茬粡鍦?080绔彛鍚姩...");
        
        //鍒涘缓HttpClient瀵硅薄
        CloseableHttpClient aDefault = HttpClients.createDefault();

        //鍒涘缓HttpGet瀵硅薄
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        try {
            //鍙戦€佽姹?
            CloseableHttpResponse execute = aDefault.execute(httpGet);

            //鑾峰彇鍝嶅簲缁撴灉
            int statusCode = execute.getStatusLine().getStatusCode();
            System.out.println("鏈嶅姟绔繑鍥炵殑鐘舵€佺爜涓?"+statusCode);

            HttpEntity entity = execute.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("鏈嶅姟绔繑鍥炵殑鍝嶅簲鏁版嵁涓?"+result);

            execute.close();
        } catch (IOException e) {
            System.err.println("杩炴帴鏈嶅姟鍣ㄥけ璐ワ紝璇锋鏌ユ湇鍔℃槸鍚﹀凡鍚姩: " + e.getMessage());
        } finally {
            aDefault.close();
        }
    }

    @Test
    public void testPost() throws IOException, JSONException {
        //纭繚鏈嶅姟绔凡缁忓惎鍔?
        System.out.println("璇风‘淇濇湇鍔＄宸茬粡鍦?080绔彛鍚姩...");
        
        CloseableHttpClient aDefault = HttpClients.createDefault();

        //鍒涘缓HttpPost瀵硅薄
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username","admin");
        jsonObject.put("password","123456");
        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        stringEntity.setContentEncoding("utf-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        try {
            //鍙戦€佽姹?
            CloseableHttpResponse execute = aDefault.execute(httpPost);

            int statusCode = execute.getStatusLine().getStatusCode();
            System.out.println("鏈嶅姟绔繑鍥炵殑鐘舵€佺爜涓?"+statusCode);

            HttpEntity entity = execute.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("鏈嶅姟绔繑鍥炵殑鍝嶅簲鏁版嵁涓?"+result);

            execute.close();
        } catch (IOException e) {
            System.err.println("杩炴帴鏈嶅姟鍣ㄥけ璐ワ紝璇锋鏌ユ湇鍔℃槸鍚﹀凡鍚姩: " + e.getMessage());
        } finally {
            aDefault.close();
        }
    }
}
