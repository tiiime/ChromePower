package com.github.tiiime.chromepower;

import android.support.annotation.NonNull;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Override;import java.lang.String;import de.robv.android.xposed.XposedBridge;

/**
 * Created by kang on 15/4/23-下午4:41.
 * 有道 API
 */
public class YouDaoTranslateDecoder extends JsonRequestDecoder {
    static String youdaoTranslateAPI = "http://fanyi.youdao.com/openapi.do?" +
            "keyfrom=tiiime" +
            "&key=873783635" +
            "&type=data" +
            "&doctype=json" +
            "&version=1.1" +
            "&q=%s";

    public YouDaoTranslateDecoder(@NonNull final String word,
                                  @NonNull final OnSuccessListener onSuccessListener,
                                  @NonNull final OnFaliedListener onFailedListener) {
        super(String.format(youdaoTranslateAPI, word),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        String showStr = "";
                        try {

                            //错误代码
                            int errorCode = jsonObject.getInt("errorCode");
                            if (errorCode != 0) {
                                showStr = "error code " + errorCode;
                                onFailedListener.onFalied(showStr);
                                return;
                            }

                            //获取翻译
                            String translate = jsonObject.getString("translation");
                            showStr += String.format("翻译: %s", translate);

                            //基本含义
                            if (jsonObject.has("basic")) {
                                JSONObject basic = jsonObject.getJSONObject("basic");
                                JSONArray explains = basic.getJSONArray("explains");
                                showStr += "\n基本释义: ";

                                for (int i = 0; i < explains.length(); i++) {
                                    showStr += explains.getString(i) ;
                                    if (i < explains.length() - 1) {
                                        showStr += "\n";
                                    }
                                }
                            }

                            //类似单词含义
                            if (!jsonObject.has("web")) {
                                onSuccessListener.onSuccess(showStr);
                                return;
                            }

                            JSONArray web = jsonObject.getJSONArray("web");
                            int loop = (web.length() > 2) ? 2 : web.length();
                            showStr += "\n相似单词: ";
                            for (int i = 0; i < loop; i++) {
                                JSONObject item = web.getJSONObject(i);

                                JSONArray value = item.getJSONArray("value");
                                String key = item.getString("key");
                                showStr += String.format("%s: ", key);

                                for (int j = 0; j < value.length(); j++) {
                                    showStr += value.getString(j);
                                    if (j < value.length() - 1) {
                                        showStr += ",";
                                    } else {
                                        showStr += "\n";
                                    }
                                }
                            }
                            onSuccessListener.onSuccess(showStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailedListener.onFalied("json decode error");
                            XposedBridge.log(e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onFailedListener.onFalied("NetworkError!");
                    }
                });


    }
}
