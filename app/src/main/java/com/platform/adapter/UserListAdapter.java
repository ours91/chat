package com.platform.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.platform.chat.R;
import com.platform.common.BaseRecycleAdapter;
import com.platform.common.BaseUtils;
import com.platform.common.BaseViewHolder;

import java.util.List;
import java.util.Map;

public class UserListAdapter extends BaseRecycleAdapter {
    /**
     * @param context  //上下文
     * @param layoutId //布局id
     * @param data     //数据源
     */
    public UserListAdapter(Context context, int layoutId, List<Map<String, Object>> data) {
        super(context, layoutId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, Map<String, Object> map) {
        holder.setText(R.id.adapter_user_list_nickName, BaseUtils.toString(map.get("nickName")));
        holder.setText(R.id.adapter_user_list_online, (Integer.parseInt(BaseUtils.toString(map.get("online"))) == 1) ? "在线" : "离线");
        holder.setImageBitmap(R.id.adapter_user_list_avatar, BaseUtils.toString(map.get("photo")));
    }
}
