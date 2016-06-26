package com.joker.luckpandemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView mIvStart;
    private LuckyPan mLuckyPan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mIvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLuckyPan.isStart()){
                    mLuckyPan.luckyStart();
                }else {
                    if (!mLuckyPan.isShouldEnd()){
                        mLuckyPan.luckyEnd();
                    }
                }
            }
        });
    }

    private void initView() {
        mIvStart = (ImageView) findViewById(R.id.point);
        mLuckyPan = (LuckyPan) findViewById(R.id.lucky_pan);
    }
}
