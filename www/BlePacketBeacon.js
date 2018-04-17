var exec = require('cordova/exec');

exports.coolMethod = function (data, timeout, success, error) {
    exec(success, error, 'BlePacketBeacon', 'sendData', [data, timeout]);
};
