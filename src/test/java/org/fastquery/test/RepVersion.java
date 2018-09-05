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

package org.fastquery.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.fastquery.util.TypeUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class RepVersion extends FastQueryTest  {

	private static final Logger LOG = LoggerFactory.getLogger(RepVersion.class);

	private static final String REG = "\\d+\\.\\d+\\.\\d+";

	@SuppressWarnings("unused")
	private static void exec(String path, String target) throws IOException, InterruptedException {
		String command = "/usr/bin/sed -i s/{\\[version\\]}/" + target + "/ " + path;
		LOG.debug(command);
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		p.destroy();
	}

	private static String showInputDialog(String initialSelectionValue) {
		String s;
		while ("".equals((s = JOptionPane.showInputDialog("请输入发布版本号: ", initialSelectionValue))) || s == null || !Pattern.matches(REG, s)) {
		}
		return s;
	}

	private static String confirm(String v, String initialSelectionValue) {
		int n = JOptionPane.showConfirmDialog(null, "发布的版本号为: " + v + " 吗?", "版本确认", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			return v;
		} else {
			return confirm(showInputDialog(initialSelectionValue), initialSelectionValue);
		}
	}

	private static String confirm(String initialSelectionValue) {
		return confirm(showInputDialog(initialSelectionValue), initialSelectionValue);
	}

	@Test
	public void main() throws IOException {

		String userDir = System.getProperty("user.dir");

		String initialSelectionValue = "";

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(userDir, "/src/main/resources/META-INF/MANIFEST.MF"))))) {
			String lineTxt = null;
			while ((lineTxt = br.readLine()) != null) {
				if (lineTxt.startsWith("Bundle-Version:")) {
					initialSelectionValue = TypeUtil.matches(lineTxt, REG).get(0);
					break;
				}
			}
		} catch (IOException e) {
			throw e;
		}

		String version = confirm(initialSelectionValue);
		LOG.debug("最终确认的版本号是: " + version);

		String[] names = new String[] { "/src/main/resources/META-INF/MANIFEST.MF", "/pom.xml", "/README.md" };
		for (String name : names) {
			File tmp = File.createTempFile("temp", ".java");// 创建临时文件
			String tpf = tmp.getAbsolutePath();
			File f = new File(userDir, name);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp)));) {
				String lineTxt = null;
				while ((lineTxt = br.readLine()) != null) {
					if (lineTxt.endsWith("<!-- fastquery.version -->") || lineTxt.startsWith("compile 'org.fastquery:fastquery")
							|| lineTxt.startsWith("Bundle-Version:")) {
						bw.write(lineTxt.replaceAll(REG, version) + "\n");
					} else {
						bw.write(lineTxt + "\n");
					}
				}
				bw.flush();
				f.delete();
				Files.move(tmp.toPath(), f.toPath());
			} catch (IOException e) {
				throw e;
			} finally {
				new File(tpf).delete();
			}
		}

	}

}
