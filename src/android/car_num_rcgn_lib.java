package com.cardcam;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.content.Intent;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.camera.CameraLauncher;
import org.apache.cordova.camera.ExifHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cardcam.carnum.CarNumRcgn;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * This class echoes a string called from JavaScript.
 */
public class car_num_rcgn_lib extends CordovaPlugin {
    public CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("recognize")) {
            String path = args.getString(0);
            this.recognize(path, callbackContext);
            return true;
        } else if (action.equals("show_gallery")) {
            this.show_gallery(callbackContext);
            return true;
        }
        return false;
    }
    private String  mPathDirectory() {
        String MainDir= Environment.getExternalStorageDirectory().getPath()+"/CarNumRcgn";
        String PathDir1=Environment.getExternalStorageDirectory().getPath()+ "/CarNumRcgn/save";
        String PathDir2=Environment.getExternalStorageDirectory().getPath()+"/CarNumRcgn/imgdata";
        String PathDir3=Environment.getExternalStorageDirectory().getPath()+"/CarNumRcgn/rawdata";

        File MainfileDir = new File(MainDir);
        if (!MainfileDir.canWrite()) {
            MainfileDir.mkdir();
        }

        File PathfileDir = new File(PathDir1);
        if (!PathfileDir.canWrite()) {
            PathfileDir.mkdir();
        }

        PathfileDir = new File(PathDir2);
        if (!PathfileDir.canWrite()) {
            PathfileDir.mkdir();
        }

        PathfileDir = new File(PathDir3);
        if (!PathfileDir.canWrite()) {
            PathfileDir.mkdir();
        }

        return PathDir3;
    }

    private int GetExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (exif != null)
        {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // If Camera Crop
        if (requestCode == 100) {
            if (resultCode == 100 ) { // canceled
//                callbackContext.success("");
                callbackContext.error("canceled");
            }// If cancelled
            else if (resultCode == 101) {
                String filepath = intent.getExtras().getString("data");
                exifResult(filepath);

//                ExifInterface exif = null;
//
//                try {
//                    exif = new ExifInterface(filepath);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            }

        }
    }
    private void exifResult(String path) {
        //callbackContext.success(path);
        int rotate = 0;
        String thisJson = "";
        String jsonResult = "";
        String jsonError = "{ \"error\": \"Unable to read exif data from remote URI\" }";

        Gson thisGson = new GsonBuilder()
                .setExclusionStrategies(new JsonExclusionStrategy(ExifInterface.class))
                .serializeNulls()
                .create();

        String fileLocation = path; //FileHelper.getRealPath(uri, this.cordova);


        JsonResultObj resultObj = new JsonResultObj();
        resultObj.filename = fileLocation;
        resultObj.json_metadata = "{}";

        ExifHelper exif = new ExifHelper();
        try {
            //We don't support PNG, so let's not pretend we do
            exif.createInFile(path);
            exif.readExifData();
            rotate = exif.getOrientation();

            // REM Modifications

            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(new JsonExclusionStrategy(ExifInterface.class))
                    .serializeNulls()
                    .create();

            //Convert exif to JSON
            thisJson = gson.toJson(exif);
            resultObj.json_metadata = thisJson;
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonResult = thisGson.toJson(resultObj);
        // success callback
        this.callbackContext.success(jsonResult);
    }
    private void show_gallery(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        Intent intent = new Intent(cordova.getActivity(), com.cardcam.scantrans.GalleryActivity.class);
        this.cordova.startActivityForResult((CordovaPlugin) this,
                intent, 100);
    }
    private boolean saveFile(File src){
        String path = cordova.getContext().getFilesDir().toString()+"/images";
        if (src.getAbsolutePath().toString().contains(path)) {
            return true;
        } else {

            File PathfileDir = new File(path);
            if (!PathfileDir.canWrite()) {
                PathfileDir.mkdir();
            }
            try {
                InputStream is = new FileInputStream(src);
                OutputStream os = new FileOutputStream(path + "/" + (new Date().getTime()) + ".jpg");
                byte[] buff = new byte[1024];
                int len;
                while ((len = is.read(buff)) > 0) {
                    os.write(buff, 0, len);
                }
                is.close();
                os.close();
            }catch(Exception e) {

            }
        }
        return true;
    }
    private void recognize(String path, CallbackContext callbackContext) {
        if (path != null && path.length() > 0) {
//            long tm,tm1;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Context context = this.cordova.getActivity().getApplicationContext();
            CarNumRcgn.SetActivity(context);

            CarNumRcgn.SetExternalStorage( mPathDirectory());

//            Bitmap ipp_image = null;
//            try {
//                ipp_image = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(path)), null, bmOptions);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                callbackContext.success("");
//                return;
//            }
            saveFile(new File(Uri.parse(path).getPath()));


            Bitmap ipp_image= BitmapFactory.decodeFile(Uri.parse(path).getPath(), bmOptions);

//            int CAM_width=ipp_image.getWidth();
//            int CAM_height=ipp_image.getHeight();

//            tm=System.currentTimeMillis();


            int captured_angle=0;
            captured_angle=GetExifOrientation(Uri.parse(path).getPath());

            if(captured_angle!=0)  // compensate Orientation Rotate
            {
                Bitmap tmp=CarNumRcgn.GetRotatedBitmap(ipp_image, captured_angle);
                if(ipp_image!=null && !ipp_image.isRecycled())
                    ipp_image.recycle();
                ipp_image=null;
                if(tmp==null)
                {
                    callbackContext.success("");
                    return;
                }
                ipp_image=tmp;
            }

            int[] cropArea= {0,0,0,0};
            int[] status= new int [8];
            Bitmap finalIpp_image = ipp_image;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    String LPRres = CarNumRcgn.bitmapCarNumRcgn(finalIpp_image,cropArea,status);

                    if(status[0]>0)
                    {
                        callbackContext.success(LPRres);
                    }
                    else
                    {
                        callbackContext.success("");
                    }
                }
            });

        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    private class JsonResultObj {
        private String filename = "";
        private String json_metadata = "";

        JsonResultObj() {
            //no-args constructor
        }
    }
    public class JsonExclusionStrategy implements com.google.gson.ExclusionStrategy {
        private final Class<?> typeToSkip;

        private JsonExclusionStrategy(Class<?> typeToSkip) {
            this.typeToSkip = typeToSkip;
        }

        public boolean shouldSkipClass(Class<?> clazz) {
            return (clazz == typeToSkip);
        }

        public boolean shouldSkipField(com.google.gson.FieldAttributes f) {
            // overridden but not required in this use case. Class filter does the job
            return false;
        }
    }
}
