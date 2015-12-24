package com.coolweather.app.service;

import com.coolweather.app.R;
import com.coolweather.app.activity.WeatherActivity;
import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.HttpUtil.HttpCallbackListener;
import com.coolweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoUpdateService extends Service{
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent,int flags,int startId){
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();
			}
		}).start();
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour = 8*60*60*1000;
		long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

			private void updateWeather() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String weatherCode = prefs.getString("weather_code", "");
				String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
				HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						Utility.handleWeatherResponse(AutoUpdateService.this, response);		
					}
					@Override
					public void onError(Exception e) {
						e.printStackTrace();
					}
				});
			}
		@Override
		public void onCreate(){
			super.onCreate();
			Notification notification = new Notification(R.drawable.logo,"虾米天气",System.currentTimeMillis());
			Intent notificationIntent = new Intent(this,WeatherActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(this, "虾米天气", "点击查看", pendingIntent);
			startForeground(1, notification);
			Log.d("AutoUpdateService", "onCreate executed");
		}
			
	}
