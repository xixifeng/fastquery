package org.fastquery.osgi.act;

import org.fastquery.osgi.impl.FQueryImpl;
import org.fastquery.osgi.service.FQuery;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	
	private ServiceRegistration serviceRegistration;
	
	public void start(BundleContext bundleContext) throws Exception {
		// 对外提供FQuery服务
		serviceRegistration = bundleContext.registerService(FQuery.class, new FQueryImpl(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if(serviceRegistration!=null) {
			serviceRegistration.unregister();
		}
		bundleContext = null;
	}
	
}
