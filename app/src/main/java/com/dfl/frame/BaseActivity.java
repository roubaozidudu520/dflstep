package com.dfl.frame;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    /**
     * 是否显示应用程序标题栏
     */
    protected boolean isHideAppTitle    = true;
    /**
     * 是否显示系统标题栏
     */
    protected boolean isHideSystemTitle = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.onInitVariable();
        if(this.isHideAppTitle){    //App标题在创建之前调用
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        super.onCreate(savedInstanceState);
        if(this.isHideSystemTitle){
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //构造view，绑定事件
        this.onInitView(savedInstanceState);
        //请求数据
        this.onRequestData();
        FrameApplication.addToActivityList(this);
    }

    @Override
    protected void onDestroy() {
        FrameApplication.removeActivityList(this);
        super.onDestroy();
    }

    /**
     * 1) 初始化变量 最先被调用 用于初始化一些变量，创建一些对象
     */
    protected abstract void onInitVariable();

    /**
     * 2) 初始化UI 布局载入操作
     *
     * @param savedInstanceState
     */
    protected abstract void onInitView(final Bundle savedInstanceState);
    /**
     * 3） 请求数据
     *
     */
    protected abstract void onRequestData();
    /**
     * 4) 数据加载 onResume时候调用
     */
    //protected abstract void onLoadData();
    /**
     * 5) 数据卸载 onPause时候调用
     */
    //protected abstract void onUnLoadData();
}
