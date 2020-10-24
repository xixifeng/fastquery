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

import org.fastquery.asm.AsmRepositoryTest;
import org.fastquery.util.BeanUtilTest;
import org.fastquery.util.TypeUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 运行所有的测试用例
 *
 * @author xixifeng (fastquery@126.com)
 */
// 指定运行器
@RunWith(Suite.class)
@SuiteClasses({
        FQueryTest.class, StudentDBServiceTest.class, UserInfoDBServiceTest.class, UserInfoDBServiceTest2.class, UserInfoDBServiceTest3.class,
        TypeUtilTest.class, MethodQueryTest.class, QueryByNamedDBExampleTest.class, BeanUtilTest.class, PageTest.class, FastQueryJSONObjectTest.class,
        FQueryResourceImplTest.class, SunnyDBServiceTest.class, SaveToIdTest.class, PlaceholderTest.class, DBTest.class, ProductDBServiceTest.class,
        FQueryPropertiesTest.class, QueryPoolTest.class, ParamFilterTest.class, ConditionTest.class, MySQLPageDialectTest.class, DefaultMethodTest.class,
        RepVersion.class, SetDBServiceTest.class, SetDBServiceTest2.class, SharpExprParserTest.class,
        TransactionalTest.class, QueryByNamedDBExtendTest.class, JudgeTest.class, Script2ClassTest.class, TxTest.class, WorldDBTest.class, TypeTestDBTest.class, SelectFieldTest.class, TypeFeatureDBServiceTest.class, AsmRepositoryTest.class, SpringSupportTest.class
})
public class AllRuleTest
{
}
