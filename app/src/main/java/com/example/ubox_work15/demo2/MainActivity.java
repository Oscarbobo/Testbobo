package com.example.ubox_work15.demo2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button recharge;
    private TextView payresult;
    private  static final int PAY_REQUEST_CODE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initListener();
    }

    private void initListener() {
        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,PayActivity.class);
                startActivityForResult(intent,PAY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String resultContent=null;
        if (requestCode==PAY_REQUEST_CODE) {
            if (resultCode==2) {
                resultContent=data.getStringExtra("resultContent");
            }else if (resultCode==3){
                resultContent=data.getStringExtra("resultContent");
            }
            payresult.setText(resultContent);
        }
    }

    //初始化控件
    private void initView() {
        recharge = (Button) this.findViewById(R.id.recharge_btn);
        payresult = (TextView) this.findViewById(R.id.pay_result);
    }
}
