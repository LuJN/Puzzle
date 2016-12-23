package com.example.puzzle.utils;

import com.example.puzzle.activity.PuzzleActivity;
import com.example.puzzle.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼图工具类，实现拼图的交换与生成算法
 */
public class GameUtil {
    // 游戏信息单元格Bean
    public static List<ItemBean> itemBeanList = new ArrayList<>();
    // 空格单元格
    public static ItemBean blankItemBean = new ItemBean();

    /**
     * 判断Item是否可以移动
     */
    public static boolean isMovable(int position) {
        int type = PuzzleActivity.mType;
        // 获取空格Item的位置（从0开始计数）
        int blankPosition = blankItemBean.getItemId() - 1;

        // 不同行 相差为type
        if(Math.abs(blankPosition - position) == type) {
            return true;
        }
        // 同一行 相差为1
        if(blankPosition / type == position / type && Math.abs(blankPosition - position) == 1) {
            return true;
        }
        return false;
    }

    /**
     * 交换空格与点击Item的位置
     */
    public static void swapItems(ItemBean from, ItemBean blank) {
        ItemBean tempItemBean = new ItemBean();
        // 交换BitmapId
        tempItemBean.setBitmapId(from.getBitmapId());
        from.setBitmapId(blank.getBitmapId());
        blank.setBitmapId(tempItemBean.getBitmapId());
        // 交换Bitmap
        tempItemBean.setBitmap(from.getBitmap());
        from.setBitmap(blank.getBitmap());
        blank.setBitmap(tempItemBean.getBitmap());
        // 设置新的blank
        blankItemBean = from;
    }

    /**
     * 生成随机的Item（打乱顺序）
     * */
    public static void getPuzzleGenerator() {
        int index = 0;
        // 随机打乱顺序
        for (int i = 0; i < itemBeanList.size(); i++) {
            index = (int) (Math.random() * (PuzzleActivity.mType * PuzzleActivity.mType));
            swapItems(itemBeanList.get(index), blankItemBean);
        }
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < itemBeanList.size(); i++) {
            data.add(itemBeanList.get(i).getBitmapId());
        }
        // 判断是否有解
        if(canSolve(data) && data.get(data.size() - 1) != 0) {
            return;
        } else {
            getPuzzleGenerator();
        }
    }

    /**
     * 该序列是否有解
     * data序列宽度为奇数，则倒置和为偶数
     * data序列宽度为偶数，则空格位于奇数行（从底往上数）时倒置和为偶数，空格位于偶数行（从底往上数）时倒置和为奇数
     */
    public static boolean canSolve(List<Integer> data) {
        // 获取空格Item的ItemId
        int blankItemId = blankItemBean.getItemId();
        // 可行性原则
        if(data.size() % 2 == 1) {
            return getInversions(data) % 2 == 0;
        } else {
            // 空格Item位于奇数行（从底往上数）
            if((PuzzleActivity.mType - (blankItemId - 1) / PuzzleActivity.mType) % 2 == 1) {
                return getInversions(data) % 2 == 0;
            } else {
                // 空格Item位于偶数行（从底往上数）
                return getInversions(data) % 2 == 1;
            }
        }
    }

    /**
     * 计算倒置和算法
     */
    public static int getInversions(List<Integer> data) {
        // 倒置变量值
        int inversionCount = 0;
        // 倒置和
        int inversions = 0;
        for (int i = 0; i < data.size(); i++) {
            int value = data.get(i);
            for (int j = i + 1; j < data.size(); j++) {
                if(data.get(j) != 0 && data.get(j) < value) {
                    inversionCount++;
                }
            }
            inversions += inversionCount;
            inversionCount = 0;
        }
        return inversions;
    }

    /**
     * 拼图是否成功
     */
    public static boolean isSuccess() {
        for (ItemBean itemBean : itemBeanList) {
            if(itemBean.getBitmapId() != 0 &&
                    itemBean.getItemId() == itemBean.getBitmapId()) {
                continue;
            } else if(itemBean.getBitmapId() == 0 &&
                    itemBean.getItemId() == PuzzleActivity.mType * PuzzleActivity.mType) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
