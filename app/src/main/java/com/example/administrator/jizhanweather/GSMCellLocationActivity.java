package com.example.administrator.jizhanweather;

/**
 * Created by Administrator on 2018/3/7.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import request.HttpThread;

/**
 * 功能描述：通过手机信号获取基站信息
 * <p>
 * # 通过TelephonyManager 获取lac:mcc:mnc:cell-id
 * <p>
 * # MCC，Mobile Country Code，移动国家代码（中国的为460）；
 * <p>
 * # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
 * <p>
 * # LAC，Location Area Code，位置区域码；
 * <p>
 * # CID，Cell Identity，基站编号；
 * <p>
 * # BSSS，Base station signal strength，基站信号强度。
 *
 * @author android_ls
 */

public class GSMCellLocationActivity extends AppCompatActivity {


    private static final String TAG = "GSMCellLocationActivity";
    private TextView tv;
    private int LAC = 0;
    private int Cell = 0;
    private int mcc =0;
    private int mnc =0;
    private String ADDRESS = "";
    private String addressOut="";
    private String urlWeather = "https://www.sojson.com/open/api/weather/json.shtml?city=";
//基站定位的url
    private String url = "";
    private String output = "";
    private HttpThread httpThread;


    @SuppressLint("HandlerLeak")
    private Handler handler0 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {

                String obj = (String) msg.obj;
                Log.i(TAG, "handleMessage:aasasassasaa "+obj.toString());
                try {
                    JSONObject jsonobject = new JSONObject(obj);

                    output += jsonobject.optString("city") + "的的天气情况" + "\n";
                    JSONObject jsonobject1 = new JSONObject(jsonobject.optString("data"));
                    JSONArray jsonArray = new JSONArray(jsonobject1.optString("forecast"));

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject2 = (JSONObject) jsonArray.get(i);
                        String date = jsonobject2.optString("date");
                        String sunrise = jsonobject2.optString("sunrise");
                        String high = jsonobject2.optString("high");
                        String low = jsonobject2.optString("low");
                        String sunset = jsonobject2.optString("sunset");
                        String aqi = jsonobject2.optString("aqi");
                        String fx = jsonobject2.optString("fx");
                        String fl = jsonobject2.optString("fl");
                        String type = jsonobject2.optString("type");
                        String notice = jsonobject2.optString("notice");
                        output += date + " " + high + " " + low + " " + fx + " " + fl + " " + type + " " + notice + "\n";
                        Log.i(TAG, "handleMessage:ssaaasasasasaaaaaaaaaaa " + output);
                    }
                    tv.setText(output);
                    output = "";
                    Toast.makeText(GSMCellLocationActivity.this, "请求成功 ！", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            super.handleMessage(msg);
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                String obj = (String) msg.obj;
                try {
                        JSONObject jsonobject = new JSONObject(obj);
                        ADDRESS = jsonobject.getString("address");
                        String strPattern  ="[\"省\"][\\w\\W]{2,4}[\"市\"]";
//                        String strPattern ="\\(?<=[省]\\).+\\(?=[市]\\)";
                        Pattern p = Pattern.compile(strPattern);
                        Matcher m = p.matcher(ADDRESS);
                        Log.i(TAG, "handleMessage: "+m.find());
                        addressOut=m.group(0).toString();
                        Log.i(TAG, "handleMessage: m.group()"+addressOut);
                        addressOut =addressOut.replace("市"," ");
                        addressOut =addressOut.replace("省"," ");
                        addressOut=addressOut.trim();
                        Log.i(TAG, "handleMessage: "+addressOut);
                        tv.setText(ADDRESS);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            super.handleMessage(msg);
        }
    };


    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        //天气获取
        findViewById(R.id.btn0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ADDRESS != null) {

//                    String out = ADDRESS.substring(0, 2).trim();
                try {

                    urlWeather += URLEncoder.encode(addressOut,"UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                    HttpThread httpThread0 = new HttpThread(urlWeather, handler0);
                    httpThread0.start();

                } else {
                    Toast.makeText(GSMCellLocationActivity.this, "还没有点击定位按钮，没有定位城市，无法获取天气信息！", Toast.LENGTH_SHORT).show();
                }


            }
        });
        // 获取基站信息

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {


            @Override

            public void onClick(View v) {


                TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


                // 返回值MCC + MNC

                String operator = mTelephonyManager.getNetworkOperator();

                 mcc = Integer.parseInt(operator.substring(0, 3));

                 mnc = Integer.parseInt(operator.substring(3));


                // 中国移动和中国联通获取LAC、CID的方式   大时代

                @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) mTelephonyManager.getCellLocation();

                LAC = location.getLac();

                Cell  = location.getCid();


                String out = " MCC = " + mcc + "\t MNC = " + mnc + "\t LAC = " + LAC + "\t CID = " + Cell;
                Log.i(TAG, " MCC = " + mcc + "\t MNC = " + mnc + "\t LAC = " + LAC + "\t CID = " + Cell);


                // 中国电信获取LAC、CID的方式

                /*CdmaCellLocation location1 = (CdmaCellLocation) mTelephonyManager.getCellLocation();

                lac = location1.getNetworkId();

                cellId = location1.getBaseStationId();

                cellId /= 16;*/


                // 获取邻区基站信息
//缺少权限
                @SuppressLint("MissingPermission") List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();

                StringBuffer sb = new StringBuffer("总数 : " + infos.size() + "\n");

                for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环

                    sb.append(" LAC : " + info1.getLac()); // 取出当前邻区的LAC

                    sb.append(" CID : " + info1.getCid()); // 取出当前邻区的CID

                    sb.append(" BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度

                }

                Log.i(TAG, " 获取邻区基站信息:" + sb.toString());
                url="http://api.cellocation.com:81/cell/?mcc="+mcc+"&mnc="+mnc+"&lac="+LAC+"&ci="+Cell+"&output=json\n";
                Log.i(TAG, " 获取邻区基站信息:  " + url);
                httpThread = new HttpThread(url, handler);
                httpThread.start();


            }

        });


    }


}
