package com.platform.adapter;

import android.content.Context;

import com.platform.chat.R;
import com.platform.common.BaseRecycleAdapter;
import com.platform.common.BaseUtils;
import com.platform.common.BaseViewHolder;
import com.platform.common.MyLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserDetailMessageAdapter extends BaseRecycleAdapter {
    /**
     * @param context  //上下文
     * @param layoutId //布局id
     * @param data     //数据源
     */
    public UserDetailMessageAdapter(Context context, int layoutId, List<Map<String, Object>> data) {
        super(context, layoutId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, Map<String, Object> map) {
        holder.setText(R.id.tv_name, map.get("nickName").toString());
        holder.setText(R.id.tv_msg, map.get("message").toString());
        holder.setText(R.id.tv_date, BaseUtils.transTime(new Date().getTime()));
        holder.setImageBitmap(R.id.iv_headImage, map.get("photo").toString());
    }
}
