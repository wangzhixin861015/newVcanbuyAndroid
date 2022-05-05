package com.vcb.vcb.photo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vcb.vcb.HomeActivity;
import com.vcb.vcb.R;
import com.vcb.vcb.bean.SouBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Jay
 * @date 2020/12/2
 * File description.
 */
public class Customcamera extends AppCompatActivity implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private int cameraId = 0;//声明cameraId属性，ID为1调用前置摄像头，为0调用后置摄像头。此处因有特殊需要故调用前置摄像头
    private Button btn_cancel_aca, btn_ok_aca, btn_photo_aca,btn_photo_album,btn_ok_help;
    // 0表示后置，1表示前置
    private int cameraPosition = 1;
    //定义照片保存并显示的方法
    private Camera.PictureCallback mpictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String mFilePath = Environment.getExternalStorageDirectory().getPath();
            // 保存图片的文件名
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            mFilePath = mFilePath + "/" + simpleDateFormat.format(date) + "mytest.jpg";
            File tempfile = new File(mFilePath);//新建一个文件对象tempfile，并保存在某路径中
            try {
                FileOutputStream fos = new FileOutputStream(tempfile);
                fos.write(data);//将照片放入文件中
                fos.close();//关闭文件
                Intent intent = new Intent(Customcamera.this, CameraReaultActivity.class);//新建信使对象
                intent.putExtra("picpath", mFilePath);//打包文件给信使
                startActivityForResult(intent,1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cautomcamera);
        initView();//代码初始化
        setClick();//设置点击方法
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

    }

    /**
     * 设置点击方法
     */
    private void setClick() {
        mPreview.setOnClickListener(new CustomcameraClick());
        btn_cancel_aca.setOnClickListener(new CustomcameraClick());
        btn_photo_album.setOnClickListener(new CustomcameraClick());
        btn_ok_aca.setOnClickListener(new CustomcameraClick());
        btn_photo_aca.setOnClickListener(new CustomcameraClick());
        btn_ok_help.setOnClickListener(new CustomcameraClick());
    }

    /**
     * 代码初始化
     */
    private void initView() {
        mPreview = findViewById(R.id.preview);//初始化预览界面
        btn_cancel_aca = findViewById(R.id.btn_cancel_aca);//取消按钮
        btn_photo_album=findViewById(R.id.btn_photo_album);//相册
        btn_ok_aca = findViewById(R.id.btn_ok_aca);//切换
        btn_photo_aca = findViewById(R.id.btn_photo_aca);//拍照按钮
        btn_ok_help=findViewById(R.id.btn_ok_help);
    }

    //定义“拍照”方法
    public void takePhoto() {
        //配置如下：
        Camera.Parameters parameters = mCamera.getParameters();// 获取相机参数集
        List<Camera.Size> SupportedPreviewSizes =
                parameters.getSupportedPreviewSizes();// 获取支持预览照片的尺寸
        Camera.Size previewSize = SupportedPreviewSizes.get(0);// 从List取出Size
        parameters.setPreviewSize(previewSize.width, previewSize.height);//
        //  设置预览照片的大小
        List<Camera.Size> supportedPictureSizes =
                parameters.getSupportedPictureSizes();// 获取支持保存图片的尺寸
        Camera.Size pictureSize = supportedPictureSizes.get(0);// 从List取出Size
        parameters.setPictureSize(pictureSize.width, pictureSize.height);//
        // 自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        // 设置照片的大小
        mCamera.setParameters(parameters);

        //摄像头聚焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, mpictureCallback);
                }
            }
        });

    }

    //activity生命周期在onResume是界面应是显示状态
    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {//如果此时摄像头值仍为空
            mCamera = getCamera();//则通过getCamera()方法开启摄像头

            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);//开启预览界面
            }
        }
    }

    //activity暂停的时候释放摄像头
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    //onResume()中提到的开启摄像头的方法
    private Camera getCamera() {
        Camera camera;//声明局部变量camera
        try {
            camera = Camera.open(cameraId);
        }//根据cameraId的设置打开前置摄像头
        catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }




    //开启预览界面
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {

            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);//如果没有这行你看到的预览界面就会是水平的
            camera.startPreview();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定义释放摄像头的方法
    private void releaseCamera() {
        if (mCamera != null) {//如果摄像头还未释放，则执行下面代码
            mCamera.stopPreview();//1.首先停止预览
            mCamera.setPreviewCallback(null);//2.预览返回值为null
            mCamera.release(); //3.释放摄像头
            mCamera = null;//4.摄像头对象值为null
        }
    }

    //定义新建预览界面的方法
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();//如果预览界面改变，则首先停止预览界面
        setStartPreview(mCamera, mHolder);//调整再重新打开预览界面
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();//预览界面销毁则释放相机
    }
    public static final int PICK_PHOTO = 102;
    private class CustomcameraClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.preview://点击预览界面聚焦
                    mCamera.autoFocus(null);
                    break;
                case R.id.btn_cancel_aca://取消按钮
                    finish();
                    break;
                case R.id.btn_photo_album://取消按钮

                    //动态申请获取访问 读写磁盘的权限
                    if (ContextCompat.checkSelfPermission(Customcamera.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Customcamera.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    } else {
                        //打开相册
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        //Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_PHOTO); // 打开相册
                    }

                    break;
                case R.id.btn_ok_aca:
                    // 切换前后摄像头
                    int cameraCount = 0;
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

                    for (int i = 0; i < cameraCount; i++) {
                        Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
                        if (cameraPosition == 1) {
                            // 现在是后置，变更为前置
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                /**
                                 * 记得释放camera，方便其他应用调用
                                 */
                                releaseCamera();
                                // 打开当前选中的摄像头
                                mCamera = Camera.open(i);
                                // 通过surfaceview显示取景画面
                                setStartPreview(mCamera,mHolder);
                                cameraPosition = 0;
                                handler.sendEmptyMessageDelayed(0,5000);
                                break;
                            }
                        } else {
                            // 现在是前置， 变更为后置
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                /**
                                 * 记得释放camera，方便其他应用调用
                                 */
                                releaseCamera();
                                mCamera = Camera.open(i);
                                setStartPreview(mCamera,mHolder);
                                cameraPosition = 1;
                                handler.sendEmptyMessageDelayed(0,5000);
                                break;
                            }
                        }

                    }

                break;
                case R.id.btn_photo_aca://拍照按钮
                    takePhoto();
                    break;
                case R.id.btn_ok_help:
                    setResult(-2);
                    finish();
                default:
                    break;
            }
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                    }
                }
            });

        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {

            case PICK_PHOTO:
                if (resultCode == RESULT_OK) { // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    }
                }

                break;
            case 1:
                if (resultCode == RESULT_OK) { // 判断手机系统版本号
                   String url=data.getStringExtra("url");
                    String value=data.getStringExtra("value");
                    boolean success=data.getBooleanExtra("success",true);
                    Intent intent=new Intent(Customcamera.this, HomeActivity.class) ;
                    intent.putExtra("url",url);
                    intent.putExtra("value",value);
                    intent.putExtra("success",success);
                    setResult(RESULT_OK,intent);
                    finish();
                }

                break;
            default:
                break;
        }
    }
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        // 根据图片路径显示图片
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private void displayImage(String imagePath) {

        if (imagePath != null) {
            Intent intent = new Intent(Customcamera.this, CameraReaultActivity.class);//新建信使对象
            intent.putExtra("picpath", imagePath);//打包文件给信使
            startActivityForResult(intent,1);
           // Customcamera.this.finish();//关闭现有界面
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }
}