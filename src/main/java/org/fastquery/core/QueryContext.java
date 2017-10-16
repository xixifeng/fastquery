package org.fastquery.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.fastquery.dsm.FQueryFactoryImpl;
import org.fastquery.dsm.FQueryProperties;
import org.fastquery.util.TypeUtil;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public final class QueryContext {

	private static ThreadLocal<QueryContext> threadLocal = new ThreadLocal<>();

	private Method method; // 当前method
	private Class<?> returnType;   // 返回类型
	private Connection connection; // 当前连接
	private String sourceName; // 当前数据源名称
	private Class<? extends Repository> iclass;// 当前拦截到的接口
	private Object[] args; // 当前方法的实参

	private String lang = "zh_CN"; // 语言编码
	private List<String> sqls = new ArrayList<>(); // 当前method所执行的SQL集合
	
	// 用作调式
	private boolean debug;

	private QueryContext() {
	}

	private static QueryContext getQueryContext() {
		if (threadLocal.get() == null) {
			threadLocal.set(new QueryContext());
		}
		return threadLocal.get();
	}

	static void start(Class<? extends Repository> iclass, Method method, Object[] args) throws SQLException {
		QueryContext context = getQueryContext();
		context.iclass = iclass;
		context.method = method;
		context.args = args;

		context.sourceName = findSource(method.getParameters(), args);
		context.connection = getDataSource(context.sourceName, iclass.getName()).getConnection();
		
		context.returnType = method.getReturnType();
	}

	public static List<String> getSqls() {
		return getQueryContext().sqls;
	}

	public static void addSqls(String... sqls) {
		if (sqls == null) {
			return;
		}
		for (String sql : sqls) {
			getQueryContext().sqls.add(sql);
		}
	}

	public static String getLang() {
		return getQueryContext().lang;
	}

	public static Method getMethod() {
		return getQueryContext().method;
	}

	public static void setLang(String lang) {
		getQueryContext().lang = lang;
	}

	public static Connection getConnection() {
		return getQueryContext().connection;
	}

	public static String getSourceName() {
		return getQueryContext().sourceName;
	}

	public static Object[] getArgs() {
		return getQueryContext().args;
	}

	public static void setConnection(Connection connection) {
		getQueryContext().connection = connection;
	}

	public static void setSourceName(String sourceName) {
		getQueryContext().sourceName = sourceName;
	}

	public static Class<? extends Repository> getIclass() {
		return getQueryContext().iclass;
	}

	public static void clear() throws SQLException {
		QueryContext context = getQueryContext();
		if(context.debug) {
			return;
		}
		context.lang = null;
		context.method = null;
		context.sqls.clear();
		if (context.connection != null) {
			context.connection.close();
		}
		threadLocal.remove();
	}

	public static void forceClear() throws SQLException {
		QueryContext context = getQueryContext();
		context.debug = false;
		clear();
	}
	
	/**
	 * 获取数据源, 注意: 根据dataSourceName查优先
	 * @param dataSourceName 数据源名称
	 * @param className Repository class
	 * @return 数据源
	 */
	private static DataSource getDataSource(String dataSourceName,String className) {
		
    	// 根据dataSourceName 查
    	DataSource dataSource = FQueryProperties.findDataSource(dataSourceName);
    	if(dataSource == null) {
    		dataSource = FQueryFactoryImpl.getInstance().getDataSource(className);
    	}
    	
		// dataSource 为null 什么也做不了
		if(dataSource==null) {
			throw new  ExceptionInInitializerError("没有找到数据源,请键查fastquery.json是否配置正确,或者是没有初始化连接池. \n 连接池的生成有两种模式:\n1).通过配置c3p0-config.xml,jdbc-config.xml \n2).通过FQueryProperties.createDataSource(...)");
		}

		return dataSource;
	}
	
	
	/**
	 * 标识有Source注解的参数的具体的实参.
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return 值
	 */
	private static String findSource(Parameter[] parameters,Object...args){
		Object obj = TypeUtil.findAnnotationParameterVal(Source.class, parameters, args);
		return obj !=null ? obj.toString() : null;
	}

	public static Class<?> getReturnType() {
		return getQueryContext().returnType;
	}

	public static void setReturnType(Class<?> returnType) {
		getQueryContext().returnType = returnType;
	}
}
