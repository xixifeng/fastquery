/*
 * Copyright (c) 2016-2016, fastquery.org and/or its affiliates. All rights reserved.
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

package org.fastquery.service;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.fastquery.core.Resource;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryResourceImplTest {

	private Resource resource = new FQueryResourceImpl();
	
	@Test
	public void testGetResourceAsStream() {		
		try (InputStream inputStream = resource.getResourceAsStream("queries/org.fastquery.dao.UserInfoDBService.queries.xml"); ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
			int b = 0;
			while ( (b=inputStream.read()) != -1 ) {
				bo.write(b);
			}
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testExist() {
		assertThat(resource.exist("fastquery.json"), is(true));
	}

}
