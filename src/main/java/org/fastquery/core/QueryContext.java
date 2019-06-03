package org.fastquery.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.fastquery.dsm.FQueryProperties;
import org.fastquery.page.Pageable;
import org.fastquery.page.PageableImpl;
import org.fastquery.util.TypeUtil;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public final class QueryContext {

	private static final Logger LOG = LoggerFactory.getLogger(QueryContext.class);

	private static ThreadLocal<QueryContext> threadLocal = new ThreadLocal<>();

	private Method method; // 当前method
	private boolean supporTx; // 是否需要事务支持
	private boolean requirePk;// 改操作的返回值是否依赖主键
	private Class<?> returnType; // 返回类型
	private Connection connection; // 当前连接
	private String sourceName; // 当前数据源名称
	private Class<? extends QueryRepository> iclass;// 当前拦截到的接口
	private Object[] args; // 当前方法的实参
	private List<String> sqls = new ArrayList<>(); // 当前method所执行的SQL集合
	private MetaData metaData; // 当前上下文元数据
	private boolean builderQuery;
	private Pageable pageable;
		
	// 作用于调式
	private static boolean debug;

	private QueryContext() {
	}

	private static QueryContext getQueryContext() {
		return threadLocal.get();
	}

	static void start(Class<? extends QueryRepository> iclass, Method method, Object[] args) throws SQLException {
		QueryContext context = getQueryContext();
		if (context != null && !debug) {
			if(context.connection != null && !context.connection.isClosed()) { // 不等于null并且没有关闭
				throw new SQLException("QueryContext的连接没有正确释放");	
			} else {
				clear();	
			}
		}
		
		if (threadLocal.get() == null) {
			threadLocal.set(new QueryContext());
			context = threadLocal.get();
		}
		context.iclass = iclass;
		context.method = method;
		context.args = args;

		context.sourceName = findSource(method.getParameters(), args);
		if (context.connection == null || context.connection.isClosed()) { // 不加这行,测试StudentDBServiceTest会卡顿
			DataSource ds = getDataSource(context.sourceName, iclass.getName());
			Id id = method.getAnnotation(Id.class);
			if(TxContext.enabled()) {
				context.connection = TxContext.getTxContext().addConn(ds);
			} else if(id == null || id.value() != MethodId.QUERY9) { // 非tx(),注意:并代表tx函数体里的方法
					context.connection = ds.getConnection();
					if (debug) {
						getQueryContext().connection.setAutoCommit(false);
					}		
			}
		}

		Transactional t = context.method.getAnnotation(Transactional.class);
		context.supporTx = t == null || !t.propagation().equals(Propagation.NOT_SUPPORTED);

		context.returnType = method.getReturnType();

		if (context.returnType == Map.class || context.returnType == JSONObject.class || context.returnType == Primarykey.class
				|| TypeUtil.hasDefaultConstructor(context.returnType)) {
			context.requirePk = true;
		}

		BuilderQuery bq = null;
		for (Object arg : args) {
			if (arg instanceof BuilderQuery) {
				bq = (BuilderQuery) arg;
				break;
			}
		}
		if (bq != null) {
			context.builderQuery = true;
			context.metaData = new MetaData();
			bq.accept(context.metaData);
		}
		
		context.pageable = null;
		for (Object arg : args) {
			if (arg instanceof Pageable) { // 如果当前arg是Pageable接口的一个实例
				context.pageable = (Pageable) arg;
				break;
			}
		}
		Parameter[] parameters = method.getParameters();
		if (context.pageable == null) {
			// 没有传递Pageable,那么必然有 pageIndex, pageSize 不然,不能通过初始化
			context.pageable = new PageableImpl(TypeUtil.findPageIndex(parameters, args), TypeUtil.findPageSize(parameters, args));
		}
	}

	static List<String> getSqls() {
		return getQueryContext().sqls;
	}

	static void addSqls(String... sqls) {
		if (sqls == null) {
			return;
		}
		for (String sql : sqls) {
			getQueryContext().sqls.add(sql);
		}
	}

	public static Method getMethod() {
		return getQueryContext().method;
	}


	public static Connection getConn() {
		return getQueryContext().connection;
	}

	public static Object[] getArgs() {
		return getQueryContext().args;
	}
	
	static Pageable getPageable() {
		return getQueryContext().pageable;
	}

	public static Class<? extends QueryRepository> getIclass() {
		return getQueryContext().iclass;
	}

	static void clear() throws SQLException {
		QueryContext context = getQueryContext();
		if (!debug && context != null) {
			context.pageable = null;
			if (context.metaData != null) {
				context.metaData.clear();
				context.metaData = null;
			}
			context.method = null;
			context.sqls.clear();
			context.sqls = null;
			context.returnType = null;
			context.sourceName = null;
			context.iclass = null;
			context.args = null;
			try {
				if (context.connection != null && !TxContext.enabled()) {
					context.connection.close();
				}
			} catch (Exception e) {
				LOG.warn(e.getMessage(), e);
				throw new SQLException(e);
			} finally {
				threadLocal.remove();
				LOG.info("当前 QueryContext 生命周期结束");
			}
		}
	}

	public static void forceClear() throws SQLException { // NO_UCD 
		debug = false;
		clear();
	}

	/**
	 * 根据数据源名称或者className,获取数据源. 注意: 根据dataSourceName查优先
	 * 
	 * @param dataSourceName 数据源名称, 若为null,那么就根据className来查找
	 * @param className Repository class
	 * @return 数据源
	 */
	private static DataSource getDataSource(String dataSourceName, String className) {

		// 根据dataSourceName 查
		DataSource dataSource = FQueryProperties.findDataSource(dataSourceName);
		if (dataSource == null) {
			// 根据basePackage 查找出 数据源的名字
			String name = FQueryProperties.findDataSourceName(className);
			// 再根据数据源的名字查寻出数据库对象
			dataSource = FQueryProperties.findDataSource(name);
		}

		// dataSource 为null 什么也做不了
		if (dataSource == null) {
			throw new ExceptionInInitializerError(
					"没有找到数据源,请键查fastquery.json是否配置正确,或者是没有初始化连接池. \n 连接池的生成有两种模式:\n1).通过配置c3p0-config.xml,jdbc-config.xml \n2).通过FQueryProperties.createDataSource(...)");
		}

		return dataSource;
	}

	/**
	 * 标识有Source注解的参数的具体的实参.
	 * 
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return 值
	 */
	private static String findSource(Parameter[] parameters, Object... args) {
		Object obj = TypeUtil.findAnnotationParameterVal(Source.class, parameters, args);
		return obj != null ? obj.toString() : null;
	}

	public static Class<?> getReturnType() {
		return getQueryContext().returnType;
	}

	/**
	 * 关闭事务自动提交
	 * 
	 * @throws SQLException 异常
	 */
	static void disableAutoCommit() throws SQLException {
		if (!debug && getQueryContext().supporTx && !TxContext.enabled()) {
			getQueryContext().connection.setAutoCommit(false);
		}
	}

	/**
	 * 提交事务
	 * 
	 * @throws SQLException 异常
	 */
	static void commit() throws SQLException {
		if (!debug && getQueryContext().supporTx && !TxContext.enabled()) {
			getQueryContext().connection.commit();
		}
	}

	/**
	 * 回滚事务
	 * 
	 * @throws SQLException 异常
	 */
	static void rollback() throws SQLException {
		if (!debug && getQueryContext().supporTx && !TxContext.enabled()) {
			getQueryContext().connection.rollback();
		}
	}

	static boolean isRequirePk() {
		return getQueryContext().requirePk;
	}

	/**
	 * 获取query语句
	 * 
	 * @return 查询语句
	 */
	public static String getQuery() {
		return getQueryContext().metaData.getQuery();
	}

	/**
	 * 获取countQuery
	 * 
	 * @return count语句
	 */
	static String getCountQuery() {
		return getQueryContext().metaData.getCountQuery();
	}

	public static boolean isBuilderQuery() {
		return getQueryContext().builderQuery;
	}

	static String getCountField() {
		return getQueryContext().metaData.getCountField();
	}
}

