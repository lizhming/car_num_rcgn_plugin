
package com.cardcam.scantrans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;

import android.view.MotionEvent;
import android.view.View;


public final class ViewDraw extends View {

    private final Paint paint;
    private int StrokeWidth=40;
    private int LineMarkColor_org =0x908000FF;
  
    public  final static int CAPTURE_START	=10;
    public  final static int CAPTURE_GotImage =20;
    public  final static int CAPTURE_GotImageReady =30;
    public  final static int CAPTURE_GotTargetData =40;
    public  final static int CAPTURE_DoneTranslate =50;
    
    public CameraActivity parent;

    int txtBoard_sx, txtBoard_sy;

    int txtBoard_ex, txtBoard_ey;

    private Typeface mType = null;

    
    public  Rect maxScanArea = new Rect(0, 0, 0, 0);
    public  Rect rScanArea = new Rect(0, 0, 0, 0);
    
    public  int camArea_mx=0,camArea_my=0;
    
    
    // ===========================================================================================


    int nTime = 0;

    long msg_tm = 0,dim_sm=0;


    private Canvas  mCanvas=null;
    private Path    mPath;
    private Paint   mPaint;
    // ==========================================================================================
    public ViewDraw(Context context) {
        super(context);

        parent = (CameraActivity)context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath = new Path();
        setTypeface(1);
        paint.setTextSize(24);        

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(LineMarkColor_org);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(StrokeWidth);
        //mFace = Utils.createTypefaceFromFile(parent.mtype_QPATH);
    }

    private void setTypeface(int which) {
        if (which == 0)
            mType = null;
        else if (which == 1)
            mType = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        else if (which == 2)
            mType = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        else if (which == 3)
            mType = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
        else if (which == 4)
            mType = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        paint.setTypeface(mType);
    }

   
    
    private void draw_BasicArea(Canvas canvas) {
   
       
        if(parent.ipp_image!=null && parent.ipp_image.isRecycled())
        	parent.ipp_image=null;
        
        if( ( parent.Captured_ModeEnable!=0 && parent.OnCapture_status>=CAPTURE_GotImage) || parent.IppStatus>=parent.IPP_IMAGEGOT )
        {
	        if(parent.menu_drawable != 0 && parent.ipp_image != null)// && 	parent.LunarProcessStatus!=parent.LUNAR_PERSPECT_OnANI) 
	        {	
	        	Bitmap tBitmap = parent.setCaptureImgbitmap(true);
				if (tBitmap != null) {
					if (mBitmap != null)
						mBitmap.recycle();
					try {
						mBitmap = Bitmap.createBitmap(parent.ScreenSize.x, parent.ScreenSize.y, Bitmap.Config.ARGB_4444);
						mCanvas = new Canvas(mBitmap);
						mBitmap.eraseColor(0x00000000);
						// // nib 11/04/13 354359
						// mCanvas.clipRect(parent.rcArea.left, parent.rcArea.top, parent.rcArea.right, parent.rcArea.bottom, Region.Op.REPLACE);
					}
					catch (Exception e) { // catch(OutOfMemoryError e)
						mBitmap = null;
					}
					catch (OutOfMemoryError e) {
						mBitmap = null;
					}
				}
	         }
        }
        
        if(parent.ipp_image==null) {
        	parent.clearCaptureImgv();
        	if(mBitmap!=null) mBitmap.recycle();
        	mBitmap=null;
        	mCanvas=null;
        	//parent.CaptureImgLay.setVisibility(View.GONE);
        }
 
        
        //parent.SetCameraArea( (parent.rcArea.left+ parent.rcArea.right)/2, (parent.rcArea.top+ parent.rcArea.bottom)/2);
        //parent.SetCameraArea( camArea_mx, camArea_my);
    }


    public int Lineguide_Height=0;
    public Bitmap MenuButton9_xExpandBitMap(int id, int width) {
        Bitmap BM, tmp;
   
        tmp = BitmapFactory.decodeResource(getResources(), id);

        Lineguide_Height=tmp.getHeight();
        
        @SuppressWarnings("deprecation")
        NinePatchDrawable np_drawable = new NinePatchDrawable(tmp, tmp.getNinePatchChunk(), new Rect(), null);
        
        np_drawable.setBounds(0, 0, width, Lineguide_Height);

        try {
        BM = Bitmap.createBitmap(width, Lineguide_Height, Bitmap.Config.ARGB_8888);
        } 
        catch(Exception e) {        // 
       	   tmp.recycle();
       	   return null;
        }

        Canvas cv = new Canvas(BM);
        np_drawable.draw(cv);
        
        tmp.recycle();
        return BM;
    }
     
 
   public void reset_ScanArea()
   {
	    int fact1000=parent.ScreenSize.y*1000/(parent.ScreenSize.y-parent.StatusBarHeight) ;
	    
	    int WA=28,WB=72;
   		rScanArea.left= (maxScanArea.left*WB + maxScanArea.right*WA)/(WA+WB);
       	rScanArea.right= (maxScanArea.left*WA + maxScanArea.right*WB)/(WA+WB);
       	rScanArea.top= (maxScanArea.top*WB + maxScanArea.bottom*WA)/(WA+WB);
       	rScanArea.bottom= (maxScanArea.top*WA + maxScanArea.bottom*WB)/(WA+WB);
   		
		if(parent.ScreenDirection==0 || parent.ScreenDirection==180)
		{		       
	       	int mx=(rScanArea.right+rScanArea.left)/2; 
	       	int wd=(rScanArea.bottom-rScanArea.top);
	   		rScanArea.left=mx-wd/2;
	   		rScanArea.right=mx+wd/2;                    	
	   	}
	   	else 
	   	{
	       	int my=(rScanArea.bottom+rScanArea.top)/2;
	   		int ht=(rScanArea.right-rScanArea.left);
	   		rScanArea.top=my-ht/2;
	   		rScanArea.bottom=my+ht/2;
	   	}
   	
	 	rScanArea.top =rScanArea.top *1000/fact1000;
	 	rScanArea.bottom =rScanArea.bottom *1000/fact1000;  
   }
    
   private void draw_ScanArea(Canvas canvas) {
	    int fact1000=parent.ScreenSize.y*1000/(parent.ScreenSize.y-parent.StatusBarHeight) ;
        
       if(parent.StatusBarHeight==0 || parent.menu_drawable == 0)
       	return;
       	
		int sx,ex,sy,ey;
		
	      
		if(parent.ScreenSize.y>parent.ScreenSize.x)
		{		       
	   		sx=8; ex=parent.ScreenSize.x-sx;
	   		sy=parent.ScreenSize.y/4;
	   		ey=parent.ScreenSize.y*3/4;
	   	}
	   	else 
	   	{
	   		sx=parent.ScreenSize.x/5; 
	   		ex=parent.ScreenSize.x*4/5;
	   		sy=parent.ScreenSize.y/5;
	   		ey=parent.ScreenSize.y*4/5;
	   	}
	 	
		//sy =sy *1000/fact1000;
	 	//ey =ey *1000/fact1000; 
		        
	 	paint.setColor(Color.BLACK); 
	    paint.setAlpha(80);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawRect(0, 0, parent.ScreenSize.x, sy, paint);
		canvas.drawRect(0, ey, parent.ScreenSize.x, parent.ScreenSize.y, paint);
		canvas.drawRect(0, sy, sx, ey, paint);
		canvas.drawRect(ex, sy, parent.ScreenSize.x, ey, paint);
		
		paint.setColor(0xffffffff);
		paint.setAlpha(250);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4);
		canvas.drawRect(sx, sy, ex, ey, paint);
		//canvas.drawRect(0, 0, parent.ScreenSize.x, parent.ScreenSize.y, paint);
	
   }
  

  
    private Bitmap rotate(Bitmap b, int degrees) {
    	int wd,ht,wd2,ht2;
            Matrix m = new Matrix();
            m.postRotate(degrees);
            wd=b.getWidth(); ht=b.getHeight();
            
            Bitmap b2 = Bitmap.createBitmap( b, 0, 0,wd, ht, m, true );
            wd2=b2.getWidth(); ht2=b2.getHeight();
            if(wd>=wd2 || ht>=ht2) return b2;
            
            int sx,sy;
            sx=(wd2-wd)/2; sy=(ht2-ht)/2; 
            Bitmap b3 = Bitmap.createBitmap( b2, sx,sy, wd, ht );
            b2.recycle();
            
           return b3;
    }

     
  
    


    public void CanvasDraw(Canvas canvas) {
        
        draw_BasicArea(canvas);
             
        if(parent.Captured_ModeEnable==0)
        {
        }
        else rScanArea.right=0;
       
    	this.setBackgroundColor(0x00000000);
    	if( parent.ipp_image!=null ) 
    	{
    		 rScanArea.left=rScanArea.right=0;
    	}   
    	
    	if(parent.ImgButtonStatus==0)
    		draw_ScanArea(canvas);
    	
    	
		if(parent.OnCapture_status==CAPTURE_DoneTranslate)
    		this.setBackgroundColor(0x80000000);
    	
        
        setTypeface(1);
        paint.setAlpha(250);        

     
    }

    int  View_introLay=1;
    public void onDraw(Canvas canvas) {

        if (parent.vfdrawing > 0) 
            return;
        
        
        if(!parent.StatusBarEnable)
        {
        	parent.GetStatusBarSize();
        }
        
        
        if(View_introLay>0 && parent.Camera_initOK)
        {
//        	parent.mIntro_layout.setVisibility(View.GONE);
        	View_introLay=0;
        }
        
        //parent.check_status();
        parent.vfdrawing = 1;  
        parent.CheckScreenDirection(1);
                
        CanvasDraw(canvas);
        if(parent.Captured_ModeEnable==0)
        {
        	//parent.DisplayLPRMark(canvas,paint);
        }
 		else
        {
	        if( mBitmap!=null)
	        	canvas.drawBitmap(mBitmap, 0, 0, mPaint);
	        
	        canvas.drawPath(mPath, mPaint);
        }
        parent.DisplayLPRMark(canvas,paint);
        
        parent.vfdrawing = 0;

  	      	
    }
  
 
    public void check_LineWordvalid(int sx,int sy,int ex,int ey)
    {
    	int i,n,x1,x2,y1,y2;
    	Rect rt = new Rect(0, 0, 0, 0);
    	int fact1000=parent.ScreenSize.y*1000/(parent.ScreenSize.y-parent.StatusBarHeight) ;    	 
    	
    		if(sx>ex){
    			x1=sx; sx=ex; ex=x1;
    		}
    		if(sy>ey){
    			y1=sy; sy=ey; ey=y1;
    		}
   
    } 
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private int touchdn_status=0;
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        	//check_LineWordvalid((int)x, (int)y, (int)mX, (int)mY);
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        if(mCanvas!=null)
        	mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    public Bitmap  mBitmap=null;
    
    private int mvWidth=0,mvHeight=0;
    public boolean vTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY()-parent.StatusBarHeight;
        int   margin=8,limit=80+80+20,ylimit=160;

        
        //if(parent.Captured_ModeEnable==0 || parent.OnCapture_status!=CAPTURE_GotTargetData )
        //	return false;        
        //int fact1000=parent.ScreenSize.y*1000/(parent.ScreenSize.y-parent.StatusBarHeight) ;
        
        if (parent.Captured_ModeEnable!=0 && parent.ipp_image!=null) {
	        switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                touch_start(x, y);
	                invalidate();
	                break;
	            case MotionEvent.ACTION_MOVE:
	                touch_move(x, y);
	                invalidate();
	                break;
	            case MotionEvent.ACTION_UP:
	                touch_up();
	                invalidate();
	               // checkValid_ScanTrans();
	                break;
	        }
	        return true;
        }	
        else  {
        	
        	if(parent.Captured_ModeEnable!=0)
        		return false;
        	
	        switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	touchdn_status=0;
	            	if(x>=rScanArea.left-margin && x<=rScanArea.left+80 && y>=rScanArea.top-margin && y<=rScanArea.top+80)
	            		touchdn_status=1;
	            	else if(x>=rScanArea.right-80 && x<=rScanArea.right+8 && y>=rScanArea.top-margin && y<=rScanArea.top+80)
	            		touchdn_status=2;
	            	else if(x>=rScanArea.left-margin && x<=rScanArea.left+80 && y>=rScanArea.bottom-80 && y<=rScanArea.bottom+8)
	            		touchdn_status=3;
	            	else if(x>=rScanArea.right-80 && x<=rScanArea.right+8 && y>=rScanArea.bottom-80 && y<=rScanArea.bottom+8)
	            		touchdn_status=4;
	            	else if(x>=rScanArea.left+10 && x<=rScanArea.right-10 && y>=rScanArea.top+10 && y<=rScanArea.bottom-10)
	            		{
	            			touchdn_status=5;
	            			mvWidth=rScanArea.right-rScanArea.left+1;
	            			mvHeight=rScanArea.bottom-rScanArea.top+1;
	            		}
	            	
	            	if(touchdn_status>0)
	            	{
	            		  mX = x;  mY = y;
	            		  invalidate();
	            	}
	            	if(touchdn_status==0)
	                 return false;
	            	
	            	break;
	                
	            case MotionEvent.ACTION_MOVE:
	                if(touchdn_status==1){
	                	rScanArea.left+=x-mX;  rScanArea.top+=y-mY;
	                	if(rScanArea.left< maxScanArea.left) rScanArea.left= maxScanArea.left;	                	
	                	if(rScanArea.left> rScanArea.right-limit) rScanArea.left= rScanArea.right-limit;
	                	if(rScanArea.top< maxScanArea.top) rScanArea.top= maxScanArea.top;
	                	if(rScanArea.top> rScanArea.bottom-ylimit) rScanArea.top= rScanArea.bottom-ylimit;
	                }
	                else if(touchdn_status==2){
	                	rScanArea.right+=x-mX;  rScanArea.top+=y-mY;
	                	if(rScanArea.right> maxScanArea.right) rScanArea.right= maxScanArea.right;
	                	if(rScanArea.right< rScanArea.left+limit) rScanArea.right= rScanArea.left+limit;	                	
	                	if(rScanArea.top< maxScanArea.top) rScanArea.top= maxScanArea.top;
	                	if(rScanArea.top> rScanArea.bottom-ylimit) rScanArea.top= rScanArea.bottom-ylimit;
	                }
	                else if(touchdn_status==3){
	                	rScanArea.left+=x-mX;  rScanArea.bottom+=y-mY;
	                	if(rScanArea.left< maxScanArea.left) rScanArea.left= maxScanArea.left;	                	
	                	if(rScanArea.left> rScanArea.right-limit) rScanArea.left= rScanArea.right-limit;                	
	                	if(rScanArea.bottom> maxScanArea.bottom) rScanArea.bottom= maxScanArea.bottom;
	                	if(rScanArea.bottom< rScanArea.top+ylimit) rScanArea.bottom= rScanArea.top+ylimit;
	                }
	                else if(touchdn_status==4){
	                	rScanArea.right+=x-mX;  rScanArea.bottom+=y-mY;
	                	if(rScanArea.right> maxScanArea.right) rScanArea.right= maxScanArea.right;
	                	if(rScanArea.right< rScanArea.left+limit) rScanArea.right= rScanArea.left+limit;	                	
	                	if(rScanArea.bottom> maxScanArea.bottom) rScanArea.bottom= maxScanArea.bottom;
	                	if(rScanArea.bottom< rScanArea.top+ylimit) rScanArea.bottom= rScanArea.top+ylimit;
	                }
	                else if(touchdn_status==5){
	                	rScanArea.left+=x-mX;  rScanArea.top+=y-mY;
	                	rScanArea.right=rScanArea.left+mvWidth-1;  
	                	rScanArea.bottom=rScanArea.top+mvHeight-1;
	                }
	                mX=x; mY=y;
	                invalidate();
	                break;
	            case MotionEvent.ACTION_UP:
	                
	                if(rScanArea.left< maxScanArea.left) rScanArea.left= maxScanArea.left;	                	
                	if(rScanArea.left> rScanArea.right-limit) rScanArea.left= rScanArea.right-limit;
                	if(rScanArea.top< maxScanArea.top) rScanArea.top= maxScanArea.top;
                	if(rScanArea.top> rScanArea.bottom-ylimit) rScanArea.top= rScanArea.bottom-ylimit;
                	if(rScanArea.right> maxScanArea.right) rScanArea.right= maxScanArea.right;
                	if(rScanArea.right< rScanArea.left+limit) rScanArea.right= rScanArea.left+limit;	                	
                	if(rScanArea.bottom> maxScanArea.bottom) rScanArea.bottom= maxScanArea.bottom;
                	if(rScanArea.bottom< rScanArea.top+ylimit) rScanArea.bottom= rScanArea.top+ylimit;
                	
	                invalidate();
	                if(touchdn_status==0)
		                 return false;
	                touchdn_status=0;
	                
	                break;
	        }
	        return true;
        }
        
    }
  
    
    public void drawViewInvalid() {
        invalidate();
    }

}
