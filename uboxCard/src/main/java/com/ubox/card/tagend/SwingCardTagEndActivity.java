package com.ubox.card.tagend;

import java.io.File;

import com.ubox.card.R;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.DeviceConfig;
import com.ubox.card.util.FileUtil;
import com.ubox.card.util.logger.Logger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * 刷卡终端号界面
 * @author weipeipei
 */
public class SwingCardTagEndActivity extends Activity{
	private EditText edit_tagendNumber;
	private Button btn_ensure;
	
	private final String configFileName = "config.ini";
	
	private String str_tagendNumber = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swingcard_tagend_no);
		
		initModel();
		initView();
	}
	
	private void initModel() {
		//取出的值类型为map<key, value>形式 TERMNO:12312312
		try {
			str_tagendNumber = DeviceConfig.getInstance().getValue("TERMNO") == null?"":DeviceConfig.getInstance().getValue("TERMNO");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d("SwingCardTagEndActivity", "str_tagendNumber = " + str_tagendNumber);
	}
	
	private void initView() {
		edit_tagendNumber = (EditText)findViewById(R.id.nber);
		edit_tagendNumber.setText(str_tagendNumber);
		
		btn_ensure = (Button)findViewById(R.id.btn_ensure);
		btn_ensure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				str_tagendNumber = edit_tagendNumber.getText().toString();
				String data = "TERMNO=" + str_tagendNumber;
				String fileName = CardConst.DEVICE_WORK_PATH + File.separator + configFileName;
				Logger.info("存储内容 = " + data);
				Logger.info("存储路径 = " + fileName);
				
				try {
					FileUtil.write(fileName, data, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				SwingCardTagEndActivity.this.finish();
			}
		});
	}
	
	// 获取点击事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    // 判定是否需要隐藏
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top
                    && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    // 隐藏软键盘
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
