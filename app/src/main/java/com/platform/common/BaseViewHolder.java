package com.platform.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.platform.chat.R;


public class BaseViewHolder extends RecyclerView.ViewHolder {
    View convertView;
    Context context;

    public BaseViewHolder(View itemView, Context context) {
        super(itemView);
        this.convertView = itemView;
        this.context = context;
    }

    public void setText(int id, String text) {
        TextView tx = (TextView) convertView.findViewById(id);
        tx.setText(text);
    }

    public void setImageResource(int id, int resouceId) {
        ImageView img = (ImageView) convertView.findViewById(id);
        img.setImageResource(resouceId);
    }

    public void setImageUrl(int id, String url) {
        ImageView img = (ImageView) convertView.findViewById(id);
        Glide.with(context).load(new HttpRequest().getFileUrl() + url).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(img);
    }

    public void setRating(int id, float rating) {
        RatingBar ratingBar = (RatingBar) convertView.findViewById(id);
        ratingBar.setRating(rating);
    }

    public void setImageBitmap(int id, String bitmap) {
        ImageView img = (ImageView) convertView.findViewById(id);
        img.setImageBitmap(stringToBitmap(bitmap));
    }

    public Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void setVisible(int id) {
        convertView.findViewById(id).setVisibility(View.VISIBLE);
    }

    public void setGone(int id) {
        convertView.findViewById(id).setVisibility(View.GONE);
    }

}
