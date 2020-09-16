//
//  libCarnumRcgn.h
//  libCarnumRcgn
//
//  Created by cardcam on 2018. 8. 10..
//  Upgraded by cardcam on 2019. 2. 17. (pro version)
//  Copyright © 2019년 Cardcam. All rights reserved.
//

#import <UIKit/UIKit.h>

//! Project version number for libCarnumRcgn.
FOUNDATION_EXPORT double libCarnumRcgnVersionNumber;

//! Project version string for libCarnumRcgn.
FOUNDATION_EXPORT const unsigned char libCarnumRcgnVersionString[];

// In this header, you should import all the public headers of your framework using statements like #import <libCarnumRcgn/PublicHeader.h>

// framework build version
#define FrameworkBuildVersion @"2.0.2.2"

// nib 08/12/2018 added libCarnumRcgn class
@interface libCarnumRcgn : NSObject {
	
}

// 번호판 인식 class instance 가져오기 (singleton)
+ (libCarnumRcgn *)getInstance;

// 이 함수를 호출해야 사용할 앱의 bundle identifier에의해 라이브러리가 활성화된다.
- (NSInteger)SetActivity:(NSString *)bundleId;

// format : CARNUM-RCGN-1.0.005
- (NSString *)GetVersion;

/* format
 CARNUN-RCGN-1.0.011 Trial (2018/06/01~2018/12/31)
 or
 CARNUN-RCGN-1.0.012 Commercial
 */
- (NSString *)GetDescription;

// set external file storage directory
- (void)SetExternalStorage:(NSString *)storagePath;

/* calculates the focus value for the region of  the center (mx, my)  focus area Size: width=200, height=160
 *  input : yuvdata (input) yuv420sp data from camera preview
 *            mx,my : Center coordinates  , defalt to (0,0)  = (width/2, height/2)
 *  return : focus value
 *
 */
- (int)GetFocusValueYuv420:(unsigned char *)yuvdata :(int)width :(int)height :(int)mx :(int)my :(int)mode;

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
- (NSString *)bitmapCarNumRcgn:(UIImage *)img :(int *)cropArea :(int *)status;

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
- (NSString *)yuvCarNumRcgn:(char *)yuvdata :(int)width :(int)height :(int)screenDir :(float *)detArea :(int *)status;



- (UIImage*)rotImg90:(UIImage*)img;
- (UIImage*)rotImg180:(UIImage*)img;

@end
