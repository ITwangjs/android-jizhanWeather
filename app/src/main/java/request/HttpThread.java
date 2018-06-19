package request;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class HttpThread extends Thread {

    private Handler handler;
    private String urlString;


//重写子线程的函数把city也传过来
    public HttpThread(String urlString, Handler handler) {
        this.handler = handler;
        this.urlString = urlString;

    }

    @Override
    public void run() {
        /*下面是重点关于中文转码的问题
        * 中文转码---URLEncoder.encode(city, "UTF-8");
        * */

        try {

//            建立连接
            URL url = new URL(urlString);
//打开连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            希望得到数据----GET
            conn.setRequestMethod("GET");
//            得到输入流---读取输入流----缓冲方式文本读取字符
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str="";
            StringBuffer jsondata = new StringBuffer();
//            把读取的数据加到jsondata中
            while ((str = reader.readLine()) != null) {
                jsondata.append(str);
            }
//建立一个消息，handler发送
            Message msg = new Message();
            msg.what = 0;
//            msg.obj = jsondata;
//            得加toString()不然程序会崩掉
            msg.obj = jsondata.toString();
            handler.sendMessage(msg);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}