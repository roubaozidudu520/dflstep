// IpedometerService.aidl
package com.dfl.service;

import com.dfl.beans.PedometerChartBean;
// Declare any non-default types here with import statements

interface IpedometerService {

    void startSetpsCount();
    void stopSetpsCount();
    void resetCount();
    int getStepsCount();

    double getCalorie();
    double getDistance();
    void saveData();

    void setSensitivity(double sensitivity);
    double getSensitivity();

    void setInterval(int interval);
    int getInterval();

    long getStartTimestmp();

    int getServiceRunningStatus();

    PedometerChartBean getChartData();

}
