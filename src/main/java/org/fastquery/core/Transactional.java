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

package org.fastquery.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 事务
 *
 * @author xixifeng (fastquery@126.com)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional
{ // NO_UCD

    /**
     * 超时,默认-1,表示永不超时
     *
     * @return int
     */
    int timeout() default -1; // NO_UCD

    /**
     * 传播行为
     *
     * @return Propagation
     */
    Propagation propagation() default Propagation.REQUIRED;

}
