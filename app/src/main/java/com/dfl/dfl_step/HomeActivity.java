package com.dfl.dfl_step;



import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dfl.Utiles.Utiles;
import com.dfl.beans.PedometerChartBean;
import com.dfl.frame.BaseActivity;
import com.dfl.frame.LogWriter;
import com.dfl.service.IpedometerService;
import com.dfl.service.PedometerService;
import com.dfl.widgets.CircleProgressBar;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity {

    private CircleProgressBar progressBar;
    private TextView textCalorie;
    private TextView time;
    private TextView distance;
    private TextView stepCount;
    private Button reset;
    private Button btnStart;
    protected BarChart dataChart;
    private IpedometerService remoteService;
    private ImageView setting;
    private int status=-1;

    private static final int STATUS_NOT_RUNNING = 0;//not running
    private static final int STATUS_RUNNING = 1;//运行中
    private boolean isRunning=false;
    private boolean isChartUpdate=false;
    private static final int MESSAGE_UPDATE_STEP_COUNT = 1000;
    private static final int MESSAGE_UPDATE_CHART_DATA= 2000;
    private static final int GET_DATA_TIME = 200;
    private static final long GET_CHART_DATA_TIME=60000L;
    private PedometerChartBean chartBean;

    private boolean bindService=false;

    @Override
    protected void onInitVariable() {

    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {
        setContentView(R.layout.act_home);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setProgress(5000);
        progressBar.setMaxProgress(10000);
        setting=findViewById(R.id.imageView);
        textCalorie=findViewById(R.id.textCalorie);
        time=findViewById(R.id.time);
        distance=findViewById(R.id.distance);
        stepCount=findViewById(R.id.stepCount);

        reset=findViewById(R.id.reset);
        btnStart=findViewById(R.id.btnStart);
        dataChart=findViewById(R.id.dataChart);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(HomeActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("确认操作");
                builder.setMessage("您的记录将要被重置，确定吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(remoteService!=null){
                            try {
                                remoteService.stopSetpsCount();
                                remoteService.resetCount();
                                chartBean=remoteService.getChartData();
                                updateChart(chartBean);
                                status=remoteService.getServiceRunningStatus();
                                if(status==PedometerService.STATUS_RUNNING){
                                    btnStart.setText("停止");
                                }else if(status==PedometerService.STATUS_NOT_RUNNING){
                                    btnStart.setText("启动");
                                }
                            } catch (RemoteException e) {
                                LogWriter.d(e.toString());
                            }
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消",null);
                AlertDialog resetDlg=builder.create();
                resetDlg.show();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    status=remoteService.getServiceRunningStatus();
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                }
                if(status==STATUS_RUNNING&&remoteService!=null){
                    try {
                        remoteService.stopSetpsCount();
                        btnStart.setText("启动");
                        isRunning=false;
                        isChartUpdate=false;
                    } catch (RemoteException e) {
                        LogWriter.d(e.toString());
                    }
                }else if(status==STATUS_NOT_RUNNING&&remoteService!=null){
                    try {
                        remoteService.startSetpsCount();
                        startStepcount();
                    } catch (RemoteException e) {
                        LogWriter.d(e.toString());
                    }
                }
            }
        });
    }

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            remoteService=IpedometerService.Stub.asInterface(service);
            try {
                status=remoteService.getServiceRunningStatus();
                if(status==STATUS_RUNNING){
                    startStepcount();
                }else {
                    btnStart.setText("启动");
                }
            } catch (RemoteException e) {
                LogWriter.d(e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            remoteService=null;
        }
    };

    private void startStepcount() throws RemoteException {
        btnStart.setText("停止");
        isChartUpdate=true;
        isRunning=true;
        chartBean=remoteService.getChartData();
        updateChart(chartBean);
        //启动两个线程，定时获取数据，刷新UI
        new Thread(new StepRunnable()).start();
        new Thread(new ChartRunnable()).start();
    }

    @Override
    protected void onRequestData() {
        //检查服务是否运行
        //服务没有运行，启动服务，如果服务已经运行，直接绑定服务
        Intent serviceIntent=new Intent(this,PedometerService.class);
        if(!Utiles.isServiceRunning(this, PedometerService.class.getName())){
            //服务没有运行，直接启动
            startService(serviceIntent);
        }else {
            //服务运行
            serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //绑定服务操作
        bindService=bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);
        //初始化一些对应状态，按钮文字等
        if(bindService&&remoteService!=null){
            try {
                status=remoteService.getServiceRunningStatus();
                if(status==PedometerService.STATUS_NOT_RUNNING){
                    btnStart.setText("启动");
                }else if(status==PedometerService.STATUS_RUNNING){
                    btnStart.setText("停止");
                    isRunning=true;
                    isChartUpdate=true;
                    //启动两个线程，定时获取数据，刷新UI
                    new Thread(new StepRunnable()).start();
                    new Thread(new ChartRunnable()).start();
                }
            } catch (RemoteException e) {
                LogWriter.d(e.toString());
            }
        }else {
            btnStart.setText("启动");
        }

    }

    private class StepRunnable implements Runnable{

        @Override
        public void run() {
            while (isRunning){
                try {
                    status=remoteService.getServiceRunningStatus();
                    if(status==STATUS_RUNNING){
                        handler.removeMessages(MESSAGE_UPDATE_STEP_COUNT);
                        //发送消息，让Handler去更新数据
                        handler.sendEmptyMessage(MESSAGE_UPDATE_STEP_COUNT);
                        Thread.sleep(GET_DATA_TIME);
                    }
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                } catch (InterruptedException e) {
                    LogWriter.d(e.toString());
                }
            }
        }
    }

    private class ChartRunnable implements Runnable{

        @Override
        public void run() {
            while (isChartUpdate){
                try {
                    chartBean=remoteService.getChartData();
                    handler.removeMessages(MESSAGE_UPDATE_CHART_DATA);
                    handler.sendEmptyMessage(MESSAGE_UPDATE_CHART_DATA);
                    Thread.sleep(GET_CHART_DATA_TIME);
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                } catch (InterruptedException e) {
                    LogWriter.d(e.toString());
                }
            }
        }
    }

    Handler handler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_UPDATE_STEP_COUNT:
                {
                    //更新计步器
                    updateStepCount();
                }
                break;
                case MESSAGE_UPDATE_CHART_DATA:
                {
                    if(chartBean!=null){
                        updateChart(chartBean);
                    }
                }
                break;
                default:
                    LogWriter.d("Default="+msg.what);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void updateChart(PedometerChartBean bean) {
        ArrayList<String> xVals=new ArrayList<String>();
        ArrayList<BarEntry> yVals=new ArrayList<BarEntry>();
        if (bean!=null) {
            for (int i=0;i<bean.getIndex();i++){
                xVals.add(String.valueOf(i)+"分s");
                int valY=bean.getDataArray()[i];
                yVals.add(new BarEntry(valY,i));
            }
            time.setText(String.valueOf(bean.getIndex())+"分");
            BarDataSet set1=new BarDataSet(yVals,"所走的步数");
            set1.setBarSpacePercent(2f);
            ArrayList<BarDataSet> dataSets=new ArrayList<BarDataSet>();
            dataSets.add(set1);
            BarData data=new BarData(xVals,dataSets);
            data.setValueTextSize(10f);
            dataChart.setData(data);
            dataChart.invalidate();
        }
    }

    private void updateStepCount() {
        if (remoteService != null) {
            //服务正在运行
            int stepCountVal = 0;
            double calorieVal = 0;
            double distanceVal = 0;
            try {
                stepCountVal = remoteService.getStepsCount();
                calorieVal = remoteService.getCalorie();
                distanceVal = remoteService.getDistance();
                LogWriter.d("distance =" + distanceVal);
            } catch (RemoteException e) {
                LogWriter.d(e.toString());
            }
            //更新数据到UI
            stepCount.setText(String.valueOf(stepCountVal) + "步");
            textCalorie.setText(Utiles.getFormatVal(calorieVal) + "卡");
            distance.setText(Utiles.getFormatVal(distanceVal));
            progressBar.setProgress(stepCountVal);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bindService){
            bindService=false;
            isRunning=false;
            isChartUpdate=false;
            unbindService(serviceConnection);
        }
    }
}