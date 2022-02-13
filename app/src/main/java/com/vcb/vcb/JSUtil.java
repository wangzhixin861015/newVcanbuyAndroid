package com.vcb.vcb;

import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;


/**
 * Created by huangjianhua on 2018/7/25.
 */

public class JSUtil {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void callJS(WebView webView , String function, String... params) {
        if (function != null && !"".equals(function)) {
            String strParam = "";
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    strParam += "'" + params[i] + "',";
                }
                strParam = strParam.substring(0, strParam.length() - 1);
            }


            if (Build.VERSION.SDK_INT < 18) {

                webView.loadUrl("javascript:window['" + function + "']&&" + function + "(" + strParam + ")");

            } else {

                webView.evaluateJavascript("javascript:"+ function +"('"+ strParam +"')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        LogUtil.i("---onReceiveValue---" + value);
                    }
                });
//                webView.evaluateJavascript("javascript:window['" + function + "']&&" + function + "(" + strParam + ")", new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        //此处为 js 返回的结果
//                        LogUtil.i("---onReceiveValue---" + value);
//                    }
//                });

            }
        }
    }

}
