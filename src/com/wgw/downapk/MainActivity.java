package com.wgw.downapk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView textApk;
	private Button downApk;
	private static final String URL = "http://218.206.179.179/ctp/ipauto/getCtpHTTPUrl.do?type=shichang&size=15M&operator=download";
	int fileSize;
	int downLoadFileSize;
	String fileEx,fileNa,filename;
	ProgressBar pb;


	private Handler handler = new Handler()
	{
	@Override
	public void handleMessage(Message msg)
	{//定义一个Handler，用于处理下载线程与UI间通讯
	if (!Thread.currentThread().isInterrupted())
	{
	switch (msg.what)
	{
	case 0:
	pb.setMax(fileSize);
	case 1:
	pb.setProgress(downLoadFileSize);
	int result = downLoadFileSize * 100 / fileSize;
	Log.d("wgw_downSize", downLoadFileSize+"");
	textApk.setText(result + "%");
	break;
	case 2:
	Toast.makeText(MainActivity.this, "文件下载完成", 1).show();
	break;
	case -1:
	String error = msg.getData().getString("error");
	Toast.makeText(MainActivity.this, error, 1).show();
	break;
	}
	}
	super.handleMessage(msg);
	}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textApk = (TextView) findViewById(R.id.textApk);
		downApk = (Button) findViewById(R.id.downApk);
		pb = (ProgressBar) findViewById(R.id.down_pb);

		downApk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.d("wgw_json", Funs.executeHttpGet(URL)+"");
						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(Funs.executeHttpGet(URL));
							final String downUrl = jsonObject.getString("url");
//							textApk.setText(downUrl);
							Log.d("wgw_downURL", downUrl);
							new Thread(){
						         public void run(){
						          try {
						     down_file(downUrl,"/sdcard/");
						     //下载文件，参数：第一个URL，第二个存放路径
						    } catch (ClientProtocolException e) {
						     // TODO Auto-generated catch block
						     e.printStackTrace();
						    } catch (IOException e) {
						     // TODO Auto-generated catch block
						     e.printStackTrace();
						    }
						         }
						        }.start();

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						super.run();
					}
					
				}.start();
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void down_file(String url,String path) throws IOException{
	     //下载函数    
	     filename=url.substring(url.lastIndexOf("/") + 1);
	     //获取文件名
	     URL myURL = new URL(url);
	     URLConnection conn = myURL.openConnection();
	     conn.connect();
	     InputStream is = conn.getInputStream();
	     this.fileSize = conn.getContentLength();//根据响应获取文件大小
	     if (this.fileSize <= 0) throw new RuntimeException("无法获知文件大小 ");
	     if (is == null) throw new RuntimeException("stream is null");
	     FileOutputStream fos = new FileOutputStream(path+filename);
	     //把数据存入路径+文件名
	     byte buf[] = new byte[1024];
	     downLoadFileSize = 0;
	     sendMsg(0);
	     do
	       {
	      //循环读取
	         int numread = is.read(buf);
	         if (numread == -1)
	         {
	           break;
	         }
	         fos.write(buf, 0, numread);
	         downLoadFileSize += numread;
	         sendMsg(1);//更新进度条
	       } while (true);
	     sendMsg(2);//通知下载完成
	     try
	       {
	         is.close();
	       } catch (Exception ex)
	       {
	         Log.e("tag", "error: " + ex.getMessage(), ex);
	       }
	    }
	 private void sendMsg(int flag)
	 {
	     Message msg = new Message();
	     msg.what = flag;
	     handler.sendMessage(msg);
	 }  

}
