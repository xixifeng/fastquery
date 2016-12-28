package org.fastquery.osgi.service;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月18日
 */
public interface FQuery {
	
	/**
	 * 集中注入服务
	 * @param bundle
	 * @param properties
	 * @return
	 */
	Registration registerService(Bundle bundle,Dictionary<String, ?> properties); 	// 好处: fquery-db不用写代理类
	
}
