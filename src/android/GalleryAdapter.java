package com.cardcam.scantrans;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends BaseAdapter {
    public interface ItemClickListener {
        public void onClickItem(int position);
    }
    Context context;
    LayoutInflater inflater;
    ItemClickListener listener;
//    String[] array;
//    boolean[] checked;
//    GalleryActivity.Item[] array;
    List<GalleryActivity.Item> array;
//    public GalleryAdapter(Context c, String[] path, boolean[] checked) {
//        context = c;
//        array = path;
//        this.checked = checked;
//    }
//    public GalleryAdapter(Context c, GalleryActivity.Item[] arr) {
//        context = c;
//        array = arr;
//    }
    public GalleryAdapter(Context c, List<GalleryActivity.Item> arr, ItemClickListener listener) {
        context = c;
        array = arr;
        this.listener = listener;
    }
//    public void updateData(String[] path, boolean[] checked) {
//        this.array = path;
//        this.checked = checked;
//    }
    @Override
    public int getCount() {
        return array.size();
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
        CheckBox checkBox = convertView.findViewById(context.getResources().getIdentifier("checkbox", "id", context.getPackageName()));
        checkBox.setChecked(array.get(position).checked);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickItem(position);
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                array.get(position).checked = checkBox.isChecked();
//                checked[position] = !checked[position];
            }
        });

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(array.get(position).path);
            textView.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        imageView.setImageURI(Uri.fromFile(new File(array[position])));
        Glide.with(context).load(Uri.fromFile(new File(array.get(position).path))).into(imageView);


        return convertView;
    }
}
