package info.tinyapps;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.Base64;
import java.util.UUID;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlePacketBeacon extends CordovaPlugin {
    private CallbackContext mCallbackContext = null;
    //private BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    private AdvertiseData data;
    private AdvertiseSettings settings;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "BlePacketBeacon";
    private ParcelUuid mUUID = new ParcelUuid(UUID.fromString("3B4D0479-8AB9-4A6A-B127-3A74DB7FE4D5"));

    final String [] PERMSSIONS = {
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION
        };

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "Initializing BlePacketBeacon");
    }

    private AdvertiseCallback mAdvertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            try {
                Log.d(TAG,"onStartSuccess");
                JSONObject data = new JSONObject();
                data.put("startAdvertising", "started");
                sendInfo(mCallbackContext, data, true);
            }
            catch (Exception e){
                Log.e(TAG,"error in onStartSuccess",e);
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
            try {
                JSONObject data = new JSONObject();
                data.put("startAdvertising", "failed");
                data.put("error code", errorCode);
                sendError(mCallbackContext, data, true);
            }
            catch (Exception e){
                Log.e(TAG,"error in onStartSuccess",e);
            }
        }
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals("startAdvertising")) {
                String data = args.getString(0);
                if(!startAdvertising(data, callbackContext)){
                    sendError(callbackContext,"devcie cannot advertise",true);
                }
                else{
                    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                }
            }
            else if(action.equals("stopAdvertising")){
                stopAdvertising();
                PluginResult result;
                JSONObject permResult = new JSONObject();
                permResult.put("method_name","stopAdvertising");

                result = new PluginResult(PluginResult.Status.OK,permResult);
                result.setKeepCallback(false);
                callbackContext.sendPluginResult(result);
            }
            else if(action.equals("hasPermissions")){
                PluginResult result;
                JSONObject permResult = new JSONObject();
                permResult.put("method_name","hasPermissions");

                if(!hasPermissions())
                    permResult.put("result",false);
                else
                    permResult.put("result",true);

                result = new PluginResult(PluginResult.Status.OK,permResult);
                result.setKeepCallback(false);
                callbackContext.sendPluginResult(result);
            }
            else if(action.equals("hasBlePeripheral")){
                hasBlePeripheral(callbackContext);
                return true;
            }
            else if(action.equals("requestPermissions")){
                PluginResult result;
                JSONObject permResult = new JSONObject();
                permResult.put("method_name","requestPermissions");

                mCallbackContext = callbackContext;
                if(requestPermissions()){
                    result = new PluginResult(PluginResult.Status.OK,permResult);
                    result.setKeepCallback(false);
                    callbackContext.sendPluginResult(result);
                }
                else{
                    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                }
            }
            else{
                return false;
            }
        }
        catch (Exception e){
            sendError(callbackContext,e);
        }

        return true;
    }

    private void stopAdvertising() throws Exception {
        if (canAdvertise())
            return;

        Log.d(TAG,"stopAdvertising called");
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        advertiser.stopAdvertising(mAdvertisingCallback);
    }

    private boolean startAdvertising(String data, CallbackContext callBack) throws Exception{
        if(!canAdvertise())
            return false;

        Log.d(TAG,"startAdvertising with " + data);
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setConnectable(false);//setTimeout(1000);
        settingsBuilder.setTimeout(100);


        AdvertiseData.Builder advertDataBuilder  = new AdvertiseData.Builder();

        byte [] dataBytes = data.getBytes();//Base64.decode(data,Base64.DEFAULT);
        advertDataBuilder.addManufacturerData(dataBytes.length,dataBytes);

        mCallbackContext = callBack;
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        advertiser.startAdvertising(settingsBuilder.build(),advertDataBuilder.build(),mAdvertisingCallback);

        return true;
    }

    //new methods
    private boolean requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions()) {
                cordova.getActivity().requestPermissions(PERMSSIONS, PERMISSION_REQUEST_COARSE_LOCATION);
                return false;
            }
        }

        return true;
    }

    boolean hasPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMSSIONS) {
                if (cordova.getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        boolean permissionsOK = true;
        try {
            JSONObject data = new JSONObject();
            data.put("method_name", "requestPemrissions");

            for(int i = 0; i < grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    permissionsOK = false;
                    return;
                }
            }

            if(permissionsOK)
                sendInfo(mCallbackContext,data,true);
            else
                sendError(mCallbackContext,data,true);
        }
        catch (Exception e){
            Log.e(TAG,"",e);
        }
    }

    public static JSONObject dumpError(String txt, Exception e) {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");

        if(txt != null) {
            buf.append(txt);
            buf.append("\n");
        }

        buf.append(e.getMessage());
        buf.append("\n");
        buf.append(e.getClass().getName());

        StackTraceElement[] traces = e.getStackTrace();

        if (traces != null) {
            for (StackTraceElement trace : traces) {
                buf.append("\n");
                buf.append(trace.getClassName());
                buf.append("\n");
                buf.append(trace.getMethodName());
                buf.append("\n");
                buf.append(trace.getFileName());
                buf.append("\n");
                buf.append(trace.getLineNumber());
                buf.append("\n");
            }
        }

        JSONObject result = new JSONObject();

        try {
            result.put("error", buf.toString());
        }
        catch (Exception ee){
        }

        return result;
    }

    protected void sendInfo(CallbackContext callbackContext, JSONObject info, boolean finalCall) {
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,info);
            result.setKeepCallback(finalCall?false:true);
            callbackContext.sendPluginResult(result);
        }
    }

    protected void sendError(CallbackContext callbackContext, String data, boolean isFinal) throws JSONException{
        JSONObject resp = new JSONObject();
        resp.put("error",data);

        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,resp.toString());
            if(isFinal)
                result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        }
    }

    protected void sendError(CallbackContext callbackContext, JSONObject data, boolean isFinal) {
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,data.toString());
            if(isFinal)
                result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        }
    }

    protected void sendError(CallbackContext callbackContext ,Exception e) {
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,dumpError("",e));
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        }
    }

    boolean hasBlePeripheral(CallbackContext callbackContext) throws Exception{
        boolean res = false;
        PluginResult result;
        JSONObject permResult = new JSONObject();

        while(true){
            if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
                permResult.put("result",false);
                permResult.put("details","BT not enabled");
                break;
            }

            if (!isTransmissionSupported()) {
                permResult.put("result",false);
                permResult.put("details","BLE peripherial is not supporeted");
                break;
            }

            res = true;
            permResult.put("result",true);
            break;
        }

        if(!res) {
            result = new PluginResult(PluginResult.Status.ERROR, permResult);
        }
        else{
            result = new PluginResult(PluginResult.Status.OK, permResult);
        }

        result.setKeepCallback(false);
        callbackContext.sendPluginResult(result);

        return res;
    }

    boolean canAdvertise(){
        if (!isTransmissionSupported())
            return false;

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled())
            return false;

        return true;
    }

    boolean isTransmissionSupported() {
        if (Build.VERSION.SDK_INT < 21) {
            return false;
        }
        else if (!cordova.getActivity().getApplicationContext().getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            return false;
        }
        else {
            try {
                if (((BluetoothManager)cordova.getActivity().getSystemService("bluetooth")).getAdapter().getBluetoothLeAdvertiser() == null) {
                    return false;
                }
            }
            catch (Exception var3) {
                return false;
            }
        }

        return true;
    }
}

