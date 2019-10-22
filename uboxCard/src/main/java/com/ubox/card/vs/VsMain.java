package com.ubox.card.vs;

import com.ubox.card.core.WorkPool;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.handler.BrushCupBoardLogHandler;
import com.ubox.card.vs.handler.HZSMKTradeHandler;
import com.ubox.card.vs.handler.MZTTradeHandler;
import com.ubox.card.vs.handler.QCQTradeHandler;
import com.ubox.card.vs.handler.UnionpayHandler;
import com.ubox.card.vs.handler.VCardInfoHandler;
import com.ubox.card.vs.handler.WHTTradeHandler;
import com.ubox.card.vs.handler.YNYLSFTradeHandler;
//import com.ubox.card.vs.runner.AllDataSyn;
import com.ubox.card.vs.runner.PollDataSyn;

public class VsMain {

	private static final VsMain instance = new VsMain();

	private final PollDataSyn pollDataSyn;
	
	private VsMain() {
		pollDataSyn = PollDataSyn.getInstance();// 心跳同步初始化 
	}

	public void init() {
		initPollHandler(); // 同步处理器初始化
		WorkPool.executeTask(pollDataSyn);
		
		Logger.info(" [ VCardServer Initial OK ] <<<<");
	}

	private void initPollHandler() {
		pollDataSyn.addHandler(new BrushCupBoardLogHandler()); // 刷卡记录处理
		pollDataSyn.addHandler(new UnionpayHandler());
		pollDataSyn.addHandler(new WHTTradeHandler());
		pollDataSyn.addHandler(new MZTTradeHandler());
		pollDataSyn.addHandler(new VCardInfoHandler());
		pollDataSyn.addHandler(new HZSMKTradeHandler());
		pollDataSyn.addHandler(new YNYLSFTradeHandler());
		pollDataSyn.addHandler(new QCQTradeHandler());
		
	}
	
	public static VsMain getInstance() {
		return instance;
	}

}
