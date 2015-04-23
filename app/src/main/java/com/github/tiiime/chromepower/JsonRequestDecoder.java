package com.github.tiiime.chromepower;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;import java.lang.String;

/**
 * Created by kang on 15/4/22-下午8:37.
 */
public abstract class JsonRequestDecoder extends JsonObjectRequest {

    public JsonRequestDecoder(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }


    public interface OnSuccessListener {
        public void onSuccess(String result);
    }

    public interface OnFaliedListener {
        public void onFalied(String result);
    }

}
