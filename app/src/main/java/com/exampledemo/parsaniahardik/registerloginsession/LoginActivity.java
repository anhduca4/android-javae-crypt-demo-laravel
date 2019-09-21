package com.exampledemo.parsaniahardik.registerloginsession;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;

import static java.lang.Math.ceil;

public class LoginActivity extends AppCompatActivity {

    private EditText etusername, etpassword;
    private Button btnlogin;
    private TextView tvreg;
    private ParseContent parseContent;
    private final int LoginTask = 1;
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        parseContent = new ParseContent(this);
        preferenceHelper = new PreferenceHelper(this);

        etusername = (EditText) findViewById(R.id.etusername);
        etpassword = (EditText) findViewById(R.id.etpassword);

        btnlogin = (Button) findViewById(R.id.btn);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void login() throws IOException, JSONException {

        if (!AndyUtils.isNetworkAvailable(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Internet is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        AndyUtils.showSimpleProgressDialog(LoginActivity.this);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        double timeInMili = ceil(calendar.getTimeInMillis()/1000);
        final HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.Params.USERNAME, etusername.getText().toString());
        map.put(AndyConstants.Params.PASSWORD, etpassword.getText().toString());
        String ts = String.format("%.0f", timeInMili);
        map.put("ts", ts);

//        map.put("ts", timeInMili.toPlainString());
//        final HashMap<String, String> mapHeader = new HashMap<>();
//        mapHeader.put("uuid", "akjsh®fjkhfa-kashjfgafh-12219678");
        new AsyncTask<Void, Void, WrapperLogin>(){
            protected WrapperLogin doInBackground(Void[] params) {
                String response="";
                String responseS="";
                String key = "";
                try {

                    HttpRequest reqS = new HttpRequest(AndyConstants.ServiceType.SETTINGS);
                    responseS = reqS.withHeaders("uuid:akjshfjkhfa-kashjfgafh-12219678").sendAndReadString();
                    Log.d("setting", responseS);
                    JSONObject jsonObject = new JSONObject(responseS);
                    key =  jsonObject.getJSONObject("data").getJSONObject("key_hash").getString("key_store");
                    String hashId = jsonObject.getJSONObject("data").getJSONObject("key_hash").getString("id");

                    HttpRequest req = new HttpRequest(AndyConstants.ServiceType.LOGIN);
                    JSONObject postLoginJson = new JSONObject(map);
                    Log.d("pLoginJson", postLoginJson.toString());
                    Log.d("postLogin", postLoginJson.toString());

                    final HashMap<String, String> postLogin = new HashMap<>();
                    postLogin.put("payload", KCrypt.encrypt(key, postLoginJson.toString()));
                    response = req.withHeaders("Hash-id:" + hashId).prepare(HttpRequest.Method.POST).withData(postLogin).sendAndReadString();
                } catch (Exception e) {
                    Log.d("e.getMessage()", e.getMessage());
                    response=e.getMessage();
                }
                WrapperLogin wrapLogin = new WrapperLogin();
                wrapLogin.key = key;
                wrapLogin.response = response;
                return wrapLogin;
            }
            protected void onPostExecute(WrapperLogin result) {
                //do something with response
                Log.d("newwwss", result.response);
                onTaskCompleted(result.response, result.key, LoginTask);
            }
        }.execute();
    }

    private void onTaskCompleted(String response, String key, int task) {
        Log.d("responsejson", response.toString());
        AndyUtils.removeSimpleProgressDialog();  //will remove progress dialog
        switch (task) {
            case LoginTask:
                if (parseContent.isSuccess(response)) {

                    parseContent.saveInfo(response, key);
                    Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    this.finish();
                }else {
                    Toast.makeText(LoginActivity.this, parseContent.getErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
        }
    }
}
