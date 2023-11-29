package com.example.flutterplugin.util;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    private static volatile HttpUtil single;

    public static HttpUtil getInstance() {
        if (single == null) {
            synchronized (HttpUtil.class) {
                if (single == null) {
                    single = new HttpUtil();
                }
            }
        }
        return single;
    }

    private OkHttpClient client;

    private HttpUtil() {
        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 上传
     * @param file
     * @return
     */
    @Nullable
    public String upload(File file){
        try{
            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("updateFile", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"), file))
                    .addFormDataPart("fileType", "1")
                    .build();

            Request request = new Request.Builder()
                    .url("https://****/upload/file")   //TODO:替换成自己的上传服务！！！
                    .post(multipartBody)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new Exception("request failed, response code= " + response.code() + " + msg= " + response.message());
            if (response.body() == null) throw new Exception("request failed, response body is null");
            String resultJson =response.body().string();
            LogUtil.log("upload result -> " + resultJson);
            JSONObject jsonObject = new JSONObject(resultJson);
            String resultCode = jsonObject.getString("code");
            String url = jsonObject.getString("data");
            if ("0".equals(resultCode)) {
                return url;
            }
        }catch (Exception e){
            LogUtil.log("upload result -> failed =>" + e.getMessage());
        }
        return null;
    }

    /**
     * 校验是否已上传
     * @return
     */
    @Nullable
    public String check(SoType type, String sdkVersion){
        //TODO: 自己实现版本校验
        return null;
    }
}
