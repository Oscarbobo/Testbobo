package com.ubox.card.bean.external;

import java.util.Map;

public class ExtRequest {
	private String  vmId;              // 售货机ID
	private String  msgType;           // 接口名称
	private Integer deviceId;          // 应用ID
	private Long    serialNo;          // 消息序列号
	private String 	createTime;         // 创建时间
	private Map<String,Object> data;   // 数据信息
	
	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
    
	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public Long getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Long serialNo) {
		this.serialNo = serialNo;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}
