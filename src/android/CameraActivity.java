
package com.cardcam.scantrans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Calendar;

//import com.cardcam.lprdemo.R;
import com.cardcam.camera.CameraControl;
import com.cardcam.carnum.CarNumRcgn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.os.Debug;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.MediaController;
import android.widget.RelativeLayout.LayoutParams;


public class CameraActivity extends Activity implements SurfaceHolder.Callback
        /*, RelativeLayoutDetectsSoftKeyboard.Listener // nib 07/11/13 54080 killed */
{
    private static final String TAG = CameraActivity.class.getSimpleName();

    private	static 	final String SaveImage_path = "/CarNumRcgn/SaveImg/";
    static String AutoImage_path = "/CarNumRcgn/imgdata/";

    public int Captured_ModeEnable=0;
    // 0 only preview  1=capture  2= fling preview,capture
    public int Clip_ModeEnable=1;

    private MessageHandler handler=null;

    public ViewDraw viewdraw;

    public int  PreviewResMode=0;
    private int hasSurface=0;
    public int  selectWordIndex=-1;

    public int 	OccupiedArea=0;
    public int  IppBitmap_AccuracyMode=0;
    public int 	LPRprevDet=0;
    public int 	detProcessStatus=0;
    public int 	ImgButtonStatus=0;
    private String characterSet;
    public static String LPRNoti;

    private SoundManager mSoundManager=null;

    public Bitmap ipp_image=null;
    private Bitmap ipp2_image=null; // half Image
    private Bitmap trans_image=null; // for animation Image
    private Bitmap dipl_image=null;

    public int Screen_width0=0;
    public int Screen_height0=0;
    public int iPage_Screen_width=0;
    public int iPage_Screen_height=0;
    public float iPage_adjSbarRat=0;
    private int LPR_left=0, LPR_top=0, LPR_right=0, LPR_bottom=0;

    // =========================================================================================
    int result = -1;

    public int UnitBlkInfo = 5, PhraseBlkSn = 40;

    //public int sel_language = 0, change_language = 0, change_txtlang = 0;

    public boolean Quit_flg=false;

    public Typeface mFace=Typeface.DEFAULT;		// nib 02/13/13 private -> public

    public String PhoneModel="",PhoneVersion="";
    public boolean ClipBubleEnableVersion=false; // true: >=4.2.0

    private int dialogID=0;
    private int mDialogID=0;
    String fileName="";
    String inform_msg="";
    // ==============================================================================================

    public String Word2Str = "";


    public int ProcessStatus = 0, init_ProcessStatus = -1, AddWord_flg = 0;

    public Point ScreenSize = new Point(800, 480);

    public Point SetCaptureSize= new Point(2048, 1200);

    SurfaceView camView=null;

    private long startTranslation_TM=0;

    // ==================1024*768=================================

    // ====================================================
    public Rect rcArea = new Rect(10, 62, 470, 250);

    public int beOtherMean = 0;

    //public int Touch_whichcorn = 0, Touch_capMove = 0, enable_leftMenu = 1;

    public long TouchMenuTime = 0;

    public long decodeTm = 0, End_tm = 0,Touch_tm=0, Resume_tm=0, CamOpenTm=0;

    public int  NthFrame=0;

    public int vfdrawing = 0;

    private int mX, mY, mmX, mmY;

    public  final static int IPP_PREVIEW =1;
    public  final static int IPP_IMAGEGOT =2;
    public  final static int IPP_IMAGEDONE =3;
    public  final static int IPP_RESTART =10;


    public  final static int DET_CAPTURE_Start =10;
    public  final static int DET_CAPTURE_Ready =20;
    public  final static int DET_CAPTURE_GetInf =30;
    public  final static int DET_PERSPECT_OnANI =40;
    public  final static int DET_PERSPECT_Doing =50;
    public  final static int DET_PERSPECT_Done =60;

    public  final static int CAPTURE_START	=10;
    public  final static int CAPTURE_GotImage =20;
    public  final static int CAPTURE_GotTargetData =40;
    public  final static int CAPTURE_DoneTranslate =50;

    public  final static int VIEW_INVISIBLE = 100;
    public  final static int VIEW_MINIMUM = 101;
    public  final static int VIEW_SIMPLE = 102;
    public  final static int VIEW_FULL = 103;
    public  final static int VIEW_REFRESH = 104;


    private final static int DISMISS_PROGRESSDIALOG = 10001;
    private final static int MSG_TURNOFF_FLASH = 10002;
    public  final static int MSG_ReleaseWAKELOCK= 10003;
    private final static int MSG_INIT_CAMERA = 10004;
    private final static int MSG_CaptureNoChg = 10005;
    private final static int MSG_ShareWare = 10006;
    private final static int MSG_SettingMode = 10007;
    private final static int MSG_VoiceMode = 10008;
    private final static int MSG_ProgBarCancel = 10009;
    private final static int MSG_ReqCaptureFrame= 10010;
    private final static int MSG_OffLineNotice = 10011;
    private final static int MSG_NOTSUPPORT_DLG = 10012;
    private final static int MSG_FLING_ParmLay = 10013;
    private final static int MSG_TURNON_FLASH = 10014;
    private final static int MSG_Close_CAMERA = 10015;
    private final static int MSG_LPRNoti = 100016;
    private final static int MSG_ShowIME	= 10017;
    private final static int MSG_TRANSABNormal= 10018;
    private final static int MSG_BlockAddList= 10019;
    private final static int MSG_VtCallFin= 10020;
    private final static int MSG_finish= 10021;
    private final static int MSG_CancelEdit= 10022;
    private final static int MSG_mIPkgService= 10023;
    private final static int MSG_InstallDialogCheck= 10024;
    private final static int MSG_endPageRcgnProcess= 10025;
    private final static int MSG_startAnimation= 10026;
    private final static int MSG_nextAnimation= 10027;
    private final static int MSG_CaptureREQ= 10028;
    private final static int MSG_CAPTURE_RETURN= 10029;
    private final static int MSG_CAPTURE_READY= 10030;
    private final static int MSG_StatusNoti= 10031;
    private final static int MSG_RetryGuide= 10032;
    private final static int MSG_RetryGuideOff= 10033;
    private final static int MSG_SetInform= 10034;
    private final static int MSG_SetImage= 10035;
    private final static int MSG_BMPLPR= 10036;
    private final static int MSG_STEPLPR= 10037;
    private final static int MSG_LPRImgRcgn= 10038;
    private final static int MSG_IPPSave= 10039;

    private final static int MSG_LPRprocessing= 10110;
    private final static int MSG_CAPTURE_PerspectTrans= 10111;

    private final static int MSG_SetViewImage= 10120;
    private final static int MSG_SetTransImage= 10130;
    private final static int MSG_EndProcess= 10140;


    public int TchVal_cMenuProc = 0x0001; // capture Image Menu-Processing

    public int TchVal_MarkOff = 0x0002; //

    public int TchVal_GetFocus = 0x0004;

    public int TchVal_sdcardFsave = 0x0008;

    public int TchVal_sdcardFRead = 0x0010;

    public int TchVal_Language = 0x0020;

    public int TchVal_Internet = 0x0040;

    public int TchVal_Capture = 0x0080;

    public int TchVal_ActWords = 0x0100;

    public int TchVal_Flash = 0x0200;

    public int TchVal_phrase = 0x0400;

    public int TchVal_Dictionary = 0x0800;

    public int TchVal_LineTrans = 0x1000;

    public int TchVal_Document = 0x2000;

    public int TchVal_RcgnWork = TchVal_phrase | TchVal_Dictionary | TchVal_LineTrans
            | TchVal_Document;

    public int TchVal_DocDir_V = 0x4000;

    public int TchVal_AddClip = 0x8000;

    public int TchVal_History = 0x10000; //

    public int TchVal_CompMoire = 0x20000; // smooth for moire

    public int CaptTextArea_V_sx = 540;

    public int CaptTextArea_H_sy = 360;

    public int Tch_Mode = TchVal_Dictionary;

    public int Tch_flashMode =0;
    public int Tch_captureMode =TchVal_Capture;

    private long[] MenuTime = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }; // 20ea

    private int Touch_zv0, Touch_x0 = 0, Touch_y0 = 0, Touch_x1 = 0, Touch_y1 = 0;

    public int maxZoom, Touch_zv, svTouch_zv;

    public int beTransDocuStr = 0, beTrnsLineResult = 0, beTrnsDocuResult = 0, beDictResult = 0;

    public int AutoAdjust_LineHeight = 0;

    private int NumChangeRes = 0;


    public long DnSetTime = 0;

    public boolean DnSetting = false;

    public float[] HeXa_magnif = {
            1.0f, 1.2f, 1.5f, 1.75f, 2.0f, 2.25f, 2.5f, 2.75f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.25f,
            7.5f
    };

    public String TranStr = "";

    public String FromStr = "", t_FromStr = "";

    public int t_wordtype=0,wordtype2n=0;
    //public int ReqType = 0;
    //public String ReqString = "";

    public int WordClipView_sIdx, WordClipView_eIdx;

    public String TransDocustr = "";

    public String iViewStr = "";

    public String FromVstr = new String();

    public String TranVstr = new String();

    public int MAX_DicWord = 300;
    public int MAX_StrNum = 300;
    public int docuEndStatus=190;

    public int TrStrSize = 512;
    public int DocuStrSize = 512;
    public int WordStrSize = 128;
    public int MAX_ClipBdSize = 4096;

    //public byte[] ClipBd_Buff = new byte[MAX_ClipBdSize];

    public byte[] TrLnStr = new byte[4 * TrStrSize]; // new byte[512];

    public int PhraseNum = 0, Crossidx = 0;

    public byte[] BufStr = new byte[TrStrSize];

    public int[] DocuLnLength = new int[MAX_DicWord];

    public int[] TrLnStrLength = new int[8];

    public int Clip_Num = 0, WordClipNum = 0;

    public int DocuLineNum = 0;

    public int TrLineChgnum = 0, TrLineChgnum1 = 0;

    public int cProcessStatus = 0;
    public int docu_autofocus = 0;


    public long DocuDirChgDTm = 0, txtScroll_tm = 0, WingScroll_tm = 0, zoom_tm = 0, zoom_sm = 0,
            Ready_tm = 0;

    public int res_chgstatus = 0,zoom_point=0;

    public int BtMode_touch = 0, Touchzoom_mode = 0;

    public int BtMode_x1 = 0, BtMode_x2, BtMode_y1, BtMode_y2;


    public int DicTextSize = 24;

    private boolean KeepScreenOn = true;

    private boolean AutoLPR = false;

    public int NonShaker = 0;

    public int ScreenDirection = 90, t1_ScreenDirection = 360; // 0=landscape

    public int Phrase_BlkNum = 0;

    public int OnCapture_status = 0;

    public boolean SurfaceChanged_tag = false;

    public boolean  Zooming_flg=false;

    public int  Dimming_flg=0;

    private long dialog_tm=0;


    public int StatusBarHeight=0;

    public static boolean Iamhere=false;
    // =========optional Menu======================================

    public int chk_nonChgImg = 0;

    public int Flingvx = 0, Flingvy = 0;

    private int canFlash=0,canFlsh_checked=0;

    UnlockReceiver unlockReceiver=null;

    public int transMode = 0;
    public boolean translating_flg = false;
    private boolean trans_URLOK=false;
    // ===========================================
    //private boolean reqBindservice_available = true;

    // private int reqBindservice_num = 0;

    public int Transing_fail = 0;

    private long  fullDnUp_tm=0;

    private boolean onUI_flg=false;

    public char FormTxt_mark = 0x25cf; //

    public char TransTxt_mark = 0x25b8; //

    public int WordCopyNum = 0;

    public int right_margine = 0;
    public int bottom_margine = 68;

    volatile public int TransScrollTxt_reNew = 0;

    volatile public int menu_drawable = 1;

    public int Docu_Direction = 0;

    public boolean  transModeChanging = false;

    private int mProgressType=0;


    public int BoundTryNum= 0;
    public int moreFreeDictNum= 0;

    private int Request_Activity = 0, t_Request_Activity = 0;
    private int chk_Request_Activity=0;

    private int onActCamera = 0;

    public int tDic_Mode = 0, Dic_Mode = 0x11;

    public int TchVal_DicSel = 1;

    public int mTitleColor, MeanTextColor;

    public int DictSn = 0, DictEn = 0, DictCn = 0;

    //public String titleStr="" ;
    // public String meaningStr="" ;
    // public String t_meaningStr="" ;
    public String DictTitleStr = "";

    public int DictSuid = 0, DictPos = 0, CurDictType = 0xB04;

    // public int DictSuid2 =0, mDicType2 = 0xB04;
    public int ClipList_id = 0, ClipList_id_px = 0, ClipList_id_py = 0;

    //public int ActiveConPreview = 10, t_ActiveConPreview = 10;

    volatile public int ViewClipList_chgflag = 0; // display

    public int FontSizeType = 2;

    public int ClipList_height = 200;

    public int BottomMenu_ht = 72;

    public boolean DictSelecting = false;

    public int DictType_ID = 0, t_DictType_ID = 0;

    LinearLayout loginLayout = null;

    public long IppTime=0;

    private int CAM_width=0,CAM_height=0;
    public boolean IppCaptureMode=false;
    // ======================= one flg ==============================
    CheckBox checkbox;

    String status_msg="";
    public int IndexConnectChk_OK=10;
    public int IndexConnectChk_NO=1;

    public boolean DictbyGooglTrans = false;

    private int NetCom_Tch_Mode = 0;

    private boolean checkBox_flg = false;

    public int DictReady_OK = 0;

    public int Req_GetDictData = 0;

    public int NetWork_UseTemporal = 0;
    public int init_DictSetLang = 0;
    public int PerProcess = 0;

    float dpiscale;

    int langItemHeight = 40;

    public RelativeLayout mIntro_layout,buttons_Layout,checkboxs_Layout;

    //public RelativeLayout mFlash_layout;	// nib 01/30/13 added


    // to here -------------------

    private RelativeLayout mText_MsgLay;


    private ImageButton mShutterBtn,mFlashBtn;

    // private ImageView idxConnectArea;
    public  RelativeLayout CaptureImgLay=null;
    public  ImageView CaptureImgView=null;
    public  TextView  mText_MsgView=null;
    public  TextView  mText_RetryGuide=null;

    public  TextView  version_Msg=null;
    public String mtype_QPATH ;
    public int IPP_animateMode=1;

    ByteBuffer CaptureImg32=null;

    long intsall_tm = 0;

    public boolean Camera_initOK=false;
    private boolean Camara_resume=false;
    private boolean ScreenLockPass=false;


    // =========================================================================
    private int mSoundID;
    private SoundPool mSound_pool,mSound_effect;
    private  AudioManager  mAudioManager;

    Intent intent = new Intent();

    private SurfaceView mSurfaceView;

    MediaController mediaController=null;

    // to here -----------------
    WakeLock wakeLock = null;

    private String appName=null,appNamek=null;
    private boolean kor_version=false;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        setContentView(getResources().getIdentifier("activity_camera", "layout", getPackageName()));


//        mIntro_layout = (RelativeLayout)findViewById(R.id.intro_lay);
//        mIntro_layout.setVisibility(View.VISIBLE);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);		// nib 03/15/13

        viewdraw = new ViewDraw(this);

        ((FrameLayout)findViewById(getResources().getIdentifier("viewdraw", "id", getPackageName()))).addView(viewdraw);

        kor_version=false;
        hasSurface=0;

        int validpkg=CarNumRcgn.SetActivity(this);
        PhoneVersion=android.os.Build.VERSION.RELEASE;
        PhoneModel=android.os.Build.MODEL;
        Tch_Mode =TchVal_Document;
        Tch_captureMode =TchVal_Capture;

        getPermissions();
    }

    public  Context TransCamCtx(){
        return getApplicationContext();
    }

    private void getPermissions() {
        // API 23 (6.0 marshmallow) 이상인 경우, runtime 시 저장장치, 카메라, 마이크 접근 허용을 받아야 사용할 수 있다.
        // 그렇지 않으면, 예를 들어, Camera.open() 에서 RuntimeException 발생.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permitted = PermissionChecker.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA);
            // data가 저장될 path를, internal/external FilesDir()을 사용하면 (see Utils.GetFilePath()),
            // 앱 자신이 read/write가능한 앱 자체 data 영역으로 잡아주므로,
            // 아래 저장장치에 대한 runtime permission은 굳이 필요없다.
            permitted += PermissionChecker.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permitted != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            granted = false;
                            break;
                        }
                    }
                }
                else {
                    // if grantResult length is 0, permission denied.
                    granted = false;
                }
                if (!granted) {
                    Toast.makeText(CameraActivity.this,"앱 실행을 하기 위해서는 모든 허용이 필요합니다.",Toast.LENGTH_LONG).show();
                    CameraActivity.this.finish();		// 앱 종료
                }
                break;
        }
    }


    private String  mPathDirectory() {
        String MainDir= Environment.getExternalStorageDirectory().getPath()+"/CarNumRcgn";
        String PathDir1=Environment.getExternalStorageDirectory().getPath()+SaveImage_path;
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

    boolean init_flg=false;
    void init_setting()
    {
        if(init_flg) return;
        CarNumRcgn.SetExternalStorage( mPathDirectory());
        init_flg=true;
    }

    @Override
    protected void onStart() {

        // File SdCard= Environment.getExternalStorageDirectory();

        initializeUI();
        transMode = 0;
        LPRprevDet=0;
        ImgButtonStatus=0;
        mSurfaceView = (SurfaceView)findViewById(getResources().getIdentifier("preview_view", "id", getPackageName()));
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        CheckScreenDirection(0); // get screen size
        dpiscale = getResources().getDisplayMetrics().density;
        //iPage.dpiScale=dpiscale;

        if (ScreenSize.x >= 1200)
            FontSizeType = 2; // 32
        else
            FontSizeType = 1; // 24;

        handler=null;

        int wd = ScreenSize.x;
        int ht = ScreenSize.y;
        if (wd < ht) {
            wd = ScreenSize.y;
            ht = ScreenSize.x;
        }

        right_margine = 0; //(int)getResources().getDimension(R.dimen.bottom_margin);
        bottom_margine = 40;


        CaptureImgLay = (RelativeLayout)findViewById(getResources().getIdentifier("imgView_Lay", "id", getPackageName()));
        CaptureImgView = (ImageView)findViewById(getResources().getIdentifier("imgView", "id", getPackageName()));
        version_Msg = (TextView)findViewById(getResources().getIdentifier("version_msg", "id", getPackageName()));
        //version_Msg.setText(CarNumRcgn.GetVersion());
        version_Msg.setText(CarNumRcgn.GetDescription());
        init_setting();

        mediaController = new MediaController(this);

        long tm = System.currentTimeMillis();

        for (int i = 0; i < 20; i++)
            MenuTime[i] = tm;

        DnSetTime = tm;
        TouchMenuTime = tm;

        End_tm = decodeTm = tm;
        End_tm= tm-100000;
        Ready_tm = tm;
        zoom_tm= zoom_sm = tm - 10000;

        CameraControl.init(getApplication());
        svTouch_zv= Touch_zv0 = Touch_zv = 1;


        init_Layout();

        //mSound_pool = new SoundPool(6, AudioManager.STREAM_SYSTEM, 0);
        mSound_pool = new SoundPool(6, AudioManager.STREAM_SYSTEM, 0);
        mSound_effect = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        mSoundID=-1;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //mSound_pool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        //mSoundID= mSound_pool.load(this,R.raw.qtranslator, 1);


        PhoneVersion=android.os.Build.VERSION.RELEASE;
        PhoneModel=android.os.Build.MODEL;


        super.onStart();

    }



    public void clear_ippBitmap()
    {
        if(ipp_image!=null && !ipp_image.isRecycled()) {
            ipp_image.recycle();
            ipp_image=null;
        }
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        //bmp1 = convertToMutable(bmp1);
        Canvas canvas = new Canvas(bmp1);
        bmp2 = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth(), bmp1.getHeight(), true);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmp1;
    }

    int IppStatus=IPP_PREVIEW;
    int captured_angle=0;
    int t_ScreenDirection=0;
    private Bitmap imgv=null;

    public void clearCaptureImgv()
    {
        int wd,ht,wd2,ht2,ddeg;

        if(ImgButtonStatus>0)
            return ;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(imgv!=null)
                {
                    imgv.recycle();
                    if(IppStatus<=IPP_PREVIEW)
                    {
                        CaptureImgLay.setVisibility(View.INVISIBLE);
                    }
                }
                if(IppStatus<=IPP_PREVIEW) return;

                CaptureImgLay.setVisibility(View.INVISIBLE);

                if(imgv!=null)
                    imgv.recycle();
                imgv=null;
                //IppStatus=IPP_PREVIEW;
                t_ScreenDirection=ScreenDirection;

            }
        });

        return ;

    }

    public void SetCamviewLayout()
    {

        SurfaceView camView=(SurfaceView) findViewById(getResources().getIdentifier("preview_view", "id", getPackageName()));
        if(camView==null)
            return;

        LayoutParams params = (LayoutParams)camView.getLayoutParams();

        params.width=ScreenSize.x;
        params.height=ScreenSize.y;

        params.leftMargin =0;
        params.topMargin =0;
        params.rightMargin=0;
        params.bottomMargin=0;

        //params.setMargins(params.leftMargin, params.topMargin,0, 0);
        camView.setLayoutParams(params);

        View v=(View) findViewById(getResources().getIdentifier("PrevLPR", "id", getPackageName()));
        params = (LayoutParams)v.getLayoutParams();
        params.rightMargin=Getdp(6);
        v.setLayoutParams(params);
    }
    public void SetCaptureImgLayout(int wd,int ht)
    {
        LayoutParams params = (LayoutParams)CaptureImgView.getLayoutParams();
        int loc[] = { 0, 0, 0, 0 };

        findViewById(getResources().getIdentifier("viewdraw", "id", getPackageName())).getLocationOnScreen(loc);
        //loc[1]=0;
        iPage_Screen_width=ScreenSize.x;
        iPage_Screen_height=ScreenSize.y-loc[1];

        float div=(float)wd/(float)ht;
        float div2=(float)iPage_Screen_width/(float)iPage_Screen_height;
        if(div>div2)
        {
            iPage_Screen_height=iPage_Screen_width*ht/wd;
        }
        else
        {
            iPage_Screen_width=iPage_Screen_height*wd/ht;
        }

        params.width=iPage_Screen_width;
        params.height=iPage_Screen_height;

        params.leftMargin =(ScreenSize.x-iPage_Screen_width)/2;
        params.topMargin =(ScreenSize.y-iPage_Screen_height)/2;
        params.rightMargin=0;
        params.bottomMargin=0;

        //params.setMargins(params.leftMargin, params.topMargin,0, 0);
        CaptureImgView.setLayoutParams(params);

    }

    public Bitmap setCaptureImgbitmap(boolean imgok)
    {
        getScreenSizeDirection();

        if(t_ScreenDirection==ScreenDirection &&  IppStatus>=IPP_IMAGEDONE)
            return null;

        IppStatus=IPP_IMAGEDONE;
        OnCapture_status=CAPTURE_GotImage;
        t_ScreenDirection=ScreenDirection;

        SetCaptureImgLayout( ipp_image.getWidth(), ipp_image.getHeight());

        if(imgv!=null)
            imgv.recycle();

        try {
            imgv= Bitmap.createScaledBitmap(ipp_image, iPage_Screen_width, iPage_Screen_height, true);
            //imgv= Bitmap.createBitmap(imgbitmap, 0, 0, imgbitmap.getWidth(), imgbitmap.getHeight()-1);
            Drawable d = new BitmapDrawable(getResources(),imgv);
            // nib 09/23/13 setBackground() can be worked only level16 and higher
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                Show_ToastString("Current OS level is "+android.os.Build.VERSION.SDK_INT+". It must be 16 and higher");

                CaptureImgView.setBackgroundDrawable(d);
                //return null;
            }
            else
                CaptureImgView.setBackground(d);
            // ---------------------------------------------
        }
        catch(Exception e) {
            imgv=null;
            CaptureImgLay.setVisibility(View.INVISIBLE);
            IppStatus=IPP_IMAGEGOT;//1;
            return null; //353479
        }
        catch(OutOfMemoryError e) {
            ipp_image=null;
            CaptureImgLay.setVisibility(View.INVISIBLE);
            IppStatus=IPP_IMAGEGOT; //
            return null; //353479
        }

        CaptureImgView.setVisibility(View.VISIBLE);
        CaptureImgLay.setVisibility(View.VISIBLE);

        return imgv;
    }

    private boolean SetCaptueImgStatus() {

        OnCapture_status=CAPTURE_START;
        LPRprevDet= 0;
        OnCapture_status=CAPTURE_GotImage;
        IppCaptureMode=true;

        //clear_ippBitmap();

        CameraControl.camControl.mCameraFlash(0);
        StopPreview(false);
        FlashBtn_OnOff(false);
        IppStatus=IPP_IMAGEGOT;
        LPRprevDet=0;
        if(handler != null)
        {
            handler.removeMessages(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
        }
        CameraControl.camControl.CloseCamera();

        detProcessStatus=DET_CAPTURE_Ready;

        OnCapture_status=CAPTURE_GotImage;
        mHandler.sendEmptyMessage(MSG_CAPTURE_READY);
        mHandler.sendEmptyMessage(MSG_RetryGuide);


        return true;
    }

    private boolean IPPImgSave() {   //by selPhotoPath

        (new Thread(RunIPPSave)).start();
        return true;
    }


    void IPPSaveFile()
    {
        if(ipp_image==null || ipp_image.isRecycled()) return;
        BitmapSave(ipp_image);
    }

    private Runnable RunIPPSave = new Runnable() {
        public void run() {
            IPPSaveFile();
        }
    };

    private boolean LPRStepRcgn() {

        SetCaptueImgStatus();

        selPhotoPath = GetAutoImageFileNmae(0);
        cropArea[0]=0;
        cropArea[1]=0;
        cropArea[2]=0;
        cropArea[3]=0;

        runfileProcess();
        return true;
    }
    private boolean LPRImgRcgn() {   //by selPhotoPath

        SetCaptueImgStatus();
        ImgButtonStatus=1;
        cropArea[0]=0;
        cropArea[1]=0;
        cropArea[2]=0;
        cropArea[3]=0;

        //(new Thread(RunLPR)).start();
        runfileProcess();
        if(handler != null)
        {
            handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
        }
        return true;
    }

    private boolean SetCam_Preview() {

        LPRprevDet=0;
        ImgButtonStatus=0;
        clear_ippBitmap();
        IppStatus=IPP_PREVIEW;
        OnCapture_status=0;
        mHandler.sendEmptyMessageDelayed(MSG_INIT_CAMERA,200);
        mHandler.removeMessages(MSG_STEPLPR);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewInvalid();
            }
        });
        return true;
    }

    private boolean GetLPRImg_byGallery() {

        SetCaptueImgStatus();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*.jpg");
        startActivityForResult(intent, 1);
        return true;
    }

    boolean ing_flg=false;
    int gFileIndex=0;
    int totalFilenum=0;
    int fixindex=0;
    int iterrationNum=0;
    String prevfilleName;
    private String GetAutoImageFileNmae(int idxmode) {
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        String filePath = "";

        File fAutoImageDir = new File(sdcardPath+AutoImage_path);
        String[] listFile = fAutoImageDir.list();
        Arrays.sort(listFile);
        prevfilleName = fileName;

        //gFileIndex=21; // 18  for test index
        //if(gFileIndex<11)
        //	gFileIndex=11;

        ing_flg=true;

        fileName=null;
        totalFilenum=listFile.length;
        if(listFile.length>0)
        {
            gFileIndex+=idxmode;

            if( gFileIndex<0)
                gFileIndex+=listFile.length;
            if( gFileIndex>=listFile.length)
            {
                gFileIndex=0;
                AutoLPR=false;
                Log.e(TAG, " AutoText End  ");
            }

            if(fixindex>0)
                gFileIndex=fixindex;

            fileName = listFile[gFileIndex];
            filePath = sdcardPath+ AutoImage_path + fileName; //listFile[gFileIndex];

            Log.e(TAG, "file-index["+gFileIndex+"/"+ totalFilenum+ "]"+filePath);

            inform_msg=   "File Name ["+gFileIndex+"]:  "  + fileName;




            if(idxmode==0)
                gFileIndex++;

            String Fileidxstr=""+gFileIndex;
            //edtxt.setText(Fileidxstr);

            if(gFileIndex >= listFile.length) {
                gFileIndex = 0;
                iterrationNum++;
                AutoLPR=false;

                if(KeepScreenOn==true)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewdraw.setKeepScreenOn(false);
                        }
                    });
                    KeepScreenOn = false;
                }
            }
        }
        else filePath=null;

        mHandler.sendEmptyMessage(MSG_SetInform);

        return filePath;
    }

    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;

    }

    String selPhotoPath="";
    private void runfileProcess() {

        if(selPhotoPath==null)
        {
            return;
        }
        if(ipp_image!=null && !ipp_image.isRecycled())
            ipp_image.recycle();
        ipp_image=null;

        try
        {
            ipp_image = BitmapFactory.decodeFile(selPhotoPath);
        }
        catch(Exception e) {
            ipp_image=null;
            return;
        }
        catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError 01");
            if(ipp_image!=null && !ipp_image.isRecycled())
                ipp_image.recycle();
            ipp_image=null;
        }

        if(ipp_image==null)
        {
            inform_msg=   " Invalid filenName:"  + fileName ;
            //mHandler.sendEmptyMessage(MSG_SetStatus);
            mHandler.sendEmptyMessage(MSG_EndProcess);
            ing_flg=false;
            //clear_allimage();
            return ;
        }


        captured_angle=GetExifOrientation(selPhotoPath);

        if(captured_angle!=0)  // compensate Orientation Rotate
        {
            Bitmap tmp=CarNumRcgn.GetRotatedBitmap(ipp_image, captured_angle);
            if(ipp_image!=null && !ipp_image.isRecycled())
                ipp_image.recycle();
            ipp_image=null;
            if(tmp==null)
            {
                captured_angle=0;
                ing_flg=false;
                return;
            }
            ipp_image=tmp;
        }


        if(ipp_image==null)
        {
            mHandler.sendEmptyMessage(MSG_EndProcess);
            ing_flg=false;
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewInvalid();
            }
        });
        //CarNumRcgn_BitmapProcessing();
        mHandler.sendEmptyMessage(MSG_LPRprocessing);

    }

    private void clear_IPPimage()
    {
        CaptureImgView.setVisibility(View.INVISIBLE);

        LPRprevDet=0;
        status_msg=  "";

        if(ipp_image!=null)
            ipp_image.recycle();
        ipp_image=null;

    }

    private Runnable RunLPR = new Runnable() {
        public void run() {
            runfileProcess();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {

            ((Button)findViewById(getResources().getIdentifier("BmpLPR", "id", getPackageName()))).setEnabled(true);
            return;
        }
        if (requestCode == 1) {
            Uri selPhotoUri = data.getData();
            selPhotoPath = getPathFromUri(selPhotoUri);
            fileName = PathToFilename(selPhotoPath);
            Log.e(TAG, "getPathFromUri" + selPhotoPath);
            //clear_IPPimage();
            inform_msg = selPhotoPath;
            ImgButtonStatus= 1;
            mHandler.sendEmptyMessage(MSG_LPRImgRcgn);

        }
    }

    private String PathToFilename(String strPath) {
        File clsFile = new File(strPath);
        if (clsFile != null) {
            return clsFile.getName();
        }
        return "";
    }
    public Bitmap MenuButton_hdpiBitMap(int id) { // 90x30
        Bitmap BM;
        BM = BitmapFactory.decodeResource(getResources(), id);
        return BM;
    }



    public Bitmap MenuButton9_hdpiBitMap(int id, int width, int height) {
        Bitmap BM, tmp;

        tmp = BitmapFactory.decodeResource(getResources(), id);

        NinePatchDrawable np_drawable = new NinePatchDrawable(getResources(),tmp, tmp.getNinePatchChunk(), new Rect(), null);

        np_drawable.setBounds(0, 0, width, height);

        try {
            BM = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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


    private int BIT4LEN(int k) {
        return (k + ((4 - (k & 3)) & 3));
    }


    public int Getdp(float dp) {
        float dpiScale = getResources().getDisplayMetrics().density;
        return (int)(dp * dpiScale);
    }


    public void CpyrcArea(Rect s) {
        rcArea.left = s.left;
        rcArea.top = s.top;
        rcArea.right = s.right;
        rcArea.bottom = s.bottom;
    }


    private int getScreenDirection()   //captured_angle when image captured
    {
        WindowManager manager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
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

    public void getScreenSizeDirection()
    {
        int x,y;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        x=metrics.widthPixels;
        y=metrics.heightPixels;
        ScreenDirection = getScreenDirection();

        ScreenSize.x = getResources().getDisplayMetrics().widthPixels;
        ScreenSize.y = getResources().getDisplayMetrics().heightPixels;
        if(ScreenDirection==0 || ScreenDirection==180){
            if(ScreenSize.x<ScreenSize.y){
                x=ScreenSize.x; ScreenSize.x=ScreenSize.y; ScreenSize.y=x;
            }
        }
        else  if(ScreenSize.x>ScreenSize.y){
            x=ScreenSize.x; ScreenSize.x=ScreenSize.y; ScreenSize.y=x;
        }
    }

    public int CheckScreenDirection(int cfrom) // mode=0 : only get ScreenDir
    {

        getScreenSizeDirection();


        if (SurfaceChanged_tag) {
            Do_surfaceChange();
        } else if ((ScreenDirection == 0 && t1_ScreenDirection == 180)
                || (ScreenDirection == 180 && t1_ScreenDirection == 0)) {
            CameraControl.camControl.ChgPreviewResolution(ScreenDirection);
        }

        t1_ScreenDirection = ScreenDirection;

        return 1;
    }

    public int CheckScreenDirection_byPreview()
    {
        int s_Direction = ScreenDirection;

        getScreenSizeDirection();

        if (SurfaceChanged_tag) {
            Do_surfaceChange();
            t1_ScreenDirection = ScreenDirection;

            if (handler != null) {
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }

            return 1;
        } else if ((ScreenDirection == 0 && t1_ScreenDirection == 180)
                || (ScreenDirection == 180 && t1_ScreenDirection == 0)) {
            CameraControl.camControl.ChgPreviewResolution(ScreenDirection);

            t1_ScreenDirection = ScreenDirection;
            if (handler != null)
            {
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }
            return 1;
        }

        t1_ScreenDirection = ScreenDirection;
        if (s_Direction == ScreenDirection)
            return 0;

        return 1;
    }

    Handler mHander = new Handler();

    public boolean  mIsLandscapemode = false;

    private boolean checkLandscapeMode() {
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;

        if(w>h) mIsLandscapemode=true;
        else mIsLandscapemode=false;

        return mIsLandscapemode;
    }

    public static void sleepdelay(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ie) { // continue
        }
    }

    private void touchSoundEffect()
    {
        if (mSound_effect != null && mAudioManager != null)
        {
            int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            mSound_effect.play(-1, streamVolume, streamVolume, 1, 0, 1f);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            mSoundManager.playSound();	// nib 03/15/13
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            mSoundManager.playSound();	// nib 03/15/13
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long tm = System.currentTimeMillis();

        if(tm-Resume_tm>=0 && tm-Resume_tm<=1000) //2000-> 1000
            return false;


        if (keyCode == KeyEvent.KEYCODE_BACK) { // KeyEvent.KEYCODE_ENTER.

            if(tm-dialog_tm>=0 && tm-dialog_tm<=200)
                return false;

            if(Captured_ModeEnable!=0)
            {
                if(OnCapture_status>=CAPTURE_GotTargetData)
                {
                    OnCapture_status=CAPTURE_GotTargetData;
                    captureCommand_OnOff(Capture_CmdGotData);
                    captureCommand_OnOff(Capture_CmdCancelBtn);
                    return false;
                }
            }


            Quit_flg=true;
            DismisprogresssDialog();

            finish();
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void ViewInvalid() {
        viewdraw.drawViewInvalid();

    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };


    private void TouchEvent_Move_Zoom(long t, int x0, int y0, int x1, int y1) {
        int m, x, y, zv, zd, dx0, dy0, dx1, dy1;

        maxZoom = CameraControl.camControl.MaxZoomValue();

        if (Touchzoom_mode == 0) {
            zoom_tm = t;
            //if(zoom_point==0)
            zoom_sm=t-200;

            Touchzoom_mode = 1;
            Touch_x0 = x0;
            Touch_y0 = y0;
            Touch_x1 = x1;
            Touch_y1 = y1;
            Touch_zv0 = Touch_zv; // =Zoom_scale & 15;
        } else {
            zoom_tm = t;
            Touchzoom_mode = 2;
            dx0 = Touch_x1 - Touch_x0;
            if (dx0 < 0)
                dx0 = -dx0;
            dy0 = Touch_y1 - Touch_y0;
            if (dy0 < 0)
                dy0 = -dy0;
            dx1 = x1 - x0;
            if (dx1 < 0)
                dx1 = -dx1;
            dy1 = y1 - y0;
            if (dy1 < 0)
                dy1 = -dy1;

            if (dx0 > dx1)
                x = dx0;
            else
                x = dx1;
            if (dy0 > dy1)
                y = dy0;
            else
                y = dy1;

            if (x >= y)
                zd = dx1 - dx0;
            else
                zd = dy1 - dy0;

            if (ScreenSize.x > ScreenSize.y)
                m = ScreenSize.x / 3;
            else
                m = ScreenSize.y / 3;

            zd = maxZoom * zd / m;
            if (zd == 0)
                return;

            zv = Touch_zv;
            Touch_zv = Touch_zv0 + zd;
            if (Touch_zv < 0)
                Touch_zv = 0;
            else if (Touch_zv > maxZoom)
                Touch_zv = maxZoom;


            Zooming_flg=true;

            svTouch_zv=Touch_zv;

            //SelViewType=0;

            if (zv == Touch_zv)
                return;
            CameraControl.camControl.mCameraZoom(Touch_zv);
            ViewInvalid();
        }
    }

    public void ShowLong_ToastString(String str) {
        Toast toast= Toast.makeText(this, str, Toast.LENGTH_LONG);
        int yOffset=Getdp(125);
        toast.setGravity(Gravity.CENTER, 0, yOffset);
        toast.show();
    }

    public void Show_ToastString(String str) {
        Toast toast= Toast.makeText(this, str, Toast.LENGTH_SHORT);
        int yOffset=Getdp(125);
        toast.setGravity(Gravity.CENTER, 0, yOffset);
        toast.show();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean iret=myTouchEvent(event, 0, 0);
        //afterTouchEvent(event);
        if (!iret && event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_DOWN)
            ViewInvalid();

        return false;//true;
    }

//    public void afterTouchEvent(MotionEvent event) {
//
//        if (DictSelecting == false && sel_language == 0 && LangOpt != t_LangOpt) {
//            Message message = Message.obtain(handler, R.id.language_btn);
//            if (handler != null && message != null)
//                message.sendToTarget();
//            //startmenu_flg = false;
//        }
//
//        if (event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_DOWN)
//            ViewInvalid();
//        else if (event.getAction() == MotionEvent.ACTION_MOVE
//                && Touchzoom_mode > 0 )
//            ViewInvalid();
//
//    }


    private final static int MSG_FOCUS_TRUE= 10001;
    private final static int MSG_FOCUS_FALSE= 10002;

    public Handler cHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {

                case MSG_FOCUS_TRUE:
                    CameraControl.camControl.SetContinuousFocus(true);
                    break;

                case MSG_FOCUS_FALSE:
                    CameraControl.camControl.SetContinuousFocus(false);
                    break;

                default:
                    break;
            }
        }
    };


    public boolean myTouchEvent(MotionEvent event, int offsetX, int offsetY) {
        int x, y, x0, y0, x1, y1;
        long tm;


        tm = System.currentTimeMillis();

        if (progressBar != null ) {
            return true;
        }
        x = (int)event.getX() + offsetX;
        y = (int)event.getY() + offsetY;
        if(y<200)
            return true;


        if( Captured_ModeEnable!=0 && OnCapture_status>=CAPTURE_START && event.getAction()==MotionEvent.ACTION_UP)
        {
            if(IppStatus==IPP_IMAGEDONE)
            {
                OnCapture_status=0;
                IppStatus=IPP_RESTART;
            }
            if( ipp_image==null )
            {
                Captured_ModeEnable=OnCapture_status=0;
                captureCommand_OnOff(Capture_CmdCancelBtn);
                return true;
            }
            if(OnCapture_status==CAPTURE_DoneTranslate)
            {
                OnCapture_status=CAPTURE_GotTargetData;
                captureCommand_OnOff(Capture_CmdGotData);
                return true;
            }
            return false;  // for draw area
        }

        Touch_tm=tm;

        if (menu_drawable == 0)
            return true;
        mmX = mmY = 0;

//        if (ActiveConPreview == 0) {
//            t_ActiveConPreview = ActiveConPreview = 10;
//        }

        //detect.onTouchEvent(event);

        x = (int)event.getX() + offsetX;
        y = (int)event.getY() + offsetY;

        if(offsetX==0 && offsetY==0 && event.getAction() == MotionEvent.ACTION_UP && IppStatus<=IPP_PREVIEW)
        {
            CameraControl.camControl.SetContinuousFocus(false);
            if (handler != null)
            {
                CameraControl.camControl.autoFocusMng.autoFocusing = 1;
                //handler.sendEmptyMessageDelayed(R.id.auto_focus,1000);
            }
            cHandler.sendEmptyMessageDelayed(MSG_FOCUS_TRUE,2000);
        }



        if (KeepScreenOn == false ) //&& tm - TouchMenuTime > 60000) // 60 sec
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SetKeepScreenOnOff(true);
                }
            });
        }

        if (event.getAction() != MotionEvent.ACTION_MOVE || event.getPointerCount() != 2)
            Touchzoom_mode = 0;

        TouchMenuTime = decodeTm = tm;

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1)
        {
            Touchzoom_mode=0;
            if(Zooming_flg)
            {
                if(tm>zoom_tm+1000)
                    Zooming_flg=false;
            }
        }


        if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1 && !Zooming_flg) {
            mmX = x;
            mmY = y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 2) {
            zoom_point=2;
            // TxtScroll_Mode = 0;
            x0 = (int)event.getX(0);
            y0 = (int)event.getY(0);
            x1 = (int)event.getX(1);
            y1 = (int)event.getY(1);

            TouchEvent_Move_Zoom(tm, x0, y0, x1, y1);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1) {
            int wp = 0;
            Touch_x0 = Touch_x1 = 0;
            Flingvx = Flingvy = 0;
            mX = x;
            mY = y;
        }

        else if (event.getAction() == MotionEvent.ACTION_UP) {
            zoom_point=0;
            //ImgButtonStatus=0;
            Zooming_flg=false;
            if(IppStatus==IPP_IMAGEDONE)
            {
                //IppStatus=IPP_RESTART;
            }
        }

        return true;
    }




    public void SetCameraArea(int mx,int my) {
        long t = System.currentTimeMillis();

        getScreenSizeDirection();

        CameraControl.camControl.Afmx = mx;
        CameraControl.camControl.Afmy = my;
        CameraControl.camControl.Afdir= ScreenDirection;
    }

    public void LPR_Notify() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText_MsgView.setText(LPRNoti);
//	            mText_MsgLay.setVisibility(View.VISIBLE);
            }
        });
    }

    public void Status_Notify() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText_MsgView.setText(status_msg);
//	            mText_MsgLay.setVisibility(View.VISIBLE);
            }
        });
    }

    public void Retry_GuideOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText_RetryGuide.setVisibility(View.VISIBLE);
            }
        });
    }

    public void Retry_GuideOff() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText_RetryGuide.setVisibility(View.INVISIBLE);
            }
        });
    }

    public Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            long tm;
            switch (msg.what) {

                case MSG_LPRNoti:
                    LPR_Notify();
                    break;

                case MSG_RetryGuide:
                    Retry_GuideOn();
                    break;

                case MSG_RetryGuideOff:
                    Retry_GuideOff();
                    break;

                case MSG_StatusNoti:
                    Status_Notify();
                    break;

                case DISMISS_PROGRESSDIALOG:
                    DismisprogresssDialog();
                    sleepdelay(200);
                    break;

                case MSG_TRANSABNormal:
                    tm = System.currentTimeMillis();

                    Log.w("com.cardcam"," MSG_TRANSABNormal: translating_flg="+translating_flg+ ", mDialogID"+mDialogID+ ", dialogID"+dialogID);

                    if(!translating_flg || mDialogID!=dialogID)
                    {
                        break;
                    }

                    if(tm-startTranslation_TM>=20000 && progressBar!=null)
                    {

                    }
                    else
                    {
                        mHandler.sendEmptyMessageDelayed(MSG_TRANSABNormal,500);
                    }
                    break;

                case MSG_ReqCaptureFrame:
                    if(CameraControl.camControl.ManagerCamera() == null)
                        break;

                    if(OnCapture_status>=CAPTURE_START)
                        break;

                    if(CameraControl.camControl.isFocus_ContinuousMode()==1)
                    {
                        if(FocusValue>=8 )//&& FocusValue>=MaxFocusValue/2)
                        {
                            RequestCaptureFrame();
                            break;
                        }
                    }
                    else if(FocusValue>=8 )
                    {
                        RequestCaptureFrame();
                        break;
                    }


                    mHandler.sendEmptyMessageDelayed(MSG_ReqCaptureFrame, 1000);

                    break;

                case MSG_ProgBarCancel:
                    if (progressBar != null) {
                        //if (progressBar.isShowing())
                        {
                            Log.w("com.cardcam"," set MSG_ProgBarCancel.");
                            progressBar.dismiss();

                            sleepdelay(200);
                            progressBar = null;

                        }
                    }
                    break;


                case MSG_BMPLPR:
                    GetLPRImg_byGallery();
                    break;
                case MSG_STEPLPR:
                    LPRStepRcgn();
                    break;

                case MSG_LPRImgRcgn:
                    LPRImgRcgn();
                    break;
                case MSG_IPPSave:
                    IPPImgSave();
                    break;
                case MSG_SetImage:
                    set_IPPimage(true);
                    //mHandler.sendEmptyMessage(MSG_SetImage);
                    break;
                case MSG_SetInform:
                    //Log.e(TAG, "MSG_Inform : " + inform_msg+memMessage);
                    // vInform.setText(inform_msg+'\n'+memMessage);
                    break;
                case MSG_FLING_ParmLay:
                    //drawUI_mWorddrag();
                    break;

                case MSG_finish:
                    Quit_flg=true;
                    finish();
                    break;

                case MSG_EndProcess:
                    //ing_flg=false;
                    break;

                case MSG_TURNON_FLASH:
                    Log.w(TAG, " #MSG_sON# Tch_flashMode= "+ Tch_flashMode);

                    if( CameraControl.camControl.previewing) {
                        Log.w(TAG, " #MSG_TURNON# Tch_flashMode= [ON] ");
                        CameraControl.camControl.mCameraFlash(1);
                        FlashBtn_OnOff(true);
                        Log.w(TAG, " #msgOn# Tch_flashMode= "+ Tch_flashMode);

                    }
                    else if((Tch_flashMode & 1)==0)
                    {
                        Log.w(TAG, " #MSG_TURNON# Tch_flashMode= "+ Tch_flashMode+ ": canflash ="+canFlash);
                        mHandler.sendEmptyMessage(MSG_TURNON_FLASH);
                    }
                    break;

                case MSG_TURNOFF_FLASH:
                    Tch_flashMode =0;
                    if(canFlash==0) break;
                    Log.w(TAG, " #msgOff# Tch_flashMode= "+ Tch_flashMode);
                    CameraControl.camControl.mCameraFlash(0);
                    FlashBtn_OnOff(false);
                    // mFlashBtn.setSelected(false);

                    break;

                case MSG_CaptureREQ:
                    if(OnCapture_status==CAPTURE_START)
                        OnCapture_status=0;
                    break;


                case MSG_CAPTURE_READY:
                    captureCommand_OnOff(0);
                    break;

                case MSG_LPRprocessing:
                    ImgButtonStatus=1;
                    (new Thread(LPRprocessing)).start();
                    break;


                case MSG_SetViewImage:
                    set_IPPimage(true);
                    //mHandler.sendEmptyMessage(MSG_EndProcess);
                    break;

                case MSG_SetTransImage:
                    set_TransImage();
                    //mHandler.sendEmptyMessage(MSG_EndProcess);
                    break;

                case MSG_INIT_CAMERA:
                    hasSurface = 1;
                    initCamera(mSurfaceView.getHolder());
                    msg.obj = null;
                    break;
                case MSG_Close_CAMERA:

                    CameraControl.camControl.CloseCamera();
                    //camView.setBackgroundColor(0xff000000);
                    break;


                default:
                    break;
            }
        }
    };

    private void set_IPPimage(boolean endMode)
    {
        //ipp_image.recycle();
        //getScreenSizeDirection();
        if(ipp_image==null || ipp_image.isRecycled())
        {
            //ing_flg=false;
            ipp_image=null;
            if(endMode)
                mHandler.sendEmptyMessage(MSG_EndProcess);
            return;
        }

        if(dipl_image!=null && !dipl_image.isRecycled()){
            dipl_image.recycle();
        }

        int wd=ipp_image.getWidth();
        int ht=ipp_image.getHeight();

        dipl_image=null;//Bitmap.createScaledBitmap(ipp_image, wd/2,ht/2, true);
        try {
            dipl_image=Bitmap.createScaledBitmap(ipp_image, wd/2,ht/2, true);
        }
        catch(Exception e) {
            dipl_image=null;
            return;
        }
        catch (OutOfMemoryError e) {
            dipl_image=null;
            return;
        }

        wd=dipl_image.getWidth();
        ht=dipl_image.getHeight();
        SetCaptureImgLayout( wd, ht);


        try {
            Drawable d = new BitmapDrawable(getResources(),dipl_image);
            // nib 09/23/13 setBackground() can be worked only level16 and higher
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                //Show_ToastString("Current OS level is "+android.os.Build.VERSION.SDK_INT+". It must be 16 and higher");
                CaptureImgView.setBackgroundDrawable(d);
            }
            else {
                CaptureImgView.setBackground(d);
            }
        }

        catch(Exception e) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CaptureImgView.setVisibility(View.INVISIBLE);

                }
            });
            return;
        }
        catch (OutOfMemoryError e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CaptureImgView.setVisibility(View.INVISIBLE);
                    //ing_flg=false;
                }
            });
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CaptureImgView.setVisibility(View.VISIBLE);
                //ing_flg=false;
            }
        });

        //CaptureImgView.invalidate();


        viewdraw.drawViewInvalid();
        //ing_flg=false;

        if(endMode)
            mHandler.sendEmptyMessage(MSG_EndProcess);

    }

    private void set_TransImage()
    {

        if(trans_image==null)
            return;

        int loc[] = { 0, 0, 0, 0 };

        findViewById(getResources().getIdentifier("viewdraw", "id", getPackageName())).getLocationOnScreen(loc);
        int Display_wd=ScreenSize.x;
        int Display_ht=ScreenSize.y-loc[1];

        LayoutParams params = (LayoutParams)CaptureImgView.getLayoutParams();

        params.leftMargin =0;
        params.topMargin =0;
        params.width=ScreenSize.x;
        params.height=ScreenSize.y-loc[1];

        if(dipl_image!=null && !dipl_image.isRecycled())
            dipl_image.recycle();
        dipl_image=null;
        if(trans_image!=null && !trans_image.isRecycled())
        {
            dipl_image=trans_image;
        }
        else
        {
            trans_image=null;
            return;
        }
        trans_image=null;

        int wd=dipl_image.getWidth();
        int ht=dipl_image.getHeight();

        float xRat=(float)params.width/(float)wd;
        float yRat=(float)params.height/(float)ht;

        if(xRat>yRat)
        {
            params.height=Display_ht;
            params.width= (int)((float)Display_wd * yRat/xRat);

        }
        else
        {
            params.height=(int)((float)Display_ht * xRat/yRat);
            params.width= Display_wd ;

        }

        params.width=ScreenSize.x;
        params.height=ScreenSize.y;

        iPage_Screen_width=params.width;
        iPage_Screen_height=params.height;

        CaptureImgView.setLayoutParams(params);

        try {
            Drawable d = new BitmapDrawable(getResources(),dipl_image);
            // nib 09/23/13 setBackground() can be worked only level16 and higher
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                //Show_ToastString("Current OS level is "+android.os.Build.VERSION.SDK_INT+". It must be 16 and higher");
                CaptureImgView.setBackgroundDrawable(d);
            }
            else {
                CaptureImgView.setBackground(d);
            }
        }

        catch(Exception e) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CaptureImgView.setVisibility(View.INVISIBLE);

                }
            });
            return;
        }
        catch (OutOfMemoryError e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CaptureImgView.setVisibility(View.INVISIBLE);

                }
            });
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CaptureImgView.setVisibility(View.VISIBLE);
            }
        });

        CaptureImgView.invalidate();
        viewdraw.drawViewInvalid();

    }


    public boolean StatusBarEnable=false;
    public void GetStatusBarSize()
    {
        if(StatusBarEnable)
            return;

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int tBarHeight= rectgle.top;
        int contentViewTop=window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int TitleBarHeight= contentViewTop - StatusBarHeight;

        if(tBarHeight>0)
        {
            StatusBarEnable=true;
            StatusBarHeight=tBarHeight;

        }
        else
            StatusBarHeight = Getdp(25);
    }

    int conti_flag=0;
    public void DisplayLPRMark(Canvas canvas, Paint paint) {

        float xRat;
        float yRat;
        int sx,sy,ex,ey;
        int PlateMark=   0xFFFF2020;
        int txtsize = 100;
        int txtcolor = 0xFFFFFFFF;
        int offset_x=0,offset_y=0;

        paint.setColor(PlateMark);
        getScreenSizeDirection(); // Get ScreenSize.x, ScreenSize.y

        //screen_angle=ScreenDirection;

        if(LPRprevDet==0)
            return;

        if(CAM_width==0 || CAM_height==0 || LPR_right<=0 || LPR_bottom<=0)
            return;


        if(ImgButtonStatus==0)
        {
            if((ScreenSize.y>ScreenSize.x && CAM_height<CAM_width) ||
                    (ScreenSize.y<ScreenSize.x && CAM_height>CAM_width))
            {
                int t=CAM_width; CAM_width=CAM_height; CAM_height=t;
            }

            xRat=(float)ScreenSize.x/(float)(CAM_width);
            yRat=(float)ScreenSize.y/(float)(CAM_height);

            iPage_adjSbarRat=(float)ScreenSize.y/(float)(ScreenSize.y-StatusBarHeight) ;
            if(ScreenDirection==0 || ScreenDirection==180)
                yRat=yRat/iPage_adjSbarRat;
        }
        else
        {
            offset_x =(ScreenSize.x-iPage_Screen_width)/2;
            offset_y =(ScreenSize.y-iPage_Screen_height)/2;

            xRat=(float)iPage_Screen_width/(float)(CAM_width);
            yRat=(float)iPage_Screen_height/(float)(CAM_height);
        }

        sx=(int)((float)LPR_left *xRat)+offset_x;
        sy=(int)((float)LPR_top *yRat)+offset_y;
        ex=(int)((float)LPR_right *xRat)+offset_x;
        ey=(int)((float)LPR_bottom *yRat)+offset_y;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(PlateMark);

        paint.setStrokeWidth(4);
        canvas.drawRect(sx, sy, ex, ey, paint);

        //canvas.drawRect(1, 1, ScreenSize.x, ScreenSize.y, paint);
        //paint.setAlpha(160);
        txtsize=Getdp(20);
        paint.setTextSize(txtsize);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(txtcolor);
        String level = LPRres ; //+ "\n\n status="+status[0];
        canvas.drawText(level, sx-txtsize/2, ey+txtsize, paint);
        Log.e(TAG, "Accuracy : " + level + " mode = " + Captured_ModeEnable);

    }




    // ------------------------------------------------
    public int Focus_Mode=0;
    public int FocusValue=0;
    public int MaxFocusValue=0;
    //public int FocusDctValue=0;
    // public int MaxFocusDctValue=0;

    public long focus_tm=0;
    public long prevTm=0;
    private int[] status= new int [8];
    private float[] detArea= {0,0,0,0};
    private int[] cropArea= {0,0,0,0};

    String LPRres="";

    private Bitmap raw2BitMap(byte[] yuv420sp, int width, int height,boolean BeDictEnable) {

        int  ParaIn[]= new int[10];
        long tm1,tm2;//=System.currentTimeMillis();
        int  ch_Mode,iret;

        if (width < height || yuv420sp==null)
            return null;

        if( Quit_flg || !Iamhere)
            return null;


        ch_Mode = Tch_Mode;

        if(viewdraw.rScanArea.right==0)
        {
            viewdraw.reset_ScanArea();
        }

        iPage_Screen_width=ScreenSize.x;
        iPage_Screen_height=ScreenSize.y;
        iPage_adjSbarRat=(float)ScreenSize.y/(float)(ScreenSize.y-StatusBarHeight) ;

        CAM_width=width;
        CAM_height=height;


        ParaIn[0] = viewdraw.rScanArea.left;
        ParaIn[1] = viewdraw.rScanArea.top;
        ParaIn[2] = viewdraw.rScanArea.right;
        ParaIn[3] = viewdraw.rScanArea.bottom;

        int fact1000=ScreenSize.y*1000/(ScreenSize.y-StatusBarHeight) ;
        ParaIn[1] = ParaIn[1] *fact1000/1000 ;
        ParaIn[3] = ParaIn[3] *fact1000/1000 ;

        ParaIn[4] = ScreenSize.x;
        ParaIn[5] = ScreenSize.y;
        ParaIn[6] = ScreenDirection;
        ParaIn[7] = 100;  // Compare Matching previous Image



        Log.e(TAG, "PreviewPerspectiveDetect :in raw2BitMap");

        Log.e(TAG, "Focus_Mode :start ");
        long dtm1=System.currentTimeMillis();
        //focusDatas[0]=focusDatas[1]=focusDatas[2]=focusDatas[3]=0;

        FocusValue=CarNumRcgn.GetFocusValueYuv420(yuv420sp, width, height, width/2, height/2, 0);//, focusDatas);
        if(FocusValue>MaxFocusValue)
            MaxFocusValue=FocusValue;


        focus_tm=System.currentTimeMillis()-dtm1;

        LPRNoti+=" myFocus= "+ FocusValue ;
        Log.e(TAG, "Focus_Val :"+FocusValue);

        if(ImgButtonStatus>0)
            return null;

        if((Tch_captureMode & TchVal_Capture)==0)
        {
            LPRprevDet=0;
            LPRres="";
        }
        else
        {
            if(FocusValue>=1000)
            {
                int SizeRat=2;  //
                Log.e(TAG, "PreviewPerspectiveDetect :SizeRat=  "+SizeRat);


                if(ScreenDirection==90 || ScreenDirection==270)
                {
                    detArea[0]=0;
                    detArea[2]=1.0f;
                    detArea[1]=1.0f/4.0f; detArea[3]=3.0f/4.0f;

                }
                else
                {
                    detArea[0]=1.0f/5.0f;
                    detArea[2]=4.0f/5.0f;
                    detArea[1]=1.0f/5.0f;
                    detArea[3]=4.0f/5.0f;
                }

                status[0]=0;
                LPRres="";//CarNumRcgn.yuvCarNumRcgn(yuv420sp, width, height, ScreenDirection,detArea, status);

                prevTm=System.currentTimeMillis()-dtm1;
                Log.e(TAG, "PreviewPerspectiveDetect :end  tm= " + prevTm);
                if(status[0]>0)
                {
                    if(ScreenDirection==90 || ScreenDirection==270)
                    {
                        CAM_width=height; CAM_height=width;
                    }
                    else
                    {
                        CAM_width=width; CAM_height=height;
                    }
                    LPR_left=status[1]; LPR_top=status[2];
                    LPR_right=status[3]; LPR_bottom=status[4];
                    decodeTm = System.currentTimeMillis();
                    LPRprevDet++;
                }
                else
                {
                    if(LPRprevDet>=2) LPRprevDet=1;
                    else
                    {
                        LPRprevDet=0;
                        LPRres="";
                    }
                }
            }
            else
            {
                LPRprevDet=0;
                LPRres="";
            }
        }

        return null;

    }


    // ==================================================================================


    private void mStartActivityNoResult(Intent i,int ActivityID)
    {
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }


    public static int Capture_CmdReady=0;
    public static int Capture_CmdGotData=1;
    public static int Capture_CmdCancelBtn=2;
    public static int Capture_CmdTransBtn=3;
    public static int Capture_CmdDictOk=4;
    public static int Capture_CmdPhraseOk=5;

    private void captureCommand_OnOff(int mode)
    {
        if(mode==Capture_CmdReady || mode==Capture_CmdGotData) // MSG_CAPTURE_READY
        {
            if(mode==Capture_CmdGotData )
            {
                selectWordIndex=-1;
                if(viewdraw.mBitmap!=null)
                {
                    viewdraw.mBitmap.eraseColor(0x00000000);
                }
            }

        }
        else if(mode==Capture_CmdCancelBtn) //cancel btn
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mFlashBtn.setVisibility(View.VISIBLE);
                    mFlashBtn.setEnabled(true);


                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    OnCapture_status=0;

                    clear_ippBitmap();
                    ViewInvalid();

                    //t_ActiveConPreview = 10;
                    if(handler != null) {
                        handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                        handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
                    }
                }
            });
        }
        else if(mode==Capture_CmdTransBtn) //Translator btn
        {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mFlashBtn.setVisibility(View.VISIBLE);
                    mFlashBtn.setEnabled(true);  // mdfy
                    FlashBtn_OnOff(false);
                    Log.w(TAG, " #3# Tch_flashMode= "+ Tch_flashMode);

                }
            });
        }


    }


    private void CaptureBtn_OnOff(boolean mode)
    {

        chk_nonChgImg=0;

        if(mode){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    Tch_captureMode = TchVal_Capture ;
                    Captured_ModeEnable=0;
                    OnCapture_status=0;

//                  mText_MsgLay.setVisibility(View.VISIBLE);
                    mText_MsgView.setText("");
                    mText_RetryGuide.setVisibility(View.INVISIBLE);

                    mShutterBtn.setVisibility(View.VISIBLE);
                    mSaveBtn.setVisibility(View.INVISIBLE);
                    // t_ActiveConPreview = ActiveConPreview = 10;
                    mFlashBtn.setEnabled(true);
//                  mText_MsgLay.setVisibility(View.VISIBLE);
                    mText_MsgView.setVisibility(View.VISIBLE);

                    checkboxs_Layout.setVisibility(View.VISIBLE);
                    buttons_Layout.setVisibility(View.INVISIBLE);

                    if(CameraControl.camControl.isFocus_ContinuousMode()==0)
                    {
                        CameraControl.camControl.autoFocusMng.autoFocusing = 1;
                        handler.sendEmptyMessage(getResources().getIdentifier("auto_focus", "id", getPackageName()));
                    }

                    if(false)
                    {
                        mHandler.sendEmptyMessage(MSG_ReqCaptureFrame);
                    }
                }
            });
        }
        else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Tch_captureMode =0;
                    mFlashBtn.setEnabled(true);
                    mSaveBtn.setVisibility(View.INVISIBLE);
                    //mText_MsgLay.setVisibility(View.INVISIBLE);
                    //mText_MsgView.setVisibility(View.INVISIBLE);
                    mText_MsgView.setText("");
                    checkboxs_Layout.setVisibility(View.INVISIBLE);
                    buttons_Layout.setVisibility(View.VISIBLE);
                    mText_RetryGuide.setVisibility(View.INVISIBLE);

                    LPRprevDet=0;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    Captured_ModeEnable=OnCapture_status=0;

                    //mShutterBtn.setVisibility(View.INVISIBLE);
                    //mSaveBtn.setVisibility(View.VISIBLE);
                    mText_MsgView.setText("");

                }
            });
        }

        clear_ippBitmap();
        IppStatus=IPP_PREVIEW;
        //startPreview();
        OnCapture_status=0;
        if(CameraControl.camControl.mCamera==null)
        {
            mHandler.sendEmptyMessageDelayed(MSG_INIT_CAMERA,200);
        }

        if(handler != null) {
            handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            return;
        }
    }

    private void FlashBtn_OnOff(boolean mode)
    {
        if(mode){
            mHandler.sendEmptyMessageDelayed(MSG_TURNOFF_FLASH, 120000);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mFlashBtn==null)
                        mFlashBtn=(ImageButton) findViewById(getResources().getIdentifier("flash_btn", "id", getPackageName()));
                    mFlashBtn.setImageResource(getResources().getIdentifier("drawable_flash_btn", "drawable", getPackageName()));
                    Tch_flashMode |=1;
                    Log.w(TAG, " #OnOff1# Tch_flashMode= "+ Tch_flashMode);
                }
            });
        }
        else {
            mHandler.removeMessages(MSG_TURNOFF_FLASH);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mFlashBtn==null)
                        mFlashBtn=(ImageButton) findViewById(getResources().getIdentifier("flash_btn", "id", getPackageName()));

                    mFlashBtn.setImageResource(getResources().getIdentifier("drawable_flash_off_btn", "drawable", getPackageName()));
                    Tch_flashMode &=~1;
                    Log.w(TAG, " #OnOff2# Tch_flashMode= "+ Tch_flashMode);
                }
            });
        }
    }


    private void StopPreview(boolean tag)
    {
        CameraControl.camControl.stopPreview(tag);
    }

    private void startPreview()
    {
        CameraControl.camControl.startPreview();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager wm = getWindowManager();
        if (wm == null) return;

    }


    public void check_MsgHandle() {  //chkMessage  for camera off or watchdog or  capture
        Message message;
        long tm = System.currentTimeMillis();

        if (handler == null)
            return;  // at this case, reforming handle. camera should be off.

        if(IppStatus==IPP_RESTART)// OnCapture_status==CAPTURE_GotImage)
        {
            clear_ippBitmap();

            IppStatus=IPP_PREVIEW;
            //startPreview();
            OnCapture_status=0;
            mHandler.sendEmptyMessageDelayed(MSG_INIT_CAMERA,200);
            //Captured_ModeEnable=0;
            if(handler != null) {
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessageDelayed(getResources().getIdentifier("preview_raw", "id", getPackageName()), 1000);

                return;
            }
        }


        if ( tm - decodeTm >= 120000 && tm - TouchMenuTime > 120000 && KeepScreenOn==true && AutoLPR==false)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SetKeepScreenOnOff(false);
                }
            });
        }

        if(handler != null)
        {
            //handler.removeMessages(R.id.preview_raw);
            handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
        }
    }



    private int PreDecode_Chk2(long tm) {
        Message message;


        if ( CameraControl.camControl.previewing==true && (Tch_flashMode & TchVal_Flash)!=0 && (Tch_flashMode & 1)==0) {
            CameraControl.camControl.mCameraFlash(1);
            FlashBtn_OnOff(true);

            Log.w(TAG, " #4# On Tch_flashMode= "+ Tch_flashMode);
            if (handler != null)
            {
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }
            return 1;
        }



        if(tm-Touch_tm>=0 && tm-Touch_tm<=100 )
        {
            if (handler != null){
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }
            return 1;
        }

        return 0;
    }

    private boolean View_introLay=false;

    private void Disable_introLay()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//	             	mIntro_layout.setVisibility(View.GONE);
                View_introLay=true;

            }
        });
    }

    private boolean para_Onechk=false;
    private boolean debugimg_flg=false;

    private void InitPara()
    {
        if(para_Onechk) return;
        para_Onechk=true;

        debugimg_flg=false;
        try{
            PackageInfo pi = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String str=pi.packageName;
            int flags=pi.applicationInfo.flags;

            if ((flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
                debugimg_flg=true;

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //    public void OccupiedTextViewClear()
//    {
//	      	runOnUiThread(new Runnable() {
//	        @Override
//	        public void run() {
//	        	mText_MsgView.setText("");
//	        }
//		  });
//    }
    //=====================================================================================
    private long entry_decode=0;
    boolean isFirst = false;
    long tm2=0;
    int PreviewUnvalid=0;
    int check_OcrReadyDim=0;
    public long Occupied_tm=0;

    public void previewProc(byte[] data, int width, int height) {
        long tm = System.currentTimeMillis();
        int ii, schg;
        int gmax ;

        if(tm2==0) tm2=tm;
        entry_decode++;
        TrLnStrLength[1] = 0;

        if(Quit_flg)
        {
            finish();
            return;
        }
        if(handler==null) return;
        InitPara();

        if(!View_introLay )   {
            Disable_introLay();
        }
        if(IppStatus==IPP_IMAGEGOT)
        {
            if(handler != null)
                handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            return;
        }

        if(Captured_ModeEnable !=0 && OnCapture_status>=CAPTURE_START  )
        {
            if(handler != null)
                handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            return;
        }

        if(data==null)
        {
            PreviewUnvalid++;
            if(PreviewUnvalid>=2)
            {
                CameraControl.camControl.CloseCamera();
                mHandler.sendEmptyMessageDelayed(MSG_INIT_CAMERA,200);
                return;
            }
            handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            return;
        }
        else PreviewUnvalid=0;


        if ( OnCapture_status>=CAPTURE_START)
        {
            handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            return;
        }

        if(CameraControl.camControl.mCamera==null)
            return;

        schg = CheckScreenDirection_byPreview(); //CheckScreenDirection(0);

        if (schg > 0 || SurfaceChanged_tag == true) {
            return;
        }


        if(handler == null || Request_Activity > 0)
            return ;

        NthFrame++;

        menu_drawable = 1;

        if(PreDecode_Chk2(tm)==1)
            return ;

        if( true) //debugimg_flg)  //for debug status
        {
            if((NthFrame % 20)==19)
            {
                int d=(int)(tm-tm2); if(d==0) d=1;
                int num= 200 *1000/d;

                tm2=tm;
            }
        }

        if(OnCapture_status>=CAPTURE_START && mProgressType==20) {
            DismisprogresssDialog();
            OnCapture_status=0;
        }

        if(CaptureImg32!=null)
        {
            CaptureImg32.clear();
            CaptureImg32=null;
        }

        if(OnCapture_status==0) {
            clear_ippBitmap();
        }


        gmax=200;
        if ( tm - TouchMenuTime > 400
                && Zooming_flg==false && Dimming_flg==0) {


            long ipp_tm1 = System.currentTimeMillis();
            LPRNoti="IPP: ";
            Bitmap tmp =raw2BitMap(data, width, height, false);
            IppTime=System.currentTimeMillis()-ipp_tm1;

            if( Tch_captureMode == TchVal_Capture )
            {
                if(LPRprevDet>0)
                {
                    LPRNoti+="\n LPRres="+LPRres+"  status= "+ (int)status[0];
                }


                android.util.Log.d(TAG,LPRNoti);
                mHandler.sendEmptyMessage(MSG_LPRNoti);
                //mHandler.sendEmptyMessage(MSG_StatusNoti);
                //mHandler.sendEmptyMessage(MSG_RetryGuide);
            }
            mHandler.sendEmptyMessage(MSG_RetryGuideOff);


            if(tmp!=null)
            {
                ipp_image=tmp;
            }


        }

        if(LPRprevDet>=2)
        {
            //mHandler.sendEmptyMessageDelayed(MSG_ReqCaptureFrame,1000);
        }

        if(!Quit_flg && handler != null) {
            handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
        }
        //ViewInvalid();

    }

    public void SetKeepScreenOnOff(boolean flg)
    {
        if(flg){
            viewdraw.setKeepScreenOn(true);
            KeepScreenOn = true;
        }
        else {
            viewdraw.setKeepScreenOn(false);
            KeepScreenOn = false;
        }
    }

    private boolean init_Resume=false;


    private void Do_Resume() {

        CamOpenTm=0;

        Iamhere=true;
        mSoundManager = new SoundManager(this);

        SetKeepScreenOnOff(true);
        SharedPreferences prefs = getSharedPreferences("ProcessStatus", 0);
        Captured_ModeEnable=prefs.getInt("Captured_ModeEnable", 0);
        OnCapture_status=prefs.getInt("OnCapture_status", 0);
        Tch_captureMode=prefs.getInt("Tch_captureMode", 0);
        captured_angle= prefs.getInt("captured_angle", 0);
        //ImgButtonStatus= prefs.getInt("ImgButtonStatus", 0);
        Captured_ModeEnable=0;
        OnCapture_status=0;
        Tch_captureMode=TchVal_Capture;

        //setLayout();
        SurfaceView camView=(SurfaceView) findViewById(getResources().getIdentifier("preview_view", "id", getPackageName()));
        camView.setVisibility(View.VISIBLE);

        FlashBtn_OnOff(false);


        if(Captured_ModeEnable !=0 && OnCapture_status>=CAPTURE_GotImage && ipp_image!=null)
        {
            if(captured_angle==0 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else if(captured_angle==180 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            else if(captured_angle==90 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else  	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }

        if (hasSurface == 1) {
            initCamera(mSurfaceView.getHolder());
            //t_ActiveConPreview =10;
        }

        //mShaker.onResume();
        Resume_tm=TouchMenuTime= System.currentTimeMillis();

        if(OnCapture_status==0){
            //clear_ippBitmap();
        }

        ViewInvalid();

        checkLandscapeMode();
        Correction_UI();

    }

    private void Correction_UI()
    {
        if(Captured_ModeEnable !=0 ){

            if(OnCapture_status>0 && ipp_image==null)
                OnCapture_status=0;
            mText_MsgView.setText("");
            //mText_MsgLay.setVisibility(View.INVISIBLE);
        }
        else {
//    		 mText_MsgLay.setVisibility(View.VISIBLE);
            mText_MsgView.setText("");
        }

    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock=null;
        }
    }


    private class UnlockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_USER_PRESENT)) {

                if (unlockReceiver != null) {
                    Do_Resume(); // real work
                    unregisterReceiver(unlockReceiver);
                    unlockReceiver = null;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check lock screen

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            IntentFilter unlockReceiverfilter = new IntentFilter();
            unlockReceiverfilter.addAction(Intent.ACTION_USER_PRESENT);
            unlockReceiver = new UnlockReceiver();
            registerReceiver(unlockReceiver, unlockReceiverfilter);
            ScreenLockPass=false;
        } else {
            ScreenLockPass=true;
            // nib 09/13/13 89393
            // There is a case of restart QT without finishing mTransCam activity
            Quit_flg = false;

            Do_Resume();
        }
    }

    public void SaveCurrentInfo() {
        SharedPreferences prefs = getSharedPreferences("ProcessStatus", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.commit();
    }

    public void SaveDataStatus(int unpluged) {

        SharedPreferences prefs = getSharedPreferences("ProcessStatus", 0);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt("ProcessStatus", 1);

        edit.putInt("Tch_Mode", Tch_Mode);

        edit.putInt("canFlash", canFlash);

        edit.putInt("OnCapture_status", OnCapture_status);
        edit.putInt("Captured_ModeEnable", Captured_ModeEnable);
        edit.putInt("Tch_captureMode", Tch_captureMode);
        edit.putInt("captured_angle", captured_angle);
        edit.putInt("ImgButtonStatus", ImgButtonStatus);
        edit.commit();
    }


    protected void ExPause(int camDelay) {
        menu_drawable = 0;

        SaveDataStatus(1);
        DismisprogresssDialog();

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        onActCamera = 0;

        Tch_flashMode =0;
        mHandler.removeMessages(MSG_TURNOFF_FLASH);

        if(canFlash==1)
        {
            if(mFlashBtn==null)
            {
                mFlashBtn=(ImageButton) findViewById(getResources().getIdentifier("flash_btn", "id", getPackageName()));
            }
            //mFlashBtn.setSelected(false);
        }


        mHandler.sendEmptyMessageDelayed(MSG_Close_CAMERA, camDelay);
        Log.w(TAG, " #camera: Close by OnPause");
        System.gc();
    }

    ////////////////////////////////// Installer ////////////////////////////////////


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onPause() {
        menu_drawable = 0;
        //// super.onPause();

        Iamhere=false;
        if(CameraControl.camControl.mCamera!=null && wakeLock == null) {//TD 63535,70191,70693,75546,78714
            PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK  , "wakelock");
            wakeLock.acquire();
        }

        if(mSoundManager!=null)
            mSoundManager.dispose(); //  dio..0817 kig0726

        if (unlockReceiver != null) {
            unregisterReceiver(unlockReceiver);
            unlockReceiver = null;
        }

        ExPause(200);

        SetKeepScreenOnOff(false);
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock=null;
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void clearApplicationCache(java.io.File dir){
        if(dir==null)
            dir = getCacheDir();
        else;
        if(dir==null)
            return;
        else;
        java.io.File[] children = dir.listFiles();
        try{
            for(int i=0;i<children.length;i++)
                if(children[i].isDirectory())
                    clearApplicationCache(children[i]);
                else children[i].delete();
        }
        catch(Exception e){}
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

//     	edUnplug=0;
//     	editing_status=0;
//     	ExPause(100);


        init_Resume=true;
        Quit_flg=true;
        if(mSound_pool!=null)
            mSound_pool.stop(mSoundID);

        if(progressBar_msg!=null) progressBar_msg.clear();
        if(progressBar_btn!=null) progressBar_btn.clear();

        DismisprogresssDialog();

        mediaController.setVisibility(View.GONE);

        clear_ippBitmap();
        if(imgv!=null)
            imgv.recycle();
        imgv=null;
        SharedPreferences prefs = getSharedPreferences("ProcessStatus", 0);
        SharedPreferences.Editor edit = prefs.edit();
        int kickMe=prefs.getInt("kickMe", 0);
        kickMe++;

        if(Captured_ModeEnable!=0 && OnCapture_status>0)
        {
            OnCapture_status=0;
            mFlashBtn.setEnabled(true);

            edit.putInt("ProcessStatus", 1);
            edit.putInt("OnCapture_status", OnCapture_status);
            edit.putInt("Tch_captureMode", Tch_captureMode);

        }
        if(kickMe==4) kickMe=0;
        edit.putInt("kickMe", kickMe);
        edit.commit();


        clearApplicationCache(null);

        System.gc();
        Debug.MemoryInfo  memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        int    size= memoryInfo.getTotalPss() / 1024;
        System.gc();

        if(kickMe==3 || size>=54) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        System.gc();
    }

    private boolean MyBeep(int mode, int toneType, int setTm) {
        if (mode == 0) {
            // nib 05/10/13 if virate or silent mode in system, then be quite!
            AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
            int ringerMode = am.getRingerMode();

            if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                return true;
            }
            // --------------------------

            // dio.jwkim2.0817
            // mSoundManager.playSound();
        }

        return true;
    }



    public void surfaceCreated(SurfaceHolder holder) {
        if (hasSurface == 0) {
            Message msg = mHandler.obtainMessage(MSG_INIT_CAMERA, holder);
            mHandler.sendMessage(msg);
        }
    }

    @SuppressWarnings("deprecation")
    public void Do_surfaceChange() {

        if (!SurfaceChanged_tag)
            return;

        getScreenSizeDirection();

        Log.w(TAG, " #surface# DoChgSurface: Dir= "+ ScreenDirection+ "wd= "+ScreenSize.x + "ht= "+ScreenSize.y);

        CameraControl.camControl.ChgPreviewResolution(ScreenDirection);

        if (handler != null) {
            handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
            handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
        }

        t1_ScreenDirection = ScreenDirection;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetCamviewLayout();
            }
        });
        SurfaceChanged_tag = false;
        // ViewInvalid();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        SurfaceChanged_tag = true;
        // SurfaceChange_holder=holder;
        // SurfaceChange_format=format;

        Log.w(TAG, " #surface# surfaceChanged= "+ SurfaceChanged_tag);
        Message message = Message.obtain(handler, getResources().getIdentifier("surface_change", "id", getPackageName()));
        if (handler != null && message != null)
            message.sendToTarget();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = 0;
        onActCamera = 0;
        CameraControl.camControl.CloseCamera();
    }

    private boolean CameraOpenTry(SurfaceHolder surfaceHolder) {

        if(ScreenSize.x> ScreenSize.y && (ScreenDirection==90 || ScreenDirection==270)) {
            Log.w(TAG, " #CamOpenDirError: Dir= "+ ScreenDirection+ "wd= "+ScreenSize.x + "ht= "+ScreenSize.y);
            //manager.getDefaultDisplay().getRotation();
        }
        if(ScreenSize.x< ScreenSize.y && (ScreenDirection==0 || ScreenDirection==180)){
            Log.w(TAG, " #CamOpenDirError: Dir= "+ ScreenDirection+ "wd= "+ScreenSize.x + "ht= "+ScreenSize.y);
            // manager.getDefaultDisplay().getRotation();
        }

        for(int n=0;n<2;n++)
        {
            try {
                if(Iamhere && CameraControl.camControl.OpenCamera(surfaceHolder, ScreenDirection))
                    return true;
            } catch (IOException ioe) {
                if(!Iamhere) break;
                sleepdelay(1000);
            } catch (RuntimeException e) {
                if(!Iamhere) break;
                sleepdelay(1000);
            }
            if(!Iamhere)////F3 TMUS
                return false;
        }

        if(!Iamhere)
            return false;

        try {
            if(Iamhere && CameraControl.camControl.OpenCamera(surfaceHolder, ScreenDirection))
                return true;
        } catch (IOException ioe) {
            Log.w(TAG, ioe);

        } catch (RuntimeException e) {
            Log.w(TAG, "error initializating camera", e);
        }


        return false;
    }

    private boolean  lowBatt_flg=false;
    private boolean Check_Battery() {

        checkBattery_tm=System.currentTimeMillis();
        if(lowBatt_flg) return  false;

        Intent battery=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int battery_value= battery.getIntExtra("level", -1);
        if(battery_value<=5)
        {
            lowBatt_flg=true;
//            String str=getResources().getString(R.string.qtranslator_battery_too_low);
//
//            if(kor_version){
//                str = getResources().getString(R.string.qtranslator_battery_too_low_kor);
//                str=str.replace("QTranslator", appName);
//            }
//            else {
//                str=str.replace("QuickTranslator", appName);
//            }
//
//            ShowLong_ToastString(str);
            //Quit_flg=true;
            mHandler.sendEmptyMessageDelayed(MSG_finish,1000);
            //finish();
            return false;
        }
        return true;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {

        int rotation;

        if(Captured_ModeEnable !=0 && OnCapture_status>=CAPTURE_GotImage)
        {
            if(captured_angle==0 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else if(captured_angle==180 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            else if(captured_angle==90 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else  	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }
        else if(Camera_initOK  && Camara_resume && ScreenLockPass==true)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        getScreenSizeDirection();

        if(!Check_Battery()){
            return;
        }

        Log.w("QT:camopen-2", " Iamhere = "+Iamhere);

        if(!CameraOpenTry(surfaceHolder))  // 1
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            CameraControl.camControl.mCamera=null;
            return;
        }

        Log.w("QT:camopen -1", " Iamhere = "+Iamhere);
        if(Quit_flg || !Iamhere)
        {
            CameraControl.camControl.CloseCamera();
            CameraControl.camControl.mCamera=null;
            return;
        }

        CameraControl.camControl.mCameraZoom(Touch_zv);

        Camera_initOK=true;
        if (handler == null)
        {
            handler = new MessageHandler(this, characterSet);
            handler.sendEmptyMessage(getResources().getIdentifier("statusWatch", "id", getPackageName()));
        }

        if(Captured_ModeEnable ==0 || OnCapture_status<=CAPTURE_GotImage){
            if(!CameraControl.camControl.can_flash)
            {
                canFlash=0;
                mFlashBtn.setVisibility(View.INVISIBLE);
            }
            else
            {
                canFlash=1;
                mFlashBtn.setVisibility(View.VISIBLE);
            }
        }

        t1_ScreenDirection = ScreenDirection;

        onActCamera = 1;
        Request_Activity = 0;
        //t_ActiveConPreview=ActiveConPreview=10;

        SurfaceChanged_tag=true;

        menu_drawable = 1;
        if(OnCapture_status==0)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            clear_ippBitmap();
        }

        handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
        handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
        CamOpenTm=decodeTm = System.currentTimeMillis();
        NthFrame=0;


    }


    private OnClickListener mSettingClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Message msg = mHandler.obtainMessage(MSG_SettingMode);
            mHandler.sendMessage(msg);

        }
    };

    private OnClickListener mVoiceClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Message msg = mHandler.obtainMessage(MSG_VoiceMode);
            mHandler.sendMessage(msg);
        }
    };



    // nib 03/15/13
    private void touchSoundEffect(View v) {
        switch (v.getId()) {

//    	case R.id.phrase_result_mini:
//    		mPhrase_result_mini.playSoundEffect(SoundEffectConstants.CLICK);
//    		break;
            default :
                break;
        }
    }
    // ----------


    private void init_Layout()
    {
        getScreenSizeDirection(); //Get screen direction and ScreenSize
    }







    public static String GetCurrentFileName(String ext) {

        String time = "";

        Calendar cal = Calendar.getInstance();

        time = String.format("%04d%02d%02d_%02d%02d%02d"+ext,// -%03d",

                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH),

                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));//, cal.get(Calendar.MILLISECOND));

        return time;

    }

    OnClickListener mSaveClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //CarNumRcgn.ChangeDebugFileName(GetCurrentFileName(".raw"));
        }
    };


    OnClickListener mToolClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int vId=v.getId();


            mToolButton(vId);
        }
    };

    private CheckBox ChkSave;
    private Button mSaveBtn;
    private Button btnBmpLPR,btnStepLPR,btnPrevLPR,btnAutoTestLPR;
    /* wclee section start */
    private void initializeUI() {

        ChkSave = (CheckBox)findViewById(getResources().getIdentifier("checkBox1", "id", getPackageName()));
        ChkSave.setChecked(true);

        mShutterBtn=(ImageButton) findViewById(getResources().getIdentifier("shutter_btn", "id", getPackageName()));
        mFlashBtn=(ImageButton) findViewById(getResources().getIdentifier("flash_btn", "id", getPackageName()));


        mSaveBtn=(Button) findViewById(getResources().getIdentifier("savebtn", "id", getPackageName()));
        mSaveBtn.setOnClickListener(mToolClickListener);
        mSaveBtn.setVisibility(View.INVISIBLE);

        mFlashBtn.setOnClickListener(mToolClickListener);
        mShutterBtn.setOnClickListener(mToolClickListener);

        checkboxs_Layout=(RelativeLayout) findViewById(getResources().getIdentifier("checkBox_lay", "id", getPackageName()));
//        checkboxs_Layout.setVisibility(View.VISIBLE);

        btnBmpLPR = (Button)findViewById(getResources().getIdentifier("BmpLPR", "id", getPackageName()));
        btnBmpLPR.setOnClickListener(mToolClickListener);
        btnStepLPR = (Button)findViewById(getResources().getIdentifier("StepLPR", "id", getPackageName()));
        btnStepLPR.setOnClickListener(mToolClickListener);
        btnAutoTestLPR = (Button)findViewById(getResources().getIdentifier("AutoTestLPR", "id", getPackageName()));
        btnAutoTestLPR.setOnClickListener(mToolClickListener);
        btnPrevLPR = (Button)findViewById(getResources().getIdentifier("PrevLPR", "id", getPackageName()));
        btnPrevLPR.setOnClickListener(mToolClickListener);

        mShutterBtn.setVisibility(View.VISIBLE);


        mText_MsgLay=(RelativeLayout) findViewById(getResources().getIdentifier("text_Msg_lay", "id", getPackageName()));
        mText_MsgView=(TextView) findViewById(getResources().getIdentifier("text_msg_noti", "id", getPackageName()));
        mText_RetryGuide=(TextView) findViewById(getResources().getIdentifier("text_guide", "id", getPackageName()));
        if(ProcessStatus!=0 && canFlash==0)
        {
            mFlashBtn.setVisibility(View.INVISIBLE);
        }
        else
        {
            mFlashBtn.setVisibility(View.VISIBLE);
        }

//        mViewHolder.toolsOnColor = getResources().getColor(R.color.tools_btn_on);
//        mViewHolder.toolsOffColor = getResources().getColor(R.color.tools_btn_off);

    }


    private void mToolButton(int vId) {

        if (vId == getResources().getIdentifier("flash_btn", "id", getPackageName())) {
            //nib            case R.id.r_flash_btn:
            if (canFlash == 0) return;
            if (Captured_ModeEnable != 0 && OnCapture_status == CAPTURE_DoneTranslate)
                return;

            Log.w(TAG, " #btn01# Tch_flashMode= " + Tch_flashMode);

            if ((Tch_flashMode & TchVal_Flash) == 0) {
                if (CameraControl.camControl.mCamera == null) return;
                if (!CameraControl.camControl.previewing) return;
            }


            if ((Tch_flashMode & TchVal_Flash) == 0) {
                // camera off state -> on
                Tch_flashMode = TchVal_Flash;

                Log.w(TAG, " #btn02# Tch_flashMode= " + Tch_flashMode);

                if (CameraControl.camControl.previewing) {
                    Log.w(TAG, " #btn03# Tch_flashMode= [ON] ");
                    CameraControl.camControl.mCameraFlash(1);
                    FlashBtn_OnOff(true);
                } else {
                    mHandler.sendEmptyMessage(MSG_TURNON_FLASH);
                }

                //t_ActiveConPreview = ActiveConPreview = 10;

            } else if ((Tch_flashMode & TchVal_Flash) != 0) {
                // camera on -> off
                Tch_flashMode = 0;
                Log.w(TAG, " #btn04# Tch_flashMode= " + Tch_flashMode + " [OFF]");

                CameraControl.camControl.mCameraFlash(0);
                FlashBtn_OnOff(false);
            }

        }else if (vId == getResources().getIdentifier("savebtn", "id", getPackageName())) {
            AutoLPR = false;
            String wcard = Environment.getExternalStorageDirectory().getPath();
            //CarNumRcgn.FocusTest(wcard);
            CameraControl.camControl.SetContinuousFocus(false);
            if (handler != null) {
                CameraControl.camControl.autoFocusMng.autoFocusing = 1;

                handler.sendEmptyMessageDelayed(getResources().getIdentifier("auto_focus", "id", getPackageName()), 500);
            }
        } else if (vId == getResources().getIdentifier("shutter_btn", "id", getPackageName()))
        {  //mCameraBtn
            AutoLPR = false;
            if (ImgButtonStatus > 0 && OnCapture_status == CAPTURE_GotImage) {
                SetCam_Preview();
                return;
            }

            if (OnCapture_status >= CAPTURE_START) return;

            CameraControl.camControl.SetContinuousFocus(false);
            if (handler != null) {
                CameraControl.camControl.autoFocusMng.autoFocusing = 0;
                //handler.sendEmptyMessageDelayed(R.id.auto_focus,500);
            }
            cHandler.sendEmptyMessageDelayed(MSG_FOCUS_TRUE, 2000);
            mHandler.sendEmptyMessageDelayed(MSG_ReqCaptureFrame, 1000);
        }else if (vId == getResources().getIdentifier("AutoTestLPR", "id", getPackageName())) {
            ImgButtonStatus = 1;
            AutoLPR = true;
            mHandler.sendEmptyMessage(MSG_STEPLPR);
        }else if (vId == getResources().getIdentifier("PrevLPR", "id", getPackageName())) {
            AutoLPR = false;
            mHandler.removeMessages(MSG_STEPLPR);
            ImgButtonStatus = 0;
            SetCam_Preview();
        }else if (vId ==  getResources().getIdentifier("BmpLPR", "id", getPackageName()) ) {
            AutoLPR = false;
            ImgButtonStatus = 1;
            mHandler.sendEmptyMessage(MSG_BMPLPR);
        }else if (vId == getResources().getIdentifier("StepLPR", "id", getPackageName())) {
                AutoLPR=false;
                ImgButtonStatus=1;
                mHandler.sendEmptyMessage(MSG_STEPLPR);
        }
    }


    private ViewHolder mViewHolder = new ViewHolder();

    class ViewHolder {
        // to here -------------------------
        String strWord, strLine, strDivider, strDividerLand, strPhrase;
        int toolsOnColor, toolsOffColor;
    }


    private  ProgressDialog Install_progressBar=null;
    private  SpannableStringBuilder Install_progressBar_msg=null;
    private  SpannableStringBuilder Install_progressBar_title=null;

    private void Install_makeProgressDialogSpanStr()
    {
        if(Install_progressBar_msg!=null)
            return;

        String msgStr= "Installing...";	//getResources().getString(R.string.sp_noti_start_install_NORMAL);
        String titleStr= "Dictionary";	//getResources().getString(R.string.qtranslator_word_detail_title_dictionary);
        Install_progressBar_msg = new SpannableStringBuilder();
        Install_progressBar_title = new SpannableStringBuilder();

        SpannableString ss_msg = new SpannableString(msgStr);
        SpannableString ss_title = new SpannableString(titleStr);

        int sTyle=android.graphics.Typeface.BOLD; // bold
        int mColor=0xFF41B2C9;
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24 , getResources().getDisplayMetrics());
        ss_msg.setSpan(new AbsoluteSizeSpan(size), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new StyleSpan(sTyle), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new ForegroundColorSpan(mColor), 0, msgStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Install_progressBar_msg.append(ss_msg);

        sTyle=android.graphics.Typeface.BOLD; // bold
        mColor=0xFF41B2C9;
        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 28 , getResources().getDisplayMetrics());
        ss_title.setSpan(new AbsoluteSizeSpan(size), 0, titleStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_title.setSpan(new StyleSpan(sTyle), 0, titleStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_title.setSpan(new ForegroundColorSpan(mColor), 0, titleStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Install_progressBar_title.append(ss_title);

    }


    public void  Install_setProgressDialog() {

        Install_makeProgressDialogSpanStr();
        //reqTransCancel=false;

        Install_progressBar = new ProgressDialog(this);
        Install_progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Install_progressBar.setMessage(Install_progressBar_msg);

        Install_progressBar.setTitle(Install_progressBar_title);
        Install_progressBar.setCancelable(false);

        Install_progressBar.show();
    }

    private long startInstall_TM=0;
    public boolean  Install_showProgressDialog() {

        if(Install_progressBar != null && Install_progressBar.isShowing())
            return false;


        startInstall_TM= System.currentTimeMillis();
        Log.w("com.cardcam"," install showProgress.");

        mHandler.sendEmptyMessageDelayed(MSG_InstallDialogCheck,500);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Install_setProgressDialog();
            }
        });
        return true;
    }

    private  ProgressDialog progressBar=null;
    private  SpannableStringBuilder progressBar_msg=null;
    private  SpannableStringBuilder progressBar_btn=null;
    public   boolean reqTransCancel=false;

    private void makeProgressDialogSpanStr()
    {
        if(progressBar_msg!=null)
            return;

        String msgStr= "Translating..";	//getResources().getString(R.string.spin_translating);
        String btnStr = "Cancel";//getResources().getString(R.string.button_camcel);
        progressBar_msg = new SpannableStringBuilder();
        progressBar_btn = new SpannableStringBuilder();

        SpannableString ss_msg = new SpannableString(msgStr);
        SpannableString ss_btn = new SpannableString(btnStr);

        int sTyle=android.graphics.Typeface.BOLD; // bold
        int mColor=0xFF41B2C9;
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24 , getResources().getDisplayMetrics());
        ss_msg.setSpan(new AbsoluteSizeSpan(size), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new StyleSpan(sTyle), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new ForegroundColorSpan(mColor), 0, msgStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        progressBar_msg.append(ss_msg);

        mColor=0xff000000;
        sTyle=android.graphics.Typeface.BOLD; // bold
        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16 , getResources().getDisplayMetrics());
        ss_btn.setSpan(new AbsoluteSizeSpan(size), 0, btnStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_btn.setSpan(new StyleSpan(sTyle), 0, btnStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_btn.setSpan(new ForegroundColorSpan(mColor), 0, btnStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        progressBar_btn.append(ss_btn);

    }

    public void  setProgressDialog() {

        makeProgressDialogSpanStr();
        reqTransCancel=false;
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(progressBar_msg);

        //progressBar.setTitle(" this ");
        progressBar.setCancelable(false);
        progressBar.setButton(DialogInterface.BUTTON_NEUTRAL,progressBar_btn, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                reqTransCancel=true;
                Dimming_flg=0;       //      TD70548
                Message msg = mHandler.obtainMessage(MSG_ProgBarCancel);
                mHandler.sendMessage(msg);
            }
        });
        progressBar.show();
    }



    public boolean  showProgressDialog() {
        if(progressBar != null && progressBar.isShowing())
            return false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressDialog();
            }
        });
        return true;
    }

    private int processMsg=0;
    public void  setProcessingDialog() {

        String msgStr;

        if( processMsg==0)  msgStr=	"Processing...";//getResources().getString(R.string.sp_Processing_NORMAL);
        else msgStr="Processing...";//	getResources().getString(R.string.sp_Processing_NORMAL);

        SpannableStringBuilder progressBar_tmsg = new SpannableStringBuilder();

        SpannableString ss_msg = new SpannableString(msgStr);

        int sTyle=android.graphics.Typeface.BOLD; // bold
        int mColor=0xFF41B2C9;
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24 , getResources().getDisplayMetrics());
        ss_msg.setSpan(new AbsoluteSizeSpan(size), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new StyleSpan(sTyle), 0, msgStr.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ss_msg.setSpan(new ForegroundColorSpan(mColor), 0, msgStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        progressBar_tmsg.append(ss_msg);


        reqTransCancel=false;
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(progressBar_tmsg);

        progressBar.setCancelable(false);
        progressBar.show();
    }

    public boolean  waitProcessingDialog(int mode) {
        if(progressBar != null && progressBar.isShowing())
            return false;

        processMsg=mode;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProcessingDialog();
            }
        });
        return true;
    }


    boolean reqRes_Unvalid=false;

    private void capture_TranslateDataProcess() {

        if( Quit_flg)  {
            return ;
        }
        OnCapture_status=CAPTURE_DoneTranslate;
    }




    Camera.PictureCallback mPictureCallbackJpeg1 = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] jpgdata, Camera cm) {

            Point CaptureResolution = CameraControl.camControl.CaptureResolution();
            getScreenSizeDirection(); //Get screen direction and ScreenSize

            captured_angle=ScreenDirection;
            OnCapture_status=CAPTURE_GotImage;
            IppCaptureMode=true;

            Log.w(TAG, " #JPG# Tch_flashMode= [OFF] captured_angle= "+captured_angle);

            if(ipp_image!=null && !ipp_image.isRecycled())
                ipp_image.recycle();


            CameraControl.camControl.mCameraFlash(0);
            StopPreview(false);
            FlashBtn_OnOff(false);
            IppStatus=IPP_IMAGEGOT;
            LPRprevDet=0;
            if(handler != null)
            {
                handler.removeMessages(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.sendEmptyMessage(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }
            CameraControl.camControl.CloseCamera();
            // nib 11/09/13 if capturing consecutively, sometimes done do anything even though capture sound sounded.
            //              for next time, removes all checking messages
            mHandler.removeMessages(MSG_ReqCaptureFrame);
            mHandler.removeMessages(MSG_CaptureREQ);
            // ----------------------------------------------

            detProcessStatus=DET_CAPTURE_Ready;
            if(captured_angle==0 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else if(captured_angle==180 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            else if(captured_angle==90 )
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else  	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;

            ipp_image= BitmapFactory.decodeByteArray(jpgdata, 0, jpgdata.length);
            if(captured_angle !=0)
            {
                ipp_image=CarNumRcgn.GetRotatedBitmap(ipp_image, captured_angle);
            }

            int width=ipp_image.getWidth();
            int height=ipp_image.getHeight();
            if(height>width)
            {
                cropArea[0]=8; cropArea[2]=width-8;
                cropArea[1]=height/4; cropArea[3]=height*3/4;

            }
            else
            {
                cropArea[0]=height/5;
                cropArea[2]=height*4/5;
                cropArea[1]=width/5;
                cropArea[3]=width*4/5;
            }
            //cropArea[0]=0;
            //cropArea[2]=0;
            //cropArea[1]=0;
            //cropArea[3]=0;
            ImgButtonStatus=1;
            OnCapture_status=CAPTURE_GotImage;
            mHandler.sendEmptyMessage(MSG_CAPTURE_READY);
            mHandler.sendEmptyMessage(MSG_LPRprocessing);
            if(ChkSave.isChecked()) {
                mHandler.sendEmptyMessage(MSG_IPPSave);
            }
            mHandler.sendEmptyMessage(MSG_RetryGuide);
        }
    };




    private Runnable LPRprocessing = new Runnable() {
        public void run() {
            CarNumRcgn_BitmapProcessing();
        }
    };



    private void BitmapSave(Bitmap img)
    {
        String wcard= Environment.getExternalStorageDirectory().getPath();
        try {
            String filename=GetCurrentFileName(".jpg");
            FileOutputStream out = new FileOutputStream(wcard+SaveImage_path+filename);
            //tmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            img.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (FileNotFoundException e)  {
        }

    }

    private void BitmapSave(String filename,String path, Bitmap img, int ImgType)
    {
        String svPath=SaveImage_path; // "/CarNumRcgn/rawdata/";
        String wcard= Environment.getExternalStorageDirectory().getPath();
        try {
            FileOutputStream out = new FileOutputStream(wcard+svPath+ filename);

            if(ImgType==1) img.compress(Bitmap.CompressFormat.PNG, 100, out);
            else {
                img.compress(Bitmap.CompressFormat.JPEG, 100, out);
                try {
                    ExifInterface mExif=new ExifInterface(wcard+svPath+ filename);
//							mExif.setAttribute(ExifInterface.TAG_MAKE, CarNumRcgn.GetVersion() + " (" + Build.VERSION.RELEASE + ")");
//							mExif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
                    mExif.saveAttributes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e)  {
        }

    }



    private boolean onIppBitmap=false;

    private void CarNumRcgn_BitmapProcessing() // input bitmap ipp_image
    {
        long tm,tm1;

        if(onIppBitmap)
            return;
        if(ipp_image==null || ipp_image.isRecycled())
            return;

        onIppBitmap=true;
        CAM_width=ipp_image.getWidth();
        CAM_height=ipp_image.getHeight();

        tm=System.currentTimeMillis();

        LPRprevDet=0;
        LPRres=CarNumRcgn.bitmapCarNumRcgn(ipp_image,cropArea,status);
        tm1=System.currentTimeMillis()-tm;

        LPRNoti="";
        if(AutoLPR) {
            LPRNoti="["+ gFileIndex +"/"+  totalFilenum + "] ";
        }
        if(status[0]>0)
        {
            LPRNoti+=" LPRres="+LPRres+" tm= "+ tm1;

            LPR_left=status[1]; LPR_top=status[2];
            LPR_right=status[3]; LPR_bottom=status[4];
            LPRprevDet=1;
        }
        else
        {
            LPRNoti+=" Retry ! "+" tm= "+ tm1;
        }

        mHandler.sendEmptyMessage(MSG_LPRNoti);

        detProcessStatus=DET_CAPTURE_Start;

        if(ipp2_image!=null)
        {
            ipp2_image.recycle();
        }

        onIppBitmap=false;

        if(ImgButtonStatus>0)
        {
            if(handler != null)
            {
                handler.sendEmptyMessage(getResources().getIdentifier("chkMessage", "id", getPackageName()));
                handler.removeMessages(getResources().getIdentifier("preview_raw", "id", getPackageName()));
            }

            if(AutoLPR){
                mHandler.sendEmptyMessageDelayed(MSG_STEPLPR, 200);
            }
        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//            	ViewInvalid();
//            }
//        });

    }

    private int surfaceDirection()   //captured_angle when image captured
    {
        WindowManager manager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int rotation = display.getRotation();

        if (rotation == Surface.ROTATION_0)
            return  0;
        else if (rotation == Surface.ROTATION_90)
            return  90;
        else if (rotation == Surface.ROTATION_180)
            return  180;
        else if (rotation == Surface.ROTATION_270)
            return 270;
        return 0;
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


    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        public void onShutter() {
            Log.i(getClass().getSimpleName(), "SHUTTER CALLBACK");
            //		SoundPool mSound_pool = new SoundPool(6, AudioManager.STREAM_SYSTEM_ENFORCED, 0);
            mSound_pool.play(mSoundID, 1, 1, 0, 0, 1);
        }
    };


    public boolean RequestCaptureFrame() {

        DismisprogresssDialog();
        if(OnCapture_status>=CAPTURE_START)
            return true;

        ImgButtonStatus=0;
        OnCapture_status=CAPTURE_START;
        LPRprevDet= 0;
        //mHandler.sendEmptyMessageDelayed(MSG_CaptureREQ,5000);

        //CameraControl.camControl.ManagerCamera().takePicture(null, null,mPictureCallbackJpeg2);
        CameraControl.camControl.ManagerCamera().takePicture(mShutterCallback, null,mPictureCallbackJpeg1);
        return true;
    }



    long checkBattery_tm=0;

    public void DismisprogresssDialog() {
        if (progressBar != null && progressBar.isShowing()) {
            progressBar.dismiss();
            progressBar = null;
        }
    }


    // nib 03/06/13 [TD318425, TD318466] stop scrolling when edit mode
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        TouchMenuTime=System.currentTimeMillis();
        return super.dispatchTouchEvent(ev);
    }
    // ----------------------------
}


