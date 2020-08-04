package com.dfl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dfl.beans.PedometerBean;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    public static final String DB_NAME = "PedmometerDB";
    public static final String TABLE_NAME = "pedometer";
    public static final String[] COLUMNS = {
            "id",
            "stepCount",
            "calorie",
            "distance",
            "pace",
            "speed",
            "startTime",
            "lastStepTime",
            "createTime"
    };

    public DBHelper(Context context, String name){
        this(context,name,VERSION);
    }
    public DBHelper(Context context, String name,int version){
        this(context,name,null,VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+
                "(id integer PRIMARY KEY AUTOINCREMENT DEFAULT NULL,"+
                "stepCount integer,"+
                "calorie Double,"+
                "distance Double DEFAULT null,"+
                "pace integer,"+
                "speed double,"+
                "startTime Timestamp DEFAULT NULL,"+
                "lastStepTime Timestamp DEFAULT NULL,"+
                "createTime Timestamp DEFAULT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * 将数据写入到数据库
     * @param data
     */
    public void writeToDatabase(PedometerBean data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stepCount", data.getStepCount());
        values.put("calorie", data.getCalorie());
        values.put("distance", data.getDistance());
        values.put("pace", data.getPace());
        values.put("speed", data.getSpeed());
        values.put("startTime", data.getStartTime());
        values.put("lastStepTime", data.getLastStepTime());
        values.put("createTime", data.getCreateTime());
        db.insert(DBHelper.TABLE_NAME, null, values);
        db.close();
    }

    /**
     * 根据天为单位获取计步的基本数据
     * @param createTime
     * @return
     */
    public PedometerBean getByCreateTime(long createTime){
        Cursor cursor = null;
        PedometerBean bean=new PedometerBean();
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("select * from " + DBHelper.TABLE_NAME +
                " where createTime=" + String.valueOf(createTime), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[0]));
                int stepCount = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[1]));
                double calorie = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[2]));
                double distance = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[3]));
                int pace = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[4]));
                double speed = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[5]));
                long startTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[6]));
                long lastStepTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[7]));
                long cTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[8]));
                bean.setId(id);
                bean.setStepCount(stepCount);
                bean.setCalorie(calorie);
                bean.setDistance(distance);
                bean.setPace(pace);
                bean.setSpeed(speed);
                bean.setStartTime(startTime);
                bean.setLastStepTime(lastStepTime);
                bean.setCreateTime(cTime);
            }
        }
        cursor.close();
        db.close();
        return bean;
    }

    /**
     * 获取全部数据，分页
     * @param offVal
     * @return
     */
    public ArrayList<PedometerBean> getFromDatabase(int offVal){
        int pageSize = 20;
        Cursor cursor = null;
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null,
                "createTime desc limit " + String.valueOf(pageSize) + " offset " + String.valueOf(offVal),
                null);
        ArrayList<PedometerBean> daraList=new ArrayList<PedometerBean>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PedometerBean bean = new PedometerBean();
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[0]));
                int stepCount = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[1]));
                double calorie = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[2]));
                double distance = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[3]));
                int pace = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMNS[4]));
                double speed = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMNS[5]));
                long startTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[6]));
                long lastStepTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[7]));
                long createTime = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMNS[8]));
                bean.setId(id);
                bean.setStepCount(stepCount);
                bean.setCalorie(calorie);
                bean.setDistance(distance);
                bean.setPace(pace);
                bean.setSpeed(speed);
                bean.setStartTime(startTime);
                bean.setLastStepTime(lastStepTime);
                bean.setCreateTime(createTime);
                daraList.add(bean);
            }
        }
        cursor.close();
        db.close();
        return daraList;
    }

    public void updateToDatabase(ContentValues values, long createTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_NAME, values, "createTime=?", new String[]{String.valueOf(createTime)});
        db.close();
    }
}
