package com.exampledemo.parsaniahardik.registerloginsession;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ParseContent {

    private final String KEY_SUCCESS = "errors";
    private final String KEY_MSG = "message";
    private final String KEY_AddressList = "addressList";
    private final String KEY_DATA = "Data";
    private  ArrayList<HashMap<String, String>> hashMap;
    private Activity activity;
    PreferenceHelper preferenceHelper;

    ArrayList<HashMap<String, String>> arraylist;

    public ParseContent(Activity activity) {
        this.activity = activity;
        preferenceHelper = new PreferenceHelper(activity);

    }

   public boolean isSuccess(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString(KEY_SUCCESS).equals("false")) {
                return true;
            } else {

                return false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

   public String getErrorMessage(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString(KEY_MSG);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "No data";
    }

   public void saveInfo(String response, String key) {
       preferenceHelper.putIsLogin(true);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString(KEY_SUCCESS).equals("false")) {
                  String dataEncrypted = jsonObject.getString("data");
                  Log.d("dataEncrypt", dataEncrypted);
                  String de = KCrypt.decrypt(key, dataEncrypted);
                  Log.d("decode-de", de);
                  JSONObject resJson = new JSONObject(de);
                  JSONObject user = resJson.getJSONObject("user");
                  preferenceHelper.putName(user.getString("name"));
                  preferenceHelper.putHobby(user.getString("email"));
//                for (int i = 0; i < dataArray.length(); i++) {
//
//                   JSONObject dataobj = dataArray.getJSONObject(i);
//                    preferenceHelper.putName(dataobj.getString(AndyConstants.Params.NAME));
//                    preferenceHelper.putHobby(dataobj.getString(AndyConstants.Params.HOBBY));
//              }
          }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("Error decode", e.getMessage());
        }

    }
}