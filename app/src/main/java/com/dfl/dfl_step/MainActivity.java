package com.dfl.dfl_step;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.dfl.frame.BaseActivity;

public class MainActivity extends BaseActivity {

    private static final int DELAY_MILLIS = 3000;
    private Handler handler;
    private Runnable jumpRunnable;

    @Override
    protected void onInitVariable() {
        handler=new Handler();
        jumpRunnable=new Runnable() {
            @Override
            public void run() {
                //跳转到Home
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,HomeActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        };
    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onRequestData() {
        handler.postDelayed(jumpRunnable, DELAY_MILLIS);
    }
}

