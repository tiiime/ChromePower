package com.github.tiiime.chromepower;

import android.app.AndroidAppHelper;
import android.content.SharedPreferences;
import android.content.res.XResources;
import android.support.annotation.NonNull;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by kang on 15/4/22-下午8:40.
 * 百度单词API
 */
public class BaiduDictDecoder extends JsonRequestDecoder {
    static String baiduDictUrl = "http://openapi.baidu.com/public/2.0/translate/dict/simple?" +
            "client_id=08q9FfnxsqRzA6eWOWFbP7zM" +
            "&q=%s" +
            "&from=en" +
            "&to=zh";

    public BaiduDictDecoder(@NonNull final String word, @NonNull final OnSuccessListener onSuccessListener, @NonNull final OnFaliedListener onFailedListener) {

        super(String.format(baiduDictUrl, word), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject jsonObject) {

                        XposedBridge.log("dict-----------|" + String.format(baiduDictUrl,word));

                        try {
                            int error = jsonObject.getInt("errno");
                            if (error != 0) {
                                onFailedListener.onFalied("查询错误");
                                return;
                            }

                            String showStr = "";
                            if (jsonObject.get("data") instanceof JSONArray) {
                                showStr += "未找到该单词";
                            } else {
                                JSONObject data = jsonObject.getJSONObject("data");

                                String word = data.getString("word_name");

                                showStr = String.format("单词:%s\n", word);
                                JSONArray symbols = data.getJSONArray("symbols");
                                for (int i = 0; i < symbols.length(); i++) {
                                    JSONObject symbol = symbols.getJSONObject(i);
                                    String ph_am = symbol.getString("ph_am");
                                    String ph_en = symbol.getString("ph_en");

                                    showStr += String.format("%d. 英音: %s 美音: %s\n", i + 1, ph_en.trim(), ph_am.trim());
                                    JSONArray parts = symbol.getJSONArray("parts");
                                    for (int j = 0; j < parts.length(); j++) {
                                        JSONObject part = parts.getJSONObject(j);
                                        String sPart = part.getString("part");

                                        showStr += String.format("%s ", sPart);
                                        JSONArray means = part.getJSONArray("means");
                                        for (int k = 0; k < means.length(); k++) {
                                            String content = means.getString(k);
                                            showStr += content + "\n";
                                        }
                                    }
                                }
                            }

                            XposedBridge.log(showStr);
                            onSuccessListener.onSuccess(showStr);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailedListener.onFalied("Json解析错误!");
                            XposedBridge.log(e.toString());
                        } finally {
                            XposedBridge.log("----finally");
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
