package com.example.puzzle.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puzzle.R;
import com.example.puzzle.adapter.GridItemsAdapter;
import com.example.puzzle.bean.ItemBean;
import com.example.puzzle.utils.GameUtil;
import com.example.puzzle.utils.ImageUtil;
import com.example.puzzle.utils.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PuzzleActivity extends AppCompatActivity implements View.OnClickListener {
    // 拼图完成时显示的最后一个图片
    public static Bitmap mLastBitmap;
    // 设置为N×N显示
    public static int mType = 2;
    // 步数显示
    private int mStepCounts;
    // 计时显示
    private int mTimeCounts;
    // UI更新Handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    // 更新计时器
                    mTimeCounts++;
                    mTvTimeCounts.setText(mTimeCounts + "");
                    break;
                default:
                    break;
            }
        }
    };
    // 选择的图片
    private Bitmap mPicSelected;
    // 本地图库照片或拍照照片的路径
    private String mPicPath;
    // 拼图GridView
    private GridView mGvPuzzle;
    // 显示原图的ImageView
    private ImageView mImageView;
    // 原图、重置、返回三个按钮
    private Button mBtnPic;
    private Button mBtnReset;
    private Button mBtnBack;
    // 显示步数
    private TextView mTvStepCounts;
    // 计时器
    private TextView mTvTimeCounts;
    // 切图后的图片
    private List<Bitmap> mBitmapItemList = new ArrayList<>();
    // GridView适配器
    private GridItemsAdapter mAdapter;
    // flag 是否已显示原图
    private boolean mIsShowing;
    // 计时器类
    private Timer mTimer;
    // 计时器线程
    private TimerTask mTimerTask;
    // 权限是否申请成功
    private boolean mPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        // 获取权限是否申请成功
        mPermissionGranted = getIntent().getBooleanExtra("permissionGranted", false);
        // 获取选择的图片
        int picSelectedId = getIntent().getIntExtra("picSelectedId", -1);
        mPicPath = getIntent().getStringExtra("picPath");
        if(picSelectedId != -1) {
            // 选择自带图片
            mPicSelected = BitmapFactory.decodeResource(getResources(), picSelectedId);
        } else {
            // 选择本地图库照片或者拍照照片
            if(mPermissionGranted) {
                mPicSelected = BitmapFactory.decodeFile(mPicPath);
            } else {
                mPicSelected = BitmapFactory.decodeResource(getResources(), R.drawable.blank);
            }
        }
        // 获取选择的种类
        mType = getIntent().getIntExtra("type", 2);
        // 对图片处理
        handleImage(mPicSelected);
        // 初始化Views
        initViews();
        // 生成游戏数据
        generateGame();
        // GridView点击事件
        mGvPuzzle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 判断是否可以移动
                if(GameUtil.isMovable(position)) {
                    // 交换点击Item与空格的位置
                    GameUtil.swapItems(GameUtil.itemBeanList.get(position), GameUtil.blankItemBean);;
                    // 重新获取图片
                    recreateData();
                    // 通知GridView更新UI
                    mAdapter.notifyDataSetChanged();
                    // 更新步数
                    mStepCounts++;
                    mTvStepCounts.setText(mStepCounts + "");
                    // 判断是否成功
                    if(GameUtil.isSuccess()) {
                        // 补充最后一张图
                        mBitmapItemList.remove(mType * mType - 1);
                        mBitmapItemList.add(mLastBitmap);
                        // 通知GridView更新UI
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(PuzzleActivity.this, "拼图成功！", Toast.LENGTH_SHORT).show();
                        mGvPuzzle.setEnabled(false);
                        mTimer.cancel();
                        mTimerTask.cancel();
                    }
                }
            }
        });
        // 按钮点击事件
        mBtnPic.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
    }

    /**
     * 重新获取图片
     */
    private void recreateData() {
        mBitmapItemList.clear();
        for (ItemBean itemBean : GameUtil.itemBeanList) {
            mBitmapItemList.add(itemBean.getBitmap());
        }
    }

    /**
     * 生成游戏数据
     */
    private void generateGame() {
        // 切图 获得初始拼图数据 正常顺序
        ImageUtil.createInitBitmaps(this, mPicSelected, mType);
        // 生成随机数据
        GameUtil.getPuzzleGenerator();
        // 获取Bitmap集合
        for (ItemBean itemBean : GameUtil.itemBeanList) {
            mBitmapItemList.add(itemBean.getBitmap());
        }
        // 数据适配器
        mAdapter = new GridItemsAdapter(this, mBitmapItemList);
        mGvPuzzle.setAdapter(mAdapter);
        // 启用计时器
        mTimer = new Timer(true);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        // Button
        mBtnPic = (Button) findViewById(R.id.id_btn_pic);
        mBtnReset = (Button) findViewById(R.id.id_btn_reset);
        mBtnBack = (Button) findViewById(R.id.id_btn_back);
        // flag 是否已显示原图
        mIsShowing = false;
        // GridView
        mGvPuzzle = (GridView) findViewById(R.id.id_gv_puzzle);
        // 设置成N*N显示
        mGvPuzzle.setNumColumns(mType);
        // 设置布局参数
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mPicSelected.getWidth(),
                mPicSelected.getHeight());
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW, R.id.id_ll_tvs);
        mGvPuzzle.setLayoutParams(params);
        // 设置GridView的item之间无间隔
        mGvPuzzle.setHorizontalSpacing(0);
        mGvPuzzle.setVerticalSpacing(0);
        // Tv步数
        mTvStepCounts = (TextView) findViewById(R.id.id_tv_step);
        mTvStepCounts.setText(mStepCounts + "");
        // Tv计时器
        mTvTimeCounts = (TextView) findViewById(R.id.id_tv_time);
        mTvTimeCounts.setText(mTimeCounts + "");
        // 添加显示原图的View
        addImgView();
    }

    /**
     * 添加显示原图的View
     */
    private void addImgView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.id_rl_puzzle);
        mImageView = new ImageView(this);
        mImageView.setImageBitmap(mPicSelected);
        int width = (int) (mPicSelected.getWidth() * 0.9f);
        int height = (int) (mPicSelected.getHeight() * 0.9f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImageView.setLayoutParams(params);
        relativeLayout.addView(mImageView);
        mImageView.setVisibility(View.GONE);
    }

    /**
     * 处理图片到合适尺寸
     */
    private void handleImage(Bitmap bitmap) {
        // 将图片放大到合适尺寸
        int screenWidth = ScreenUtil.getScreenSize(this).widthPixels;
        int screenHeight = ScreenUtil.getScreenSize(this).heightPixels;
        mPicSelected = ImageUtil.resizeBitmap(screenWidth * 0.8f, screenHeight * 0.6f, bitmap);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.id_btn_pic:
                Animation animShow = AnimationUtils.loadAnimation(this, R.anim.pic_show_anim);
                Animation animHide = AnimationUtils.loadAnimation(this, R.anim.pic_hide_anim);
                if(mIsShowing) {
                    mImageView.startAnimation(animHide);
                    mImageView.setVisibility(View.GONE);
                    mIsShowing = false;
                } else {
                    mImageView.startAnimation(animShow);
                    mImageView.setVisibility(View.VISIBLE);
                    mIsShowing = true;
                }
                break;
            case R.id.id_btn_reset:
                // 清除相关参数设置
                cleanConfig();
                // 重新生成打乱顺序的拼图块
                generateGame();
                // 重建mBitmapItemList
                recreateData();
                mTvTimeCounts.setText(mTimeCounts + "");
                mTvStepCounts.setText(mStepCounts + "");
                mAdapter.notifyDataSetChanged();
                mGvPuzzle.setEnabled(true);
                break;
            case R.id.id_btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 清空相关参数设置
     */
    private void cleanConfig() {
        // 清空相关参数设置
        GameUtil.itemBeanList.clear();
        // 停止计时器
        mTimer.cancel();
        mTimerTask.cancel();
        mStepCounts = 0;
        mTimeCounts = 0;
        // 清除拍摄的照片
        if(mPicPath != null) {
            // 删除照片
            File file = new File(MainActivity.TEMP_IMAGE_PATH);
            if(file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空相关参数设置
        cleanConfig();
    }
}
