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

import org.fastquery.dao.TypeFeatureDBService;
import org.fastquery.dao.UserInfoDBService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
@ContextConfiguration(locations = {"classpath:beans.xml"})
@RunWith(value = SpringJUnit4ClassRunner.class)
public class SpringSupportTest extends FastQueryTest {

    @Resource
    private UserInfoDBService userInfoDBService;

    @Autowired
    private TypeFeatureDBService typeFeatureDBService;

    @Test
    public void notNullVal(){
        assertThat(userInfoDBService,notNullValue());
        assertThat(typeFeatureDBService,notNullValue());
    }

}
