package org.fastquery.osgi.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.apache.cxf.bus.blueprint.BundleDelegatingClassLoader;
import org.apache.log4j.Logger;
import org.fastquery.osgi.service.Registration;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import org.fastquery.core.GenerateRepository;
import org.fastquery.core.Repository;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.mapper.QueryPool;
import org.fastquery.asm.AsmRepository;
import org.fastquery.util.LoadPrperties;

/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月16日
 */
public class GenerateOSGiRepositoryImpl extends BundleDelegatingClassLoader implements GenerateRepository {
	
	private static final Logger LOG = Logger.getLogger(GenerateOSGiRepositoryImpl.class);
	
	private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
	
	public GenerateOSGiRepositoryImpl(Bundle bundle,Dictionary<String, ?> properties) {
		super(bundle);
		
		LOG.debug("GenerateOSGiRepositoryImpl 已实例化..");
		
		Resource resource = new FqueryResourceImpl(bundle);
		
		// 装载配置文件
		Set<FastQueryJson> fqPropertie = LoadPrperties.load(resource);
		
		// 批量生成 Repository 的实现类
		Set<String> basePackages = null;
		for (FastQueryJson fQueryPropertie : fqPropertie) {
			basePackages = fQueryPropertie.getBasePackages();
			for (String basePackage : basePackages) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends Repository> clazz = (Class<? extends Repository>) bundle.loadClass(basePackage); // 装载这个bundle的类,显然很合理!
					Repository repository = generate(clazz);
					// 生成代理对象
					repository = (Repository) Proxy.newProxyInstance(this,new Class<?>[] {clazz},new DBInvocationHandler(repository,this));
					// 存储sql模板
					QueryPool.put(clazz.getName(), resource);
					// 注册服务
					serviceRegistrations.add(bundle.getBundleContext().registerService(clazz.getName(), repository, properties));
				} catch (ClassNotFoundException e) {
					throw new ExceptionInInitializerError(e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Repository> T generate(Class<T> repositoryClazz) {

		String name = repositoryClazz.getName()+ SUFFIX;
		
		Class<T> clazz = (Class<T>) findLoadedClass(name);
		
		if(clazz == null) { // 等待生成的类,不存在才能生成.
			LOG.info(name + "这个类在当前classloader中不存在,准备生成...");
			byte[] bytes = AsmRepository.generateBytes(repositoryClazz);
			try {
				return (T) defineClass(name,bytes, 0, bytes.length).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassFormatError e) {
				e.printStackTrace();
			}
		} else { // 表明已经生成了
			LOG.info(name + "这个类已经生成并装载!");
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Repository> T getProxyRepository(Class<T> clazz) {
		T t = null;
		try {
			t = (T) this.findLoadedClass(clazz.getName() + SUFFIX).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return t;
	}

	public Registration getRegistration() {
		return new Registration(serviceRegistrations);
	}
}
