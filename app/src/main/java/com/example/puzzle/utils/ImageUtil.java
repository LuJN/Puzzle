package com.example.puzzle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.puzzle.R;
import com.example.puzzle.activity.PuzzleActivity;
import com.example.puzzle.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像工具类，实现图像的分割和自适应
 */
public class ImageUtil {
    /**
     * 切图 初始状态（正常顺序）
     */
    public static void createInitBitmaps(Context context, Bitmap selectedBitmap, int type) {
        ItemBean itemBean = null;
        Bitmap bitmap = null;
        List<Bitmap> bitmapList = new ArrayList<>();
        // 每个item的宽高
        int itemWidth = selectedBitmap.getWidth() / type;
        int itemHeight = selectedBitmap.getHeight() / type;
        for (int i = 1; i <= type; i++) {
            for (int j = 1; j <= type; j++) {
                bitmap = Bitmap.createBitmap(selectedBitmap, (j - 1) * itemWidth, (i - 1) * itemHeight,
                        itemWidth, itemHeight);
                bitmapList.add(bitmap);
                itemBean = new ItemBean((i - 1) * type + j, (i - 1) * type + j, bitmap);
                GameUtil.itemBeanList.add(itemBean);
            }
        }
        // 保存最后一个图片在拼图完成时填充
        PuzzleActivity. mLastBitmap = bitmapList.get(type * type - 1);
        // 设置最后一个为空item
        bitmapList.remove(type * type - 1);
        GameUtil.itemBeanList.remove(type * type - 1);
        Bitmap blankBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank);
        blankBitmap = Bitmap.createBitmap(blankBitmap, 0, 0, itemWidth, itemHeight);
        bitmapList.add(blankBitmap);
        GameUtil.itemBeanList.add(new ItemBean(type * type, 0, blankBitmap));
        GameUtil.blankItemBean = GameUtil.itemBeanList.get(type * type - 1);
    }

    /**
     * 处理图片 放大缩小到合适尺寸
     */
    public static Bitmap resizeBitmap(float newWidth, float newHeight, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(newWidth / bitmap.getWidth(), newHeight / bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        return newBitmap;
    }
}
