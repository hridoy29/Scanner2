package com.example.android.scanner2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button btn,btnSend,btnDelete;
    private String str="";
    private  String departmentCode="";
    DatabaseHelper myDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         myDb=new DatabaseHelper(this);
       // sendDataToDb();




        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btn=(Button) findViewById(R.id.button);
        btnSend = (Button) findViewById(R.id.btnSendToDb);


        sendDataToDb();
       // DeleteDataFromSqLightDb();
        final Activity activity=this;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator =new  IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
       /* IntentIntegrator integrator =new  IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(1);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();*/
    }

    public void sendDataToDb()
    {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor=myDb.getAllData();
                if(cursor.getCount()!=0)
                {
                   // int count=cursor.getCount();
                    while ((cursor.moveToNext())){
                       // Toast.makeText(MainActivity.this, cursor.getString(0), Toast.LENGTH_SHORT).show();
                        new LoadUserDetails().execute(new String(cursor.getString(0)));

                    }
                    if(cursor.getCount()!=0) {
                        Toast.makeText(MainActivity.this, "Successfull Send To DataBase", Toast.LENGTH_SHORT).show();
                        myDb.deleteAll();
                        Toast.makeText(MainActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public  void DeleteDataFromSqLightDb()
    {
        /*btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDb.deleteAll();
                Toast.makeText(MainActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
            }
        });*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancel the scanning", Toast.LENGTH_SHORT).show();//"Scanned Successfully"
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();

                str=result.getContents();
               boolean isInserted= myDb.insertData(str);

                if(isInserted == true)
                    Toast.makeText(MainActivity.this,"Data Inserted Successfully",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this,"Data not Inserted",Toast.LENGTH_SHORT).show();
               // new LoadUserDetails().execute(new String(str));
                ///// For Vibrator////
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_RING, 5000); //.STREAM_MUSIC
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE,500);//.TONE_CDMA_PIP
                } else {
                    //deprecated in API 26
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_RING, 5000);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE,500);
                    v.vibrate(1000);
                }
                /////For Vibrator////
                //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        final Activity activity=this;
        IntentIntegrator integrator =new  IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(1);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    public class LoadUserDetails extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(String i) {
            super.onPostExecute(i);
        }


        protected String doInBackground(String... params) {

            Integer result = 0;
            JSONObject jObject;
            JSONArray jsonArray = null;
            // departmentCode="08";
            // departmentCode=params[0].toString();
            String passed="";
            int i = 0;
            String str = "http://192.168.99.15:83/qrCodeService.svc/SetUserInfo";
            String response = "";
            String outletsEntities = "";
            URL url = null;
            try {
                url = new URL(str);
            } catch (MalformedURLException e) {
                response = e.getMessage();
            } catch (Exception ex) {
                response = ex.getMessage();
            }
            HttpURLConnection conn = null;


            JSONObject jsonObject = null;
            JSONStringer userJson = null;
            OutputStreamWriter outputStreamWriter = null;
            int responseCode;
            BufferedReader br;
            String line;


            try {
                assert url != null;
                conn = (HttpURLConnection) url.openConnection();
                try {

                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    jsonObject = new JSONObject();
                    userJson =new JSONStringer().object().key("userInfo").value(params[0].toString()+"("+ Calendar.getInstance().getTime()+")").endObject();//
                    outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                    outputStreamWriter.write(userJson.toString());
                    outputStreamWriter.close();
                    responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while (true) {

                            line = br.readLine();
                            if (line == null) {
                                break;
                            }
                            response = response + line;
                        }
                    } else {
                        response = responseCode + "";
                    }
                } catch (Exception e2) {
                    response = e2.getMessage();
                }
            } catch (Exception ex2) {
                response = ex2.getMessage();

            } finally {
                assert conn != null;
                conn.disconnect();
            }
            result = 0;
            jObject = null;
            if (!response.isEmpty()) {

            }
            return outletsEntities;
        }
    }

}
