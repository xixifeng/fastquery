package org.fastquery.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public final class QueryContext {

	private static ThreadLocal<QueryContext> threadLocal = new ThreadLocal<>();

	private String lang = "zh_CN";
	private Method method;
	private List<String> sqls = new ArrayList<>();

	private QueryContext() {
	}

	public List<String> getSqls() {
		return sqls;
	}

	public void addSqls(String... sqls) {
		if (sqls == null) {
			return;
		}
		for (String sql : sqls) {
			this.sqls.add(sql);
		}
	}

	public static QueryContext getQueryContext() {
		if (threadLocal.get() == null) {
			threadLocal.set(new QueryContext());
		}
		return threadLocal.get();
	}

	public String getLang() {
		return lang;
	}

	public Method getMethod() {
		return method;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void clear() {
		lang = null;
		method = null;
		sqls.clear();
		threadLocal.remove();
	}

}
