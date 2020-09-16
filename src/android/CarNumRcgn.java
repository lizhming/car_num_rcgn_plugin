
package com.cardcam.carnum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public  class CarNumRcgn {

	static  
    {
        try { 
            System.loadLibrary("LPRPro");  //LPRcar(일반버젼),LPRPRo(프로버젼)
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    } 
	      
	
  /*
   *  이함수를 호출해야  사용할 앱의  패키지명에 의해 라이브러리가 활성화된다.  
   */ 
  public static native int SetActivity(Context thiz);
  
  
  // format: CARNUM-RCGN-1.0.012
  public static native String GetVersion();
  
/* format 
     CARNUN-RCGN-1.0.011 Trial (2018/06/01~2018/12/31)
  		or
  	 CARNUN-RCGN-1.0.012 Commercial
*/
  public static native String GetDescription();
    
  /* 
   *  Set External File Storage Directory (aleady directory) 
   */
  public final  static native void SetExternalStorage(String path);
  
   
  
  /* calculates the focus value for the region of  the center (mx, my) 
   *  input : yuvdata (input) yuv420sp data from camera preview    
   *  		  mx,my : Center coordinates  , defalt to (0,0)  = (width/2, height/2) 
   *  	 
   *  return : focus value
   *  
   *  example) int FocusValue=ERISprep.GetFocusValueYuv420fDatas(yuvdata, width, height, width/2, height/2, 0);
   */ 
  public final	static native int GetFocusValueYuv420(byte[] yuvdata, int width, int height, int mx,int my,int mode);

 

  
  /*
   *  input : bitmap32  
   *  cropArea[0]: 검출영역 left 
      cropArea[1]: 검출영역 top	
      cropArea[2]: 검출영역 right
      cropArea[3]: 검출영역 bottom
      cropArea[]={0,0,0,0} 이면 전체 영역

   *  output :   
      status[]:
      status[0]: 결과 상태 status
      status[1]: 번호판 left
      status[2]: 번호판 top
      status[3]: 번호판 right
      status[4]: 번호판 bottom
      
   * return : result string 
   *  
   */ 
   
  public final  static native String bitmapCarNumRcgn(Bitmap bitmap32,int cropArea[],int status[]);
 
  /*
	Input 
  yuvdata (input) yuv420sp data from camera preview)
  width : preview image's width 
  height: preview image's width 
  ScreenDir - reference of getScreenDirection() fuction 
  detArea[0]:  (float) left/width   [0~1.0f]
  detArea[1]:  (float) top/height
  detArea[2]:  (float) right/width
  detArea[3]:  (float) bottom/height
  detArea[]={0,0,0,0} 이면 전체 영역
      
  SizeRat :  default=3 (Not Used , Reserved Value )
  
  Output
   status[]:
      status[0]: 결과 상태 status
      status[1]: 번호판 left
      status[2]: 번호판 top
      status[3]: 번호판 right
      status[4]: 번호판 bottom
        
 * return : result string    
 */ 

public final  static native String yuvCarNumRcgn( byte[] yuvdata,int width,int height,int ScreenDir, float detArea[],int statsus[]);

/*
	Input 
yuvdata (input) yuv420sp data from camera preview)
width : preview image's width 
height: preview image's width 
ScreenDir - reference of getScreenDirection() fuction at HSPrepe.java
SizeRat :  default=3 (Not Used , Reserved Value )

Output
 status[]:
    status[0]: 결과 상태 status
    status[1]: 번호판 left
    status[2]: 번호판 top
    status[3]: 번호판 right
    status[4]: 번호판 bottom
      
* return : 1= Carnum DIgit4    
*/ 
// TO DO
//public final  static native int yuvCarDigit4Check( byte[] yuvdata,int width,int height,int ScreenDir, int statsus[]);


/*
*  input : bitmap32  
*  input : deg  = CW angle  degree  
*  output : roteated bitmap32
*  return : bitmap
*  
*/
public final  static native Bitmap MatrixRotateBitmap(Bitmap bitmap32, Matrix m,float deg);

public static Bitmap GetRotatedBitmap(Bitmap bitmap, float fdeg) 
{
	  if (fdeg != 0 && bitmap != null) 
	  { 

	       Matrix m = new Matrix();

	   	   m.setRotate(fdeg, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);	  

		   try {
			    //Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);	
			    Bitmap b2 = MatrixRotateBitmap(bitmap, m, fdeg);	   

			    if (bitmap != b2) {
			     bitmap.recycle();
			     bitmap = b2;
			    }
			    else {
			    	b2.recycle();
			    	b2 = null;
			    }
		   } catch (OutOfMemoryError e) {
     	   Log.e("LPR", "OutOfMemoryError ");
		   }

	  }
	  return bitmap;
}



   /**
    
    private int getScreenDirection(Context ctx)  
    {
    	   WindowManager manager = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
         Display display = manager.getDefaultDisplay();
         int rotation = display.getRotation();

         if (rotation == Surface.ROTATION_0)
             return  90;
         else if (rotation == Surface.ROTATION_90)
        	 return  0;
         else if (rotation == Surface.ROTATION_180)
        	 return  270;
         else if (rotation == Surface.ROTATION_270)
        	 return 180;
         return 0;
    }
    
  **/

     
}
