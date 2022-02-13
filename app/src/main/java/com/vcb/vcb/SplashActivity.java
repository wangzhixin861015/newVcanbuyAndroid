package com.vcb.vcb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.vcb.vcb.bean.FlashScreenUrlBean;
import com.vcb.vcb.bean.ImageUrlBean;

import androidx.appcompat.app.AppCompatActivity;
import cn.jpush.android.api.JPushInterface;


public class SplashActivity extends AppCompatActivity {

    /**
     * 闪屏页
     */
    private RelativeLayout rlSplash;
    String registationId;
   // private String hotUrl = "http://120.27.228.29:8081/";
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        rlSplash = findViewById(R.id.rl_splash);
        image=findViewById(R.id.image);
        Glide.with(this)
                .load(R.drawable.ic_splash)
                .into(image);

        startAnim();
        JPushInterface.init(getApplicationContext());
        registationId = JPushInterface.getRegistrationID(this);
        saveRegistationId(SplashActivity.this,registationId);
        //getFlashScreenImage();

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

//    public void getFlashScreenImage(){
//        String url =hotUrl+ "gateway/indexarea/get_index";
//        OkHttpUtils
//                .get()
//                .url(url)
//                .addParams("product", "2")
//                .addParams("position", "flashScreen")
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
//                                SharedPreferences sp=getSharedPreferences("flashScreen", Context.MODE_PRIVATE);
//                                //像SharedPreference中写入数据需要使用Editor
//                                SharedPreferences.Editor editor = sp.edit();
//                                //类似键值对
//                                editor.putString("imageUrl", guideBean.getDatalist().get(i).getContent());
//                                editor.commit();
//                            }
//                            // ImageUrlBean  imageUrlBean=gson.fromJson(guideBean.getDatalist().get(i).getContent(), ImageUrlBean.class);
//                        }
//
//                    }
//                });
//    }
    /**
     * 启动动画
     */
    private void startAnim() {
        // 渐变动画,从完全透明到完全不透明
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        // 持续时间 2 秒
        alpha.setDuration(2000);
        // 动画结束后，保持动画状态
        alpha.setFillAfter(true);

        // 设置动画监听器
        alpha.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            // 动画结束时回调此方法
            @Override
            public void onAnimationEnd(Animation animation) {
                // 跳转到下一个页面
                jumpNextPage();
            }
        });

        // 启动动画
        rlSplash.startAnimation(alpha);
    }

    /**
     * 跳转到下一个页面
     */
    private void jumpNextPage() {


        SharedPreferences sp=getSharedPreferences("guide", Context.MODE_PRIVATE);
        //第一个参数是键名，第二个是默认值
        String imageUrl=sp.getString("imageUrl", "");
        if(imageUrl!=null&&!imageUrl.equals("")){
            Gson gson = new Gson();
            ImageUrlBean imageUrlBean=gson.fromJson(imageUrl, ImageUrlBean.class);
            for(int i=0;i<imageUrlBean.getImageUrl().size();i++){
                Glide.with(this)
                        .load(imageUrlBean.getImageUrl().get(i).getUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }

        SharedPreferences flashScreenSp=getSharedPreferences("flashScreen", Context.MODE_PRIVATE);
        //第一个参数是键名，第二个是默认值
        String flashScreenImageUrl=flashScreenSp.getString("imageUrl", "");
        if(flashScreenImageUrl!=null&&!flashScreenImageUrl.equals("")){
            Gson gson = new Gson();
            FlashScreenUrlBean flashScreenUrlBean=gson.fromJson(flashScreenImageUrl, FlashScreenUrlBean.class);
            Glide.with(this)
                    .load(flashScreenUrlBean.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
        }

        // 判断之前有没有展示过功能引导
        boolean guideShowed = PrefUtils.getBoolean(SplashActivity.this,
                PrefUtils.GUIDE_SHOWED, false);
        if (!guideShowed) {
            // 跳转到功能引导页
            startActivity(new Intent(SplashActivity.this, GuideActivity.class));
        } else {
            // 跳转到主页面
           startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        }

        finish();
    }

}
