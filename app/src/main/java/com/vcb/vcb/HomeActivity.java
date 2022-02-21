package com.vcb.vcb;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.vcb.jpush.ExampleUtil;
import com.vcb.jpush.LocalBroadcastManager;
import com.vcb.vcb.bean.FlashScreenUrlBean;
import com.vcb.vcb.util.OnFinishListener;
import com.vcb.vcb.util.ProgressView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import cn.jpush.android.api.JPushInterface;


public class HomeActivity extends AppCompatActivity {
     private String url = "http://m.vcanbuy.com/#/";
    private String hotUrl = "http://m.vcanbuy.com/#/";
    private String hotUrl2 = "https://m.vcanbuy.com/#/";
//    private String hotUrl = "http://47.118.71.175:8081/#/";
//    private String url = "http://47.118.71.175:8081/#/";

    //private String url = "http://hk.vmall.vcanbuy.com/#/";
    private WebView webView;

    private static final String APP_CACAHE_DIRNAME = "/webcache";

    private ImageView imageView;
    private ImageView backIcon;
    private LinearLayout titleBar;
    public static boolean isForeground = false;
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";
   // LoadingDialog loadingDialog = null;
    WebSettings settings;
    private boolean isFinish=false;
    private long[] mHits = new long[2]; // 双击事件
    private boolean isFirst=true;



    public ValueCallback<Uri>mUploadCallBack;
    public ValueCallback<Uri[]> mUploadCallBackAboveL;
    public String mCameraFilePath;
    public int REQUEST_CODE_FILE_CHOOSER=12345;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission_group.STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA"};
    String  itemId;
    String registationId;
    String  sessionId;

    private ProgressView mProgressView;
    private RelativeLayout mRlEnter;
    String  transactionReserveId;
    Uri data;
    private boolean isTwoBack=false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        verifyStoragePermissions(this);
        imageView = findViewById(R.id.imageView);
        backIcon=findViewById(R.id.back_icon);
        titleBar=findViewById(R.id.title_bar);
         data = getIntent().getData();

        SharedPreferences flashScreenSp=getSharedPreferences("flashScreen", Context.MODE_PRIVATE);
        //第一个参数是键名，第二个是默认值
        String flashScreenImageUrl=flashScreenSp.getString("imageUrl", "");
        if(flashScreenImageUrl!=null&&!flashScreenImageUrl.equals("")){
            Gson gson = new Gson();
            FlashScreenUrlBean flashScreenUrlBean=gson.fromJson(flashScreenImageUrl, FlashScreenUrlBean.class);
            imageView.setBackgroundColor(Color.parseColor(flashScreenUrlBean.getBackgroundColor()));
            Glide.with(this)
                    .load(flashScreenUrlBean.getImageUrl())
                    .into(imageView);
        }else {
            imageView.setBackgroundColor(getResources().getColor(R.color.wither));
            Glide.with(this)
                    .load(R.drawable.ic_splash_new)
                    .into(imageView);
        }
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();//返回上一页面
                    if(isTwoBack){
                        webView.goBack();//返回上一页面
                        isTwoBack=false;
                    }
                }
            }
        });

        registerMessageReceiver();
        init();
      //  getFlashScreenImage();


    }

    public static void saveRegistationId(Context context, String registationId){
        //获取SharedPreferences对象
        SharedPreferences sharedPre=context.getSharedPreferences("config", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("registationId", registationId);
        //提交
        editor.commit();
    }
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
                    //    setCostomMsg(showMsg.toString());
                }
            } catch (Exception e) {
            }
        }
    }

    private void init() {

        webView = findViewById(R.id.wv_webview);

        mRlEnter = (RelativeLayout) findViewById(R.id.rl_enter);
        mProgressView = (ProgressView) findViewById(R.id.progress);

        //跳过的点击事件
        mRlEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterMain();
            }
        });

        //设置进度条颜色
        mProgressView.setPaintColor("#F17119");
        //开始倒计时
        mProgressView.startDownTime(7000, new OnFinishListener() {
            @Override
            public void onFinish() {
                enterMain();
            }
        });

        //启用支持JavaScript
         settings = webView.getSettings();

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
   /*     if (Build.VERSION.SDK_INT >= 19) {
            settings.setBlockNetworkImage(true);
        } else {
            settings.setLoadsImagesAutomatically(false);
        }*/



        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        settings.setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME;
            //  String cacheDirPath = getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;

        //设置数据库缓存路径
        settings.setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        settings.setAppCachePath(cacheDirPath);
        settings.setAppCacheMaxSize(20*1024*1024);
        //开启 Application Caches 功能
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);



        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        settings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(true);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(true);
        String ua = settings.getUserAgentString();// 获取默认的UA
        settings.setUserAgentString(ua + ";VcanbuyAndroid");// UA追加自定义标识符
        //开启JavaScript支持
        // 支持缩放
        settings.setSupportZoom(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        webView.setWebChromeClient(new WebChromeClient(){

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                mUploadCallBack = valueCallback;
                showFileChooser();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                mUploadCallBack = valueCallback;
                showFileChooser();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                mUploadCallBack = valueCallback;
                showFileChooser();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadCallBackAboveL = filePathCallback;
                showFileChooser();
                return true;
            }
        });


        webView.addJavascriptInterface(new MyJavascriptInterface(this), "injectedObject");


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
                if(url.indexOf(hotUrl) == 0||url.indexOf(hotUrl2) == 0){
                    titleBar.setVisibility(View.GONE);
                }else {
                    titleBar.setVisibility(View.VISIBLE);
                }
              super.onPageStarted(view,url,favicon);
            }

            //目的是要让我们应用自己来加载网页，而不是交给浏览器
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final Activity context = HomeActivity.this;
                LogUtil.i("---onReceiveValue---" + url);
                try {
                    if(url.startsWith("alipays:") || url.startsWith("alipay")) {
                        try {
                            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                        } catch (Exception e) {
                            new AlertDialog.Builder(context)
                                    .setMessage("未检测到支付宝客户端，请安装后重试。")
                                    .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                            context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                        }
                                    }).setNegativeButton("取消", null).show();
                        }
                        return true;
                    }
                    if(url.indexOf("https://web-pay.line.me")==0){
                          transactionReserveId=getUrlPramNameAndValue(url,"transactionReserveId");
                        view.loadUrl("https://web-pay.line.me/web/payment/waitPreLogin?transactionReserveId=="+transactionReserveId);
                        return true;
                    }


                    if(url.indexOf("vcanbuy://")==0){
                        String schemUrl=url.toString().replace("vcanbuy:","http:");
                        LogUtil.i("---onReceiveValue---3" + schemUrl);
                        view.loadUrl(schemUrl);
                    }
                    if(url.indexOf("intent://pay/payment")==0){
                        try {
                            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("line://pay/payment/"+transactionReserveId)));
                        } catch (Exception e) {
                            new AlertDialog.Builder(context)
                                    .setMessage("未检测到LINE客户端，请安装后重试。")
                                    .setNegativeButton("确定", null).show();
                        }
                        return true;
                    }

                    if(url.indexOf("intent://pay.airpay.in.th/pay")==0){

                        String type=getUrlPramNameAndValue(url,"type");
                        String app_id=getUrlPramNameAndValue(url,"app_id");
                        String key=getUrlPramNameAndValue(url,"key");
                        String order_id=getUrlPramNameAndValue(url,"order_id");
                        String return_url=getUrlPramNameAndValue(url,"return_url");
                        String source=getUrlPramNameAndValue(url,"source");
                        String newUrl="http://pay.airpay.in.th/pay?type="+type+"&app_id="+app_id
                                       +"&key="+key+"&order_id="+order_id+"&return_url="+return_url
                                       +"&source="+source;
                        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(newUrl)));
                        return true;
                    }


                    if (url.startsWith("http:") ||url.startsWith("https:")) {

                        if (url.indexOf("https://h5.m.taobao.com/awp/core/detail.htm") == 0) {
                            itemId= getUrlPramNameAndValue(url,"id");
                            view.loadUrl(hotUrl+"goodsDetailAgent/"+itemId+"?platform=4&urltb="+url);

                        }else if(url.indexOf("https://m.1688.com/offer/") == 0) {

                            if(url.indexOf("offerId") == 0){
                                itemId= getUrlPramNameAndValue(url,"offerId");
                            }else if(url.indexOf("itemId") == 0){
                                itemId= getUrlPramNameAndValue(url,"itemId");
                            }else {
                                itemId=url.substring(25,37);
                            }
                            view.loadUrl(hotUrl+"goodsDetailAgent/"+itemId+"?platform=3");
                        }else if(url.indexOf("https://detail.tmall.com/item.htm") == 0||url.indexOf("https://item.taobao.com/item.htm") == 0) {
                            itemId= getUrlPramNameAndValue(url,"id");
                            view.loadUrl(hotUrl+"goodsDetailAgent/"+itemId+"?platform=4&urltb="+url);
                        }else if(url.indexOf("https://detail.1688.com/offer") == 0) {
                            itemId= readValueFromUrlStrByParamName(url,"object_id");
                            view.loadUrl(hotUrl+"goodsDetailAgent/"+itemId+"?platform=3");
                        }else if(url.indexOf("https://i.click.taobao.com") == 0){
                            isTwoBack=true;
                            return super.shouldOverrideUrlLoading(view, url);
                        }else {
                            view.loadUrl(url);
                            return true;
                        }
                    }
                }
                catch (Exception e){
                    return false;
                }


                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    GangUpInvite(webView);
                }
                isFirst=false;
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                isFinish=true;
           /*     if(loadingDialog!=null){
                    loadingDialog.dismiss();
                }*/
                if (Build.VERSION.SDK_INT >= 19) {
                    settings.setBlockNetworkImage(false);
                } else {
                    settings.setLoadsImagesAutomatically(true);
                }
            }

        });

        if(data!=null){

            String schemUrl=   data.toString().replace("vcanbuy","http");
            Toast.makeText(this, schemUrl, Toast.LENGTH_SHORT).show();
            LogUtil.i("---onReceiveValue---1" + url);
            webView.loadUrl(schemUrl);
        }else {
            LogUtil.i("---onReceiveValue---2" + url);
            webView.loadUrl(url);
        }


    }
    public static void launchAppDetail(Context context, String appPkg) {	//appPkg 是应用的包名
        final String GOOGLE_PLAY = "com.android.vending";//这里对应的是谷歌商店，跳转别的商店改成对应的即可
        try {
            if (TextUtils.isEmpty(appPkg))
                return;
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(GOOGLE_PLAY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }


    public boolean  verifyStoragePermissions(Activity activity) {
        boolean isOK=true;
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                isOK=false;
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }else {

                //检测是否有写的权限
                if(ActivityCompat.checkSelfPermission(activity,"android.permission.CAMERA")!= PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,"android.permission.CAMERA")){
                        ActivityCompat.requestPermissions(activity, new String[]{"android.permission.CAMERA"},REQUEST_EXTERNAL_STORAGE);
                    }else {
                        ActivityCompat.requestPermissions(activity, new String[]{"android.permission.CAMERA"},REQUEST_EXTERNAL_STORAGE);
                    }
                    isOK=false;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!isOK){
            clearUploadMessage();
        }

        return isOK;
    }


    private void showFileChooser() {

      if(verifyStoragePermissions(this)){
          Intent intent1 = new Intent(Intent.ACTION_PICK, null);
          intent1.setDataAndType(
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
//        intent1.addCategory(Intent.CATEGORY_OPENABLE);
//        intent1.setType("image/*");

          Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
              mCameraFilePath = this.getExternalFilesDir("") + File.separator + "Media" + File.separator+System.currentTimeMillis() + ".jpg";
          }else {
              mCameraFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                      System.currentTimeMillis() + ".jpg";
          }


          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
              // android7.0注意uri的获取方式改变
              Uri photoOutputUri = FileProvider.getUriForFile(
                      HomeActivity.this,
                      BuildConfig.APPLICATION_ID + ".FileProvider",
                      new File(mCameraFilePath));
              intent2.putExtra(MediaStore.EXTRA_OUTPUT, photoOutputUri);
          } else {
              intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
          }

          Intent chooser = new Intent(Intent.ACTION_CHOOSER);
          chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
          chooser.putExtra(Intent.EXTRA_INTENT,intent1);
          chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent2});
          startActivityForResult(chooser, REQUEST_CODE_FILE_CHOOSER);
      }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            // camear 权限回调

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // 表示用户授权
              //  Toast.makeText(this, " user Permission" , Toast.LENGTH_SHORT).show();
            } else {
                //用户拒绝权限
                Toast.makeText(this, "กรุณาเปิดกล้องและสิทธิ์การจัดเก็บ" , Toast.LENGTH_SHORT).show();

            }



        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            // 压缩到多少宽度以内
            int maxW = 1000;
            // 压缩到多少大小以内,1024kb
            int maxSize = 1024;
            if (result == null) {
                // 看是否从相机返回
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile!=null&&cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            if (result != null) {
                // 根据uri获取路径
                String path = FileUtils.getPath(this, result);
                if (!TextUtils.isEmpty(path)) {
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        // 按大小和尺寸压缩图片
                        Bitmap b = getCompressBitmap(path, maxW, maxW, maxSize);
                        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                        String compressPath = basePath + File.separator + "photos" + File.separator
                                + System.currentTimeMillis() + ".jpg";

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            compressPath = this.getExternalFilesDir("") + File.separator + "Media" + File.separator+System.currentTimeMillis() + ".jpg";
                        }
                        // 压缩完保存在文件里
                        if (saveBitmapToFile(b, compressPath)) {
                            Uri newUri = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                newUri = FileProvider.getUriForFile(
                                        HomeActivity.this,
                                        BuildConfig.APPLICATION_ID + ".FileProvider",
                                        new File(compressPath));
                            } else {
                                newUri = Uri.fromFile(new File(compressPath));
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (mUploadCallBackAboveL != null) {
                                    if (newUri != null) {
                                        mUploadCallBackAboveL.onReceiveValue(new Uri[]{newUri});
                                        mUploadCallBackAboveL = null;
                                        return;
                                    }

                                }
                            } else if (mUploadCallBack != null) {
                                if (newUri != null) {
                                    mUploadCallBack.onReceiveValue(newUri);
                                    mUploadCallBack = null;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            clearUploadMessage();
            return;
        }
    }

    /**
     * webview没有选择图片也要传null，防止下次无法执行
     */
    private void clearUploadMessage(){
        if (mUploadCallBackAboveL != null) {
            mUploadCallBackAboveL.onReceiveValue(null);
            mUploadCallBackAboveL = null;
        }
        if (mUploadCallBack != null) {
            mUploadCallBack.onReceiveValue(null);
            mUploadCallBack = null;
        }
    }

    /**
     * 根据路径获取bitmap（压缩后）
     *
     * @param srcPath 图片路径
     * @param width   最大宽（压缩完可能会大于这个，这边只是作为大概限制，避免内存溢出）
     * @param height  最大高（压缩完可能会大于这个，这边只是作为大概限制，避免内存溢出）
     * @param size    图片大小，单位kb
     * @return 返回压缩后的bitmap
     */
    public static Bitmap getCompressBitmap(String srcPath, float width, float height, int size) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int scaleW = (int) (w / width);
        int scaleH = (int) (h / height);
        int scale = scaleW < scaleH ? scaleH : scaleW;
        if (scale <= 1) {
            scale = 1;
        }
        newOpts.inSampleSize = scale;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        // 压缩好比例大小后再进行质量压缩
        return compressImage(bitmap, size);
    }

    /**
     * 图片质量压缩
     *
     * @param image 传入的bitmap
     * @param size  压缩到多大，单位kb
     * @return 返回压缩完的bitmap
     */
    public static Bitmap compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于size,大于继续压缩
        while (baos.toByteArray().length / 1024 > size) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            options -= 10;
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * bitmap保存为文件
     *
     * @param bm       bitmap
     * @param filePath 文件路径
     * @return 返回保存结果 true：成功，false：失败
     */
    public static boolean saveBitmapToFile(Bitmap bm, String filePath) {
        try {
            File file = new File(filePath);
            file.deleteOnExit();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            boolean b = false;
            if (filePath.toLowerCase().endsWith(".png")) {
                b = bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
            } else {
                b = bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            }
            bos.flush();
            bos.close();
            return b;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return false;
    }
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
  /*      if(loadingDialog!=null){
            loadingDialog.dismiss();
            loadingDialog = null;
        }*/

        super.onDestroy();

    }

    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Toast.makeText(this, webView.getUrl(), Toast.LENGTH_SHORT).show();
            if (webView.canGoBack()) {
                webView.goBack();//返回上一页面
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {
            System.exit(0);//退出程序
            super.onBackPressed();
        } else {
            Toast toast=Toast.makeText(HomeActivity.this,"คลิกอีกครั้งเพื่อออกจากโปรแกรม",Toast.LENGTH_SHORT    );
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        if(!isFirst){
            this.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    GangUpInvite(webView);
                }});

        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(getIntent().getData()!=null){
            if(getIntent().getData().getScheme().equals("vcanbuy")){
                String schemUrl= getIntent().getData().toString().replace("vcanbuy","http");
                webView.loadUrl(schemUrl);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void GangUpInvite(WebView webView) {

        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        if (data != null && data.getItemCount() > 0) {
            ClipData.Item item = data.getItemAt(0);

            if (item != null && item.getText() != null) {

                webView.evaluateJavascript("javascript:onClipboardListener('" + item.getText().toString().replaceAll("[\\s*\t\r\n]", "") + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        LogUtil.i("---onReceiveValue---" + value);
                    }
                });
            }
            cm.setPrimaryClip(ClipData.newPlainText(null, ""));

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void callJS(WebView webView, String function, String param) {
        JSUtil.callJS(webView, function, new String[]{param});
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    /**
     * 获取URL中的参数名和参数值的Map集合
     * @param url
     * @return
     */
    private String getUrlPramNameAndValue(String url, String name) {
        String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(url);
        String value="";
        //   Map<String, String> paramMap = new LinkedHashMap<String, String>();
        while (m.find()) {
            String paramName = m.group(2);//获取参数名
            String paramVal = m.group(3);//获取参数值
            if(paramName.equals(name)){
                value=paramVal;
                break;
            }
            // paramMap.put(paramName, paramVal);
        }
        return value;
    }
    /**
     * @param urlStr    url字符串。
     * @param paramName 指定参数名称。
     * @return NA.
     * @desc 获取url中指定参数的值。
     */
    public static String readValueFromUrlStrByParamName(String urlStr, String paramName) {
        if (urlStr != null && urlStr.length() > 0) {
            int index = urlStr.indexOf("?");
            String temp = (index >= 0 ? urlStr.substring(index + 1) : urlStr);
            String[] keyValue = temp.split("&");
            String destPrefix = paramName + "=";
            for (String str : keyValue) {
                if (str.indexOf(destPrefix) == 0) {
                    return str.substring(destPrefix.length());
                }
            }
        }
        return null;
    }

    /**
     * Created by jingbin on 2016/11/17.
     * js通信接口
     */
    public class MyJavascriptInterface {
        private Context context;

        public MyJavascriptInterface(Context context) {
            this.context = context;
        }




        /**
         * 网页使用的js，方法无参数
         */
        @JavascriptInterface
        public String getRegistationId() {

            if(registationId==null||registationId.equals("")){
                registationId = JPushInterface.getRegistrationID(HomeActivity.this);
                if(registationId==null){
                    SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
                    registationId=sharedPre.getString("registationId", "");
                }
                saveRegistationId(HomeActivity.this,registationId);
            }
            return registationId;
        }

    }
    private void enterMain(){
                imageView.setVisibility(View.GONE);
                mRlEnter.setVisibility(View.GONE);
           /*     if (!isFinishing()&&!isFinish) {
                    loadingDialog = LoadingDialog.getInstance(HomeActivity.this);
                    loadingDialog.show();
                }*/
    }

//
//    public void getGuideImage(){
//        String url =hotUrl+ "gateway/indexarea/get_index";
//        OkHttpUtils
//                .get()
//                .url(url)
//                .addParams("product", "2")
//                .addParams("position", "guide")
//                .addParams("page", "1")
//                .addParams("page_size", "5")
//                .build()
//                .execute(new StringCallback()
//                {
//                    @Override
//                    public void onError(Request request, Exception e)
//                    {
//
//                    }
//
//                    @Override
//                    public void onResponse(String response)
//                    {
//
//                        Gson gson = new Gson();
//                        GuideBean guideBean = gson.fromJson(response, GuideBean.class);
//
//                        for(int i=0;i<guideBean.getDatalist().size();i++){
//                            if(guideBean.getDatalist().get(i).getStatus()==1){
//                                //可以创建一个新的SharedPreference来对储存的文件进行操作
//                                SharedPreferences sp=getSharedPreferences("guide", Context.MODE_PRIVATE);
//                                //像SharedPreference中写入数据需要使用Editor
//                                SharedPreferences.Editor editor = sp.edit();
//                                //类似键值对
//                                editor.putString("imageUrl", guideBean.getDatalist().get(i).getContent());
//                                editor.commit();
//                            }
//                           // ImageUrlBean  imageUrlBean=gson.fromJson(guideBean.getDatalist().get(i).getContent(), ImageUrlBean.class);
//                        }
//                        getFlashScreenImage();
//
//                    }
//                });
//    }


/*    public void getFlashScreenImage(){
        String url =hotUrl+ "gateway/indexarea/get_index";
        OkHttpUtils
                .get()
                .url(url)
                .addParams("product", "2")
                .addParams("position", "flashScreen")
                .addParams("page", "1")
                .addParams("page_size", "5")
                .build()
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Request request, Exception e)
                    {

                    }

                    @Override
                    public void onResponse(String response)
                    {

                        Gson gson = new Gson();
                        GuideBean guideBean = gson.fromJson(response, GuideBean.class);

                        for(int i=0;i<guideBean.getDatalist().size();i++){
                            if(guideBean.getDatalist().get(i).getStatus()==1){
                                //可以创建一个新的SharedPreference来对储存的文件进行操作
                                SharedPreferences sp=getSharedPreferences("flashScreen", Context.MODE_PRIVATE);
                                //像SharedPreference中写入数据需要使用Editor
                                SharedPreferences.Editor editor = sp.edit();
                                //类似键值对
                                editor.putString("imageUrl", guideBean.getDatalist().get(i).getContent());
                                editor.commit();
                            }
                            // ImageUrlBean  imageUrlBean=gson.fromJson(guideBean.getDatalist().get(i).getContent(), ImageUrlBean.class);
                        }

                    }
                });
    }*/

}
