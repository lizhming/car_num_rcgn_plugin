package com.cardcam.scantrans;

import android.Manifest;
import android.app.Activity;
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
import java.util.Arrays;

import kr.go.seoul.seoulSmartReport.R;

public class GalleryActivity extends AppCompatActivity {

    String[] path;

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
        this.path = new String[files.length];
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            this.path[i] = files[i].getAbsolutePath();
        }
        Arrays.sort(this.path);

        GridView gridView = findViewById(getResources().getIdentifier("gridView", "id", getPackageName()));
        GalleryAdapter adapter = new GalleryAdapter(this, this.path);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //

                Intent intent = new Intent();
                intent.putExtra("data", GalleryActivity.this.path[position]);
                setResult(101, intent);
                finish();
            }
        });
    }
}