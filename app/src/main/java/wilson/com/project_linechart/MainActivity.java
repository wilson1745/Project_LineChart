package wilson.com.project_linechart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.hardware.Sensor.TYPE_LIGHT;

public class MainActivity extends AppCompatActivity {

   private SensorManager sensor_manager;
   private MySensorEventListener listener;
   private Sensor sensor;
   private static final int msgKey1 = 1;
   long StartTime = System.currentTimeMillis(); // 取出目前時間
   private TextView date_v, time_v, sensor_v, lux_v;
   private Button btn_play, btn_stop, btn_track_off, btn_restart, btn_chart;
   private float lux;
   private String sensor_name;
   private Storage storage;
   private ArrayList<Storage> storageList;
   private int i = 0;
   private TimeThread timeThread;
   private boolean run = true;
   private boolean isRunning = true;
   private long second = 0;
   float lux_ran;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      timeThread = new TimeThread();
      storageList = new ArrayList<>();
      init();
      setSensor();

      btn_play.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            run = true;
            timeThread.interrupt();
         }
      });

      btn_stop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            run = false;
         }
      });

      btn_track_off.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            isRunning = false;
         }
      });

      btn_restart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            run = true;
            isRunning = true;
            second = 0;
            i = 0;
            storageList = new ArrayList<>();
            timeThread = new TimeThread();
            timeThread.start();
         }
      });

      btn_chart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            isRunning = false;
            Intent intent = new Intent(MainActivity.this, LineChartActivity.class);
            intent.setAction("send storageList");
            intent.putExtra("storageList", storageList);
            startActivity(intent);
         }
      });

      timeThread.start();
   }

   private void setSensor() {
      sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensor_manager.getDefaultSensor(TYPE_LIGHT); // Light傳感器
      listener = new MySensorEventListener();
      sensor_manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
   }

   private void init() {
      date_v = findViewById(R.id.date_view);
      time_v = findViewById(R.id.time_view);
      sensor_v = findViewById(R.id.sensor_view);
      lux_v = findViewById(R.id.lux_view);
      btn_play = findViewById(R.id.btn_play);
      btn_stop = findViewById(R.id.btn_stop);
      btn_track_off = findViewById(R.id.btn_track_off);
      btn_restart = findViewById(R.id.btn_restart);
      btn_chart = findViewById(R.id.btn_chart);
   }

   public class TimeThread extends Thread {
      @Override
      public void run () {
         while(isRunning) {
            try {
               if(!run) {
                  timeThread.sleep(Long.MAX_VALUE);
               }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

            try {
               Thread.sleep(1000);
               Message msg = new Message();
               msg.what = msgKey1;
               timeHandler.sendMessage(msg);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
         Log.e("TAG", "Thread has been terminated!");
      }
   }

   @SuppressLint("HandlerLeak")
   private Handler timeHandler = new Handler() {
      @Override
      public void handleMessage (Message msg) {
         super.handleMessage(msg);
         switch (msg.what) {
            case msgKey1:
               long sysTime = System.currentTimeMillis();
               long ProcessTime = sysTime - StartTime; // 計算處理時間
               long FinalTime = ProcessTime / 1000;   // 毫秒換算成秒

               second++;

               CharSequence sysTimeStr = DateFormat.format("yyyy-MM-dd hh:mm:ss", sysTime);
               String date = (String) sysTimeStr;
               date_v.setText(sysTimeStr);
               //time_v.setText(String.valueOf(FinalTime));
               time_v.setText(String.valueOf(second));
               sensor_v.setText(sensor_name);
               //lux_v.setText(String.valueOf(lux));
               lux_v.setText(String.valueOf(lux_ran));

               //storage = new Storage(date, lux);
               storage = new Storage(date, lux_ran);
               storageList.add(storage);

               if(storageList != null) {
                  Log.e("TAG", "date: " + storageList.get(i).getDate());
                  Log.e("TAG", "lux_ran: " + storageList.get(i).getLux());
                  //Log.e("TAG", "lux_v: " + storageList.get(i).getLux());
                  Log.e("TAG", "list.size(): " + storageList.size());
                  i++;
               }

               break;
            default:
               break;
         }
      }
   };

   // 感應器事件監聽器
   private class MySensorEventListener implements SensorEventListener {
      // 監控感應器改變
      @Override
      public void onSensorChanged(final SensorEvent event) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // Android 的 Light Sensor 照度偵測內容只有 values[0] 有意義!
               sensor_name = event.sensor.getName();
               lux = event.values[0];
               lux_ran = new Random().nextFloat()*100;
            }
         });
      }

      // 對感應器精度的改變做出回應
      @Override
      public void onAccuracyChanged(Sensor sensor, int accuracy) {

      }
   }
}
