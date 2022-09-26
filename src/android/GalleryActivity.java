package com.cardcam.scantrans;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

// import com.cardcam.lprdemo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.go.seoul.seoulSmartReport.R;
public class GalleryActivity extends AppCompatActivity implements GalleryAdapter.ItemClickListener {

    public class Item {
        String date;
        String path;
        boolean checked;
    }
//    String[] path;
//    boolean[] checked;
    List<Item> arrFile = new ArrayList<>();
    int currentPosition = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                reload();
            }
            else {
                Intent intent = new Intent();
                intent.putExtra("data", "");
                setResult(100, intent);
                finish();
            }
        }
    }
    GalleryAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(super.getResources().getIdentifier("Theme.AppCompat", "style", getPackageResourcePath()));
        // setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
//        kr.go.seoul.seoulSmartReport.R.style.Theme_AppCompat_NoActionBar

        setContentView(getResources().getIdentifier("activity_gallery", "layout", getPackageName())) ;//R.layout.activity_gallery);

        Button cancel = findViewById(getResources().getIdentifier("buttonCancel", "id", getPackageName()));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data", "");
                setResult(100, intent);
                finish();
            }
        });
        Button selectButton = findViewById(getResources().getIdentifier("buttonSelect", "id", getPackageName()));
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0;
                boolean checked = true;
                for (int i = arrFile.size(); i > 0; i --) {
                    if (arrFile.get(i-1).checked)
                        count = count + 1;
                }
                if (count == arrFile.size())
                    checked = false;
                for (int i = arrFile.size(); i > 0; i --) {
                    arrFile.get(i-1).checked = checked;
                }
                adapter.notifyDataSetChanged();
            }
        });

        Button deleteBtn = findViewById(getResources().getIdentifier("buttonDelete", "id", getPackageName()));
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected= false;
                for (int i = arrFile.size(); i > 0; i --) {
                    if (arrFile.get(i-1).checked) {
                        selected = true;
                    }
                }
                if (!selected) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                builder.setMessage("정말 삭제하시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = arrFile.size(); i > 0; i --) {
                                    if (arrFile.get(i-1).checked) {
                                        try {
                                            new File(arrFile.get(i - 1).path).delete();
                                        } catch (Exception e) {}
                                        arrFile.remove(i-1);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("선택 삭제");
                alert.show();

            }
        });

        RelativeLayout layout = findViewById(getResources().getIdentifier("layoutPreview", "id", getPackageName()));
        Button cancelButton = findViewById(getResources().getIdentifier("retry_btn", "id", getPackageName()));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.GONE);
            }
        });
        Button confirmButton = findViewById(getResources().getIdentifier("confirm_btn", "id", getPackageName()));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("data", GalleryActivity.this.arrFile.get(currentPosition).path);
                setResult(101, intent);
                finish();
            }
        });

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

        reload();
    }
    void reload() {

//        String pathToExternalStorage = Environment.getexternel().toString();
//        File appDirectory = new File(pathToExternalStorage + "/" + getText(R.string.app_name));
//
//        if (!appDirectory.isDirectory() || !appDirectory.exists()) //Checks if the directory exists
//            appDirectory.mkdir();

//        getApplicationContext().getDataDir()

        String path = getApplicationContext().getFilesDir().toString()+"/images"; //Environment.getExternalStorageDirectory().toString()
//        String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/kr.go.seoul.seoulSmartReport/files/images"; //Environment.getExternalStorageDirectory().toString()
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            files = new File[0];
        }
//        this.arrFile = new Item[files.length];
//        this.path = new String[files.length];
//        this.checked = new boolean[files.length];
        Log.d("Files", "Size: "+ files.length);
        ExifInterface exif = null;
        for (int i = 0; i < files.length; i++)
        {
            try {
                exif = new ExifInterface(files[i].getAbsolutePath());
                String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
                Log.d("Files", "FileName:" + files[i].getName());
//            this.path[i] = files[i].getAbsolutePath();
//            this.checked[i] = false;
//            this.arrFile[i].path = files[i].getAbsolutePath();
//            this.arrFile[i].checked = false;
                this.arrFile.add(new Item());
                this.arrFile.get(this.arrFile.size() - 1).date = date;
                this.arrFile.get(this.arrFile.size() - 1).path = files[i].getAbsolutePath();
                this.arrFile.get(this.arrFile.size() - 1).checked = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Arrays.sort(this.path, Collections.reverseOrder());
//        Arrays.sort(this.arrFile, Collections.reverseOrder());
        Comparator c = Collections.reverseOrder(new Comparator<Item>() {
            @Override
            public int compare(Item item, Item t1) {
                return item.date.compareTo(t1.date);
            }
        });
        Collections.sort(arrFile, c);
//        Collections.reverse(arrFile);

        GridView gridView = findViewById(getResources().getIdentifier("gridView", "id", getPackageName()));
        adapter = new GalleryAdapter(this, this.arrFile, this); // this.path, this.checked);



        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //

                Intent intent = new Intent();
                intent.putExtra("data", GalleryActivity.this.arrFile.get(position).path);
                setResult(101, intent);
                finish();
            }
        });


    }
    @Override
    public void onClickItem(int position) {
        currentPosition = position;
        RelativeLayout layout = findViewById(getResources().getIdentifier("layoutPreview", "id", getPackageName()));
        ImageView imgView = findViewById(getResources().getIdentifier("imgView", "id", getPackageName()));
        imgView.setImageURI(Uri.parse(GalleryActivity.this.arrFile.get(position).path));
        layout.setVisibility(View.VISIBLE);
        TextView textView = findViewById(getResources().getIdentifier("textPictureDate", "id", getPackageName()));
        textView.setText("촬영일시: " + arrFile.get(position).date);

//        Intent intent = new Intent();
//        intent.putExtra("data", GalleryActivity.this.arrFile.get(position).path);
//        setResult(101, intent);
//        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);

        super.attachBaseContext(newBase);
    }
}