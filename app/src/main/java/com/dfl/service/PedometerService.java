package com.dfl.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.dfl.Utiles.ACache;
import com.dfl.Utiles.Settings;
import com.dfl.Utiles.Utiles;
import com.dfl.beans.PedometerBean;
import com.dfl.beans.PedometerChartBean;
import com.dfl.db.DBHelper;
import com.dfl.frame.FrameApplication;

public class PedometerService extends Service {

    private SensorManager sensorManager;
    private PedometerBean pedmoeterBean;//记录步数等信息
    private PedmodeterListener pedmodeterListener;//监听运动状态
    public static final int STATUS_NOT_RUNNING = 0;//非运行中
    public static final int STATUS_RUNNING = 1;//运行中
    private int runStatus = STATUS_NOT_RUNNING;//当前运行状态
    private static final long SAVE_CHART_TIME = 60000L;

    private Settings settings;
    private PedometerChartBean pedometerChartBean; //记录显示数据

    private static Handler handler=new Handler();

    private Runnable timeRunnable=new Runnable() {
        @Override
        public void run() {
            if(runStatus==STATUS_RUNNING){
                if(handler!=null&&pedometerChartBean!=null){
                    handler.removeCallbacks(timeRunnable);
                    updateChartData();  //更新数据
                    handler.postDelayed(timeRunnable,SAVE_CHART_TIME);  //隔一分钟刷新
                }
            }
        }
    };

    public double getCalorieBySteps(int stepCount){
        //步长
        float steoLen=settings.getSetpLength();
        //体重
        float bodyWeight=settings.getBodyWeight();
        double METRIC_WALKING_FACTOR=0.708; //走路
        double METRIC_RUNNING_FACTOR=1.02784823;    //跑步
        //跑步热量 (kcal)=体重（kg）x 距离（公里）x 1.02784823
        //走步热量 (kcal)=体重（kg）x 距离（公里）x 1.0.708
        double calories=(bodyWeight*METRIC_WALKING_FACTOR)*steoLen*stepCount/100000.0;
        return calories;
    }

    public double getDistanceVal(int stepCount){
        //步长
        float steoLen=settings.getSetpLength();
        double distance=(stepCount*(long)(steoLen))/100000.0f;
        return distance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        pedmoeterBean = new PedometerBean();
        pedmodeterListener = new PedmodeterListener(pedmoeterBean);
        pedometerChartBean=new PedometerChartBean();
        settings = new Settings(this);
    }

    //更新了计步器的图表数据
    private void updateChartData() {
        if(pedometerChartBean.getIndex()<1440-1){
            pedometerChartBean.setIndex(pedometerChartBean.getIndex()+1);
            pedometerChartBean.getDataArray()[pedometerChartBean.getIndex()]=
                    pedmoeterBean.getStepCount();
        }
    }

    private void saveChartData() {
        String jsonStr=Utiles.objToJson(pedometerChartBean);
        ACache.get(FrameApplication.getInstance()).put("JsonChartData",jsonStr);
    }

    private IpedometerService.Stub ipedometerService=new IpedometerService.Stub() {
        @Override
        public void startSetpsCount() throws RemoteException {
            if(sensorManager!=null&&pedmodeterListener!=null){
                Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(pedmodeterListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
                pedmoeterBean.setStartTime(System.currentTimeMillis());
                pedmoeterBean.setCreateTime(Utiles.getTimestempByDay());  //记录的是哪天的数据
                runStatus=STATUS_RUNNING;
                handler.postDelayed(timeRunnable,SAVE_CHART_TIME);  //开始触发数据刷新
            }
        }

        @Override
        public void stopSetpsCount() throws RemoteException {
            if(sensorManager!=null&&pedmodeterListener!=null){
                Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.unregisterListener(pedmodeterListener,sensor);
                runStatus=STATUS_NOT_RUNNING;
                handler.removeCallbacks(timeRunnable);
            }

        }

        @Override
        public void resetCount() throws RemoteException {
            if(pedmoeterBean!=null){
                pedmoeterBean.reset();
                saveData();
            }
            if(pedometerChartBean!=null){
                pedometerChartBean.reset();
                saveChartData();
            }
            if(pedmodeterListener!=null){
                pedmodeterListener.setCurrentSteps(0);
            }
        }

        @Override
        public int getStepsCount() throws RemoteException {
            if(pedmoeterBean!=null){
                return pedmoeterBean.getStepCount();
            }
            return 0;
        }

        @Override
        public double getCalorie() throws RemoteException {
            if(pedmoeterBean!=null){
                return getCalorieBySteps(pedmoeterBean.getStepCount());
            }
            return 0;
        }

        @Override
        public double getDistance() throws RemoteException {
            return getDistanceVal();
        }

        @Override
        public void saveData() throws RemoteException {
            if(pedmoeterBean!=null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBHelper dbHelper=new DBHelper(PedometerService.this,DBHelper.DB_NAME);
                        //设置距离
                        pedmoeterBean.setDistance(getDistanceVal());
                        //设置热量消耗
                        pedmoeterBean.setCalorie(getCalorieBySteps(pedmoeterBean.getStepCount()));
                        long time=(pedmoeterBean.getLastStepTime()-pedmoeterBean.getStartTime())/1000;
                        if (time==0) {
                            pedmoeterBean.setPace(0);   //设置多少步/分钟
                            pedmoeterBean.setSpeed(0);
                        }else {
                            int pace=Math.round(60*pedmoeterBean.getStepCount()/time);
                            pedmoeterBean.setPace(pace);
                            long speed=Math.round((pedmoeterBean.getStepCount()/1000)/(time/60*60));
                            pedmoeterBean.setSpeed(speed);
                        }
                        dbHelper.writeToDatabase(pedmoeterBean);
                    }
                }).start();
            }
        }

        @Override
        public void setSensitivity(double sensitivity) throws RemoteException {
//            if(settings!=null){
//                settings.setSensitivity((float) sensitivity);
//            }
            if(pedmodeterListener!=null){
                pedmodeterListener.setSensitivity((float) sensitivity);
            }
        }

        @Override
        public double getSensitivity() throws RemoteException {
            if(settings!=null){
                return settings.getSensitivity();
            }
            return 0;
        }

        @Override
        public void setInterval(int interval) throws RemoteException {
            if(settings!=null){
                settings.setInterval(interval);
            }
            if(pedmodeterListener!=null){
                pedmodeterListener.setLimit(interval);
            }
        }

        @Override
        public int getInterval() throws RemoteException {
            if(settings!=null){
                return settings.getInterval();
            }
            return 0;
        }

        @Override
        public long getStartTimestmp() throws RemoteException {
            if(pedmoeterBean!=null){
                return pedmoeterBean.getStartTime();
            }
            return 0L;
        }

        @Override
        public int getServiceRunningStatus() throws RemoteException {
            return runStatus;
        }

        @Override
        public PedometerChartBean getChartData() throws RemoteException {
            return pedometerChartBean;
        }
    };

    private double getDistanceVal() {
        if(pedmoeterBean!=null){
            return getDistanceVal(pedmoeterBean.getStepCount());
        }
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ipedometerService;
    }
}
