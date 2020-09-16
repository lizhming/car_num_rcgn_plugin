/********* car_num_rcgn_lib.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <libLPRpro/libCarnumRcgn.h>
#import <ImageIO/ImageIO.h>
#import "CarGalleryViewController.h"
@interface car_num_rcgn_lib : CDVPlugin {
  // Member variables go here.
}

- (void)recognize:(CDVInvokedUrlCommand*)command;
- (void)show_gallery:(CDVInvokedUrlCommand*)command;
@end

@implementation car_num_rcgn_lib

- (BOOL)checkFolderExistence:(NSString *)folderpath {
    BOOL isFolder;
    return ([[NSFileManager defaultManager] fileExistsAtPath:folderpath isDirectory:&isFolder] && isFolder);
}
- (NSString *)GetPath:(NSString *)name {
    NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentDirectory = [documentPaths objectAtIndex:0];
    NSString *path = [documentDirectory stringByAppendingPathComponent:name];
    return path;
}
- (void)show_gallery:(CDVInvokedUrlCommand*)command
{
//    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
//    UIViewController * yourViewController = [storyboard   instantiateViewControllerWithIdentifier:@"iController"] ;
    CarGalleryViewController *vc = [[CarGalleryViewController alloc] initWithNibName:@"CarGalleryViewController" bundle:nil];
    vc.callback = ^(NSString* result){
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: result];
        if (result.length != 0){
            NSURL *localFileUrl = [NSURL URLWithString:result];
            CGImageSourceRef imageSource = CGImageSourceCreateWithURL((__bridge CFURLRef)localFileUrl, NULL);
            NSDictionary* metadata = (__bridge NSDictionary*) CGImageSourceCopyPropertiesAtIndex(imageSource, 0, NULL);
            NSLog (@"image meta data %@", metadata);
            
            NSError *error;
            NSString *jsonString = nil;
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:metadata
                                            options:kNilOptions
                                            error:&error];
            
            NSString *thisFileName = result; //[[NSURL fileURLWithPath:result] absoluteString];
            
            if (!jsonData){
                NSLog(@"Error converting to JSON: %@",error);
                jsonString = @"{}";

            } else {
                jsonString = [[NSString alloc] initWithData: jsonData encoding:NSUTF8StringEncoding];
            }
            
            
            NSLog(@"JSON -> %@\n\n", jsonString);

            NSMutableDictionary* thisResult = [[NSMutableDictionary alloc] init];
            [thisResult setObject: thisFileName forKey:@"filename"];
            [thisResult setObject: jsonString forKey:@"json_metadata"];
            
            // JSON Conversion for compatibility with Android plugin results
            NSData *thisJsonResult;
            NSError *jsonError = nil;
            
            // convert thisResult object to JSON
            if ([NSJSONSerialization isValidJSONObject:thisResult]) {
                thisJsonResult = [NSJSONSerialization dataWithJSONObject:thisResult
                                                      options:NSJSONWritingPrettyPrinted
                                                      error:&jsonError];
            }
            
            if (thisJsonResult != nil && jsonError == nil) {
                NSString *jsonStringResult = [[NSString alloc] initWithData:thisJsonResult encoding:NSUTF8StringEncoding];
                
                // filter results, remove "{}" from key values
                
                NSMutableString *filteredJsonResult = [NSMutableString stringWithString:jsonStringResult];
                NSRange idx = [filteredJsonResult rangeOfString:@"{GPS}"];
                if (idx.location == NSNotFound) {
                    NSLog(@"{GPS} string not found.");
                } else {
                    [filteredJsonResult replaceCharactersInRange:idx withString:@"GPS"];
                }
                
                idx = [filteredJsonResult rangeOfString:@"{Exif}"];
                if (idx.location == NSNotFound) {
                    NSLog(@"{Exif} string not found.");
                } else {
                    [filteredJsonResult replaceCharactersInRange:idx withString:@"Exif"];
                }

                NSLog(@"JSON Result Returned: %@\n\n",filteredJsonResult);
                
                // return filtered JSON string
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:filteredJsonResult];
                
            } else {
                // json conversion failed, return dictionary, this should never happen
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:thisResult];
            }
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };
    [self.viewController presentViewController: vc animated:YES completion:nil];
    
}
- (void)recognize:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* path = [command.arguments objectAtIndex:0];

    if (path != nil && [path length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:path];
        NSError *error;
        NSURL *url = [NSURL URLWithString:path];
        NSData *data = [NSData dataWithContentsOfURL:url];
        UIImage *img1 = [[UIImage alloc] initWithData:data];
        
        // GCD (Grand Central Dispatch)에서 외부 함수 호출시 reference counter 를 위해 __weak keyword(iOS 5.0 and up)를 사용.
        //__weak typeof(self) weakself = self;

        NSFileManager *fileManager = [NSFileManager defaultManager];

        libCarnumRcgn *m_libCarnumRcgn;
        m_libCarnumRcgn = [libCarnumRcgn getInstance];
        
        // init libCarnumRcgn
        NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
        [m_libCarnumRcgn SetActivity:bundleIdentifier];
        
        // init working folders
        // raw 데이터를 보관하는 폴더.
        NSString *rawdata = [self GetPath:@"rawdata"];
        if (![self checkFolderExistence:rawdata]) {
            // not exist
            if (![fileManager createDirectoryAtPath:rawdata withIntermediateDirectories:YES attributes:nil error:&error]) {
                // Error
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                return;
            }
        }
        
        //이미지 패스 생성
        NSString *imagePath = [self GetPath:@"images"];
        if (![self checkFolderExistence:imagePath]) {
            // not exist
            if (![fileManager createDirectoryAtPath:imagePath withIntermediateDirectories:YES attributes:nil error:&error]) {// Error
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                return;
            }
        }
        
        // 이미지 저장
        if (![path containsString:imagePath]) {
            NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
            [formatter setDateFormat:@"yyyy-dd-MM HH:mm:ss"];

            NSDate *currentDate = [NSDate date];
            NSString *dateString = [formatter stringFromDate:currentDate];
            
            [[NSFileManager defaultManager] createFileAtPath:[NSString stringWithFormat:@"%@/%@.png", imagePath, dateString] contents:data attributes:nil];
        }
        
        
        // send CarnumRcgn working folder
        [m_libCarnumRcgn SetExternalStorage:[self GetPath:@"rawdata"]];
        
        dispatch_queue_t addQueue = dispatch_queue_create("recognizeCarnum", NULL);
        dispatch_async(addQueue, ^{
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                UIImage *img = img1;
                
                int croparea[4] = {0};
                // 카메라 촬영에 의해 온 것이라면, 스캔 영역을 지정할 수 있다...
                
                int status[5] = {0};
                __block int *pStatus = status;
                
                NSString *res = [m_libCarnumRcgn bitmapCarNumRcgn:img :croparea :status];
                
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:res];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                if (pStatus[0] > 0) {
                    
                }
            });
        });
        
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

@end
