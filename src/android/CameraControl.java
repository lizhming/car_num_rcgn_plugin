package com.cardcam.camera;

import android.content.Context;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cardcam.scantrans.CameraActivity;


public final class CameraControl {

  public static CameraControl camControl;

  
  private String TAG="CamControl:";
  private  final CameraConfig config;
  public  Camera mCamera=null;
  public boolean previewing=false;
  public long    reqCam_tm=0;
  public boolean    can_flash=true;
  private final boolean useOneShotPreviewCallback;
  int cPriviewMax=0;
  public  int onActivity=0;
  public  long focus_tm=0;
  private int CamDirection=360;
  private SurfaceHolder mholder=null;
  private int entryMode=0;
  public  int Afmx=0,Afmy=0,Afdir=90;
  private Context ctx=null;
  public String PhoneModel=android.os.Build.MODEL;
  public final class AutoFocusCallback implements Camera.AutoFocusCallback {

//	  private static final String TAG = "AutoFocus";

	  public Handler autoFocusHandler;
	  private int autoFocusMessage;
	  public  int autoFocusing=0;
	  public  int tryFocusing=0;
	  public  int thisFocused=0;
	  public  long FocusedCbTime=0;
	  
	  void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
	    this.autoFocusHandler = autoFocusHandler; // activity
	    this.autoFocusMessage = autoFocusMessage; //R.id.auto_focus
	  }

	  public void onAutoFocus(boolean success, Camera camera) {
		autoFocusing=0;
	    if (autoFocusHandler != null) { 
	       autoFocusHandler.obtainMessage(autoFocusMessage, success);

	      autoFocusHandler = null;
	      if(success || tryFocusing>=2)
	      {
	    	  thisFocused=1;
	    	  tryFocusing=0;
	      }
	      else  
	      {
	    	  thisFocused=0;	    
	    	  tryFocusing++;
	      }

	      FocusedCbTime = System.currentTimeMillis();
	      
	    } else {
	      Log.d(TAG, "  no handler for autofocus ");
	    }
	  }
	   
  }

  
  final class PreviewCallback implements Camera.PreviewCallback {

	  private CameraConfig thisconfig;
	  private boolean useOneShotPreviewCallback;
	  private Handler previewHandler;
	  private int previewMessage;

	  PreviewCallback(CameraConfig config, boolean useOneShotPreviewCallback) {
	    this.thisconfig = config;
	    this.useOneShotPreviewCallback = useOneShotPreviewCallback;
	  }

	  void setHandler(Handler previewHandler, int previewMessage) {
	    this.previewHandler = previewHandler;
	    this.previewMessage = previewMessage;
	  }

	  public void onPreviewFrame(byte[] data, Camera camera) {
	    Point PreviewResolution = thisconfig.getPreviewResolution();
		    
	    	reqCam_tm=0; 
	    	if (!useOneShotPreviewCallback) {
		      camera.setPreviewCallback(null);
		    }
		    
		    if (previewHandler != null) {
		      Message message = previewHandler.obtainMessage(previewMessage, PreviewResolution.x, PreviewResolution.y, data);
		      message.sendToTarget();
		      previewHandler = null;
		    } 
	  }

	}
  
  private final PreviewCallback previewCallback;  
  public final AutoFocusCallback autoFocusMng;  
  
  public static void init( Context app) {
 
    if (camControl == null) {
    	camControl = new CameraControl(app);
    }
  }

  public  Point cameraResolution=new Point( 800,480);
  public  Point cameraMaxRes=new Point( 1600,1200);
  
//  public static CameraControl mget() {
//    return camControl;
//  }

  public Camera ManagerCamera() {
	    return mCamera;
  }

  public int MaxZoomValue() {
	    return config.getZoomMax();
  }
  public Point CaptureResolution() {
	    return config.getCaptureResolution();
  }
  
  public Point PreviewResolution() {
	    return config.getPreviewResolution();
  }
  
  
  public Point PreviewMaxRes() {
	    return config.getPreviewMaxRes();
  }
   
  private CameraControl(Context context) {

	  ctx=context; 
	  config = new CameraConfig(context);    
	  config.initScreenSize();
	  useOneShotPreviewCallback = true; 
      PhoneModel=android.os.Build.MODEL; 
	  previewCallback = new PreviewCallback(config, useOneShotPreviewCallback);
	  autoFocusMng = new AutoFocusCallback();
  }
  
  public boolean OpenCamera(SurfaceHolder holder,int ScreenDir) throws IOException {
	   
 
    if (mCamera == null)
    {
    	 previewing=false;
    	 
     	//synchronized(this)
     	{
	        mCamera = Camera.open();
	        if (mCamera == null) 
	        {	        	
	        	throw new IOException();
	        }
     	}
    }

    mholder=holder;
    if(!CameraActivity.Iamhere) //F3 TMUS
    {
    	
    	if(mCamera != null) { 
    		Log.w("Qt:", "cam release: by pause ");  
	    	mCamera.release();
	        mCamera = null;
    	}
    	else {
        	Log.w("Qt:", "cam out: by pause ");      		
    	}
    	return false;
    }
    
    mCamera.setPreviewDisplay(mholder);
    config.initFromCameraParameters(mCamera);

    config.setDesiredCameraParameters(mCamera,ScreenDir);
	mCamera.cancelAutoFocus();
	 
	Camera.Parameters parameters = mCamera.getParameters();	
	if(parameters.getFlashMode()==null)
		can_flash=false;

    startPreview();
    
    return true;
  }
  
  public void RecoverCamAF() 
  {
	  	stopPreview(true);
	    Camera.Parameters parameters = mCamera.getParameters();	
		mCamera.setParameters(parameters);
		startPreview();     
  }
  

  public void BlackCamera() throws IOException {
	   if(mCamera==null)
		   return;
	    stopPreview(true);
	    mCamera.setPreviewDisplay(null);
	    mCamera.setPreviewCallback(null);
  }
  
public void mCameraFlash(int onoff) {
	if(mCamera==null || !previewing)
		   return;
	config.setCameraFlash(mCamera,onoff);
}


public void mCameraRefresh() {
	if(mCamera==null) return;
	if( mCamera != null && previewing)
	{
		stopPreview(true);
		startPreview();
	}
}

  public boolean ChangeablePreviewRes(float Rat) {
	   if(mCamera==null)
		  return false;
	  return config.ChangeableCameraPreviewRes(mCamera,Rat);
  }
  
  public void  SetPreviewResolution() {
		if(mCamera==null)
			   return;
	  config.setCameraPreviewRes(mCamera);
  }

 
  public  void ChgPreviewResolution(int ScreenDirection) 
 {
        if (mCamera == null) {
            return;
        }
               	
        synchronized (this) {
            CamDirection = ScreenDirection;
            //stopPreview();

            try { 

            	 if (mCamera == null) 
                      return;
             
            	Camera.Parameters parameters = mCamera.getParameters();
                
            	mCamera.setDisplayOrientation(ScreenDirection);


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        int i=0;
    } 
   
  
    public boolean mCameraZoom(int zv) {
        if (mCamera == null)
            return false;
        config.setCameraZoom(mCamera, zv);
        return true;
    }

    public void CloseCamera() {
        synchronized (this) {
            if (mCamera != null) {
                stopPreview(true);
                mCamera.setPreviewCallback(null);
                previewCallback.setHandler(null, 0);
                autoFocusMng.setHandler(null, 0);
                
                mCamera.release();
                mCamera = null;

            }
            previewing = false;
        }
    }


    public void Destroy() throws IOException {
        synchronized (this) {
            if (mCamera == null)
                return;
            
            stopPreview(true);
            
            mCamera.setPreviewCallback(null);
            previewCallback.setHandler(null, 0);
            autoFocusMng.setHandler(null, 0);
            
            mCamera.release();
            mCamera = null;
            previewing = false;
        }
    }
  
  public void startPreview() {
//   if (mCamera != null && !previewing && onActivity==0) {
   if (mCamera != null) { // && onActivity==0) {  

	   if(previewing == true)
		   return;
	   
	   try {
		   if(mCamera != null) 
			   mCamera.startPreview();
	        previewing = true;
	   }
	   finally
	   {
		   // previewing = true;
	   }
	   
    }
  }


    public void stopPreview(boolean act) {

        if (mCamera != null && previewing) {

            try {
            	
                if(act) mCamera.stopPreview();
                previewing = false;
            } finally {
                // previewing = false;
            }
        }
    }
  
    public void changePreviewSize(int wd,int ht) {

      if(ht>wd) { int t=wd; wd=ht; ht=t;      }
      
      stopPreview(true);
      Point PreviewResolution=new Point( wd,ht);
      Point PreviewResolution1= new Point( wd,ht);
      
      Camera.Parameters parameters = mCamera.getParameters();
      int width=parameters.getPreviewSize().width;
      int height=parameters.getPreviewSize().height; 
      
      config.GetPreviewSize(mCamera,PreviewResolution,PreviewResolution1 );

      parameters.setPreviewSize(PreviewResolution.x, PreviewResolution.y);
       mCamera.setParameters(parameters);
       startPreview(); 
    }
  
    public Point GetScreenSize() {
        return config.getScreenResolution();
    }

    public void closePreview() {

        if (mCamera != null)// && previewing)
        {
            stopPreview(true);

            mCamera.setPreviewCallback(null);
            previewCallback.setHandler(null, 0);
            autoFocusMng.setHandler(null, 0);
        }
    }
  
  public void ChangeSceneMode() {		
		if(mCamera==null)
			   return;
		
	  Camera.Parameters parameters = mCamera.getParameters();

		List<String> scenemodes = mCamera.getParameters().getSupportedSceneModes();
		if(scenemodes != null)
		{
			if(scenemodes.indexOf(Camera.Parameters.SCENE_MODE_AUTO   ) != -1)  // For Special Phone
			  parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
		}	
		mCamera.setParameters(parameters);
  }
  
    public void requestPreviewFrame(Handler handler, int message) {
        if (mCamera != null) { // && previewing) {
            startPreview();
            
            reqCam_tm=System.currentTimeMillis();
            
            if (!previewing)
                return;

            if (handler != null)
                previewCallback.setHandler(handler, message);

            if (useOneShotPreviewCallback) {
                mCamera.setOneShotPreviewCallback(previewCallback);
            } else {
                // previewCallback.setHandler(null, 0);
                mCamera.setPreviewCallback(previewCallback);
            }
            
        }
    }

  public void recover_focus(Handler handler, int message) {
	    if (mCamera != null && previewing) {

	    	closePreview();
	    	
	   	 	mCamera.cancelAutoFocus();  
		    Camera.Parameters parameters = mCamera.getParameters();		    
			
		    //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			 List<String> focusModes = parameters.getSupportedFocusModes(); 		
			 if(focusModes != null )
			 { 				
					if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
						parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
					}
					else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
						parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}
		
			 }
			 
			mCamera.setParameters(parameters);			
	    	requestPreviewFrame(handler, message);  	 
						 
			mCamera.autoFocus(autoFocusMng);
	    	requestAutoFocus(handler, message) ;
	    }
	}
  
 
  public boolean IsAFContinuos() {
  
    if (mCamera != null && previewing) 
    {    	
	      Camera.Parameters parameters = mCamera.getParameters();	  
	      List<String> focusModes = parameters.getSupportedFocusModes(); 		
		  if(focusModes != null ) //&& PhoneModel.contains("F180"))
		  {  

				if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				}
				else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				}
	  	  }
    }
    return false;
  }
  


  int focus_sx,focus_sy,focus_ex, focus_ey;
  public boolean setAutoFocusArea()
  {
		 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return false;
		 }
		
		 if (Afmx <=0 || Afmy <=0 || ctx==null) {
			return false;
		 }
		
		 int xRange=200;  // 1/5
		 int yRange=200;
		
		 int wd =  ctx.getResources().getDisplayMetrics().widthPixels;
	     int ht =  ctx.getResources().getDisplayMetrics().heightPixels;
	     int mx,my;
	     
	     
	     if(Afdir==0)
	     {
	    	 mx= Afmx * 2000/ wd -1000;
	    	 my= Afmy * 2000/ ht -1000;
	     }
	     else if(Afdir==180)
	     {
	    	 mx= 1000- Afmx * 2000/ wd;
	    	 my= 1000- Afmy * 2000/ ht;
	     }
	     else if(Afdir==90)
	     {
	    	 my= Afmx * 2000/ wd -1000;
	    	 mx= Afmy * 2000/ ht -1000;
	     }
	     else //if(Afdir==270)
	     {
	    	 my=1000-  Afmx * 2000/ wd ;
	    	 mx=1000-  Afmy * 2000/ ht ;
	     }
	     
	     focus_sx=mx -xRange; 
	     focus_ex=mx +xRange;
	     focus_sy=my -yRange;
	     focus_ey=my +yRange;
	     
	     if(focus_sx<-1000) {
	    	 focus_sx=-1000; focus_ex=focus_sx+xRange;
	     }
	     if(focus_sy<-1000) {
	    	 focus_sy=-1000; focus_ey=focus_sy+yRange;
	     }
	     if(focus_ex>=1000-1) {
	    	 focus_ex=1000-1; focus_sx=focus_ex-xRange;
	     }
	     if(focus_ey>=1000-1) {
	    	 focus_ey=1000-1; focus_sy=focus_ey-yRange;
	     }
	     
	     return true;
	     
	}
	
	
		
  public void SetContinuousFocus(boolean continuousMode) 
  {  
	    if (mCamera != null && previewing) 
	    {
		      Camera.Parameters parameters = mCamera.getParameters();	 
		      mCamera.cancelAutoFocus();  
		      
		      if(continuousMode==true)
		      {		    	 
			      List<String> focusModes = parameters.getSupportedFocusModes(); 		
				  if(focusModes != null )//&& PhoneModel.contains("F180")) 
				  {  		 
				  		if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
				  		{
				  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); 
				  		}
				  		else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
				  		{
				  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				  		}
				  		
			  	  }  
				  
				  mCamera.setParameters(parameters); 
				  mCamera.cancelAutoFocus();  
				  
		      }
		      else
		      {
		    	  List<String> focusModes = parameters.getSupportedFocusModes(); 		
				  if(focusModes != null )//&& PhoneModel.contains("F180")) 
				  {  		 
				  		if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
				  		{
				  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				  		}				  		
			  	  }  				 
				  mCamera.setParameters(parameters);  
				
				  for(int ntry=0;ntry<2;ntry++)
				  {
				   	try {
				  		Thread.sleep(200);
				  		mCamera.autoFocus(autoFocusMng);
				  		break;
				  	 } catch (InterruptedException e) {
				  		    // TODO Auto-generated catch block
				  		    e.printStackTrace();
				  	}
				  }
					          
		      }	
	
	    }
	
  }
  public int isFocus_ContinuousMode() {
	  
	    if (mCamera != null && previewing) {	    	
	      	  
	      focus_tm= System.currentTimeMillis();
	      
	      Camera.Parameters parameters = mCamera.getParameters();	  
	      List<String> focusModes = parameters.getSupportedFocusModes(); 		
		  if(focusModes != null )
		  {  		 
			    if(parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
			    {
			    	return 1;
			    }
		  }
	    }
	    return 0;
  }
  
  
 // CameraManager.get().requestAutoFocus(this, R.id.auto_focus); call by CaptureActivity
  int conti_AF=0;
  public int requestAutoFocus(Handler handler, int message) {
  
    if (mCamera != null && previewing) {
    	
      	  
      focus_tm= System.currentTimeMillis();
      
      autoFocusMng.setHandler(handler, message);
      
      Camera.Parameters parameters = mCamera.getParameters();	  
      List<String> focusModes = parameters.getSupportedFocusModes(); 		
	  if(focusModes != null )//&& PhoneModel.contains("F180")) 
	  {  		 
		    if(parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
		    {
		    	 CameraControl.camControl.autoFocusMng.autoFocusing =0;
		    	return 0;
		    }
		    
	  		if(conti_AF==0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
	  		{
	  			mCamera.cancelAutoFocus(); 
	  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	  			conti_AF=1;
	  		    mCamera.setParameters(parameters);
	  	        mCamera.autoFocus(null);
	  	      CameraControl.camControl.autoFocusMng.autoFocusing = 0;
	  	        return 1;
	  		}
	  		else if(conti_AF==0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
	  		{
	  			mCamera.cancelAutoFocus();
	  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
	  			conti_AF=1;
	  		    mCamera.setParameters(parameters);
	  	        mCamera.autoFocus(null);
	  	      CameraControl.camControl.autoFocusMng.autoFocusing =0;
	  	        return 1;
	  		}
	  		else
	  		
	  		if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
	  		{
	  			mCamera.cancelAutoFocus(); 
	  			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	  			conti_AF=0;
	  		}
  	  }  
   
	  if(setAutoFocusArea()) 
	  {
		    Rect rect = new Rect((int) focus_sx, (int) focus_sy, (int) focus_ex, (int) focus_ey);
			ArrayList<Camera.Area> arraylist = new ArrayList<Camera.Area>();
			arraylist.add(new Camera.Area(rect, 1000));  //100 %
			
			if (parameters.getMaxNumFocusAreas() > 0) {
				parameters.setFocusAreas(arraylist); 
			}

			if (parameters.getMaxNumMeteringAreas() > 0) {
				parameters.setMeteringAreas(arraylist);
			}
	  }
		
	  mCamera.setParameters(parameters);
      mCamera.autoFocus(autoFocusMng);
    }
    return conti_AF;
  }

}
