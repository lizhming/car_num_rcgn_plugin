//
//  CarGalleryViewController.h
//  seoulSmartReport
//
//  Created by Flymax on 9/13/20.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CarGalleryViewController : UIViewController <UICollectionViewDelegateFlowLayout, UICollectionViewDataSource> {
    

}
@property (nonatomic, copy) void (^callback)(NSString* result);
@end

NS_ASSUME_NONNULL_END
