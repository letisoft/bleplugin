# BlePacketBeacon
This plugin in lets you advertise a package of data through ble


# Installation
As like any other plugin in phonegap, from your cli, go to the main root folder of your application's project and run the following command:

 cordova plugin add cordova-plugin-ble-packet-beacon

Upon completion your app will have successfully installed the plugin.

# Usage

From your applications javascript file reference the plug in the following way:

window.sendData = function(data, timeout, callback) {
    cordova.exec(callback, function(err) {
        callback('Nothing to echo.');
    }, "BlePacketBeacon", "sendData", [data, timeout]);
};

Where data is a string to be broadcasted through ble advertiser and timeout representing the amount of time for the data to be broadcast for.
