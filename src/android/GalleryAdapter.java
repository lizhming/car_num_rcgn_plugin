package com.cardcam.scantrans;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    String[] array;
    public GalleryAdapter(Context c, String[] path) {
        context = c;
        array = path;
    }
    @Override
    public int getCount() {
        return array.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(context.getResources().getIdentifier("layout_gallery", "layout", context.getPackageName()), null);
        }
        ImageView imageView = convertView.findViewById(context.getResources().getIdentifier("imageView", "id", context.getPackageName()));
        TextView textView = convertView.findViewById(context.getResources().getIdentifier("textDate", "id", context.getPackageName()));

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(array[position]);
            textView.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        imageView.setImageURI(Uri.fromFile(new File(array[position])));
        Glide.with(context).load(Uri.fromFile(new File(array[position]))).into(imageView);
        return convertView;
    }
}
