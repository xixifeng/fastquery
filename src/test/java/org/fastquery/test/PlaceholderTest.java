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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.Placeholder;
import org.fastquery.util.TypeUtil;
import org.junit.Test;

/**
 * 测试正则
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class PlaceholderTest extends TestFastQuery
{
    @Test
    public void Q_MATCH()
    {
        String str = "select * from UserInfo where name like `- - ?_  --- -`   and age like `-_?-` and akjgew `-  ?_-` and sge`-  ?                     -`";

        List<String> ssms = TypeUtil.matches(str, Placeholder.SMILE_PATT);

        for (String string : ssms)
        {
            log.debug(string);
        }

        assertThat(ssms.size(), is(4));

        assertThat(ssms.get(0), equalTo("`- - ?_  --- -`"));

        assertThat(ssms.get(1), equalTo("`-_?-`"));

        assertThat(ssms.get(2), equalTo("`-  ?_-`"));

        assertThat(ssms.get(3), equalTo("`-  ?                     -`"));

        ssms.forEach(log::debug);
    }

    @Test
    public void SL_REG1()
    {
        String str = ":a[]%:b% %:c%";
        List<String> ssms = TypeUtil.matches(str, Placeholder.COLON_REG_PATT);
        assertThat(ssms.size(), is(3));
        assertThat(ssms.get(0), equalTo(":a"));
        assertThat(ssms.get(1), equalTo(":b"));
        assertThat(ssms.get(2), equalTo(":c"));
    }

    @Test
    public void SL_REG2()
    {
        String str = "_:%aa_b  %:Ccc_d%:eeE:F_:1%%%%_jkjgwoxl:abc";
        List<String> ssms = TypeUtil.matches(str, Placeholder.COLON_REG_PATT);
        assertThat(ssms.size(), is(5));
        assertThat(ssms.get(0), equalTo(":Ccc"));
        assertThat(ssms.get(1), equalTo(":eeE"));
        assertThat(ssms.get(2), equalTo(":F"));
        assertThat(ssms.get(3), equalTo(":1"));
        assertThat(ssms.get(4), equalTo(":abc"));
    }

    @Test
    public void SL_REG3()
    {
        String str = ":id,:name,:age";
        List<String> ssms = TypeUtil.matches(str, Placeholder.COLON_REG_PATT);
        assertThat(ssms.size(), is(3));
        assertThat(ssms.get(0), equalTo(":id"));
        assertThat(ssms.get(1), equalTo(":name"));
        assertThat(ssms.get(2), equalTo(":age"));
    }

    @Test
    public void PERCENT()
    {
        String str = "";
        boolean b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(false));

        str = "%";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(true));

        str = "a%";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(false));

        str = "%aa";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(false));

        str = "%%";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(true));

        str = "%%%";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(true));

        str = "%%%";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(true));

        str = "%%%a";
        b = Pattern.matches(Placeholder.PERCENT, str);
        assertThat(b, is(false));
    }

    @Test
    public void SEARCH_NUM()
    {
        String s = "`-%?103%?1kjgw?2?398klgw?3-`";
        List<String> strs = TypeUtil.matches(s, Placeholder.SEARCH_NUM_PATT);
        for (String str : strs)
        {
            assertThat(Pattern.matches("\\d+", str), is(true));
        }
    }

    @Test
    public void EL_OR_COLON()
    {
        String str = "jklgjw ,gwlljlgw `- :name_ :info- $name_ $ok.ax -` and ${abc_} conm ${mark} orfkljgw xg ${a} lgwiouo$_iwk$ jhlkgikw $abc}";
        List<String> strs = TypeUtil.matches(str, Placeholder.EL_OR_COLON_PATT);
        assertThat(strs.size(), is(9));
        assertThat(strs.get(0), equalTo(":name"));
        assertThat(strs.get(1), equalTo(":info"));
        assertThat(strs.get(2), equalTo("$name_"));
        assertThat(strs.get(3), equalTo("$ok.ax"));
        assertThat(strs.get(4), equalTo("${abc_}"));
        assertThat(strs.get(5), equalTo("${mark}"));
        assertThat(strs.get(6), equalTo("${a}"));
        assertThat(strs.get(7), equalTo("$_iwk"));
        assertThat(strs.get(8), equalTo("$abc}"));
    }

}
