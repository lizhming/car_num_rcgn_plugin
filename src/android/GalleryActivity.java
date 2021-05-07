package com.cardcam.scantrans;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

// import com.cardcam.lprdemo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kr.go.seoul.seoulSmartReport.R;
public class GalleryActivity extends AppCompatActivity implements GalleryAdapter.ItemClickListener {

    public class Item {
        String path;
        boolean checked;
    }

//    String[] path;
//    boolean[] checked;
    List<Item> arrFile = new ArrayList<>();

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
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
//            this.path[i] = files[i].getAbsolutePath();
//            this.checked[i] = false;
//            this.arrFile[i].path = files[i].getAbsolutePath();
//            this.arrFile[i].checked = false;
            this.arrFile.add(new Item());
            this.arrFile.get(this.arrFile.size() - 1).path = files[i].getAbsolutePath();
            this.arrFile.get(this.arrFile.size() - 1).checked = false;
        }
//        Arrays.sort(this.path, Collections.reverseOrder());
//        Arrays.sort(this.arrFile, Collections.reverseOrder());
        Collections.reverse(arrFile);

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
        Intent intent = new Intent();
        intent.putExtra("data", GalleryActivity.this.arrFile.get(position).path);
        setResult(101, intent);
        finish();
    }

}