package com.ubox.card.deploy;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ubox.card.R;
import com.ubox.card.util.logger.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeployActivity extends Activity {
	
	private Reader reader;
	private Writer writer;
    private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		reader = new Reader(getApplicationContext());
		writer = new Writer();
		
		if(null == reader) {
			setContentView(R.layout.error_show);
		} else {
			showCardsList();
		}
	}
	
	/**
	 * 显示刷卡项目列表
	 */
	private void showCardsList() {
		String       cards = reader.readCards();
		List<String> names = new ArrayList<String>(); 
		
		final List<Card> list = (List<Card>) JSON.parseArray(cards, Card.class);
		for(Card card : list) names.add(card.getName());
		
		listView = new ListView(this);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String txt  = ((TextView)view).getText().toString();
				for(Card card : list) {
					if(txt.equals(card.getName())) {
						showDialog(card);
						break;
					}
				}
			}
		});
		
		setContentView(listView);
	}
	
	
	/**
	 * 显示项目配置信息
	 */
	@SuppressLint("InflateParams")
	private void showDialog(final Card card) {
		AlertDialog.Builder builder  = new AlertDialog.Builder(DeployActivity.this);
		View                view     = LayoutInflater.from(DeployActivity.this).inflate(R.layout.dialog_show, null);
		TextView            app_type = (TextView) view.findViewById(R.id.e_app_type);
		TextView            app_abr  = (TextView) view.findViewById(R.id.e_app_abr);
		TextView            app_name = (TextView) view.findViewById(R.id.e_app_name);
		
		
		app_type.setText(card.getType());
		app_abr.setText(card.getAbbreviation());;
		app_name.setText(card.getName());;
		
		builder.setTitle(R.string.dialog_title);
		builder.setView(view);
		builder.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CardJson cardJson = new CardJson();
				cardJson.setIsCardDevice(1);
				cardJson.setAppType(Integer.valueOf(card.getType()));
				cardJson.setCardName(card.getAbbreviation());
				
				writer.writeCardJson(cardJson);
			}
		});
		builder.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Logger.info("cancel do nothing");
			}
		});
		
		builder.show();
	}

}
