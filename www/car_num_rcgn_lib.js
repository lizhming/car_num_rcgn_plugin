var exec = require('cordova/exec');

exports.recognize = function (path, success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'recognize', [path]);
};

exports.showGallery = function (success, error) {
    exec(success, error, 'car_num_rcgn_lib', 'show_gallery', []);
};
