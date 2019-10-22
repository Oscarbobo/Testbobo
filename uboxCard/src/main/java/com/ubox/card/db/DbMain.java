package com.ubox.card.db;

import java.util.ArrayList;
import java.util.List;

import com.ubox.card.db.dao.QCQDao;
import com.ubox.card.db.dao.BrushCupBoardLogDao;
import com.ubox.card.db.dao.HZSMKDao;
import com.ubox.card.db.dao.IDao;
import com.ubox.card.db.dao.MZTTradeDao;
import com.ubox.card.db.dao.QuickPassTradeDao;
import com.ubox.card.db.dao.UnionpayTradeDao;
import com.ubox.card.db.dao.WHTTradeDao;
import com.ubox.card.db.dao.YNYLSFTradeDao;
import com.ubox.card.util.logger.Logger;

public class DbMain {

	private static final DbMain instance = new DbMain();

	private final List<IDao> daos;

	private DbMain() {
        daos = new ArrayList<IDao>();
		daos.add(BrushCupBoardLogDao.getInstance());
		daos.add(UnionpayTradeDao.getInstance());
		daos.add(WHTTradeDao.getInstance());
		daos.add(MZTTradeDao.getInstance());
		daos.add(HZSMKDao.getInstance());
		daos.add(YNYLSFTradeDao.getInstance());
		daos.add(QCQDao.getInstance());
		daos.add(QuickPassTradeDao.getInstance());
	}

	public static DbMain getInstance() {
		return instance;
	}

	public void init() {
	    for (IDao dao : daos) { 
	    	dao.init(); 
	    }
	    
	    Logger.info(" [ DB Initial OK ] <<<< ");
	}

}
