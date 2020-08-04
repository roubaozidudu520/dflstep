package com.dfl.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class PedometerChartBean implements Parcelable {

    //记录全天的运动数据,用来生成曲线
    private  int [] arrayData;
    //当前记录的索引值
    private int index ;

    public PedometerChartBean()
    {
        index =0;
        arrayData =new int[1440];
    }

    protected PedometerChartBean(Parcel in) {
        arrayData = in.createIntArray();
        index = in.readInt();
    }

    public static final Creator<PedometerChartBean> CREATOR = new Creator<PedometerChartBean>() {
        @Override
        public PedometerChartBean createFromParcel(Parcel in) {
            return new PedometerChartBean(in);
        }

        @Override
        public PedometerChartBean[] newArray(int size) {
            return new PedometerChartBean[size];
        }
    };

    public void setDataArray(int[] pDataArray)
    {
        arrayData = pDataArray;
    }

    public int[] getDataArray()
    {
        return arrayData;
    }

    public void setIndex(int pIndex)
    {
        index = pIndex;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(arrayData);
        parcel.writeInt(index);
    }

    public  void reset() {
        index=0;
        for (int i=0;i<arrayData.length;i++) {
            arrayData[i]=0;
        }
    }
}
