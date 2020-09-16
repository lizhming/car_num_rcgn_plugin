
package com.cardcam.camera;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.util.List;

final class CameraConfig {
  // TEN_DESIRED_ZOOM = 27;
  
  private final Context application;
  private  Point screenResolution=  new Point( 800,480);
  private  Point previewResolution=new Point( 800,480);
  private  Point previewMaxRes=new Point( 1600,1200);
  private  Point captureResolution=new Point( 1600,1200);
  private  Point PreviewResolution2= new Point( 1600,1200);
  
  private int previewFormat;
  private String previewFormatString; 
  private int zoomMax;
  private int cExposureRatio;
  private int MinExposure, MaxExposure; //=parameters.getMaxExposureCompensation();
  public String PhoneModel=android.os.Build.MODEL;
  
  CameraConfig(Context app) {
    this.application = app;
    PhoneModel=android.os.Build.MODEL; 
  }
  public Point getPreviewMaxRes() {
	    return previewMaxRes;
	  }
  
  public Point getPreviewResolution() {
	    return previewResolution;
	  }
	  
  public Point getCaptureResolution() {
		    return captureResolution;
	  }
	  
  public  Point getScreenResolution() {
	    return screenResolution;
	  }

  public int getZoomMax() {
	    return zoomMax;
	  }
  
  public  int getPreviewFormat() {
	    return previewFormat;
	  }

	  
  public  String getPreviewFormatString() {
	    return previewFormatString;
	  }
	  
  public  void initScreenSize() {
	    WindowManager manager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
	    Display display = manager.getDefaultDisplay();
	    
	    screenResolution.x=display.getWidth(); 
	    screenResolution.y=display.getHeight();

	
	    if(screenResolution.x<screenResolution.y)
	    {
	    	int t=screenResolution.x; screenResolution.x=screenResolution.y; screenResolution.y=t;
	    }
	  
	 }
  
  void initFromCameraParameters(Camera camera) {
	if(camera==null)
			   return;
		
    Camera.Parameters parameters = camera.getParameters();
    previewFormat = parameters.getPreviewFormat();
    zoomMax= parameters.getMaxZoom();

    previewFormatString = parameters.get("preview-format");
    
    initScreenSize();
    GetPreviewSize(camera,previewResolution,screenResolution );
    
    MinExposure=parameters.getMinExposureCompensation();
    MaxExposure=parameters.getMaxExposureCompensation();
  }

  
  public boolean ChangeableCameraPreviewRes(Camera camera,float Rat) 
  {	    
		if(camera==null) 
			return false; 
		
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> pvsizes = parameters.getSupportedPreviewSizes();
		Point DesiredRes=new Point( 800,480);
		
		DesiredRes.x=previewResolution.x;
		DesiredRes.y=previewResolution.y;
		int tk_height=(int)(((float)previewResolution.y) *Rat);
				
		for(Size x: pvsizes)
		{
			if(x.width>=screenResolution.x && x.height>=screenResolution.y)
			{
			  if(x.height>=tk_height) 
			  {
				  DesiredRes.x = x.width;
				  DesiredRes.y = x.height;
				  break;
			  }
			}
		}
		
		if(previewResolution.x!=DesiredRes.x && previewResolution.y!=DesiredRes.y)
		{
			previewResolution.x=DesiredRes.x;
			previewResolution.y=DesiredRes.y;
			return true;
		}
		else return false;
  }
  
  public  void setCameraPreviewRes(Camera camera) 
  {
	 if(camera==null) return; 
		
	  Camera.Parameters parameters = camera.getParameters();	  
	  parameters.setPreviewSize(previewResolution.x, previewResolution.y);
	  camera.setParameters(parameters);	  
   }
  
  
  public  void chgCameraPreviewRes(Camera camera,int width,int height) 
  {
	  if(camera==null) return; 
		
	  Camera.Parameters parameters = camera.getParameters();	  
	 
      if(width<height)  
      {
    	  parameters.setPreviewSize(height, width); 
    	  camera.setDisplayOrientation(90);
      }
      else
      {
    	  parameters.setPreviewSize(width, height);
      	  camera.setDisplayOrientation(0);
      }
      camera.setParameters(parameters);	 
      //camera.startPreview();	  

   }

  
  
  public void GetPreviewSize(Camera mCamera,Point DesiredRes,Point RefRes)
  {
		if(mCamera==null)
			   return;
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> pvsizes = parameters.getSupportedPreviewSizes();
//		List<Integer> zoom=parameters.getZoomRatios();
		
		
			int dir,wd,ht,d1,d,dmax=8000000;
			
//			for(int z: zoom)
//			{
//				int y=z;
//			}
			
			if(RefRes.x<RefRes.y) 
			{
				wd=RefRes.x; RefRes.x=RefRes.y; RefRes.y=wd;
			}
					
			
			previewMaxRes.x=previewMaxRes.y=0;
			for(Size x: pvsizes)
			{
				wd=x.width; ht=x.height;
				if(x.width<x.height) 
				{
					ht=x.width; wd=x.height;
				}
				
				if(wd>=previewMaxRes.x  && ht>previewMaxRes.y)
				{
					previewMaxRes.x = wd;
					previewMaxRes.y = ht;
				}
			}

			for(Size x: pvsizes)
			{
				wd=x.width; ht=x.height;
				if(x.width<x.height) 
				{
					ht=x.width; wd=x.height;
				}
				if(wd>2048 ) 
					continue;
				
				
				d1= wd-RefRes.x; 
				d= ht-RefRes.y;
				d=d*d+d1*d1;
				
		
				if(d>=0 && d<dmax)  {
					  dmax=d;
					  DesiredRes.x = wd;
					  DesiredRes.y = ht;
				 }
			}	
  }

  private void GetCaptureSize2(Camera mCamera,Point DesiredRes,Point RefRes)
  {
		int wd,ht,d1,d,dmax=8000000; 
		float sa,sa2,sb;
		if(mCamera==null)
			   return; 
		
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> pvsizes = parameters.getSupportedPictureSizes();
		sa=(float)RefRes.y/(float)RefRes.x;
		sa2=sa*0.95f;
		
		for(Size x: pvsizes)  //2560x1920 , 2048x1536 2304x1296  1280x960 1536x864
		{
			wd=x.width; ht=x.height;
			if(x.width<x.height) 
			{
				ht=x.width; wd=x.height;
			}
			
			sb=(float)ht/(float)wd;
			//if(sb<sa2) continue;
			
			d1= wd-RefRes.x; 
			d= ht-RefRes.y;
			d=d*d+d1*d1;
			d= (int) ((float)d * sa/sb);
	
			if(d<dmax)  {
				  dmax=d;
				  DesiredRes.x = wd;
				  DesiredRes.y = ht;
			 }
		}
  }

  private void GetCaptureSize(Camera mCamera,Point DesiredRes,int desired_width)
  {
		int width,height;
		if(mCamera==null)
			   return;
		
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> pvsizes = parameters.getSupportedPictureSizes();
		
	
		width=height=0;

		for(Size x: pvsizes){
		    if(x.width<x.height) continue;
		    
			if( x.width>width && x.width<=3264){ // 8M
				width= x.width;
				height = x.height;
			}
		}

		if(desired_width>0) 
		{
			int d,dmax=10000;
					
			for(Size x: pvsizes){
				
			  if(x.width<x.height ) continue;
			  
			  d=desired_width-x.width; if(d<0) d=-d;
			  if(d<dmax)  {
				  dmax=d;
				  DesiredRes.x = x.width;
				  DesiredRes.y = x.height;
			  }
			}
		}
		else
		{
			DesiredRes.x=width ;
			DesiredRes.y=height ;
		}

  }
  
  void setDesiredCameraParameters(Camera camera,int ScreenDir) {  // 800x480 1600x1200
	 int t,isel;
	 float ratx;
	 float[] difs = new float[3];
	 int[] wdRes = { 1280, 1280, 1280 };
	 int[] htRes = { 720, 768, 960 };
	            
	if(camera==null) return; 
	 
    Camera.Parameters parameters = camera.getParameters();
   
    if(screenResolution.x<screenResolution.y) 
    { 
    	t=screenResolution.x; screenResolution.x=screenResolution.y; screenResolution.y=t;
    }
    
    isel=0;
    float a1=(float)screenResolution.x/ (float)screenResolution.y;
	difs[0]= a1- (float)1280/(float)htRes[0]; if(difs[0]<0) difs[0]=-difs[0];  //1.78
	difs[1]= a1- (float)1280/(float)htRes[1]; if(difs[1]<0) difs[1]=-difs[1];  //1.66
	difs[2]= a1- (float)1280/(float)htRes[2]; if(difs[2]<0) difs[2]=-difs[2];  // 1.33
	
	for(t=1;t<3;t++)
		if(difs[t]<difs[isel]) isel=t;
	
	PreviewResolution2.x=1280;
	PreviewResolution2.y=720;
	if(PreviewResolution2.x==1280 || PreviewResolution2.x==720)
	{  	
		GetPreviewSize(camera,previewResolution,PreviewResolution2 );
	}
	else if(screenResolution.x==960 || screenResolution.x==800)
	{  	
		PreviewResolution2.x=1280;
		PreviewResolution2.y=htRes[isel];
		GetPreviewSize(camera,previewResolution,PreviewResolution2 );
	}
	else if(screenResolution.x>=1920 && screenResolution.y>=1080)
	{  	
		PreviewResolution2.x=1920;
		PreviewResolution2.y=htRes[isel];
	 	GetPreviewSize(camera,previewResolution,PreviewResolution2 );
	}
	else {

		GetPreviewSize(camera,previewResolution,screenResolution );
	}
	
    //GetCaptureSize(camera,captureResolution,2048);
    Point captureRef=new Point( 0,0); 
    
    captureRef.x=screenResolution.x*20/10;
    captureRef.y=screenResolution.y*20/10;
    if(captureRef.x>=2048)
    {
    	ratx=2048.0f/screenResolution.x;
        captureRef.x=2048;
        captureRef.y=(int)((float)screenResolution.y*ratx);
    }
    
    captureRef.x=2048;
    GetCaptureSize2(camera,captureResolution,captureRef);

     
    if( captureResolution.x<captureResolution.y)
    {
    	int v=captureResolution.x; captureResolution.x=captureResolution.y; captureResolution.y=v;
    }

    
    parameters.setPreviewSize(previewResolution.x, previewResolution.y);
    parameters.setPictureSize(captureResolution.x, captureResolution.y);
    parameters.setPictureFormat(ImageFormat.JPEG);

    
	parameters.setZoom(0); // x1.5
	List<String> scenemodes = camera.getParameters().getSupportedSceneModes();
	
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

    cExposureRatio=50;  
	
    camera.setDisplayOrientation(ScreenDir);   
   	camera.setParameters(parameters);
    //camera.cancelAutoFocus(); 
  
  }
  

  void setCameraFlash(Camera camera,int onoff) {

	     if(camera==null) return; //kig
		  Camera.Parameters parameters = camera.getParameters();
		  if(parameters.getFlashMode()==null)
				  return;
		  
		  if(onoff==0) parameters.setFlashMode("off");
		  else parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		  
		  camera.setParameters(parameters);
   }
  
 
  
  void setCameraZoom(Camera camera,int zv) {
	 int zvMin,zvMax;
	 
	 if(camera==null) return; //kig
	 
	  Camera.Parameters parameters = camera.getParameters();
	  
	  zvMax=parameters.getMaxZoom();
	  if(zv<=zvMax)  parameters.setZoom(zv);
	  else  parameters.setZoom(zvMax);
	  
	  camera.setParameters(parameters);	  
   }

  
}
