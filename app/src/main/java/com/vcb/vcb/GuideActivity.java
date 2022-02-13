package com.vcb.vcb;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vcb.vcb.bean.ImageUrlBean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class GuideActivity extends AppCompatActivity {


    private static final String TAG = "GuideActivity";
    /**
     * 功能引导页
     */
    private ViewPager mVpGuide;

    /**
     * 功能引导页展示的 ImageView 集合
     */
    private List<ImageView> mImageList;

    private Button mBtnStart;
    private int number = 4;
    ImageUrlBean imageUrlBean;
    /**
     * 功能引导页展示的图片集合
     */
    private static int[] mImageIds = new int[]{R.mipmap.guide_1,
            R.mipmap.guide_2, R.mipmap.guide_3, R.mipmap.guide_4, R.mipmap.guide_5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();

        //mVpGuide.setAdapter(new GuideAdapter());
    }

    /**
     * 初始化页面
     */
    private void initView() {
        mVpGuide = (ViewPager) findViewById(R.id.vp_guide);
        mBtnStart = (Button) findViewById(R.id.btn_start);

        mImageList = new ArrayList<ImageView>();
        // 将要展示的 3 张图片存入 ImageView 集合中
        number = 5;
        for (int i = 0; i < mImageIds.length; i++) {
            ImageView image = new ImageView(this);
            // 将图片设置给对应的 ImageView
            //  image.setBackgroundResource(mImageIds[i]);
            BitmapFactory.Options opt = new BitmapFactory.Options();

            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

            opt.inPurgeable = true;

            opt.inInputShareable = true;

            InputStream is = getResources().openRawResource(

                    mImageIds[i]);

            Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

            Glide.with(this)
                    .load(bm)
                    .into(image);


            mImageList.add(image);
        }


        mVpGuide.setAdapter(new GuideAdapter());
        mVpGuide.setOnPageChangeListener(new GuidePageChangeListener());

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 按钮一旦被点击，更新 SharedPreferences
                PrefUtils.setBoolean(GuideActivity.this, PrefUtils.GUIDE_SHOWED, true);
                // 跳转到主页面
                startActivity(new Intent(GuideActivity.this, HomeActivity.class));
                finish();
            }
        });

    }

    /**
     * 适配器
     */
    class GuideAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mImageList.get(position));
            return mImageList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mImageList.get(position));
        }
    }

    /**
     * 滑动监听
     */
    class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        // 页面滑动时回调此方法
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        // 某个页面被选中时回调此方法
        @Override
        public void onPageSelected(int position) {
            // 如果是最后一个页面，按钮可见，否则不可见
            if (position == number - 1) {
                mBtnStart.setVisibility(View.VISIBLE);
            } else {
                mBtnStart.setVisibility(View.INVISIBLE);
            }
        }
    }

}
