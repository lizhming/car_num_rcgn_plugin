package com.cardcam;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.apache.cordova.BuildHelper;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.camera.CameraLauncher;
import org.apache.cordova.camera.CameraOptions;
//import org.apache.cordova.camera.CordovaLocationServices;
//import org.apache.cordova.camera.CordovaUri;
//import org.apache.cordova.camera.ExifHelper;
import org.apache.cordova.camera.FileHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cardcam.carnum.CarNumRcgn;
import com.cardcam.scantrans.CordovaLocationServices;
import com.cardcam.scantrans.CordovaUri;
import com.cardcam.scantrans.ExifHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

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
    private CordovaUri imageUri;            // Uri of captured image
    final String LOG_TAG = "car_num_rcgn";

    private int mQuality = 100;                   // Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
    private int targetWidth = -1;                // desired width of the image
    private int targetHeight = -1;               // desired height of the image
    private int encodingType = JPEG;               // Type of encoding to use 1: PNG
    private int mediaType = 0;                  // What type of media to retrieve
    private int destType;                   // Source type (needs to be saved for the permission handling)
    private int srcType;                    // Destination type (needs to be saved for permission handling)
    private boolean saveToPhotoAlbum;       // Should the picture be saved to the device's photo album
    private boolean correctOrientation;     // Should the pictures orientation be corrected
    private boolean orientationCorrected;   // Has the picture's orientation been corrected
    private boolean allowEdit;              // Should we allow the user to crop the image.

    private static final int JPEG = 0;                  // Take a picture of type JPEG
    private static final int PNG = 1;                   // Take a picture of type PNG

    public car_num_rcgn_lib() {

    }

    public car_num_rcgn_lib(CordovaInterface cordova, CallbackContext callbackContext, CordovaUri imageUri) { //, CameraOptions cameraOptions) {
        this.callbackContext = callbackContext;
        this.cordova = cordova;
        this.imageUri = imageUri;

//        this.destType = cameraOptions.destType;
//        this.srcType = cameraOptions.srcType;
//        this.mQuality = cameraOptions.mQuality;
//        this.targetWidth = cameraOptions.targetWidth;
//        this.targetHeight = cameraOptions.targetHeight;
//        this.encodingType = cameraOptions.encodingType;
//        this.mediaType = cameraOptions.mediaType;
//        this.allowEdit = cameraOptions.allowEdit;
//        this.correctOrientation = cameraOptions.correctOrientation;
//        this.saveToPhotoAlbum = cameraOptions.saveToPhotoAlbum;

    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("recognize")) {
            String path = args.getString(0);
            this.recognize(path, callbackContext);
            return true;
        } else if (action.equals("show_gallery")) {
            this.show_gallery(callbackContext);
            return true;
        } else if (action.equals("open_camera")) {
            this.open_camera(callbackContext);
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
        if (requestCode == 100) { // gallery
            if (resultCode == 100 ) { // canceled
//                callbackContext.success("");
                callbackContext.error("canceled");
            }// If cancelled
            else if (resultCode == 101) {
                String filepath = intent.getExtras().getString("data");
                exifResult(filepath);
            }
        } else if (requestCode == 101) { // camera
            if (resultCode == 100) { // cancel
                callbackContext.error("canceled");
            } else if (resultCode == 101) {
                String filepath = intent.getExtras().getString("data");
                File photo = new File(filepath); //createCaptureFile(encodingType);
                String applicationId = cordova.getActivity().getPackageName(); //(String) BuildHelper.getBuildConfigValue(cordova.getActivity(), "APPLICATION_ID");

                this.imageUri = new CordovaUri(FileProvider.getUriForFile(cordova.getActivity(),
                        applicationId + ".provider",
                        photo));
//                exifResult(filepath);

                try {
                    processResultFromCamera(destType, intent);
                } catch (IOException e) {
                    e.printStackTrace();
                    callbackContext.error("canceled");
                }
            }
            else {
                callbackContext.error("canceled");
            }
        }
    }

    /**
     * Applies all needed transformation to the image received from the camera.
     *
     * @param destType In which form should we return the image
     * @param intent   An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    private void processResultFromCamera(int destType, Intent intent) throws IOException {
        int rotate = 0;
        String thisJson = "";

        // Create an ExifHelper to save the exif data that is lost during compression
        ExifHelper exif = new ExifHelper();
        String sourcePath =
//                (this.allowEdit && this.croppedUri != null) ? FileHelper.stripFileProtocol(this.croppedUri.toString()) :
                this.imageUri.getFilePath();

        if (this.encodingType == JPEG) {
            try {
                //We don't support PNG, so let's not pretend we do
                exif.createInFile(sourcePath);
                exif.readExifData();
                rotate = exif.getOrientation();

                // REM Modifications

                Gson gson = new GsonBuilder()
                        .setExclusionStrategies(new JsonExclusionStrategy(ExifInterface.class))
                        .serializeNulls()
                        .create();

                //Convert exif to JSON
                thisJson = gson.toJson(exif);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (exif.getGpsLatitude() == null ||exif.getGpsLatitude() == null) {
                this.getGpsLocation(sourcePath, rotate, thisJson, exif, intent);
            } else {
                this.processPhotoFromCamera(sourcePath, rotate, thisJson, exif, intent);
            }

        } else {
            this.processPhotoFromCamera(sourcePath, rotate, thisJson, exif, intent);
        }

    }

    private void getGpsLocation(String sourcePath, int rotate, String thisJson, ExifHelper exif, Intent intent) {

        CameraOptions cameraOptions = new CameraOptions();

        CordovaLocationServices cordovaLocationServices = new CordovaLocationServices(cordova);
        cordovaLocationServices.getLocation(callbackContext, sourcePath, rotate, thisJson, exif, imageUri, intent, cameraOptions);
    }
    public void failPicture(String err) {
        this.callbackContext.error(err);
    }
    public void processPhotoFromCamera(String sourcePath, int rotate, String thisJson, ExifHelper exif, Intent intent) throws IOException {
        Bitmap bitmap = null;
        Uri galleryUri = null;

        // CB-5479 When this option is given the unchanged image should be saved
        // in the gallery and the modified image is saved in the temporary
        // directory
//        if (this.saveToPhotoAlbum) {
//            galleryUri = Uri.fromFile(new File(getPicutresPath()));
//
//            if (this.allowEdit && this.croppedUri != null) {
//                writeUncompressedImage(this.croppedUri, galleryUri);
//            } else {
//
//                Uri imageUri = this.imageUri.getFileUri();
//                writeUncompressedImage(imageUri, galleryUri);
//            }
//
//            refreshGallery(galleryUri);
//        }



        // package up file name and exif as JSON

        JsonResultObj resultObj = new JsonResultObj();
        resultObj.json_metadata = thisJson;

        Gson thisGson = new Gson();
        String jsonResult = "";

        // If all this is true we shouldn't compress the image.
//            if (this.targetHeight == -1 && this.targetWidth == -1 && this.mQuality == 100 &&
//                    !this.correctOrientation) {

        // If we saved the uncompressed photo to the album, we can just
        // return the URI we already created
//                if (this.saveToPhotoAlbum) {
//                    resultObj.filename = galleryUri.toString();
//                    jsonResult = thisGson.toJson(resultObj);
//                    this.callbackContext.success(jsonResult);
//                } else {
        Uri uri = Uri.fromFile(createCaptureFile(this.encodingType, System.currentTimeMillis() + ""));

        Uri imageUri = this.imageUri.getFileUri();
        writeUncompressedImage(imageUri, uri);
//                // Restore exif data to file
        if (this.encodingType == JPEG) {
            String exifPath;
            exifPath = uri.getPath();
            exif.createOutFile(exifPath);
            exif.writeExifData();
        }
        resultObj.filename = uri.toString();
        jsonResult = thisGson.toJson(resultObj);

        // success callback
        this.callbackContext.success(jsonResult);
//                }
//            } else {
//                Uri uri = Uri.fromFile(createCaptureFile(this.encodingType, System.currentTimeMillis() + ""));
//                bitmap = getScaledBitmap(sourcePath);
//
//                // Double-check the bitmap.
//                if (bitmap == null) {
//                    Log.d(LOG_TAG, "I either have a null image path or bitmap");
//                    this.failPicture("Unable to create bitmap!");
//                    return;
//                }
//
////                if (rotate != 0 && this.correctOrientation) {
////                    bitmap = getRotatedBitmap(rotate, bitmap, exif);
////                }
//
//                // Add compressed version of captured image to returned media store Uri
//                OutputStream os = this.cordova.getActivity().getContentResolver().openOutputStream(uri);
//                Bitmap.CompressFormat compressFormat =
////                        encodingType == JPEG ? Bitmap.CompressFormat.JPEG :
//                        Bitmap.CompressFormat.PNG;
//
//                bitmap.compress(compressFormat, 100, os);
//                os.close();
//
//                // Restore exif data to file
////                if (this.encodingType == JPEG) {
////                    String exifPath;
////                    exifPath = uri.getPath();
////                    exif.createOutFile(exifPath);
////                    exif.writeExifData();
////                }
//                resultObj.filename = uri.toString();
//                jsonResult = thisGson.toJson(resultObj);
//
//                // success callback
//                this.callbackContext.success(jsonResult);
//            }


//        this.cleanup(FILE_URI, this.imageUri.getFileUri(), galleryUri, bitmap);
        bitmap = null;
    }

    /**
     * In the special case where the default width, height and quality are unchanged
     * we just write the file out to disk saving the expensive Bitmap.compress function.
     *
     * @param src
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeUncompressedImage(Uri src, Uri dest) throws FileNotFoundException,
            IOException {
        FileInputStream fis = null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(FileHelper.stripFileProtocol(src.toString()));
            os = this.cordova.getActivity().getContentResolver().openOutputStream(dest);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOG.d(LOG_TAG, "Exception while closing output stream.");
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOG.d(LOG_TAG, "Exception while closing file input stream.");
                }
            }
        }
    }
    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @return a File object pointing to the temporary picture
     */
    private File createCaptureFile(int encodingType) {
        return createCaptureFile(encodingType, "");
    }

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @param fileName     or resultant File object.
     * @return a File object pointing to the temporary picture
     */
    private File createCaptureFile(int encodingType, String fileName) {
        if (fileName.isEmpty()) {
            fileName = ".Pic";
        }

        if (encodingType == JPEG) {
            fileName = fileName + ".jpg";
        } else if (encodingType == PNG) {
            fileName = fileName + ".png";
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
        }

        return new File(getTempDirectoryPath(), fileName);
    }


    private String getTempDirectoryPath() {
        File cache = null;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cache = cordova.getActivity().getExternalCacheDir();
        }
        // Use internal storage
        else {
            cache = cordova.getActivity().getCacheDir();
        }

        // Create the cache directory if it doesn't exist
        cache.mkdirs();
        return cache.getAbsolutePath();
    }
    /**
     * Return a scaled bitmap based on the target width and height
     *
     * @param imageUrl
     * @return
     * @throws IOException
     */
    private Bitmap getScaledBitmap(String imageUrl) throws IOException {
        // If no new width or height were specified return the original bitmap
//        if (this.targetWidth <= 0 && this.targetHeight <= 0) { // -1, -1
        InputStream fileStream = null;
        Bitmap image = null;
        try {
            fileStream = FileHelper.getInputStreamFromUriString(imageUrl, cordova);
            image = BitmapFactory.decodeStream(fileStream);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOG.d(LOG_TAG, "Exception while closing file input stream.");
                }
            }
        }
        return image;
//        }

//        // figure out the original width and height of the image
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        InputStream fileStream = null;
//        try {
//            fileStream = FileHelper.getInputStreamFromUriString(imageUrl, cordova);
//            BitmapFactory.decodeStream(fileStream, null, options);
//        } finally {
//            if (fileStream != null) {
//                try {
//                    fileStream.close();
//                } catch (IOException e) {
//                    LOG.d(LOG_TAG, "Exception while closing file input stream.");
//                }
//            }
//        }
//
//        //CB-2292: WTF? Why is the width null?
//        if (options.outWidth == 0 || options.outHeight == 0) {
//            return null;
//        }
//
//        // determine the correct aspect ratio
//        int[] widthHeight = calculateAspectRatio(options.outWidth, options.outHeight);
//
//        // Load in the smallest bitmap possible that is closest to the size we want
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, -1, -1); //this.targetWidth, this.targetHeight);
//        Bitmap unscaledBitmap = null;
//        try {
//            fileStream = FileHelper.getInputStreamFromUriString(imageUrl, cordova);
//            unscaledBitmap = BitmapFactory.decodeStream(fileStream, null, options);
//        } finally {
//            if (fileStream != null) {
//                try {
//                    fileStream.close();
//                } catch (IOException e) {
//                    LOG.d(LOG_TAG, "Exception while closing file input stream.");
//                }
//            }
//        }
//        if (unscaledBitmap == null) {
//            return null;
//        }
//
//        return Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0], widthHeight[1], true);
    }


    /**
     * Maintain the aspect ratio so the resulting image displays correctly
     *
     * @param origWidth
     * @param origHeight
     * @return
     */
    public int[] calculateAspectRatio(int origWidth, int origHeight) {
        int newWidth = -1; //this.targetWidth;
        int newHeight = -1; //this.targetHeight;

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            newWidth = origWidth;
            newHeight = origHeight;
        }
        // Only the width was specified
        else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        }
        // only the height was specified
        else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        }
        // If the user specified both a positive width and height
        // (potentially different aspect ratio) then the width or height is
        // scaled so that the image fits while maintaining aspect ratio.
        // Alternatively, the specified width and height could have been
        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
        // would result in whitespace in the new image.
        else {
            double newRatio = newWidth / (double) newHeight;
            double origRatio = origWidth / (double) origHeight;

            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }

        int[] retval = new int[2];
        retval[0] = newWidth;
        retval[1] = newHeight;
        return retval;
    }

    /**
     * Figure out what ratio we can load our image into memory at while still being bigger than
     * our desired width and height
     *
     * @param srcWidth
     * @param srcHeight
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;

        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        } else {
            return srcHeight / dstHeight;
        }
    }

    /**
     * Creates a cursor that can be used to determine how many images we have.
     *
     * @return a cursor
     */
    private Cursor queryImgDB(Uri contentStore) {
        return this.cordova.getActivity().getContentResolver().query(
                contentStore,
                new String[]{MediaStore.Images.Media._ID},
                null,
                null,
                null);
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
    private void open_camera(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        Intent intent = new Intent(cordova.getActivity(), com.cardcam.scantrans.CameraActivity.class);
        this.cordova.startActivityForResult((CordovaPlugin) this,
                intent, 101);
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

        @SerializedName("filename")
        private String filename = "";
        @SerializedName("json_metadata")
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
