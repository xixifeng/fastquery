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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.RegexCache;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;
import org.fastquery.where.Judge;
import org.fastquery.where.Set;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author mei.sir@aliyun.cn
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Script2Class
{
    private static final Map<String, Judge> judges = new HashMap<>();

    /**
     * 处理脚本中的冒号表达式
     *
     * @param script 脚本
     * @param method 脚本的所属方法
     * @return 被处理后的脚本
     */
    private static String processParam(String script, Method method)
    {
        List<String> names = TypeUtil.matches(script, RegexCache.COLON_REG_PATT);
        for (String paramName : names)
        {
            String name = paramName.replace(":", StringUtils.EMPTY);
            Class<?> oldC = Judge.getParamType(name, method);
            Class<?> newC = ClassUtils.primitiveToWrapper(oldC);
            Pattern pattern = RegexCache.getPattern(paramName);
            if (oldC == newC)
            {
                // 包装类型 如果后面跟着
                script = RegExUtils.replaceAll(script,pattern,"((" + newC.getName() + ")this.getParameter(\"" + name + "\"))");

            }
            else
            { // 基本类型 -> 包装类型 那么就要解包
                // 加上解包方法
                script = RegExUtils.replaceAll(script,pattern,"((" + newC.getName() + ")this.getParameter(\"" + name + "\"))." + oldC.getName() + "Value()");
            }
        }
        return script;
    }

    private static String makeClassName(String genericString, int index)
    {
        String src = genericString + "$#" + index;
        return Base64.getEncoder().encodeToString(src.getBytes());
    }

    public static Judge getJudge(int index)
    {
        return judges.get(makeClassName(QueryContext.getMethodInfo().toGenericString(), index));
    }

    /**
     * 生成脚本源码, 编译, 实例化
     *
     * @param clazz Repository 类
     */
    public static void generate(Class<?> clazz)
    {
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
            pickScript(method, Set.class, Set::ignoreScript, Set::if$);
            pickScript(method, Condition.class, Condition::ignoreScript, Condition::if$);
        }
    }

    private static <T extends Annotation> void pickScript(Method method, Class<T> clazz, Function<T, String> sf, Function<T, String> ifun)
    {
        T[] ts = method.getAnnotationsByType(clazz);
        int len = ts.length;
        for (int i = 0; i < len; i++)
        {
            String script = sf.apply(ts[i]);
            if (!script.equals("false"))
            {
                gr(method, i, script);
            }

            String ifScript = ifun.apply(ts[i]);
            if (!"true".equals(ifScript))
            {
                gr(method, i, ifScript);
            }
        }
    }

    private static void gr(Method method, int i, String script)
    {
        try
        {
            generate(script, method, i);
        }
        catch (InstantiationException | IllegalAccessException | CannotCompileException | NotFoundException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new RepositoryException(method + "中的脚本  \"" + script + "  \" 编译错误", e);
        }
    }

    private static void generate(String script, Method method, int annotationIndex) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {

        ClassPool pool = ClassPool.getDefault();

        String className = makeClassName(method.toGenericString(), annotationIndex);

        CtClass ctClass = pool.makeClass(className);

        // 增加父类
        ctClass.setSuperclass(pool.get("org.fastquery.where.Judge"));

        // 增加方法
        CtMethod ctMethod = new CtMethod(CtClass.booleanType, "ignore", new CtClass[]{}, ctClass);
        //为自定义方法设置修饰符
        ctMethod.setModifiers(Modifier.PUBLIC);

        String code = processParam(script, method);
        StringBuilder body = new StringBuilder();
        body.append("return ");
        body.append(code);
        body.append(";");

        log.debug("生成源代码: {}", body);
        ctMethod.setBody(body.toString());
        ctClass.addMethod(ctMethod);

        judges.put(ctClass.getName(), (Judge) ctClass.toClass().getDeclaredConstructor().newInstance());

    }
}
