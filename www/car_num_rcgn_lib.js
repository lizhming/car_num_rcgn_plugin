var exec = require('cordova/exec');

exports.recognize = function (path, success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'recognize', [path]);
};

exports.showGallery = function (success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'show_gallery', []);
};

exports.showGalleryTmp = function (success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'show_gallery_tmp', []);
};

exports.openCamera = function(success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'open_camera', []);
};

exports.openCameraTmp = function(success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'open_camera_tmp', []);
};
