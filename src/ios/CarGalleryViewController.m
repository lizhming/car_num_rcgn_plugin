//
//  CarGalleryViewController.m
//  seoulSmartReport
//
//  Created by Flymax on 9/13/20.
//

#import "CarGalleryViewController.h"

@interface CarGalleryViewController ()
{
    NSMutableArray *recipeImages;
    NSMutableArray* checked;
    __weak IBOutlet UICollectionView *collectionView;
    NSString *selectedImage;
    BOOL returned;
}

@end

@implementation CarGalleryViewController
-(NSArray *)listFileAtPath:(NSString *)path
{
    int count;

    NSArray *directoryContent = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:NULL];

    for (count = 0; count < (int)[directoryContent count]; count++)
    {
        NSLog(@"File %d: %@", (count + 1), [directoryContent objectAtIndex:count]);
    }
    return directoryContent;
}
-(void) create:(NSString*) path {
    if(![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:NULL];
    }
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    [self create:[NSString stringWithFormat:@"%@/images", documentsDirectory]];
//    paths = [self listFileAtPath:documentsDirectory];
    NSString *imagePath = [NSString stringWithFormat:@"%@/images", documentsDirectory];
    NSArray *directoryContent = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:imagePath error:NULL];
    
    recipeImages = [NSMutableArray array];
    checked = [NSMutableArray array];

    for (int count = 0; count < (int)[directoryContent count]; count++)
    {
        [recipeImages addObject: [NSString stringWithFormat:@"%@/%@", imagePath, [directoryContent objectAtIndex:count]]];
        [checked addObject:[NSNumber numberWithBool:false]];
    }
    recipeImages = [[recipeImages sortedArrayUsingComparator:^(id a, id b) {
        return [b compare:a];
    }] mutableCopy];
    
    [collectionView registerNib:[UINib nibWithNibName:@"GalleryCell" bundle:nil] forCellWithReuseIdentifier:@"GalleryCell"];
    
    returned = false;

}
- (void) viewDidDisappear:(BOOL)animated {
    if (!returned) {
        _callback(@"");
    }
}
- (IBAction)onClickCancel:(id)sender {
    _callback(@"");
    [self dismissViewControllerAnimated:true completion:nil];
}
- (IBAction)onClickDelete:(id)sender {
    BOOL isCheck = false;
    for (int i = (int)recipeImages.count; i > 0; i--) {
        if ([[checked objectAtIndex:i - 1] boolValue]) {
            isCheck = true;
        }
    }
    if (!isCheck) return;
    
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:@"선택 삭제"
                               message:@"정말 삭제하시겠습니까?"
                               preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action1 = [UIAlertAction actionWithTitle:@"확인" style:UIAlertActionStyleDefault handler: ^(UIAlertAction * action) {
        for (int i = (int)self->recipeImages.count; i > 0; i--) {
            if ([[self->checked objectAtIndex:i - 1] boolValue]) {
                [[NSFileManager defaultManager] removeItemAtPath:[self->recipeImages objectAtIndex:i - 1] error:NULL];
                [self->checked removeObjectAtIndex: i - 1];
                [self->recipeImages removeObjectAtIndex: i - 1];
            }
        }
        
        [self->collectionView reloadData];
    }];

    UIAlertAction *action2 = [UIAlertAction actionWithTitle:@"취소" style:UIAlertActionStyleDefault handler: ^(UIAlertAction * action) {
        
    }];
    
    [alert addAction:action1];
    [alert addAction:action2];
    [self presentViewController:alert animated:YES completion:nil];
    
    
}
- (IBAction)onClickCheck:(id)sender {
    UIButton *button = (UIButton*) sender;
    if ([[checked objectAtIndex:button.tag] boolValue]) {
        [button setImage:[UIImage systemImageNamed:@"square"] forState: UIControlStateNormal];
    } else {
        [button setImage:[UIImage systemImageNamed:@"checkmark.square"] forState: UIControlStateNormal];
    }
    
    checked[button.tag] = [NSNumber numberWithBool:![[checked objectAtIndex:button.tag] boolValue]];
    
    
    
}
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return recipeImages.count;
}
 
 
- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *identifier = @"GalleryCell";
    
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:identifier forIndexPath:indexPath];
    
    UIImageView *recipeImageView = (UIImageView *)[cell viewWithTag:100];
    UILabel *label = (UILabel*)[cell viewWithTag:101];
    UIButton *button = (UIButton*) [cell viewWithTag:102];
    
    NSArray* subviews = [[cell contentView] subviews];
    for (id aView in subviews) {
        if ([aView isKindOfClass:[UIButton class]]) {
            button = aView;
        } else if ([aView isKindOfClass:[UILabel class]]) {
            label = aView;
        } else if ([aView isKindOfClass:[UIImageView class]]) {
            recipeImageView = aView;
        }
    }
    [button setTag:indexPath.row];
    [button setImage:[UIImage systemImageNamed:@"square"] forState: UIControlStateNormal];
    
    if ([[checked objectAtIndex: indexPath.row] boolValue]) {
        [button setImage:[UIImage systemImageNamed:@"checkmark.square"] forState: UIControlStateNormal];
    } else {
        [button setImage:[UIImage systemImageNamed:@"square"] forState: UIControlStateNormal];
    }
    
    recipeImageView.image = [UIImage imageNamed:[recipeImages objectAtIndex:indexPath.row]];
    
    [button addTarget:self action:@selector(onClickCheck:) forControlEvents: UIControlEventTouchUpInside];
    
    NSURL *localFileUrl = [NSURL fileURLWithPath:[recipeImages objectAtIndex:indexPath.row]];
    CGImageSourceRef imageSource = CGImageSourceCreateWithURL((__bridge CFURLRef)localFileUrl, NULL);
    NSDictionary* metadata = (__bridge NSDictionary*) CGImageSourceCopyPropertiesAtIndex(imageSource, 0, NULL);
    NSDictionary *exif = [metadata objectForKey:(NSString*)kCGImagePropertyExifDictionary];
//    NSDictionary *gps = [metadata objectForKey:(NSString*)kCGImagePropertyGPSDictionary];
//    NSString *date = [gps objectForKey:(NSString*) kCGImagePropertyGPSDateStamp];
//    NSString *time = [gps objectForKey:(NSString*) kCGImagePropertyGPSTimeStamp];
    NSString *dtime = [exif objectForKey:(NSString*) kCGImagePropertyExifDateTimeOriginal];
    
    label.text = [NSString stringWithFormat:@"%@", dtime];
    return cell;
}
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return CGSizeMake(CGRectGetWidth(collectionView.frame) / 3, CGRectGetWidth(collectionView.frame) / 3 + 45);
}
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    returned = true;
    NSURL *fileURL = [[NSURL alloc] initFileURLWithPath:recipeImages[indexPath.row]];
    
    _callback([fileURL absoluteString]);
//    _callback(recipeImages[indexPath.row]);
    [self dismissViewControllerAnimated:true completion:nil];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
