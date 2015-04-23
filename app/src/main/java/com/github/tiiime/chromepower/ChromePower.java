package com.github.tiiime.chromepower;


import android.app.AndroidAppHelper;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.Override;import java.lang.String;import java.lang.Throwable;import java.util.HashMap;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChromePower implements IXposedHookLoadPackage {

    private static final HashMap<String, String> map = new HashMap<String, String>();

    static {
        map.put(" ", "%20");
        map.put("+", "%2B");
        map.put("#", "%23");
        map.put("/", "%2F");
        map.put("&", "%26");
        map.put("~", "%7e");
        map.put("$", "%24");
        map.put("^", "%5e");
    }

    /**
     * 找到 Chrome 的选择文字方法
     * 监听复制操作
     *
     * @param lpparam
     * @throws Throwable
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.chrome"))
            return;
        findAndHookMethod("org.chromium.content.browser.SelectActionModeCallback",
                lpparam.classLoader,
                "onActionItemClicked",
                ActionMode.class,
                MenuItem.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //TODO: 动态获取按下item的id 判断  复制/分享/全选
                        Context mContext = AndroidAppHelper.currentApplication().getBaseContext();
                        ClipboardManager clipboardManager =
                                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

                        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }

                });

    }

    /**
     * 剪贴板事件监听器
     */
    private ClipboardManager.OnPrimaryClipChangedListener
            onPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            final Context mContext = AndroidAppHelper.currentApplication().getBaseContext();

            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            String textToPaste = null;
            if (clipboardManager.hasPrimaryClip()) {
                ClipData clip = clipboardManager.getPrimaryClip();
                textToPaste = clip.getItemAt(0).coerceToText(mContext).toString();

                if (!TextUtils.isEmpty(textToPaste)) {
                    XposedBridge.log("clip " + textToPaste);
                }
            }
            textToPaste = textToPaste.trim();
            textToPaste = textToPaste.replace("%", "%25");

            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonRequestDecoder jsonRequestDecoder = null;

//            if (isSentence(textToPaste)) {
//                jsonRequestDecoder = new BaiduTranslateDecoder(encodeStr(textToPaste),
//                        onSuccessListener,
//                        onFaliedListener);
//            } else {
//                jsonRequestDecoder = new BaiduDictDecoder(encodeStr(textToPaste),
//                        onSuccessListener,
//                        onFaliedListener);
//            }


            jsonRequestDecoder = new YouDaoTranslateDecoder(encodeStr(textToPaste),
                    onSuccessListener,
                    onFaliedListener);

            if (jsonRequestDecoder != null) {
                queue.add(jsonRequestDecoder);
            }

            clipboardManager.removePrimaryClipChangedListener(onPrimaryClipChangedListener);
        }
    };


    JsonRequestDecoder.OnSuccessListener onSuccessListener = new JsonRequestDecoder.OnSuccessListener() {
        @Override
        public void onSuccess(String result) {
            makeLongText(result);
        }
    };

    JsonRequestDecoder.OnFaliedListener onFaliedListener = new JsonRequestDecoder.OnFaliedListener() {
        @Override
        public void onFalied(String result) {
            makeShortText(result);
        }
    };

    private void makeShortText(String text) {
        makeText(text, Toast.LENGTH_SHORT);
    }

    private void makeLongText(String text) {
        makeText(text, Toast.LENGTH_LONG);
    }

    private void makeText(String text, int l) {
        Context context = AndroidAppHelper.currentApplication().getBaseContext();

        Toast.makeText(context, text, l).show();
    }

    private String encodeStr(String str) {
        for (String key : map.keySet()) {
            if (str.contains(key)) {
                str = str.replace(key, map.get(key));
            }
        }
        return str;
    }

    private boolean isSentence(String str) {
        boolean result = false;
        for (String key : map.keySet()) {
            if (str.contains(key)) {
                result = true;
                break;
            }
        }
        return result;
    }

}

/**
 *TODO:
 * 1. 添加界面  选择翻译来源
 * 2. 整理字符串资源
 * */