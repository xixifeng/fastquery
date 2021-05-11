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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.ProductDBService;
import org.fastquery.service.FQuery;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author mei.sir@aliyun.cn
 */
public class TransactionalTest extends TestFastQuery
{

    private final ProductDBService pdbs = FQuery.getRepository(ProductDBService.class);

    @Test
    public void testUpdates()
    {
        try{
            pdbs.updates();
        }catch (Exception e) {
            assertThat("断言: 在执行pdbs.updates()之后,将会抛出 RepositoryException 异常!",e instanceof RepositoryException, is(true));
            assertThat("断言: 在执行pdbs.updates()之后,抛出的异常信息是 \"Column 'pname' cannot be null\"", ExceptionUtils.getMessage(e),containsString("Column 'pname' cannot be null"));
        }
    }

}
