package com.ubox.card.device;

import com.ubox.card.bean.external.ExtResponse;

public abstract class Device {

    public abstract void init();
    
//	public abstract String cardInfo(String json);
//	
//	public abstract String cost(String json);
    
    public abstract ExtResponse cardInfo(String json);
	
	public abstract ExtResponse cost(String json);
}
