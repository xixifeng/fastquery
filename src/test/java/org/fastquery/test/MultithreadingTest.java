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

package org.fastquery.test;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fastquery.bean.sunny.Card;
import org.fastquery.dao.SunnyDBService;
import org.fastquery.service.FQuery;

/**
 * 多线程测试
 * @author xixifeng (fastquery@126.com)
 */
public class MultithreadingTest {
	static int count = 0;
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		SunnyDBService sunnyDBService = FQuery.getRepository(SunnyDBService.class);
		 ExecutorService executorService = Executors.newFixedThreadPool(1000);
		 int sum = 10000000;
		 for (int i = 0; i < sum; i++) {
			 executorService.execute(new Runnable() {
				@Override
				public void run() {
					/*
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
					Card card = new Card();
					card.setNumber("852000XXXXX");
					Card c = sunnyDBService.save(card);
					//Integer id = c.getId();
					//int effect = sunnyDBService.delete(id);
					count = count + 1;
					if(count==(sum-1)){
						System.out.println("用时: " + (System.currentTimeMillis()-s) + "毫秒");
					} 
				}
			});
		}
		
	}
}
