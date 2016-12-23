package com.example.puzzle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */
public class GridItemsAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> itemList;

    public GridItemsAdapter(Context context, List<Bitmap> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ImageView pic = null;
        if(convertView == null) {
            pic = new ImageView(context);
            // 设置布局
            pic.setLayoutParams(new GridView.LayoutParams(itemList.get(position).getWidth(),
                    itemList.get(position).getHeight()));
            // 设置显示比例类型
            pic.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            pic = (ImageView) convertView;
        }
        pic.setImageBitmap(itemList.get(position));
        return pic;
    }
}
