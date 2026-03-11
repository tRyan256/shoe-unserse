package com.su.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http宸ュ叿绫?
 */
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 鍙戦€丟ET鏂瑰紡璇锋眰
     * @param url
     * @param paramMap
     * @return
     */



    public static String doGet(String url,Map<String,String> paramMap){
        // 鍒涘缓Httpclient瀵硅薄
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder(url);
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //鍒涘缓GET璇锋眰
            HttpGet httpGet = new HttpGet(uri);
            
            //鍙戦€佽姹?
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                //鍒ゆ柇鍝嶅簲鐘舵€?
                int status = response.getStatusLine().getStatusCode();
                String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(),"UTF-8");
                if(status == 200){
                    return body;
                }
                throw new RuntimeException("HTTP GET failed, status=" + status + ", body=" + body);
            }
        }catch (Exception e){
            throw new RuntimeException("HTTP GET request failed, url=" + url, e);
        }
    }

    /**
     * 鍙戦€丳OST鏂瑰紡璇锋眰
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 鍒涘缓Httpclient瀵硅薄
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 鍒涘缓Http Post璇锋眰
            HttpPost httpPost = new HttpPost(url);

            // 鍒涘缓鍙傛暟鍒楄〃
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 妯℃嫙琛ㄥ崟
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 鎵цhttp璇锋眰
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 鍙戦€丳OST鏂瑰紡璇锋眰
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 鍒涘缓Httpclient瀵硅薄
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 鍒涘缓Http Post璇锋眰
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //鏋勯€爅son鏍煎紡鏁版嵁
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll(paramMap);
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //璁剧疆璇锋眰缂栫爜
                entity.setContentEncoding("utf-8");
                //璁剧疆鏁版嵁绫诲瀷
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 鎵цhttp璇锋眰
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            throw e;
        }
    }
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

}
