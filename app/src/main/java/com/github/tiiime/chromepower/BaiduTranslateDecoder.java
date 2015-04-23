package com.github.tiiime.chromepower;

import android.support.annotation.NonNull;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;import java.lang.Override;import java.lang.String;

/**
 * Created by kang on 15/4/22-下午8:40.
 * 百度翻译API
 */
public class BaiduTranslateDecoder extends JsonRequestDecoder {
    static String baiduTranslateUrl = "http://openapi.baidu.com/public/2.0/bmt/translate?" +
            "client_id=08q9FfnxsqRzA6eWOWFbP7zM" +
            "&q=%s" +
            "&from=auto" +
            "&to=auto";

    public BaiduTranslateDecoder(@NonNull final String sentence,
                                 @NonNull final OnSuccessListener onSuccessListener,
                                 @NonNull final OnFaliedListener onFailedListener) {

        super(String.format(baiduTranslateUrl, sentence),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject jsonObject) {
                        try {
                            String str = jsonObject.getJSONArray("trans_result")
                                    .getJSONObject(0).getString("dst");
                            onSuccessListener.onSuccess("句子:" + str);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailedListener.onFalied("getJsonError!");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onFailedListener.onFalied("networkError!");
                    }
                });
    }


}
