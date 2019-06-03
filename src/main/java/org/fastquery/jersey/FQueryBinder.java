/*
 * Copyright (c) 2016-2088, fastquery.org and/or its affiliates. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For more information, please see http://www.fastquery.org/.
 * 
 */

package org.fastquery.jersey;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.fastquery.core.Extends;
import org.fastquery.core.RepositoryException;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryBinder extends AbstractBinder { // NO_UCD

	private FqClassLoader fqClassLoader;

	private FQueryBinder(ClassLoader webClassLoader) {
		this.fqClassLoader = new FqClassLoader(webClassLoader, this);
		// 生成rest实现类
		new GenerateRepositoryImpl(fqClassLoader).persistent();
	}

	@Override
	protected void configure() {
		//
	}

	// 自定义MessageBodyWriter
	// 用于支持 httpsign(https://github.com/xixifeng/httpsign) 所定义的数据结构
	private static class DataStruct implements MessageBodyWriter<Object> {

		private static boolean extended(String name, Annotation[] annos) {
			for (Annotation annotation : annos) {
				Class<? extends Annotation> cls = annotation.annotationType();
				if (cls == Extends.class) {
					Extends exts = (Extends) annotation;
					Class<? extends Annotation>[] value = exts.value();
					for (Class<? extends Annotation> c : value) {
						String cn = c.getSimpleName();
						if (cn.equals(name)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		public boolean isWriteable(Class<?> returnType, Type genericType, Annotation[] annotations, MediaType mediaType) {
			// 注意 isWriteable 若发生异常,会直接导致MessageBodyWriter失效,并且异常没有被捕捉.
			return extended("Authorization", annotations);
		}

		@Override
		public void writeTo(Object returnInstance, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
			JSONObject json = new JSONObject();
			json.put("code", 0);
			if (returnInstance == null) {
				json.put("data", new JSONObject());
			} else {
				json.put("data", JSON.toJSON(returnInstance));
			}
			entityStream.write(json.toJSONString().getBytes());
		}

	}

	public static void bind(ResourceConfig resource) { // NO_UCD 
		FQueryBinder fb = new FQueryBinder(resource.getClassLoader());
		resource.register(fb);
		try {
			resource.registerClasses(fb.fqClassLoader.getResourceClasses());
		} catch (ClassNotFoundException e) {
			throw new RepositoryException("没有找到类", e);
		}
		// httsign 数据结构
		resource.register(DataStruct.class);
	}
}
