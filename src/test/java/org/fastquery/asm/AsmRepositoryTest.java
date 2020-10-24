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

package org.fastquery.asm;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
public class AsmRepositoryTest
{
    private static final Logger log = LoggerFactory.getLogger(AsmRepositoryTest.class);

    @Test
    public void getParameterDef() throws Exception
    {
        String str = AsmRepository.getParameterDef(new Class<?>[]{String[][].class, String.class});
        assertThat(str, containsString("java.lang.String[][] p0,"));
        str = AsmRepository.getParameterDef(new Class<?>[]{String[].class, String.class});
        assertThat(str, containsString("java.lang.String[] p0,"));
        str = AsmRepository.getParameterDef(new Class<?>[]{String[][][].class, String.class});
        log.info(str, equalTo("java.lang.String[][][] p0,java.lang.String p1"));
        str = AsmRepository.getParameterDef(new Class<?>[]{int[].class});
        assertThat(str, equalTo("int[] p0"));
    }

}