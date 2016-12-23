package com.example.puzzle.bean;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/12/17.
 */
public class ItemBean {
    private int itemId;
    private int bitmapId;
    private Bitmap bitmap;

    public ItemBean() {

    }

    public ItemBean(int itemId, int bitmapId, Bitmap bitmap) {
        this.itemId = itemId;
        this.bitmapId = bitmapId;
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getBitmapId() {
        return bitmapId;
    }

    public void setBitmapId(int bitmapId) {
        this.bitmapId = bitmapId;
    }

    public int getItemId() {
        return itemId;
    }
}
