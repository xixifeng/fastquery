package org.fastquery.osgi.impl;

import java.util.Dictionary;

import org.fastquery.osgi.service.FQuery;
import org.fastquery.osgi.service.Registration;
import org.osgi.framework.Bundle;

/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月18日
 */
public class FQueryImpl implements FQuery {

	@Override
	public Registration registerService(Bundle bundle,Dictionary<String, ?> properties) {
		GenerateOSGiRepositoryImpl generateOSGiRepositoryImpl =  new GenerateOSGiRepositoryImpl(bundle,properties);
		return 	generateOSGiRepositoryImpl.getRegistration();
	}
	
}
