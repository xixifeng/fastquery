package org.fastquery.osgi.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.fastquery.core.Repository;
import org.fastquery.core.Prepared;

/**
 * 
 * @author meigh@chinatenet.com
 * @version 1.0 2016年4月18日
 */
public class DBInvocationHandler  implements InvocationHandler{

	private Repository repository;
	private ClassLoader classLoader;
	//private Dictionary<String, ?> properties;
	public DBInvocationHandler(Repository repository,ClassLoader classLoader) { // ,Dictionary<String, ?> properties
		this.repository = repository;
		this.classLoader = classLoader;
		//this.properties = properties;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 请特别注意args 有可能为null
		
		
		Prepared.setClassLoader(classLoader);// 把classLoader存储在当前线程里
		// proxy.getClass().getClassLoader() // 这样也是可以的,虽说可以减少DBInvocationHandler的参数,但是每次调用到要获取,感觉效率不高,还不如传递进来.
		/*
		Object val = null;
		Object returnval = void.class; // 注意: 这个初始值必须为void.class!!!
		Class<?> clazz = null;
		// 注册 beforeFilter
		if(properties!=null){
		val = properties.get(method.getDeclaringClass().getName());
		returnval = void.class; // 注意: 这个初始值必须为void.class!!!
		if(val!=null){
			clazz = val.getClass();
			if( (clazz.isArray() && BeforeFilter.class.isAssignableFrom(clazz.getComponentType())) || BeforeFilter.class.isAssignableFrom(clazz) ) {
					if(clazz.isArray()) {
						returnval = FilterChainHandler.bindBeforeFilterChainx(repository,method, args,(BeforeFilter[]) val);
					} else {
						returnval = FilterChainHandler.bindBeforeFilterChainx(repository,method, args,(BeforeFilter) val);
					}	
			}
		} 
		if(returnval!=void.class) {
                return returnval;
        }
		}
		// 注册 beforeFilter End
		*/
		
		Object result = method.invoke(repository, args);
		
		/*
		if(properties!=null){
		// 注册 afterFilter
		if(val!=null){
			clazz = val.getClass();
			if( (clazz.isArray() && AfterFilter.class.isAssignableFrom(clazz.getComponentType())) || AfterFilter.class.isAssignableFrom(clazz) ) {
				if(clazz.isArray()) {
					result = FilterChainHandler.bindAfterFilterChain(repository,method,args,result, (AfterFilter[])val);
				} else {
					result = FilterChainHandler.bindAfterFilterChain(repository,method,args,result, (AfterFilter)val);
				}	
			}
		}
		// 注册 afterFilter End
		}
		*/
		// 执行之后
		return result;
	}
}







