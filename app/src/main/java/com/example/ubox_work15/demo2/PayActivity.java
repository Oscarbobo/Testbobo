package com.example.ubox_work15.demo2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PayActivity extends AppCompatActivity {

    private EditText inputBox;
    private Button payBnt;
    private Button cancelBnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        initView();
        initListener();
    }

    private void initListener() {
        payBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePay();
            }

            private void handlePay() {
                String payNumber=inputBox.getText().toString().trim();
                if (TextUtils.isEmpty(payNumber)) {
                    Toast.makeText(PayActivity.this,"请输入金额",Toast.LENGTH_SHORT).show();
                }

                Intent intent=new Intent();
                intent.putExtra("resultContent","充值成功");
                setResult(2,intent);
                finish();
            }
        });

        cancelBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCancel();
            }
        });

    }

    private void handleCancel() {
        Intent intent=new Intent();
        intent.putExtra("resultContent","充值失败");
        setResult(3,intent);
        finish();
    }

    private void initView() {
        inputBox = (EditText) this.findViewById(R.id.pay_input_box);
        payBnt = (Button) this.findViewById(R.id.start_pay_bnt);
        cancelBnt = (Button) this.findViewById(R.id.cancel_pay_bnt);
    }
}
