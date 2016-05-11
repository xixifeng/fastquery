#FastQuery 快速操作数据层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.做为一个开发者,仅仅只需要设计编写DAO接口即可,其内部采用ASM动态生成实现,执行快. 因此,代码简洁而优雅.从而,大幅度提升开发效率.
##FastQuery 主要特性如下:
1. 设计优雅,配置简单,简易上手.
2. 采用ASM动态生成字节码,因此支持编译前预处理,可最大限度减少运行期的错误.显著提升程序的强壮性.
3. 支持安全查询,防止SQL注入.
4. 支持与主流连接池框架集成,如集成c3p0,dbcp等等
5. 支持 **@Query** 查询,使用 **@Condition**,可实现动态 where 条件查询.
6. 查询结果集支持JSON类型
7. 支持AOP,注入拦截只需简单几个注解,如: **@Before** , **@After**

##运行环境要求
jdk1.8+

##配置文件
###jdbc-config.xml
用来配置支持jdbc. 注意:如果采用连接池,该配置文件可以不要.
```
<?xml version="1.0" encoding="UTF-8"?>
<jdbc-config>  
        <!-- 配置第一个数据源 -->
        <named-config name="xk_db">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="url">jdbc:mysql://192.168.1.1:3306/xk?user=xk&amp;password=abc123</property>
        </named-config>

        <!-- 配置第二个数据源 -->
        <named-config name="shtest_db">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  <!-- jdbc 驱动 -->
        <property name="databaseName">dbname</property>  <!-- 数据库的名称 -->
        <property name="user">username</property>  <!-- 数据库用户名称 -->
        <property name="password">userpasswd</property>  <!-- 数据库用户的密码 --> 
        <property name="portNumber">3306</property>   <!-- 端口 -->
        <property name="serverName">192.168.1.1</property> <!-- 数据库主机地址 -->
    </named-config>
</jdbc-config>
```
###c3p0-config.xml
支持c3p0配置,详情配置请参照c3p0官网的说明: http://www.mchange.com/projects/c3p0/.
```
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
    <!--
    <default-config>  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://...</property>
        <property name="user">root</property>  
        <property name="password">123***</property>  
        <property name="initialPoolSize">10</property>  
        <property name="maxIdleTime">30</property>  
        <property name="maxPoolSize">20</property>  
        <property name="minPoolSize">5</property>  
        <property name="maxStatements">200</property>  
    </default-config> 
-->   
    <named-config name="xk-c3p0">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://192.168.1.1:3306/xk</property>  
        <property name="user">xk</property>  
        <property name="password">abc123</property>  
        <property name="acquireIncrement">50</property>  
        <property name="initialPoolSize">100</property>  
        <property name="minPoolSize">50</property>  
        <property name="maxPoolSize">1000</property>
        <property name="maxStatements">0</property>  
        <property name="maxStatementsPerConnection">5</property>     
        </named-config> 
</c3p0-config>
```
### fastquery.json
配置数据源的作用范围

```js
// @author xixifeng (fastquery@126.com)
// 配置必须遵循标准的json语法.
[
    // config目前支持的可选值有"jdbc","c3p0"
    {
        "config": "c3p0",            // 表示由c3p0负责提供数据源
        "dataSourceName": "xk-c3p0", // 数据源的名称
        "basePackages": [            // 该数据源的作用范围
            "org.fastquery.example.StudentDBService"
        ]
    },
    
    /*
     再配置一个数据源作用域
    */
    {
        "config" : "jdbc",            // 表示由jdbc驱动负责提供数据源
        "dataSourceName": "shtest_db",
        "basePackages": [ // 该数据源的作用范围
            "org.fastquery.example.DataAcquireDbService"
        ]
    }
]
```

##一个完整的入门例子
- 准备一个实体

```java
 public class Student
 {
      private String no;
      private String name;
      private String sex;
      private Integer age;
      private String dept;
      // getter /setter 省略...
 } 
```

- DAO接口

```java
 public interface StudentDBService extends QueryRepository {
    @Query("select * from student")
    JSONArray findAll();
    @Query("select * from student")
    Student[] find();      
 }
```

- 使用DAO接口.

```java
 // get porxy impl
 StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
 // call findAll
 JSONArray jsonArray = studentDBService.findAll();
 // call find
 Student[] students = studentDBService.find(); 
```

##联系作者
fastquery#126.com
