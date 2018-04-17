package cordova.plugin.ble.packet.beacon;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class advertise a set of data through ble
 */
public class BlePacketBeacon extends CordovaPlugin {

    private String uuid = "3B4D0479-8AB9-4A6A-B127-3A74DB7FE4D5";
    private ParcelUuid pUuid = new ParcelUuid(UUID.fromString(uuid));
    private BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    private AdvertiseData data;
    private AdvertiseSettings settings;


    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };

    private AdvertiseCallback resettingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
            advertiser.startAdvertising(settings, data, advertisingCallback);
        }
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            callbackContext.success("This device does not support this feature");
            return true;
        }

        if (action.equals("sendData")) {
            String data = args.getString(0);
            int timeout = args.getInt(1);
            this.sendData(data, timeout, callbackContext);
            return true;
        }


        return false;
    }

    private void sendData(String data, int timeout, CallbackContext callbackContext) {

        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setConnectable(false).setTimeout(1000)
                .build();


        AdvertiseData advertData  = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, "Data".getBytes(Charset.forName("UTF-8")))
                .build();

        if (advertiser != null) {
            advertiser.stopAdvertising(resettingCallback);
        } else {
            advertiser.startAdvertising(settings, advertData, advertisingCallback);
        }

    }
}
