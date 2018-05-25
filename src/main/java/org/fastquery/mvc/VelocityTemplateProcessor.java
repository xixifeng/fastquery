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

package org.fastquery.mvc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;

/**
 * jersey velocity模板处理器
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class VelocityTemplateProcessor implements TemplateProcessor<String> { // NO_UCD (unused
																				// code)

	@Context
	private HttpServletRequest request;

	@Override
	public String resolve(String path, final MediaType mediaType) {
		return path;
	}

	@Override
	public void writeTo(String templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream out) throws IOException {

		// 获取 模板引擎
		VelocityEngine velocityEngine = getVelocityEngine();

		// 实例化一个VelocityContext
		VelocityContext context = (VelocityContext) viewable.getModel();
		Enumeration<String> enums = request.getParameterNames();
		while (enums.hasMoreElements()) {
			String key = enums.nextElement();
			context.put(key, request.getParameter(key));
		}
		// 把request放进模板上下文里
		context.put("request", request);

		// 渲染并输出
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
		velocityEngine.mergeTemplate(templateReference, "utf8", context, outputStreamWriter);
		outputStreamWriter.flush();
		outputStreamWriter.close(); // 有必要关闭吗? 关闭了是否对jax-rs拦截器,servlet有影响,需要继续学习,参考jsp模板实现
	}

	private VelocityEngine getVelocityEngine() throws IOException {
		// 当前servletContext环境有没有
		Object ve = request.getServletContext().getAttribute("velocityEngine");
		if (ve != null) {
			return (VelocityEngine) ve;
		}

		// 注册velocity
		String path = request.getServletContext().getRealPath("/WEB-INF/classes/velocity.properties");
		Properties properties = new Properties();
		try (FileInputStream in = new FileInputStream(path)) {
			properties.load(in);
			properties.setProperty("file.resource.loader.path", request.getServletContext().getRealPath("/WEB-INF/vm"));
		} catch (IOException e) {
			throw e;
		}

		VelocityEngine velocityEngine = new VelocityEngine(properties);
		request.getServletContext().setAttribute("velocityEngine", velocityEngine);
		return velocityEngine;
	}

}
