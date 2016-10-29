# FastQuery 数据持久层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.<br />
做为一个开发者, **仅仅只需要设计DAO接口即可**,其内部采用ASM动态生成实现类,执行快. 因此,代码简洁而优雅.从而,大幅度提升开发效率.<br />
遵循非侵入式原则设计,松耦合,很容易与其它容器或框架集成.<br />
提供了一组简单的`Annotation`.消费者只用关心注解的含义.这就使得框架的核心便于重构,便于持续良性发展.<br />

## FastQuery 主要特性如下:
1. 设计优雅,配置简单,极易上手.
2. 采用ASM动态生成字节码,因此支持编译前预处理,可最大限度减少运行期的错误.显著提升程序的强壮性.
3. 支持安全查询,防止SQL注入.
4. 支持与主流数据库连接池框架集成,如集成c3p0,dbcp等等
5. 支持 `@Query` 查询,使用 `@Condition`,可实现动态 `where` 条件查询.
6. 支持查询结果集以JSON类型返回
7. 拥有非常优雅的`Page`(分页)设计
8. 支持`AOP`,注入拦截器只需要标识几个简单的注解,如: `@Before` , `@After`
9. 使用`@Source`可实现动态适配数据源.这个特性特别适合多租户系统中要求数据库彼此隔离其结构相同的场景里
10. 支持`@QueryByNamed`命名式查询,SQL动态模板.

## 运行环境要求
jdk1.8+

## 配置文件
### jdbc-config.xml
用来配置支持jdbc. **注意**:如果采用连接池,该配置文件可以不要.

```xml
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
        <property name="databaseName">dbname</property>                <!-- 数据库的名称 -->
        <property name="user">username</property>                      <!-- 数据库用户名称 -->
        <property name="password">userpasswd</property>                <!-- 数据库用户的密码 --> 
        <property name="portNumber">3306</property>                    <!-- 端口 -->
        <property name="serverName">192.168.1.1</property>             <!-- 数据库主机地址 -->
    </named-config>
</jdbc-config>
```

### c3p0-config.xml
完全支持c3p0官方配置,详情配置请参照c3p0官网的说明.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
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
{
  "scope":[
		    // config目前支持的可选值有"jdbc","c3p0"
		    {
		        "config": "c3p0",            // 表示由c3p0负责提供数据源
		        "dataSourceName": "xk-c3p0", // 数据源的名称
		        "basePackages": [            // 该数据源的作用范围
		            "org.fastquery.example",              // 包地址
		            "org.fastquery.dao.UserInfoDBService" // 完整类名称. 
		            // 在这可以配置多个DB接口或包地址,以","号隔开
		            // 提醒:在json结构中,数组的最后一个元素的后面不能加","
		        ]
		    },
		    
		     /*
		      再配置一个数据源作用域
		     */
		     {
		        "config" : "jdbc",             // 表示由jdbc驱动负责提供数据源
		        "dataSourceName": "shtest_db", // 数据源的名称
		        "basePackages": [              // 该数据源的作用范围
		            "org.fastquery.example.DataAcquireDbService"
		             // 在这可以配置多个DB接口,以","号隔开
		        ]
		     },
		    
		     {
		        "config": "c3p0",              // 表示由c3p0负责提供数据源
		        "basePackages": [   
		             "org.fastquery.dao2.UserInfoDBService2"
		        ]
		     }
		  ],
  // 基准目录,注意: 后面记得加上 "/"
  // 该目录用来放SQL文件,需要执行SQL文件时,指定其名称就够了
  "basedir" : "/root/git/fastquery/fastquery/tmp/"
}
```
**注意**: 在fastquery.json中配置作用域,其中"dataSourceName"不是必须的,"dataSourceName"要么不指定,要指定的话那么必须正确.如果没有指定"dataSourceName",那么在调用接口的时候必须指定数据源的名称.下面的适配数据源章节会讲到.

## 一个完整的入门例子
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
    @Query("select no, name, sex from student")
    JSONArray findAll();
    @Query("select no as no,name,sex,age,dept from student")
    Student[] find();      
 }
```

- 使用DAO接口.

 **注意**:不用去实现StudentDBService接口.

```java
   // get porxy impl
   StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
   // call findAll
   JSONArray jsonArray = studentDBService.findAll();
   // call find
   Student[] students = studentDBService.find(); 
```

## 针对本文Query的由来
该项目开源后,有很多关注此项目的网友表示,"使用`@Query`语义不强,为何不用@SQL,@Select,@Insert,@Update...?". 采用Query的用意: SQL的全称是 Structured Query Language,本文的 Query 就是来源于此,因此,不要片面的认为Query就是select操作. 笔者认为,针对数据库操作的注解没有必要根据SQL的四种语言(DDL,DML,DCL,TCL)来定义,定义太多,只会增加复杂度,并且毫无必要.如果是改操作加上`@Modifying`注解,反之,都是"查",这样不更简洁实用吗? 

## 带条件查询

```java
// sql中的?1 表示对应当前方法的第1个参数
// sql中的?2 表示对应当前方法的第2个参数
//       ?N 表示对应当前方法的第N个参数
	
// 查询返回数组格式
@Query("select no as no,name,sex,age,dept from student s where s.sex=?2 and s.age > ?1")
Student[] find(Integer age,String sex);
 	
// 查询返回JSON格式
@Query("select no, name, sex from student s where s.sex=?1 and s.age > ?2")
JSONArray find(String sex,Integer age);
	
// 查询返回List Map
@Query("select no, name, sex from student s where s.sex=?1 and s.age > ?2")
List<Map<String, Object>> findBy(String sex,Integer age);

// 查询返回List 实体
@Query("select id,name,age from `userinfo` as u where u.id>?1")
List<UserInfo> findSome(Integer id);
```

**注意**: 在没有查询到数据的情况下,如果返回值是集合类型或`JSON`类型或者是数组类型.具体的值不会是`null`,而是一个空集合或空`JSON`或者是长度为0的数组.   
举例说明: 

```java
// 针对该方法,如果没有查询到数据,返回值的结果是一个长度为0的Student[]
@Query("sql statements")
Student[] find(Integer age,String sex); 

// 针对该方法,如果没有查询到数据,返回值的结果是一个空Map(非null)
@Query("sql statements")
Map<String,Object> find(Integer id);

// 针对该方法,如果没有查询到数据,返回值的结果是一个空List<Map>(非null)
@Query("sql statements")
List<Map<String, Object>> find(String sex);
```

**注意**: 查询单个字段,支持返回如下格式:
- `List<String>`
- `List<Byte>`
- `List<Short>`
- `List<Integer>`
- `List<Long>`
- `List<Float>`
- `List<Double>`
- `List<Character>`
- `List<Boolean>`  
例如: 

```java
@Query("select name from Student limit 3")
List<String> findNames(); 
```

## 动态条件查询

### 采用`Annotation`实现简单动态条件  
看到这里,可别认为`SQL`只能写在类上.所有的`SQL`还可以写入到配置文件里.  

```java
@Query("select no, name, sex from Student #{#where} order by age desc")
// 增加若干个条件
@Condition("no like ?1")                            // ?1的值,如果是null, 该行条件将不参与运算
@Condition("and name like ?2")                      // 参数 ?2,如果接收到的值为null,该条件不参与运算
// 通过 ignoreNull=false 开启条件值即使是null也参与运算
@Condition(value = "and age > ?3",ignoreNull=false) // ?3接收到的值若为null,该条件将保留
@Condition("and name not like ?4") 
@Condition("or age between ?5 and ?6")
Student[] findAllStudent(... args ...);
```

**注意**:  
- 如果参数是`String`类型,值若为`null`或""(空字符串),在默认情况下,都会使条件移除
- `ignoreNull=false` : 参数值即便为null,条件也参与
- `ignoreEmpty=false` : 参数值即使为"",条件也保留

当然,实现动态`SQL`,`FastQuery`还提供了另一种方案:采用`@QueryByNamed`(命名式查询),将`SQL`写入到模板文件中,并允许在模板文件里做复杂的逻辑判断,相当灵活.下面章节有详细描述.

## count

统计查询行数
```java
@Query("select count(no) from student")
long count();
```

## exists

判断是否存在
```java
@Query("select no from student s where s.no=?1")
boolean exists(String no);
```

## 改操作
```java
// 返回修改之后所影响的行数
@Query("update student s set s.age=?3,s.name=?2 where  s.no=?1")
@Modifying
int update(String no,String name,int age); 

// 改成功了返回true,反之,false
@Modifying
@Query("delete from `userinfo` where id=?1")
boolean deleteUserinfoById(int id);

// 以实体bean格式,返回当前保存的数据
@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
@Modifying(table="student",id="no")
// 注意: // 注意: student的主键是字符串,因此不会自增长,在此处需要用@Id标识哪个就是主键字段
Student addStudent(@Id String no,String name,String sex,int age,String dept);
	
// 以Map格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Map<String, Object> addUserInfo(String name,Integer age);

// 以JSON格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
JSONObject saveUserInfo2(String name,Integer age);

// 返回当前保存的数据的主键信息
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Primarykey saveUserInfo(String name,Integer age);

```

## QueryRepository的内置方法
凡是继承`QueryRepository`的接口,都可以使用它的方法,并且不用写实现类.   
- `<E> E save(E entity);` 保存一个实体,返回保存成功之后的实体(返回的实体包含有主键)
- `<E> E update(E entity);` 更新一个实体,返回更新成功之后的实体.注意:实体的成员变量如果是null,将不会参与改运算
- `<E> E saveOrUpdate(E entity);` 不存在就保存,反之更新(前提条件:这个实体必须包含有主键值).
- `int update(Object entity,String where);` 更新实体时,自定义条件(有时候不一定是根据主键来修改),返回影响行数

举例说明:  
先准备一个实体  

```java
public class UserInfo {
	@Id
	private Integer id;
	private String name;
	private Integer age;
    // getter /setter 省略...	
}
```

保存实体,更新实体,保存或更新实体示例如下:  
``` java
	UserInfo u1 = new UserInfo(36,"Jsxxv", 23);
	
	// 保存实体
	studentDBService.save(u1)
	
	Integer id = 36;
	String name = "Jsxxv";
	Integer age = null;
	UserInfo u2 = new UserInfo(id,name,age);
	// age是null值, age就不会参与修改运算了.
	studentDBService.update(u2); // 更新语句为: update UserInfo set name = ? where id = ?
	
	// 保存或更新实体
	studentDBService.saveOrUpdate(u1);
```

使用update时,同时自定义条件的例子:

```java
Integer id = 1;
String name = "好哇瓦";
Integer age = 3;
UserInfo entity = new UserInfo(id,name,age);
// 会解析成:update `UserInfo` set `id`=?, `age`=? where name = ?
int effect = studentDBService.update(entity,"name = :name");
// 断言: 影响的行数大于0行
assertThat(effect, greaterThan(0));

// 不想让id字段参与改运算
entity.setId(null);
// 会解析成:update `UserInfo` set `age`=? where name = ?
effect = studentDBService.update(entity,"name = :name");
assertThat(effect, greaterThan(0));
```

更多内置方法,请参阅fastquery javadoc.  


## @Transactional

```java
// 将三条改操作纳入到一个事务中.
@Transactional
@Modifying
@Query("update `userinfo` set `name`=?1 where id=?3")
@Query("update `userinfo` set `age`=?2 where id=?3")
// 把主键id修改为1,目前主键id=1是存在的.这行会报错.那么前两行所做的操作全部失效.
@Query("update `userinfo` set `id`=1 where `id`=?3")
int updateBatch(String name,Integer age,Integer id);
// 注意: 
// 1).返回值如果是int类型,表示这个事务成功提交后所有改操作所影响的行数总和.
// 2).返回值如果是int[]类型,表示这个事务成功提交后,每个最小修改单元所影响行数的集合.
//    举例说明: 若有个事务T,它里面有3条改操作,分别叫U1,U2,U3. T成功提交后,U1,U2,U3所影响的数据行数分别为N1,N2,N3.
//    则: 返回值为: new int[]{N1,N2,N3}
```

## @Param参数模板

**SQL中的变量用命名式**
 
```java
@Query("select name,age from UserInfo u where u.name = :name or u.age = :age")
UserInfo[] findUserInfoByNameOrAge(@Param("name") String name, @Param("age")Integer age);
```

其中`:name`对应`@Param("name")`所指定的方法变量值;`:age`对应`@Param("age")`所指定的方法变量值.当然SQL中的变量也可以用`?N`(N={正整数})的形式来表达,且不用标识`@Param`.  
如:`select name,age from UserInfo u where u.name = :name or u.age = :age`以防SQL注入问题,在执行语句之前,最终会编译成`select name,age from UserInfo u where u.name=? or u.age=?`


**SQL中的变量采用${name}表达式**  
实现原样替换.不过请注意避免SQL注入问题.   

```java
@Query("select * from `userinfo` where ${one} ${orderby}")
UserInfo findUserInfo(@Param("orderby") String orderby, @Param("one") int i);
// String orderby 这个形参接受到的值会原样取代掉 "${orderby}", orderby 如果接受到的值为null,那么${orderby}默认为""
// int i 接受到的值会取代掉 "${one}"

// 假设: orderby的值为: "order by age desc", i的值为:1
// 则: 最终的SQL为: "select * from `userinfo` where 1 order by age desc"
```

**SQL IN**  
IN的值是变量时:

```java
@Query("select id,name,age from UserInfo where id in (${ids})")
UserInfo[] findByIds(@Param("ids") int[] ids);
```

### 采用${name}时请注意: 
- 传递null值,模板变量默认取""
- 参数模板仅仅用来辅助开发者构建SQL语句
- 请堤防使用不当,引发SQL注入问题
- 请避免模板参数的值完全来源于用户层的输入
- 请确保参数值可控.  

通过`defaultVal`属性指定:若参数接受到null值,应该采用的默认值(该属性不是必须的,默认为"").例如:

```java
@Query("select * from `userinfo` ${orderby}")
// orderby 若为null, 那么 ${orderby}的值,就取defaultVal的值
JSONArray findUserInfo(@Param(value="orderby",defaultVal="order by age desc") String orderby);
```

## @QueryByNamed命名式查询
就是把`SQL`语句写在配置文件里(在配置文件中可以进行逻辑判断),然后用`@QueryByNamed`绑定配置文件中的id值,以便引用到解析后的`SQL`.       
配置文件的命名格式: `类的长名称(包含包地址).queries.xml`,每个类文件对应一个配置文件,请放到`classpath`目录下.  
配置文件里的SQL代码段,会被**Velocity**的模板引擎所渲染,因此,很方便写出复杂的动态SQL语句.    
例如: `org.fastquery.dao.QueryByNamedDBExample.queries.xml`  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<queries>
	<query id="findUserInfoAll">
		select id,name,age from UserInfo
	</query>

	<query id="findUserInfoOne">
		<value>
			## :id 最终会替换成 ?
			## ${id} 不会替换还成"?",引用的是参数源值
			select id,name,age from UserInfo where id = :id
		</value>
	</query>

	<query id="findUserInfoByNameAndAge">
		<value>
			select id,name,age from UserInfo where 1 #{#condition}
		</value>

		<parts>
			<part name="condition">
			   <![CDATA[
				#if(${name})
				and name = :name
				#end

				#if(${age})
				and age = :age
				#end
			    ]]>
			</part>
		</parts>
	</query>
</queries>
```

假如您在 XML 文档中放置了类似 "<" 或 "&" 字符,那么这个文档会产生一个错误,这是因为 XML 解析器会把它解释为新元素的开始.为了避免此类错误.可以将SQL代码片段定义为CDATA.CDATA中的所有内容都会被 XML 解析器忽略.CDATA 部分由`<![CDATA[` 开始,由 `]]>`结束.   
若不用CDATA,那么有些字符必须采用**命名实体**的方式引入. 在 XML 中有 5 个预定义的实体引用:

| 字符 | 命名实体 | 说明 |
|:-----:|:----:|:----:|
|  <   | &amp;lt;  | 小于 |
|  >   | &amp;gt;  | 大于 |
|  &   | &amp;amp; | 和号 |
|  '   | &amp;apos;| 省略号|
|  "   | &amp;quot;| 引号 |

如果想把一些公用的SQL代码片段提取出来,以便重用,通过定义`<parts>`元素(零件集)就可以做到. 在`<value>`,`<countQuery>`元素中,可以通过`#{#name}`表达式引用到名称相匹配的零件.如:`#{#condition}`表示引用name="condition"的零件. 

```java
public interface QueryByNamedDBExample extends QueryRepository {

	// 从该类的配置文件里寻找id="findUserInfoAll"节点,然后绑定其SQL代码段
	@QueryByNamed("findUserInfoAll")
	JSONArray findUserInfoAll();
	
	@QueryByNamed("findUserInfoOne")
	UserInfo findUserInfoOne(@Param("id")Integer id);
	
	@QueryByNamed("findUserInfoByNameAndAge")
	JSONArray findUserInfoByNameAndAge(@Param("name") String name, @Param("age")Integer age);
}
```

当然,采用`@QueryByNamed`同样适合于改操作,例如:

```java
@Modifying
@QueryByNamed("updateUserInfoById")
int updateUserInfoById(@Param("id") int id,@Param("name") String name,@Param("age") int age);
```

对应的SQL模板配置

```xml
<query id="updateUserInfoById">
    ## 在这里支持velocity语法
	update UserInfo set name = :name,age = :age where id = :id
</query>
```

**注意**: `$name`和`:name`这两种表达式的主要区别是——`$name`表示引用的是参数源值,用于在模板中做逻辑判断,而`:name`用于标记参数位,解析SQL时会自动替换成`?`号.

## 处理异常

捕获和处理`Repository`实例在运行期抛出的异常.   
例如: 捕获`UserInfoDBService`中的`updateBatch`在运行期间可能抛出的异常.

```java
// 获取 Repository
UserInfoDBService udb = FQuery.getRepository(UserInfoDBService.class);

try {
	int effect = udb.updateBatch("小不点", 6, 2);
} catch (RepositoryException e) {
	// Handle exceptional condition
	// TODO ... ...
}
```

## 分页

- 通过`@QueryByNamed`实现分页

```xml
<query id="findPage">
	<!-- 查询主体语句 -->
	<value>
		select no, name, sex from Student #{#condition} #{#order}
	</value>

	<!-- 求和语句 -->
	<countQuery>
		select count(no) from Student #{#condition}
	</countQuery>

	<!-- 定义零件集,他们可以被value,countQuery节点引用,以达到复用的效果 -->
	<parts>
		<part name="condition">
	       <![CDATA[
		    <where>
			#if($no)
			no like :no
			#end

			#if($name)
			or name like :name
			#end

			#if($age)
			or age > :age
			#end
	        </where>
	        ]]>
		</part>

		<part name="order">
			order by age desc
		</part>
	</parts>
</query>
```

### 注意: 
- `{#limit}`是分页模板的内置零件,表示分页区间. `#{#limit}`默认是放在尾部,在符合`SQL`语法的前提下也可以把它放在`SQL`语句中的其他地方
- 动态条件部分若用`<where>`元素进行包裹,会自动处理好条件连接符问题(避免出现where紧接`or`或`and`)
- `<value>`和`<countQuery>`节点引用的零件中已经包含有`<where>`元素,那么该节点中禁止出现where字符串

DB接口:

```java
@QueryByNamed("findPage") // 引用id为"findPage"的分页模板
Page<Student> findPage(Pageable pageable, @Param("name") String name,@Param("age") Integer age);
```

- 通过@Query实现分页

```java
public interface UserInfoDBService extends QueryRepository {

     // Pageable 用做描述当前页的索引和每页条数.
    
	// countField : 明确指定用来统计总行数的字段,count(countField)中的countField默认值是"id"
	@Query(value="select id,name,age from `userinfo` where 1",countField="id")
	Page<Map<String, Object>> findAll(Pageable pageable);
	
	// 如果没有指定求和语句,那么由fastquery分析出最优的求和语句
	@Query("select id,name,age from `userinfo` #{#where}")
	@Condition("age > ?1")     // 若age的值传递null,该条件将不参与运算
	@Condition("and id < ?2")  // 若id的值传递null,该条件将不参与运算
	Page<UserInfo> find(Integer age,Integer id,Pageable pageable);
	
	// countQuery : 指定自定义求和语句
	@Query(value = "select id,name,age from `userinfo` #{#where}", 
	       countQuery = "select count(id) from `userinfo` #{#where}")
	@Condition("age > ?1")        // 若age的值传递null,该条件将不参与运算
	@Condition("and id < ?2")     // 若id的值传递null,该条件将不参与运算
	Page<UserInfo> findSome(Integer age,Integer id,Pageable pageable);
}
```

### @PageIndex和@PageSize
`@PageIndex` 用来指定当前页索引   
`@PageSize`  用来指定当前页应该显示多少条数据   
**注意**: 该注解组合不能和`Pageable`一起使用  
例如:

```java
@NotCount // 分页不统计总行数
@Query(value = "select id,name,age from `userinfo`")
Page<Map<String,Object>> findSome(Integer age, Integer id,@PageIndex int pageIndex, @PageSize int pageSize);
```


### 使用分页     
`Page`是分页的抽象.通过它可以获取分页中的各种属性. 并且开发者不用去实现.

```java
int p = 1;    // 指定访问的是第几页
int size = 3; // 设定每一页最多显示几条记录
Integer age=10,id = 50;
Pageable pageable = new PageableImpl(p, size);
Page<UserInfo> page  = userInfoDBService.findSome(age, id,pageable);
List<UserInfo> userInfos = page.getContent(); // 获取这页的数据
Slice slice = page.getNextPageable();         // 下一页
int number = page.getNumber();                // 当前页数(当前是第几页)
// 更多 page.? 就不赘述了.
``` 

`Page`转换成`JSON`后的结构如下:

```js
{
	"content":[                  // 这页的数据
		{
			"name":"查尔斯·巴贝奇","id":2,"year":1792
		},
		{
			"name":"约翰·冯·诺依曼","id":3,"year":1903
		},
		{                     
			"name":"阿兰·麦席森·图灵","id":1,"year":1912
		},
		{
			"name":"约翰·麦卡锡","id":4,"year":1927
		},
		{
			"name":"丹尼斯·里奇","id":5,"year":1941
		},
		{
			"name":"蒂姆·伯纳斯·李","id":6,"year":1955
		}
	],
    "first": true,           	// 是否是第一页
    "hasContent": true,      	// 这页是否有数据
    "hasNext": true,         	// 是否有下一页
    "hasPrevious": false,    	// 是否有上一页
    "last": false,           	// 是否是最后一页
    "nextPageable": {        	// 下一页的基本属性
        "number": 1,         	// 定位的页码
        "size": 15           	// 每页多少条数据
    },
    "number": 1,             	// 当前页码,从1开始
    "numberOfElements": 6,  	// 当前页的真实记录行数
    "previousPageable": {    	// 上一页的基本属性
        "number": 0,         	// 定位的页码
        "size": 15           	// 每页多少条数据
    },
    "size": 15,              	// 每页行数
    "totalElements": 188,    	// 总行数
    "totalPages": 13         	// 总页数
}
```

### 注意:
- 如果在分页函数上标识`@NotCount`,表示在分页中不统计总行数.那么分页对象中的`totalElements`的值为-1L,`totalPages`为-1.其他属性都有效并且真实.    
- 如果明确指定不统计行数,那么设置`countField`和`countQuery`就会变得无意义.    
- `#{#limit}`不仅能使用在 XML 文件里,也可以使用在`@Query`里,无特殊要求,建议不要指定`#{#limit}`.

## 执行SQL文件
```java
String sqlName = "update.sql";
String output = "out.txt";
studentDBService.executeBatch(sqlName, output);
```

- sqlName 基准目录下的SQL文件名称. 注意: 基准目录在fastquery.json里配置
- output 指定执行SQL后的输出将放在哪个文件里. 注意: 会在基准目录里寻找output文件

## 动态适配数据源
### 创建数据源
如果您想在项目运行期间动态创建一个新数据源,那么请使用`FQuery.createDataSource`.

```java
// 数据源名称
String dataSourceName = "xk1";

// 连接池配置
Properties properties = new Properties();
properties.setProperty("driverClass", "com.mysql.jdbc.Driver");
properties.setProperty("jdbcUrl", "jdbc:mysql://192.168.8.10:3306/xk1");
properties.setProperty("user", "xk1");
properties.setProperty("password", "abc1");

// 创建数据源
FQuery.createDataSource(dataSourceName, properties);
```   

### 适配数据源
使用`@Source`动态适配当前`Repository`的方法应该采用哪个数据源. 显然这个功能很有用.      
在多租户系统中,数据库彼此隔离,表结构一样.那么使用这个特性是非常方便的.    
**注意:** `@Source`如果标识在参数前面,那么该参数只能是字符串类型.

```java
@Query("select id,name,age from `userinfo` as u where u.age>?1")
Map<String, Object> findOne(Integer age,@Source String dataSourceName);
```

### 适配数据源的优先级
如果在fastquery.json文件里明确指定了数据源的作用域,同时接口函数也存在`@Source`,那么以`@Source`指定的数据源优先,其次是配置文件.

## @Before拦截器
在执行方法之前拦截  
- 准备一个BeforeFilter

```java
 /**
  * @author xixifeng (fastquery@126.com)
  */
 public class MyBeforeFilter1 extends BeforeFilter<Repository> {

 	@Override
 	public void doFilter(Repository repository, Method method, Object[] args) {
 	
 		// repository: 当前拦截到的实例
 		// method: 当前拦截到的方法
 		// args: 当前传递进来的参数值,args[N]表示第N个参数,从第0开始计数.
 		
 		// this.abortWith(returnVal); // 中断拦截器,并指定返回值
 		// 中断后立马返回,针对当前方法后面的所有Filter将不会执行
		
 	}
 }
```

- 注入Filter

```java
// 可以同时标识多个@Before
@Before(MyBeforeFilter1.class)
@Before(MyBeforeFilter2.class)
@Before(MyBeforeFilter3.class)
public interface StudentDBService extends QueryRepository {
   // some code ... ...
}
```

## @After拦截器
在执行方法之后,即将返回执行结果之前拦截  
```java
/**
 * @author xixifeng (fastquery@126.com)
 */
public class MyAfterFilter extends AfterFilter<Repository> {

	@Override
	public Object doFilter(Repository repository, Method method, Object[] args, Object returnVal) {
		
		// repository: 当前拦截到的实例
		// method: 当前拦截到的method
		// args: 当前传递进来的参数值,args[N]表示第N个参数,从第0开始计数.
		// returnVal 即将返回的值
		
		// 在这里可以中途修改 returnVal
		
		return returnVal;
	}
}
```


```java
// 可以同时标识多个@After
@After(MyAfterFilter.class)
@After(MyAfterFilter2.class)
public interface StudentDBService extends QueryRepository {
	// some code ... ...
}
```

## 控制拦截器的作用域
若: 一个拦截器继承自`F<T>`,那么:这个拦截器的作用范围只能在`T`类或`T`的子类里.<br />
举例:
```java
// 这个拦截器的作用范围在 DataAcquireDbService里或在DataAcquireDbService子类里.
// 换言之: MyBeforeFilter3这个拦截器只能标注在DataAcquireDbService里或标注在DataAcquireDbService的子类里.
// 否则,程序不能顺利通过初始化阶段.
public class MyBeforeFilter3 extends BeforeFilter<DataAcquireDbService> { 
     // some code ... ...
}
```

### @SkipFilter
跳过当前接口绑定的所有非默认的Filter(系统默认的Filter不会跳过).<br />
举例:

```java
@SkipFilter // 标识该方法将不受“自定义Filter”的约束
@Query("select no from `course` limit 1")
String findOneCourse();
```

### 注意:
- `@Before`和`@After`不仅可以标注在接口类上,也可以标注在方法上
- 标识在类的上方:表示其拦截的作用范围是整个类的方法
- 标识在方法上:表示其拦截的作用范围是当前方法
- 一个方法的拦截器总和=它的所属类的拦截器+自己的拦截器

## FAQ
1. 类属性名称与表字段不一致时,如何映射?  
答: 为了说明这个问题先准备一个实体  

```java
public class UserInformation {
	private Integer uid;
	private String myname;
	private Integer myage;
	// getters / setters
	// ... ...
}
```

而数据库中的表字段分别是id,name,age,通过`SQL`别名的方式,可以解决类属性名称与表字段不一致的映射问题.如下:  

```java
// 把查询到的结果映射给UserInformation
@Query("select id as uid,name as myname,age as myage from UserInfo u where u.id = ?1")
UserInformation findUserInfoById(Integer id);
```

## 源码

- https://git.oschina.net/xixifeng.com/fastquery
- https://github.com/xixifeng/fastquery

## 开发环境
仅仅是建议,并不局限于此         
  IDE: eclipse          
build: maven 

## 反馈问题
https://git.oschina.net/xixifeng.com/fastquery/issues  
地球人都知道,开源中国秉承自由、开放、分享的精神,本项目每次升级之后,代码和文档手册都会在第一时间完全开源,以供大家查阅、批评、指正.笔者技术水平有限,bug或不周之处在所难免,所以,遇到有问题或更好的建议时,还请大家通过码云[issue](https://git.oschina.net/xixifeng.com/fastquery/issues)来向我们反馈.  

## 联系作者
@习习风 fastquery#126.com  
欢迎批评指正.
