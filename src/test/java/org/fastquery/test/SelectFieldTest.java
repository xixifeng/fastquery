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

import org.fastquery.bean.UserInfo;
import org.fastquery.core.SelectField;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
public class SelectFieldTest {

    @Test
    public void getFields() {
        SelectField<UserInfo> selectField = new SelectField<>(UserInfo.class, true);
        assertThat(selectField.getFields(),equalTo("id,name,age"));

        selectField = new SelectField<>(UserInfo.class,false);
        assertThat(selectField.getFields(),equalTo("id,name,age"));

        selectField = new SelectField<>(UserInfo.class,true,"name");
        assertThat(selectField.getFields(),equalTo("name"));
        selectField = new SelectField<>(UserInfo.class,true,"name","age");
        assertThat(selectField.getFields(),equalTo("name,age"));

        selectField = new SelectField<>(UserInfo.class,false,"name");
        assertThat(selectField.getFields(),equalTo("id,age"));
        selectField = new SelectField<>(UserInfo.class,false,"name","id");
        assertThat(selectField.getFields(),equalTo("age"));
    }

    @Test
    public void getFields2() {
        SelectField<UserInfo> selectField = new SelectField<>(UserInfo.class,false,"id","age","name");
        assertThat(selectField.getFields(),equalTo("1"));

        selectField = new SelectField<>(UserInfo.class,true,"id","age","name");
        assertThat(selectField.getFields(),equalTo("id,name,age"));
    }
}
