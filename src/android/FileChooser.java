package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.content.ClipData;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;

    public static final String MIME = "mime";

    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            JSONObject filters = inputs.optJSONObject(0);
            chooseFile(filters, callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(JSONObject filter, CallbackContext callbackContext) {
        String uri_filter = filter.has(MIME) ? filter.optString(MIME) : "*/*";

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(uri_filter);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(requestCode == PICK_FILE_REQUEST && callback != null)) { return; }
        if (resultCode == Activity.RESULT_OK) {
            ClipData clipData = data.getClipData();
            JSONArray savedUris = new JSONArray();
            if (clipData != null) {
                final int clipDataCount = clipData.getItemCount();
                if(clipDataCount > 0) {
                    for (int i = 0; i < clipDataCount; i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        if (uri != null) {
                            Log.w(TAG, uri.toString());
                            savedUris.put(uri.toString());
                        }
                    }
                }
            } else {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.w(TAG, uri.toString());
                    savedUris.put(uri.toString());
                } else {
                    callback.error("File uri was null");
                }
            }
            callback.success(savedUris.toString());
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // keep this string the same as in iOS document picker plugin
            // https://github.com/iampossible/Cordova-DocPicker
            callback.error("User canceled.");
        } else {
            callback.error(resultCode);
        }
    }
}
