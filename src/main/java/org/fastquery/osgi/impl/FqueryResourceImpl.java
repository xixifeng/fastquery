package org.fastquery.osgi.impl;

import java.io.IOException;
import java.io.InputStream;

import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.osgi.framework.Bundle;

/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月19日
 */
public class FqueryResourceImpl implements Resource {

	private Bundle bundle;
	
	public FqueryResourceImpl(Bundle bundle){
		this.bundle = bundle;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if(!exist(name)) {
			return null;
		}
		try {
			return bundle.getResource(name).openStream();
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}
	
	@Override
	public boolean exist(String name) {
		if(name==null || name.charAt(0) == '/') {
			return false;
		}
		return bundle.getResource(name) != null;
	}


}
