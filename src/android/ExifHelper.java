/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.cardcam.scantrans;

import android.media.ExifInterface;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExifHelper {
    @SerializedName("aperture")
    private String aperture = null;
    @SerializedName("datetime")
    private String datetime = null;
    @SerializedName("datetimeOriginal")
    private String datetimeOriginal = null;
    @SerializedName("exposureTime")
    private String exposureTime = null;
    @SerializedName("flash")
    private String flash = null;
    @SerializedName("focalLength")
    private String focalLength = null;
    @SerializedName("gpsAltitude")
    private String gpsAltitude = null;
    @SerializedName("gpsAltitudeRef")
    private String gpsAltitudeRef = null;
    @SerializedName("gpsDateStamp")
    private String gpsDateStamp = null;
    @SerializedName("gpsLatitude")
    private String gpsLatitude = null;
    @SerializedName("gpsLatitudeRef")
    private String gpsLatitudeRef = null;
    @SerializedName("gpsLongitude")
    private String gpsLongitude = null;
    @SerializedName("gpsLongitudeRef")
    private String gpsLongitudeRef = null;
    @SerializedName("gpsProcessingMethod")
    private String gpsProcessingMethod = null;
    @SerializedName("gpsTimestamp")
    private String gpsTimestamp = null;
    @SerializedName("iso")
    private String iso = null;
    @SerializedName("make")
    private String make = null;
    @SerializedName("model")
    private String model = null;
    @SerializedName("orientation")
    private String orientation = null;
    @SerializedName("whiteBalance")
    private String whiteBalance = null;


    private ExifInterface inFile = null;
    private ExifInterface outFile = null;

    /**
     * The file before it is compressed
     *
     * @param filePath
     * @throws IOException
     */
    public void createInFile(String filePath) throws IOException {
        this.inFile = new ExifInterface(filePath);
    }

    /**
     * The file after it has been compressed
     *
     * @param filePath
     * @throws IOException
     */
    public void createOutFile(String filePath) throws IOException {
        this.outFile = new ExifInterface(filePath);
    }

    /**
     * Reads all the EXIF data from the input file.
     */
    public void readExifData() {
        this.aperture = inFile.getAttribute(ExifInterface.TAG_APERTURE);
        this.datetime = inFile.getAttribute(ExifInterface.TAG_DATETIME);
        this.datetimeOriginal = inFile.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
        this.exposureTime = inFile.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        this.flash = inFile.getAttribute(ExifInterface.TAG_FLASH);
        this.focalLength = inFile.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        this.gpsAltitude = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        this.gpsAltitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        this.gpsDateStamp = inFile.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        this.gpsLatitude = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        this.gpsLatitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        this.gpsLongitude = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        this.gpsLongitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        this.gpsProcessingMethod = inFile.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
        this.gpsTimestamp = inFile.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        this.iso = inFile.getAttribute(ExifInterface.TAG_ISO);
        this.make = inFile.getAttribute(ExifInterface.TAG_MAKE);
        this.model = inFile.getAttribute(ExifInterface.TAG_MODEL);
        this.orientation = inFile.getAttribute(ExifInterface.TAG_ORIENTATION);
        this.whiteBalance = inFile.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
    }


    /**
     * Writes the previously stored EXIF data to the output file.
     *
     * @throws IOException
     */
    public void writeExifData() throws IOException {
        // Don't try to write to a null file
        if (this.outFile == null) {
            return;
        }

        if (this.aperture != null) {
            this.outFile.setAttribute(ExifInterface.TAG_APERTURE, this.aperture);
        }
        if (this.datetime != null) {
            this.outFile.setAttribute(ExifInterface.TAG_DATETIME, this.datetime);
        } else {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.outFile.setAttribute(ExifInterface.TAG_DATETIME, format.format(date));
        }
        if (this.datetimeOriginal != null) {
            this.outFile.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, this.datetime);
        } else {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.outFile.setAttribute(ExifInterface.TAG_DATETIME, format.format(date));
        }
        if (this.exposureTime != null) {
            this.outFile.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, this.exposureTime);
        }
        if (this.flash != null) {
            this.outFile.setAttribute(ExifInterface.TAG_FLASH, this.flash);
        }
        if (this.focalLength != null) {
            this.outFile.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, this.focalLength);
        }
        if (this.gpsAltitude != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, this.gpsAltitude);
        }
        if (this.gpsAltitudeRef != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, this.gpsAltitudeRef);
        }
        if (this.gpsDateStamp != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, this.gpsDateStamp);
        }
        if (this.gpsLatitude != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE, this.gpsLatitude);
        }
        if (this.gpsLatitudeRef != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, this.gpsLatitudeRef);
        }
        if (this.gpsLongitude != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, this.gpsLongitude);
        }
        if (this.gpsLongitudeRef != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, this.gpsLongitudeRef);
        }
        if (this.gpsProcessingMethod != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, this.gpsProcessingMethod);
        }
        if (this.gpsTimestamp != null) {
            this.outFile.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, this.gpsTimestamp);
        }
        if (this.iso != null) {
            this.outFile.setAttribute(ExifInterface.TAG_ISO, this.iso);
        }
        if (this.make != null) {
            this.outFile.setAttribute(ExifInterface.TAG_MAKE, this.make);
        }
        if (this.model != null) {
            this.outFile.setAttribute(ExifInterface.TAG_MODEL, this.model);
        }
        if (this.orientation != null) {
            this.outFile.setAttribute(ExifInterface.TAG_ORIENTATION, this.orientation);
        }
        if (this.whiteBalance != null) {
            this.outFile.setAttribute(ExifInterface.TAG_WHITE_BALANCE, this.whiteBalance);
        }

        this.outFile.saveAttributes();
    }

    public String getGpsLatitude() {
        return this.gpsLatitude;
    }

    public String getGpsLongitude() {
        return this.gpsLongitude;
    }

    public int getOrientation() {
        int o = Integer.parseInt(this.orientation);

        if (o == ExifInterface.ORIENTATION_NORMAL) {
            return 0;
        } else if (o == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (o == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (o == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else {
            return 0;
        }
    }


    public void setGpsLatitudeRef(String gpsLatitudeRef) {
        this.gpsLatitudeRef = gpsLatitudeRef;
    }

    public void setGpsLatitude(String gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public void setGpsLongitudeRef(String gpsLongitudeRef) {
        this.gpsLongitudeRef = gpsLongitudeRef;
    }

    public void setGpsLongitude(String gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public void resetOrientation() {
        this.orientation = "" + ExifInterface.ORIENTATION_NORMAL;
    }
}
