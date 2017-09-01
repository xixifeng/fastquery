package org.fastquery.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.fastquery.core.QueryProcess;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 * @date 2017年8月31日
 */
public class QueryProcessTest {

	@Test
	public void mapValueTyep() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Method m = QueryProcess.class.getDeclaredMethod("mapValueTyep", Method.class);
		m.setAccessible(true);

		class A {
			public Map<String, String> todo() {
				return null;
			}
		}

		Class<?> clazz = A.class;
		Method method = clazz.getMethod("todo");
		assertThat(m.invoke(null, method) == String.class, is(true));
	}

	@Test
	public void listMapValueTyep() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Method m = QueryProcess.class.getDeclaredMethod("listMapValueTyep", Method.class);
		m.setAccessible(true);

		class B {
			public List<Map<String, String>> todo() {
				return null;
			}
		}

		Class<?> clazz = B.class;
		Method method = clazz.getMethod("todo");
		assertThat(m.invoke(null, method) == String.class, is(true));
	}

}
