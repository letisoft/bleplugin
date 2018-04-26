cordova.define("ble-packet-beacon.BlePacketBeacon", function(require, exports, module) {
var exec = require('cordova/exec');

var PLUGIN_NAME = 'BlePacketBeacon';

var BlePacketBeacon = {
  startAdvertising: function(onSuccess, onError, data) {
    exec(onSuccess, onError, PLUGIN_NAME, 'startAdvertising', [data]);
  },

  stopAdvertising: function(onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'stopAdvertising',[]);
  },

  hasPermissions: function(onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'hasPermissions', []);
  },

  hasBlePeripheral: function(onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'hasBlePeripheral', []);
  },

  requestPermissions: function(onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'requestPermissions', []);
  }
};

module.exports = BlePacketBeacon;

});
