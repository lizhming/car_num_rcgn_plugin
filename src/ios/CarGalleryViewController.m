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

    for (int count = 0; count < (int)[directoryContent count]; count++)
    {
        [recipeImages addObject: [NSString stringWithFormat:@"%@/%@", imagePath, [directoryContent objectAtIndex:count]]];
        
    }
    recipeImages = [recipeImages sortedArrayUsingComparator:^(id a, id b) {
        return [a compare:b];
    }];
//    recipeImages = [NSArray arrayWithObjects:@"angry_birds_cake.jpg", @"creme_brelee.jpg", @"egg_benedict.jpg", @"full_breakfast.jpg", @"green_tea.jpg", @"ham_and_cheese_panini.jpg", @"ham_and_egg_sandwich.jpg", @"hamburger.jpg", @"instant_noodle_with_egg.jpg", @"japanese_noodle_with_pork.jpg", @"mushroom_risotto.jpg", @"noodle_with_bbq_pork.jpg", @"starbucks_coffee.jpg", @"thai_shrimp_cake.jpg", @"vegetable_curry.jpg", @"white_chocolate_donut.jpg", nil];
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
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return recipeImages.count;
}
 
 
- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *identifier = @"GalleryCell";
    
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:identifier forIndexPath:indexPath];
    
    UIImageView *recipeImageView = (UIImageView *)[cell viewWithTag:100];
    UILabel *label = (UILabel*)[cell viewWithTag:101];
    recipeImageView.image = [UIImage imageNamed:[recipeImages objectAtIndex:indexPath.row]];
    
    
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
