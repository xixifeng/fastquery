package org.fastquery.osgi.service;

import java.util.List;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月19日
 */
public class Registration {

	private List<ServiceRegistration> serviceRegistrations;	
	public Registration(List<ServiceRegistration> serviceRegistrations){
		this.serviceRegistrations = serviceRegistrations;
	}
	
	/**
	 * 注销
	 */
	public void unregister(){
		// 注销注册的服务
		for (ServiceRegistration serviceRegistration : serviceRegistrations) {
			serviceRegistration.unregister();
		}
	}
}
