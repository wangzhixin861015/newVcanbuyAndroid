package com.vcb.vcb.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.vcb.vcb.LoadingDialog;
import com.vcb.vcb.R;
import com.vcb.vcb.bean.SouBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Jay
 * @date 2020/12/2
 * File description.
 * 显示照相机结果的界面
 */
public class CameraReaultActivity extends AppCompatActivity {
    private Button btn_again_act, btn_userphoto_act;
    private ImageView iv_photo_act;
    private String mFilePath = "";
    String firestrUrl = "http://47.118.71.175/cgi/upload/file/misc/image";
    private File compressedImage1File;
    LoadingDialog loadingDialog = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameraresult);
        initView();//代码初始化
        getInfo();//获取上一个界面传递过来的信息
        setClick();//设置点击方法
    }

    /**
     * 设置点击方法
     */
    private void setClick() {
        btn_again_act.setOnClickListener(new CameraResultClick());
        btn_userphoto_act.setOnClickListener(new CameraResultClick());
    }

    /**
     * 获取上一个界面传递过来的信息
     */
    private void getInfo() {
        mFilePath = getIntent().getStringExtra("picpath");//通过值"picpath"得到照片路径
        try {
            FileInputStream fis = new FileInputStream(mFilePath);//通过path把照片读到文件输入流中
            Bitmap bitmap = BitmapFactory.decodeStream(fis);//将输入流解码为bitmap
            Matrix matrix = new Matrix();//新建一个矩阵对象
            matrix.setRotate(90);//矩阵旋转操作让照片可以正对着你。但是还存在一个左右对称的问题
//新建位图，第2个参数至第5个参数表示位图的大小，matrix中是旋转后的位图信息，并使bitmap变量指向新的位图对象
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            iv_photo_act.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 代码初始化
     */
    private void initView() {
        btn_again_act = findViewById(R.id.btn_again_act);//重拍按钮
        btn_userphoto_act = findViewById(R.id.btn_userphoto_act);//使用按钮
        iv_photo_act = findViewById(R.id.iv_photo_act);//展示图片的控件
    }
    // 主线程
    Handler handler = new Handler();
    // 我的返回数据组成为　data，return_code mesg
    public void httpsPostImgRequest(final String url , File file, String imagpath){
        try {

            // OkHttpClient okHttpClient = new OkHttpClient();
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(50000, TimeUnit.MILLISECONDS)
                    .readTimeout(50000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody image=RequestBody.create(MediaType.parse("multipart/form-data"),file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    // 此处可添加上传 参数
                    // photoFile 表示上传参数名,logo.png 表示图片名字
                    //     .addFormDataPart("json",json)//参数以json的格式传输
                    .addFormDataPart("file",imagpath,image)//第一个参数是服务端接收的标记，第二个是路径，第三个是请求体信息
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = okHttpClient.newCall(request).execute();

            String requestData = response.body().string();
            // 信息打印 是否是服务器返回的数据 ， 默认即便是空值，对象也存在值为""
            Gson gson = new Gson();
            final SouBean souBean=gson.fromJson(requestData, SouBean.class);


            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(souBean.isSuccess()){

                        Toast.makeText(CameraReaultActivity.this, "上传图片成功！URL="+souBean.getData().getUrl(), Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(CameraReaultActivity.this, Customcamera.class) ;
                        intent.putExtra("url",souBean.getData().getUrl());
                        intent.putExtra("value",souBean.getData().getValue());
                        intent.putExtra("success",souBean.isSuccess());
                        setResult(RESULT_OK,intent);
                        finish();
                    }else {
                        Toast.makeText(CameraReaultActivity.this, "上传图片失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });




        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public void onDestroy() {
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
    /**
     * 设置点击方法
     */
    private class CameraResultClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_again_act://重拍
                    startActivity(new Intent(CameraReaultActivity.this, Customcamera.class));
                    finish();
                    break;
                case R.id.btn_userphoto_act://使用照片


                        compressedImage1File = new File(mFilePath);
                    // String param = compressedImage1File.getPath();
//                                Bitmap bitmap= BitmapFactory.decodeFile(param);
//                                imageView.setImageBitmap(bitmap);
                    Log.i("lgq","wj======="+compressedImage1File);
                    // Android 4.0 之后不能在主线程中请求HTTP请求
                    loadingDialog = LoadingDialog.getInstance(CameraReaultActivity.this);
                    loadingDialog.show();
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            httpsPostImgRequest(firestrUrl,compressedImage1File,mFilePath);
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }
    }
}