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

package org.fastquery.core;

/**
 * 任务的生命周期
 * @author xixifeng (fastquery@126.com)
 */
public enum QuartzStatus {
		/**
		 * 开始
		 */
		START,
		/**
		 * 暂停
		 */
		PAUSE,
		/**
		 * 继续/恢复
		 */
		RESUME,
		/**
		 * 删除
		 */
		DELETE,
		/**
		 * 立即运行，只会运行一次,本次任务运行完成之后自动删除
		 */
		TRIGGER,
		
		/**
		 * 立即运行，只会运行一次,本次任务运行完成之后不是删除,而是挂起,等待再次使用
		 */
		TRIGGER_HANGUP,
		/**
		 * 重新装载运行,在更新调度规则时,需要reload. 或者恢复删除的作业
		 */
		RELOAD;
		
		public enum Active {
			/**
			 * 激活按照计划任务执行作业
			 */
			YES,
			/**
			 * 挂起,等待触发使用
			 */
			HANGUP,
			/**
			 * 触发立即运行，只会运行一次,本次任务运行完成之后自动删除
			 */
			TRIGGER,
			
			/**
			 * 触发立即运行，只会运行一次,本次任务运行完成之后不是删除,而是挂起,等待再次使用
			 */
			TRIGGER_HANGUP;
		}
}
