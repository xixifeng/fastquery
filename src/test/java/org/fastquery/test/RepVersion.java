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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.regex.Pattern;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.util.TypeUtil;
import org.junit.Test;

/**
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class RepVersion extends FastQueryTest
{
    private static final String REG = "\\d+\\.\\d+\\.\\d+(\\.enforce)*";

    private static String showInputDialog(String initialSelectionValue)
    {
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        String s;
        while ("".equals((s = JOptionPane.showInputDialog(jf, "请输入发布版本号: ", initialSelectionValue))) || s == null || !Pattern.matches(REG, s))
        {
            log.warn("输入的版本号是空，重新输入");
        }
        return s;
    }

    private static String confirm(String v, String initialSelectionValue)
    {
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        int n = JOptionPane.showConfirmDialog(jf, "发布的版本号为: " + v + " 吗?", "版本确认", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION)
        {
            return v;
        }
        else
        {
            return confirm(showInputDialog(initialSelectionValue), initialSelectionValue);
        }
    }

    private static String confirm(String initialSelectionValue)
    {
        return confirm(showInputDialog(initialSelectionValue), initialSelectionValue);
    }

    @Test
    public void main() throws IOException
    {

        String userDir = System.getProperty("user.dir");

        String initialSelectionValue = "";

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(userDir, "/pom.xml")))))
        {
            String lineTxt;
            while ((lineTxt = br.readLine()) != null)
            {
                if (lineTxt.endsWith("<!-- fastquery.version -->"))
                {
                    initialSelectionValue = TypeUtil.matches(lineTxt, REG).get(0);
                    break;
                }
            }
        }
        catch (IOException e)
        {
            throw e;
        }

        String version = confirm(initialSelectionValue);
        log.debug("最终确认的版本号是: " + version);

        String[] names = {"/pom.xml", "/README.md"};
        for (String name : names)
        {
            File tmp = File.createTempFile("temp", ".fquery");// 创建临时文件
            File f = new File(userDir, name);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp))))
            {
                String lineTxt;
                while ((lineTxt = br.readLine()) != null)
                {
                    if (lineTxt.endsWith("<!-- fastquery.version -->") || lineTxt.startsWith("compile 'org.fastquery:fastquery"))
                    {
                        bw.write(lineTxt.replaceAll(REG, version) + "\n");
                    }
                    else
                    {
                        bw.write(lineTxt + "\n");
                    }
                }
                bw.flush();
                br.close();
                log.debug("f: {}", f.getAbsolutePath());
                log.debug("tmp.toPath():{}", tmp.toPath());
                assertThat(f.delete(), is(true));
                //Files.move(tmp.toPath(), f.toPath());
                Files.copy(tmp.toPath(), f.toPath());
            }
            catch (IOException e)
            {
                throw e;
            }
        }

    }

}
