package com.ubox.card.db.dao;

public interface IDao {
	
	public static final int DB_SUCCESS = 0;
	public static final int DB_FAIL = -1;
	
	/**
	 * 初始化操作
	 */
	public int init();
	
	/**
	 * 返回表名
	 * 
	 * @return
	 */
	public String tableName();
	
	/**
	 * 单数据插入，参数为Object，实际应该是插入的参数数据对象
	 * 
	 * 注意，若是插入数据的主键id在表中存在，那么做modify操作
	 * 
	 * 此方法参数定义有误，重写变成了重载
	 * 
	 * @param record
	 * @return 0成功
	 */
	public int insertOne(Object record);
	
	/**
	 * 根据表主键id删除单条数据
	 * 
	 * @param id
	 * @return 0成功
	 */
	public int deleteOne(String id);

}
