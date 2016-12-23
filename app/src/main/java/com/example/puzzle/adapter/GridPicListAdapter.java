package com.example.puzzle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.puzzle.utils.ScreenUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */
public class GridPicListAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> picList;

    public GridPicListAdapter(Context context, List<Bitmap> picList) {
        this.context = context;
        this.picList = picList;
    }

    @Override
    public int getCount() {
        return picList.size();
    }

    @Override
    public Object getItem(int position) {
        return picList.get(position);
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
            pic.setLayoutParams(new GridView.LayoutParams(ScreenUtil.dp2px(context, 80),
                    ScreenUtil.dp2px(context, 100)));
            // 设置显示比例类型
            pic.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            pic = (ImageView) convertView;
        }
        pic.setImageBitmap(picList.get(position));
        return pic;
    }
}
