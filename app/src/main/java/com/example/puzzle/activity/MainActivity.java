package com.example.puzzle.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puzzle.R;
import com.example.puzzle.adapter.GridPicListAdapter;
import com.example.puzzle.utils.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // GridView显示图片
    private GridView mGvPicList;
    private List<Bitmap> mPicList;
    // 主页图片资源id
    private int[] mPicResIds;
    // 显示Type
    private TextView mTypeSelected;
    private View mPopupView;
    private PopupWindow mPopupWindow;
    private TextView mType2;
    private TextView mType3;
    private TextView mType4;
    // 游戏类型N×N
    private int mType = 2;
    // 选择项
    private String[] mSelectItems = new String[] {"本地图册", "相机拍照"};
    // image type
    private static final String IMAGE_TYPE = "image/*";
    // 返回码：本地图库
    private static final int RESULT_IMAGE = 100;
    // 返回码：拍照
    private static final int RESULT_CAMERA = 200;
    // 裁剪照片
    private static final int RESULT_CROP = 300;
    // 传到PuzzleActivity的图片路径
    private String mImagePath;
    // 拍照照片的存储地址以及裁剪拍照照片的存储地址
    public static String TEMP_IMAGE_PATH;
    // 裁剪图册照片的存储地址
    public static String CROP_ALBUM_IMAGE_PATH;
    // 权限是否申请成功
    private boolean mPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestStoragePermission();
        init();
        // 数据适配器
        mGvPicList.setAdapter(new GridPicListAdapter(this, mPicList));
        // Item点击监听
        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position == mPicList.size() - 1) {
                    // 显示选择本地还是拍照
                    showSelectDialog();
                } else {
                    // 选择默认的图片 跳转到拼图界面
                    Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);
                    intent.putExtra("picSelectedId", mPicResIds[position]);
                    intent.putExtra("type", mType);
                    intent.putExtra("permissionGranted", mPermissionGranted);
                    startActivity(intent);
                }
            }
        });
        // 显示难度Type
        mTypeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出popup window
                popupShow(view);
            }
        });
        // 给popup window里的item设置事件监听
        mType2.setOnClickListener(this);
        mType3.setOnClickListener(this);
        mType4.setOnClickListener(this);
    }

    /**
     * 动态申请权限
     */
    private void requestStoragePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showDialog();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            mPermissionGranted = true;
        }
    }

    /**
     * 显示提示对话框
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("申请SD卡权限").setMessage("无SD卡权限，从相册中选图和拍照取图功能将不正常")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPermissionGranted = false;
                        Toast.makeText(MainActivity.this, "Permission Denied",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true;
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    mPermissionGranted = false;
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    // 拒绝权限申请的话，跳出对话框提示为什么要申请权限）
                    showDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择：")
                .setItems(mSelectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(which == 0) {
                            // 本地图册
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    IMAGE_TYPE);
                            startActivityForResult(intent, RESULT_IMAGE);
                        } else if(which == 1) {
                            // 系统相机
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            Uri photoUri = Uri.fromFile(new File(TEMP_IMAGE_PATH));
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityForResult(intent, RESULT_CAMERA);
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            return;
        }
        if(requestCode == RESULT_IMAGE && data != null) {
            // 对本地相册的照片进行裁剪
            Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    String albumImagePath = cursor.getString(cursor.getColumnIndex("_data"));
                    cursor.close();
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(new File(albumImagePath)), "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(CROP_ALBUM_IMAGE_PATH)));
                    mImagePath = CROP_ALBUM_IMAGE_PATH;
                    startActivityForResult(intent, RESULT_CROP);
                }
            }
        } else if(requestCode == RESULT_CAMERA) {
            // 对拍照照片进行裁剪
            Uri cameraImageUri = Uri.fromFile(new File(TEMP_IMAGE_PATH));
            Uri cropCameraImageUri = cameraImageUri;
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(cameraImageUri, "image/*");
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropCameraImageUri);
            mImagePath = TEMP_IMAGE_PATH;
            startActivityForResult(intent, RESULT_CROP);
        } else if(requestCode == RESULT_CROP) {
            // 将图片路径传到PuzzleActivity
            Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);
            intent.putExtra("picPath", mImagePath);
            intent.putExtra("type", mType);
            intent.putExtra("permissionGranted", mPermissionGranted);
            startActivity(intent);
        }
    }

    private void init() {
        // GridView显示图片
        mGvPicList = (GridView) findViewById(R.id.id_gv_puzzle_main_pic_list);
        // 显示Type
        mTypeSelected = (TextView) findViewById(R.id.id_tv_puzzle_main_type_selected);
        mPopupView = LayoutInflater.from(this).inflate(R.layout.puzzle_main_type_selected, null);
        mType2 = (TextView) mPopupView.findViewById(R.id.id_tv_main_type_2);
        mType3 = (TextView) mPopupView.findViewById(R.id.id_tv_main_type_3);
        mType4 = (TextView) mPopupView.findViewById(R.id.id_tv_main_type_4);

        // 初始化bitmap数据
        mPicResIds = new int[] {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4,
                R.drawable.pic5, R.drawable.pic6, R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12, R.drawable.pic13, R.drawable.pic14,
                R.drawable.pic15, R.drawable.ic_launcher};
        mPicList = new ArrayList<>();
        Bitmap[] bitmaps = new Bitmap[mPicResIds.length];
        for (int i = 0; i < mPicResIds.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), mPicResIds[i]);
            mPicList.add(bitmaps[i]);
        }

        // 拍照照片的存储地址
        TEMP_IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.png";
        // 裁剪相册照片的存储地址
        CROP_ALBUM_IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/crop_album_image.png";
    }

    private void popupShow(View view) {
        // 显示popup window
        mPopupWindow = new PopupWindow(mPopupView, ScreenUtil.dp2px(this, 200),
                ScreenUtil.dp2px(this, 50));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 透明背景
        Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(transparent);
        // 获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - ScreenUtil.dp2px(this, 40),
                location[1] + ScreenUtil.dp2px(this, 30));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.id_tv_main_type_2:
                mType = 2;
                mTypeSelected.setText("2×2");
                mPopupWindow.dismiss();
                break;
            case R.id.id_tv_main_type_3:
                mType = 3;
                mTypeSelected.setText("3×3");
                mPopupWindow.dismiss();
                break;
            case R.id.id_tv_main_type_4:
                mType = 4;
                mTypeSelected.setText("4×4");
                mPopupWindow.dismiss();
                break;
        }
    }
}
