<p align="center"><img src="file/logo.png" alt="FastQuery logo"></p>

### Apache Maven
```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>fastquery</artifactId>
    <version>1.0.111</version> <!-- fastquery.version -->
</dependency>
```

### Gradle/Grails
```
compile 'org.fastquery:fastquery:1.0.111'
```

# FastQuery 数据持久层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.<br />
提供少许`Annotation`,消费者只用关心注解的含义,这就使得框架的核心便于重构,便于持续良性发展.<br />

## FastQuery 主要特性如下:
1. 遵循非侵入式原则,设计优雅或简单,极易上手
2. 在项目初始化阶段采用ASM生成好字节码,因此支持编译前预处理,可最大限度减少运行期的错误,显著提升程序的强壮性
3. 支持安全查询,防止SQL注入
4. 支持与主流数据库连接池框架集成
5. 支持 `@Query` 查询,使用 `@Condition`,可实现动态 `where` 条件查询
6. 支持查询结果集以JSON类型返回
7. 拥有非常优雅的`Page`(分页)设计
8. 支持`AOP`,注入拦截器只需要标识几个简单的注解,如: `@Before` , `@After`
9. 使用`@Source`可实现动态适配数据源.这个特性特别适合多租户系统中要求数据库彼此隔离其结构相同的场景里
10. 支持`@QueryByNamed`命名式查询,SQL动态模板
11. 支持存储过程
12. 支持批量更新集合实体(根据主键,批量更新不同字段,不同内容).

## 运行环境要求
JRE 8+

## 配置文件

### 配置文件的存放位置

默认从`classpath`目录下去寻找配置文件. 配置文件的存放位置支持自定义, 如: `System.setProperty("fastquery.config.dir","/data/fastquery/configs");`, 它将会覆盖`classpath`目录里的同名配置文件.  如果项目是以jar包的形式启动,那么可以通过java命令的 `-D` 参数指定配置文件的目录, 如: `java -jar Start.jar -Dfastquery.config.dir=/data/fastquery/configs`. 

### c3p0-config.xml
完全支持c3p0官方配置,详情配置请参照c3p0官网的说明.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
    <named-config name="xk-c3p0">  
        <property name="driverClass">com.mysql.cj.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://192.168.1.1:3306/xk?useSSL=false</property>  
        <property name="user">xk</property>  
        <property name="password">abc123</property>  
        <property name="acquireIncrement">50</property>  
        <property name="initialPoolSize">100</property>  
        <property name="minPoolSize">50</property>  
        <property name="maxPoolSize">1000</property>
        <property name="maxStatements">0</property>  
        <property name="maxStatementsPerConnection">5</property>     
    </named-config>
     <!-- 可以配置多个named-config节点,多个数据源 -->
    <named-config name="name-x"> ... ... </named-config>
</c3p0-config>
```

### druid.xml
用于配置支持Druid连接池,详细配置请参照 https://github.com/alibaba/druid

```xml
<beans>
	 <bean name="xkdb1" id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" 
                                        init-method="init" destroy-method="close"> 
	     <property name="url" value="jdbc:mysql://db.fastquery.org:3305/xk" />
	     <property name="username" value="xk" />
	     <property name="password" value="abc123" />
	     <property name="filters" value="stat" />
	     <property name="maxActive" value="20" />
	     <property name="initialSize" value="1" />
	     <property name="maxWait" value="60000" />
	     <property name="minIdle" value="1" />
	     <property name="timeBetweenEvictionRunsMillis" value="60000" />
	     <property name="minEvictableIdleTimeMillis" value="300000" />
	     <property name="testWhileIdle" value="true" />
	     <property name="testOnBorrow" value="false" />
	     <property name="testOnReturn" value="false" />
	     <property name="poolPreparedStatements" value="true" />
	     <property name="maxOpenPreparedStatements" value="20" />
	 </bean>
	 <!-- 再配置一个数据源 --> 
	 <bean name="xkdb2" id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" 
                                        init-method="init" destroy-method="close"> 
	     <property name="url" value="jdbc:mysql://db.fastquery.org:3305/xk" />
	     <property name="username" value="xk" />
	     <property name="password" value="abc123" />
	 </bean>
</beans>
```

### hikari.xml
用于配置支持HikariCP连接池,详细配置选项请参照 https://github.com/brettwooldridge/HikariCP  
连接MySQL,为了得到更好的性能,推荐配置

```xml
<beans>
	<bean name="xkdb2">
		<property name="jdbcUrl" value="jdbc:mysql://192.168.1.1:3306/xk" />
		<property name="dataSource.user" value="xk" />
		<property name="dataSource.password" value="abc123" />
		<property name="dataSource.cachePrepStmts" value="true" />
		<property name="dataSource.prepStmtCacheSize" value="250" />
		<property name="dataSource.prepStmtCacheSqlLimit" value="2048" />
		<property name="dataSource.useServerPrepStmts" value="true" />
		<property name="dataSource.useLocalSessionState" value="true" />
		<property name="dataSource.rewriteBatchedStatements" value="false" />
		<property name="dataSource.cacheResultSetMetadata" value="true" />
		<property name="dataSource.cacheServerConfiguration" value="true" />
		<property name="dataSource.elideSetAutoCommits" value="true" />
		<property name="dataSource.maintainTimeStats" value="false" />
	</bean>
	<!-- 可以配置多个bean节点,提供多个数据源 -->
	<bean name="name-x"> ... ... </bean>
</beans>
```

支持多连接池共存,如,同时让Druid,HikariCP工作,并配置多个数据源.

### fastquery.json
配置数据源的作用范围

```js
// @author xixifeng (fastquery@126.com)
// 配置必须遵循标准的json语法.
{
  "scope":[
		    // config 用于指定由谁来提供数据源,如,"c3p0","druid","hikari"等等
		    {
		        "config": "c3p0",            // 表示由c3p0负责提供数据源
		        "dataSourceName": "xk-c3p0", // 数据源的名称
		        "basePackages": [            // 该数据源的作用范围
		            "org.fastquery.example",              // 包地址
		            "org.fastquery.dao.UserInfoDBService" // 完整类名称 
		            // 在这可以配置多个DB接口或包地址,以","号隔开
		            // 提醒:在json结构中,数组的最后一个元素的后面不能加","
		        ]
		    },
		    
		     /*
		      再配置一个数据源作用域
		     */
		     {
		        "config" : "mySQLDriver",      // 表示由mySQLDriver负责提供数据源
		        "dataSourceName": "shtest_db", // 数据源的名称
		        "basePackages": [              // 该数据源的作用范围
		            "org.fastquery.example.DataAcquireDbService"
		             // 在这可以配置多个DB接口,以","号隔开
		        ]
		     },
		    
		     {
		        "config": "c3p0",              // 表示由c3p0负责提供数据源
		        "basePackages": [   
		             "org.fastquery.dao2.UserInfoDB"
		        ]
		     }
		  ] 
}
```
**注意**: 在fastquery.json中配置作用域,其中"dataSourceName"不是必须的,"dataSourceName"要么不指定,要指定的话那么必须正确.如果没有指定"dataSourceName",那么在调用接口的时候必须指定数据源的名称.下面的适配数据源章节会讲到."basePackages"若配置了包地址,那么对应的数据源会作用这个包的所有类,及所有子包中的类.  

数据源的初始化是从"fastquery.json"开始的,根据从里面读到"dataSourceName"的值,取相应的配置,继而完成数据源的创建.如,创建一个名为"rex-db"的数据源:

```js   
{
    "config": "c3p0",           
    "dataSourceName": "rex-db"
}
```
在这里,"basePackages"不是必须的,该数据源可以当做是一个服务,供没有明确指定数据源的Repository使用.

## 入门例子
当看到一个例子时,切勿断章取义,多看一眼,往往会有意想不到的结果.  
- 准备一个实体

```java
 public class Student
 {
      private String no;
      private String name;
      private String sex;
      private Integer age;
      private String dept;
      // getter / setter 省略... 
 } 
```

**实体属性跟数据库映射的字段必须为包装类型,否则被忽略**. 在实体属性上标识`@Transient`,表示该字段不参与映射.   

- DAO接口

```java
 public interface StudentDBService extends org.fastquery.core.Repository {
    @Query("select no, name, sex from student")
    JSONArray findAll();
    @Query("select no,name,sex,age,dept from student")
    Student[] find();      
 }
```

- 使用DAO接口.

```java
public class StudentDBServiceTest {
	// 获取实现类
	private static StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
	@Test
	public void test() {
		// 调用 findAll 方法
		JSONArray jsonArray = studentDBService.findAll();
		// 调用 find 方法
		Student[] students = studentDBService.find(); 
	}
}
```

**注意**:不用去实现StudentDBService接口.通过`FQuery.getRepository`获取DAO接口对应的实例,虽然每次获取实例消耗的性能微乎其微可以忽略不计,但是,作为一个接口并且频繁被调用,因此,建议把获取到的实例赋值给类成员变量,最好是用`static`修饰.`FQuery.getRepository`获得的实例是唯一的,不可变的.  

一个接口不实现它的`public abstract`方法就毫无作用可言,因此,与之对应的实例对象是必须的,只不过是FastQuery内部替用户实现了.读者可能会问,这个自动生成的实例在什么时候生成? 动态生成的效率如何保持高效? 为此, 笔者做了相当多的功课:让所有DB实现类在项目初始化阶段进行,并且尽可能地对接口方法做静态分析,把有可能在运行期发生的错误尽最大努力提升到初始化阶段,生成代码前会检测SQL绑定是否合法有效、检测方法返回值是否符合常规、方法的参数是否满足模版的调用、是否正确地使用了分页...诸如此类问题.这些潜在问题一旦暴露,项目都启动不起来,错误信息将在开发阶段详细输出,并且必须干掉这些本该在生产环境才发生的错误,才能继续开发,迫使开发者必须朝正确的道路走,或者说框架的优良设计其核心理念引导开发者不得不写出稳健的程式.项目进入运行期,大量的校验就没必要写了,从而最大限度保证快速执行.  

唯一的出路,只能引用接口,这就使得开发者编程起来不得不简单,因为面对的是一个高度抽象的模型,而不必去考虑细枝末节.接口可以看成是一个能解析SQL并能自动执行的模型,方法的参数、绑定的模版和标识的注解无不是为了实现一个目的:执行SQL,返回结果.  

这种不得不面向接口的编程风格,有很多好处:耦合度趋向0,天然就是**对修改封闭,对扩展开放**,不管是应用层维护还是对框架增加新特性,这些都变得特别容易.隐藏实现,可以减少bug或者是能消灭bug,就如**解决问题,不如消灭问题**一般,解决问题的造诣远远落后于消灭问题,原因在于问题被解决后,不能证明另一个潜在问题在解决代码中不再出现,显然消灭问题更胜一筹.应用层只用写声明抽象方法和标识注解,试问bug从何而来?该框架最大的优良之处就是让开发者没办法去制造bug,至少说很难搞出问题来.不得不简便,没法造bug,显然是该项目所追求的核心目标之一.  

不管用不用这个项目,笔者期望读者至少能快速地检阅一下该文档,有很多设计是众多同类框架所不具备的,希望读者从中得到正面启发或反面启发,哪怕一点点,都会使你收益.  

## 针对本文@Query的由来
该项目开源后,有些习惯于繁杂编码的开发者表示,"*使用`@Query`语义不强,为何不用@SQL,@Select,@Insert,@Update...?*". SQL的全称是 Structured Query Language,本文的 `@Query` 就是来源于此. `@Query`只作为运行SQL的载体,要做什么事情由SQL自己决定.因此,不要片面的认为Query就是select操作. 针对数据库操作的注解没有必要根据SQL的四种语言(DDL,DML,DCL,TCL)来定义,定义太多,只会增加复杂度,并且毫无必要,如果是改操作加上`@Modifying`注解,反之,都是"查",这样不更简洁实用吗? 诸如此类:`@Insert("insert into table (name) values('Sir.Xi')")`,`@Select("select * from table")`,SQL的表达能力还不够吗? 就不觉得多出`@insert`和`@Select`有拖泥带水之嫌? SQL的语义本身就很强,甚至连`@Query`和`@Modifying`都略显多余,但是毕竟SQL需要有一个载体和一个大致的分类.

## 带条件查询

```java
// sql中的?1 表示对应当前方法的第1个参数
// sql中的?2 表示对应当前方法的第2个参数
//       ?N 表示对应当前方法的第N个参数
	
// 查询返回数组格式
@Query("select no,name,sex,age,dept from student s where s.sex=:sex and s.age > ?1")
Student[] find(Integer age,@Param("sex")String sex);
 	
// 查询返回JSON格式
@Query("select no, name, sex from student s where s.sex=:sex and s.age > ?2")
JSONArray find(@Param("sex")String sex,Integer age);
	
// 查询返回List Map
@Query("select no, name, sex from student s where s.sex=?1 and s.age > :age")
List<Map<String, Object>> findBy(String sex,@Param("age")Integer age);

// 查询返回List 实体
@Query("select id,name,age from `userinfo` as u where u.id>?1")
List<UserInfo> findSome(@Param("id")Integer id);
```
参数较多时不建议使用问号(?)引用参数,因为它跟方法的参数顺序有关,不便维护,可以使用冒号(:)表达式,跟顺序无关, ":name" 表示引用标记有@Param("name")的那个参数.  
若返回`List<Map<String, String>>`或`Map<String, String>`,会把查询出的字段值(value)包装成字符串.   

**注意**: 在没有查询到数据的情况下,如果返回值是集合类型或`JSON`类型或者是数组类型,返回具体的值不会是`null`,而是一个空对象(empty object)集合或空对象`JSON`或者是长度为0的数组.   
使用空对象来代替返回`null`,它与有意义的对象一样,并且能避免`NullPointerException`,阻止`null`肆意传播,可以减少运行期错误.反对者一般都从性能的角度来考虑,认为`new`一个空对象替代`null`,会增加系统的开销.可是,&lt;&lt;Effective Java&gt;&gt;的作者**Josh Bloch**说,在这个级别上担心性能问题是不明智的,除非有分析表明,返回空对象来替代返回`null`正是造成性能问题的源头.细心的人可能已经发现JDK新版本的API都在努力避免返回`null`.  
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

**注意**: 查询单个字段,还支持返回如下类型:
- `List<String>`,`String[]` 或 `String`
- `List<Byte>`,`Byte[]` 或 `Byte`
- `List<Short>`,`Short[]` 或 `Short`
- `List<Integer>`,`Integer[]` 或 `Integer`
- `List<Long>`,`Long[]` 或 `Long`
- `List<Float>`,`Float[]` 或 `Float`
- `List<Double>`,`Double[]` 或 `Double`
- `List<Character>`,`Character[]` 或 `Character`
- `List<Boolean>`,`Boolean[]` 或 `Boolean`  

除了改操作或count外,查单个字段不能返回基本类型,因为:基本类型不能接受`null`值,而SQL表字段可以为`null`.
返回类型若是基本类型的包装类型,若返回null, 表示:没有查到或查到的值本身就是null.
例如: 

```java
// 查询单个字段,若没有查到,就返回空List<String>(非null)
@Query("select name from Student limit 3")
List<String> findNames(); 
```

## 类属性名称与表字段不一致时,如何映射?  
为了说明这个问题先准备一个实体  

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

## 动态条件查询

### 采用`Annotation`实现简单动态条件  
看到这里,可别认为`SQL`只能写在Annotation(注解)里.`FastQuery`还提供了另二种方案: ① 采用`@QueryByNamed`(命名式查询),将`SQL`写入到模板文件中,并允许在模板文件里做复杂的逻辑判断,相当灵活. ② 通过`QueryBuilder`构建`SQL`.下面章节有详细描述. 

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

`@Condition(value="name = ?1",ignoreNull=false)`表示`?1`接受到的值若是`null`,该条件也参与运算,最终会翻译成`name is null`.`SQL`中的`null`无法跟比较运算符(如`=`,`<`,或者`<>`)一起运算,但允许跟`is null`,`is not null`,`<=>`操作符一起运算,故,将`name = null`想表达的意思,解释成`name is null`.  
`@Condition(value="name != ?1",ignoreNull=false)` 若`?1`的值为`null`,最终会解释成`name is not null`. 

在`In`查询作为一个条件单元时,请忽略null判断,如`@Condition("or dept in(?4,?5,?6)"`其中的一个参数为`null`就将条件移除显然不太合理.

### 结构包装
有时候我们需要返回如下结构的数据：
```js
{
	"departmentId":1,
	"departmentName":"研发",
	"emps":[
		{
			"name":"小明",
			"id":1
		},
		{
			"name":"张三",
			"id":2
		},
		{
			"name":"李思",
			"id":3
		}
	]
}
```

举例说明，部门对应员工是 1：N 关系，查询某一个部门下面的员工，可以这样写：
```java
@Query("select d.id as departmentId, d.name as departmentName, emps[e.id, e.name] from `department` d left join employee e on d.id = e.departmentId where d.id = :departmentId")
Department findDepartment(@Param("departmentId") Long departmentId);
```

其中 `emps[e.id, e.name]` 是关键，`[]` 确定集合 `emps` 里的元素，`Department` 类中需要有 emps 成员属性。


### 通过JAVA脚本控制条件增减
`@Condition`中的`ignoreScript`属性可以绑定一个JAVA脚本(不是JS),根据脚本运行后的布尔结果,来决定是否保留条件项.脚本运行后的结果如果是`true`,那么就删除该条件项,反之,保留条件项,默认脚本是`false`,表示保留该条件项. 注意: 脚本执行后得到的结果必须是布尔类型,否则,项目都启动不起来.  
举例:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition("age > :age")
@Condition(value="and name like :name",ignoreScript=":age > 18 && :name!=null && :name.contains(\"Rex\")")
Page<UserInfo> find(@Param("age")int age,@Param("name")String name,Pageable pageable);
```
其中, `:age`引用的是`@Param("age")int age`的实参值.`:name`是`@Param("name")String name`的实参值.这个脚本要表达的意思不言而喻. 不过脚本的解析能力还不能自动**拆箱**(unboxing),需要调用拆箱方法,在这里age变量如果是`Integer`类型,要想如上脚本能正确编译,必须这么做: `":age.intValue() > 18 && :name!=null && :name.contains(\"Rex\")"`, 请留意住`:age.intValue()`. 其他包装类型`Short`, `Long`, `Byte`, `Boolean`, `Character`, `Float`, `Double` 以此类推.  

### 什么是JAVA脚本?
在这里将一段承载着程序的字符串称之为JAVA脚本.脚本在初始化阶段被解释成能在`JVM`里运行的字节码,在脚本里能通过`:expression`(冒号表达式)获取当前方法在运行时所接受到的所有参数,引用的参数可以是一个复杂对象,完全可以把`:expression`当成是对象的引用句柄.虽然允许把脚本写得很长,更支持写出较为复杂的逻辑,但是,不建议这么做,因为那样可读性极差,不便迭代维护.做再庞杂的程序,都应该拆分成若干小而简单的功能,然后以优良的设计将其串联起来.`FastQuery`自始自终会遵守简单,严谨,清晰的编程风格.

### @Condition 中的 if...else
条件是否保留可以通过`if`条件来确定,`if`绑定的JAVA脚本运行后的结果若为`true`就保留该`Condition`,反之就取`else`的捆绑值,`else`如果没有值或者是空值,表示移除该`Condition`.  
举例:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition(value="age > :age",if$=":age < 18", else$="name = :name")
Page<UserInfo> findPage(@Param("age")int age,@Param("name")String name,Pageable pageable);
```
如果`age`的实参值小于18,则,保留该`Condition`,否则,该`Condition`的值变为`name = :name`.当然,`else`不是必须的,如果`if`运算为假,直接删除该行`SQL`条件.

### 自定义类控制条件增减
决定一个条件是否参与运算,有时候需要根据多个不同的参数进行某种计算来决定, 并且这种计算逻辑用JAVA脚本(非JS)难以表达或者不太乐意让JAVA脚本登场. 那么就使用`@Condition`中的`ignore`选项,指定一个类,它叫`Judge`,是一个裁判员,条件是否去除的决定权可以理所当然地委托给自定义的`Judge`类来处理.   
举例: 若:年龄大于18及姓名不为空且包含"Rex".则,剔除条件`and name like :name`.  
定制一个决定条件存活的类,需要遵循一些约定: 继承`org.fastquery.where.Judge`,当完成这一步,IDE就会提示开发者必须实现ignore方法, 否则,面对的是红叉. 这样的设计可以减少犯错的可能. 当`ignore`方法最终返回`true`时,则,删除相对应的条件;当最后返回`false`时,则,保留条件.

```java
public class LikeNameJudge extends Judge {
	@Override
	public boolean ignore() {
		// 获取方法中名称为"age"的参数值
		int age = this.getParameter("age", int.class);
		// 获取方法中名称为"name"的参数值
		String name = this.getParameter("name", String.class);
		return age > 18 && name!=null && name.contains("Rex");
	}
}
```
在`LikeNameJudge`的`this`范围内可以获得当前DB方法的所有实参.这些参数都有资格决定条件的存亡.   
指定 `LikeNameJudge`:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition("age > :age")
@Condition(value="and name like :name",ignore=LikeNameJudge.class)
Page<UserInfo> find(@Param("age")int age,@Param("name")String name,Pageable pageable);
```

其中,`ignore`选项默认指定`DefaultJudge`,它是一个无所事事的裁判员,当它是空气好了.

若`@Condition`的值使用了`${表达式}`,`$表达式`,不管方法的参数传递了什么都不会使条件移除,因为`$`表达式(或称之为EL表达式)仅作为简单模版使用,传null,默认会替换为""(空字符串).举例:

```java
@Query("select * from `userinfo` #{#where}")
@Condition("age between $age1 and ${age2}")
List<Map<String, Object>> between(@Param("age1") Integer age1,@Param("age2") Integer age2);	
```
该例中`@Condition`使用到了`$`表达式,`$age1`,`${age2}`仅作为模板替换,age1为null,即便设置`ignoreNull=true`也不会影响条件的增减.**总之,`$` 表达式不会动摇条件的存在**.  

单个`@Condition`针对出现多个`SQL`参数的情形,如 `@Condition("or age between ?5 and ?6")` 或 `@Condition("or age between :age1 and :age2")` 参数 `?5`、`?6`、`:age1`、 `:age2`中的任意一个为`null`都会导致该行条件移除.

## 类型映射

不是强类型映射，只要 Java 类型的存储空间 `>=` `SQL`类型的存储空间。换言之，只要 Java 类型装得下就行，反之，不行。

| Java      | SQL                               |
| --------- | --------------------------------- |
| Boolean   | `BIT(1)`, `TINYINT(1)`, `CHAR(1)` |
| Byte      | TINYINT                           |
| Short     | SMALLINT                          |
| Integer   | INT                               |
| Long      | BIGINT                            |
| Float     | FLOAT                             |
| Double    | DOUBLE                            |
| Character | CHAR(1)                           |
| Enum      | ENUM                              |
| EnumSet   | SET                               |
| String    | 所有类型                          |


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

// 以JSON格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
JSONObject saveUserInfo2(String name,Integer age);

// 返回当前保存的数据的主键信息
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Primarykey saveUserInfo(String name,Integer age);
```

新增一条记录,返回实体,可以通过`@Modifying`中的`selectFields`配置项明确指定待查询的字段. 如:

```java
// 以Map格式,返回当前保存的数据
@Modifying(id="id",table="userinfo",selectFields="name,age")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Map<String, Object> addUserInfo(String name,Integer age);
```

其中,`selectFields` 默认是 **<code>\*</code>**,字段与字段之间请用英文逗号隔开.  

**注意**:
- 改操作返回int类型:表示影响的行数,没有找到可以修改的,那么影响行数为0,并不能视为改失败了
- 改操作返回boolean类型:表示是否改正确,依据是,影响行数若大于或等于0都会返回true,反之,返回false

## Annotation
针对FastQuery中的所有注解,做个说明:

| Annotation | 作用 |
|:---|:---|
|`@Id`|用来标识表主键|
|`@Table`|用来指定表名称|
|`@Modifying`|标识改操作|
|`@Param`|标识参数名称,便于运行期获取|
|`@Query`|标识查询语句|
|`@QueryByNamed`|标识根据命名式查询(语句放在配置文件中)|
|`@Source`|标识用来适配数据源的参数|
|`@Transactional`|事务|
|`@Transient`|标识实体中的属性是临时的(例如:save对象时,该属性不存储到数据库里)|
|`@NotCount`|标识分页中不统计总行数|
|`@PageIndex`|标识页索引|
|`@PageSize`|标识页行数|
|`@Condition`|标识条件单元|
|`@Set`|标识设置字段单元|
|`@Before`|标识函数执行前|
|`@After`|标识函数执行后|
|`@SkipFilter`|标识跳过拦截器|

## QueryRepository的内置方法
凡是继承`QueryRepository`的接口,都可以使用它的方法,并且不用写实现类.

| 方法 | 描述 |
|:---|:---|
| `<E> E find(Class<E> entityClass,long id)` | 根据主键查询实体 |
| `<E> int insert(E entity)` | 插入一个实体(主键字段的值若为null,那么该字段将不参与运算),返回影响行数 |
| `<B> int save(boolean ignoreRepeat,Collection<B> entities)` | 保存一个集合实体,是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录 |
| `int saveArray(boolean ignoreRepeat,Object...entities)` | 保存一个可变数组实体,是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录 |
| `BigInteger saveToId(Object entity)` | 保存实体后,返回主键值.**注意**:主键类型必须为数字且自增长,不支持联合主键 |
| `<E> E save(E entity)` | 保存实体后,返回实体 |
| `<E> int executeUpdate(E entity)` | 更新一个实体,返回影响行数.**注意**:实体的成员属性如果是null,那么该属性将不会参与改运算 |
| `<E> E update(E entity)` | 更新一个实体,返回被更新的实体 |
| `<E> int executeSaveOrUpdate(E entity)` | 不存在就保存,反之更新(前提条件:这个实体必须包含主键字段,主键值若是null,直接存),返回影响行数 |
| `<E> E saveOrUpdate(E entity)` | 不存在就保存,反之更新(前提条件:这个实体必须包含主键字段,主键值若是null,直接存),返回被更新的实体或返回已存储的实体 |
| `int update(Object entity,String where)` | 更新实体时,自定义条件(有时候不一定是根据主键来修改),若给where传递null或"",默认按照主键修改,返回影响行数 |
| `<E> int update(Collection<E> entities)` | 更新集合实体,成员属性如果是null,那么该属性将不会参与改运算,每个实体必须包含主键 |
| `int delete(String tableName,String primaryKeyName,long id)` | 根据主键删除实体,返回影响行数 |
| `int[] executeBatch(String sqlName)` | 根据指定的SQL文件名称或绝对路径,执行批量操作SQL语句,返回int[],数组中的每个数对应一条SQL语句执行后所影响的行数 |
| `int tx(Supplier<Integer> fun)` | 事务函数.fun的返回值等于tx的返回值.fun返回null,-1或向上抛异常,tx会被回滚,并返回-1 |
| `<E> long count(E entity)` | 根据指定的条件统计总记录数，实体属性若为 null 值，则，该属性不参与运算，反之，参与 and 运算 |

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

使用QueryRepository的内置函数,必须要继承它:

```java
public interface StudentDBService extends QueryRepository {
   ... ...
}
```

**提醒**: 继承`Repository`适合应用于不使用内置函数的场景,显然更加轻量级.   

保存实体,更新实体,保存或更新实体示例如下:

``` java
UserInfo u1 = new UserInfo(36,"Dick", 23);

// 保存实体
studentDBService.save(u1)

Integer id = 36;
String name = "Dick";
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
String name = "可馨";
Integer age = 3;
UserInfo entity = new UserInfo(id,name,age);
// 会解析成:update `UserInfo` set `id`=?, `age`=? where name = ?
int effect = studentDBService.update(entity,"name = :name");
// 断言: 影响的行数大于0行
assertThat(effect, greaterThan(0));

// 不想让id字段参与改运算,那么就把它的值设置为null
entity.setId(null);
// 会解析成:update `UserInfo` set `age`=? where name = ?
effect = studentDBService.update(entity,"name = :name");
assertThat(effect, greaterThan(0));
```

批量更新(update),如果是把多条记录更新成相同的内容,没有什么好说的.在此主要解决:批量更新不同字段,不同内容.  
举例:  
假设需求是:

- 把id=77的用户的姓名修改成"茝若",年龄修改成18
- 把id=88的用户的姓名修改成"芸兮",注意:不修改年龄
- 把id=99的用户的年龄修改成16

实现代码:

```java
// 步骤1: 准备要修改的实体
List<UserInfo> userInfos = new ArrayList<>();
userInfos.add(new UserInfo(77,"茝若", 18));
userInfos.add(new UserInfo(88,"芸兮", null));
userInfos.add(new UserInfo(99,null, 16));

// 步骤2: 批量更新
int effect = userInfoDBService.update(userInfos);
assertThat(effect, is(3));
```

最终会解释成一条SQL语句:

```sql
update UserInfo set
  name = case id
  when 77 then '茝若'
  when 88 then '芸兮'
  else name end 
  ,
  age = case id
  when 77 then '18'
  when 99 then '16'
  else age end
where id in (77, 88, 99)
```

## @Set 实现动态修改不同字段

往往只需要修改表的中的个别字段: A处需要修改table.x字段,B处要修改table.y字段,C处同时改x,y字段,. 设计`@Set`就是为了满足诸如此类的需求. 根据传递参数的不同动态地增减需要set的字段,让一条SQL尽可能地满足多个要求.

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?5")
@Set("`name` = ?1") // ?1 若是 null 或是 "" , 则, 该行set移除
@Set("`credit` = ?2")
@Set("`semester` = ?3")
@Set("`period` = ?4")
int updateCourse(String name,Integer credit, Integer semester, Integer period, String no);
```

`#{#sets}` 用于引用设置选项. `@Set(value="name = ?1" , ignoreNull=true , ignoreEmpty=true)` 中的可选配置项,顾名思义.    

方法上的所有`@Set`有可能全部被移除,那么就会得到一个错误的SQL`update Course set where no = ?5`,避免此错误有两个方法: 1). 加一条不含有SQL参数的`@set`,如: `@set("name = name")`,它永远不会被删除,并且不会对原有数据造成任何影响; 2).调用方法前对参数做校验,以排除因为参数导致全部`@set`被丢弃的可能.  

单个`@Set`针对出现多个`SQL`参数的情形,如 `@Set("name = ?1","credit = ?2")` 或 `@Set("name = :name","credit = :credit")` 参数 `?1`、`?2`、`:name`、 `:credit`中的任意一个为`null`都会导致该行设置项被移除.  

### 通过JAVA脚本控制设置项增减
`@Set`中的`ignoreScript`属性可以绑定一个JAVA脚本(非JS),根据脚本运行后的布尔结果,来决定是否保留设置项.脚本运行后的结果如果是`true`,那么就删除该设置项,反之,保留设置项,默认脚本是`false`,表示保留该设置项. 注意: 脚本执行后得到的结果必须是布尔类型,否则,项目都启动不起来.  
举例:

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?3")
@Set(value="`name` = :name",
     ignoreScript=":name!=null && :name.startsWith(\"计算\") && :credit!=null && :credit.intValue() > 2")
@Set("`credit` = :credit")
int updateCourse(@Param("name") String name,@Param("credit") Integer credit,String no);
```
其中, `:credit`引用的是`@Param("credit") Integer credit`的实参值.`:name`是`@Param("name")String name`的实参值.这个脚本要表达的意思不言而喻. 不过脚本的解析能力还不能自动**拆箱**(unboxing),需要调用拆箱方法,请留意住`:credit.intValue() > 2`. 若写成`:credit > 2`是编译不了的. 其他包装类型`Short`, `Long`, `Byte`, `Boolean`, `Character`, `Float`, `Double` 以此类推.  
脚本的编译工作在项目初始化阶段完成,因此不存在性能问题.建议不要把脚本写得太长,那样会破坏可读性.

### @Set 中的 if...else
这个`SQL`设置项是否保留,可以通过`if`...`else`...来确定.`if`的表达式用`=`号与之绑定.`if`成立,则,保留当前设置项,反之,就取`else`所指定的值.当然,`else`在语法上不是必须的,若不写`else`,`if`条件不成立,则,直接删除当前`@Set`.  
举例:

```java
@Modifying
@Query("update `User` #{#sets} where id = ?3")
@Set(value="`name` = :name",if$="!:name.contains(\"root\")",else$="`name` = name")
int updateUser(@Param("name") String name,int id);
```
其中,如果`name`的值不包含"root",就保留`"name = :name"`这个设置选项,否则,设置选项为`name = name`(表示`name`的值保持原样).

### 自定义类控制设置项增减
决定一个Set项是否参与运算,可以根据多个参数进行某种计算来决定,`@Set`允许关联一个自定义的`Judge`类,作为这种计算的载体.  
举例: 若: name值的前缀是"*计算*" 并且 credit的值大于2, 则,删除`name = :name`这条设置项.  
NameJudge 类:

```java
public class NameJudge extends Judge {
	@Override
	public boolean ignore() {
		// 获取方法中名称为"name"的参数值
		String name = this.getParameter("name", String.class);
		// 获取方法中名称为"credit"的参数值
		Integer credit = this.getParameter("credit", Integer.class);
		return name.startsWith("计算") && credit!=null && credit > 2;
	}
}
```
设置项绑定 NameJudge: 

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?3")
@Set(value="`name` = :name",ignore=NameJudge.class)
@Set("`credit` = :credit")
int updateCourse(@Param("name") String name,@Param("credit") Integer credit,String no);
```
关于修改`name`的那个设置项, 有三种可能使它作废: ① name的值是null; ② name的值是""; ③ NameJudge类的ignore方法返回了`true`.

根据参数动态增减set不同字段,除了用`@Set`实现之外,别忘了还有其他几种解决办法: a.调用内置方法`int executeUpdate(E entity)`,实体的字段若是`null`值,那么,该字段将不会参与set运算; b.使用SQL模版,在里头做逻辑判断; c.采用`QueryBuilder`; d.采用`$表达式`. 开发者将会发现很难不能选择出适合的解决方式.

## 事务

### 用`@Transactional`实现简单事务

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

### 事务函数式接口
在`QueryRepository`中提供了一个内置事务函数`tx`.支持多个数据源加入到同一个事务里.

```java
int effect = userInfoDBService.tx(() -> {
	// 把需要纳入到一个事务内的改操作放入到这里来
	// update1
	// to do...
	// update2
	// return 影响行数;
});
```

以上`Lambda`表达式,`()->{}`中的`{}`里的所有操作是原子性的,要么统统成功,要么全部失败回滚.在`{}`里抛出异常或`return null`或返回-1,都会导致`{}`全体回滚并返回-1.`Lambda`表达式对**值**封闭,对**变量**开放(Lambda expressions close over values,not variables),正因为这个特性,不能在`{}`中修改外界的值,但是可以给外界的对象设置值. 

```java
... ...
Map<String, Object> map = new HashMap<>();
int sum = 0;
tx(() -> {
     sum = sum + 1; // 编译报错,不能修改sum的值(Illegal, close over values)
     map.put(K, V); // 这是允许的(Legal, open over variables)
});
```
因此,要想把`{}`中处理的数据拿出来使用,将其设置给一个外界的对象就行了. `tx`方法被回滚后会返回-1.

## @Param参数

**SQL中使用冒号表达式**

```java
@Query("select name,age from UserInfo u where u.name = :name or u.age = :age")
UserInfo[] findUserInfoByNameOrAge(@Param("name") String name, @Param("age")Integer age);
```

其中`:name`对应`@Param("name")`所指定的方法变量值;`:age`对应`@Param("age")`所指定的方法变量值.当然SQL中的变量也可以用`?N`(N={正整数})的形式来表达,且不用标识`@Param`.  
如:`select name,age from UserInfo u where u.name = :name or u.age = :age`以防SQL注入问题,在执行语句之前,最终会被编译成`select name,age from UserInfo u where u.name=? or u.age=?`

> **注意**: 有时候在`@Query`中使用`:`不一定是表达式,而是字面字符.为了避开跟冒号表达式冲突,请额外加一个`:`以起到转义作用.

**SQL中的变量采用${name}表达式**  
实现原样替换,当然,也可以写成`$name`.不过请注意避免SQL注入问题.   

```java
@Query("select * from `userinfo` where ${one} ${orderby}")
UserInfo findUserInfo(@Param("orderby") String orderby, @Param("one") int i);
// String orderby 这个形参接受到的值会原样取代掉 "${orderby}", orderby 如果接受到的值为null,那么${orderby}默认为""
// int i 接受到的值会取代掉 "${one}"

// 假设: orderby的值为: "order by age desc", i的值为:1
// 则: 最终的SQL为: "select * from `userinfo` where 1 order by age desc"
```

### 采用${name}时请注意: 
- 传递null值,模板变量默认取""
- 参数模板仅仅用来辅助开发者构建SQL语句
- 请提防使用不当,引发SQL注入问题
- 请避免模板参数的值完全来源于用户层的输入
- 请确保参数值可控.  

通过`defaultVal`属性指定:若参数接受到null值,应该采用的默认值(该属性不是必须的,默认为"").例如:

```java
@Query("select * from `userinfo` ${orderby}")
// orderby 若为null, 那么 ${orderby}的值,就取defaultVal的值
JSONArray findUserInfo(@Param(value="orderby",defaultVal="order by age desc") String orderby);
```

`$表达式` 的值如果不可避免需要从用户层录入，并且不可控，为了安全起见，防止 SQL 注入，请使用 `@Safe` 标识形参，如

```java
@Query("select * from `userinfo` ${orderby}")
JSONArray findUserInfo(@Param(value="orderby") @Safe String orderby);
```

那么，框架会严格检测 `orderby`的实参是否有注入风险，一旦存在注入嫌疑，拒绝请求。

## 微笑表达式

定义: **以<code>\`-</code> 作为开始,以<code>-\`</code>作为结尾,包裹着若干字符,因为<code>\`- -\`</code>酷似微笑表情,因此将这样的表达式称之为`微笑表达式`.** <br>例如: <code> \`-%${name}%-\` </code>. **\`** 反撇号的位置如下图所示:<br>
![反撇号示意图](file/fanpie.png "反撇号示意图")    
作用:  
1.可以作为实参的模板,举例: 查询出姓"张"的用户.没有`微笑表达式`时的写法:

```java
db.findLikeName(name + "%");
```
这种写法不好,实参和模糊关键字`%`被融在一起了.实参是程序语言特性,而`%`是`SQL`特性,把`%`放在`@Query`里或`SQL`模板里更为适合.  
现在有`微笑表达式`了,在模板中,可以配置name实参的模板.假设模板中通过<code>\`-:name%-\`</code>引用这个实参,那么<code>\`-:name%-\`</code>将会作为这个实参的模板. name的值为"张",实际上传递的是"张%".   
举例:

```java
@Query("select * from UserInfo where id > :id and age > 18 or name like `-%:name%-`")
```

或

```xml
<?xml version="1.0" encoding="UTF-8"?>
<queries>
	<query id="findUserInfo">
		select * from UserInfo where id > :id and age > 18 or name like `-%:name%-`
	</query>
</queries>
```

2.采用`微笑表达式`的片段,会过滤敏感关键字,严格防止SQL注入. 建议将其用在`$表达式`/`${表达式}`上,因为 **$表达式的存在仅仅是为了开发者方便构建SQL**,使用不当很危险,加上`微笑表达式`可以防止由于开发者的疏忽而引发的SQL注入问题.**注意**: 冒号表达式,如`:name`最终会解释成SQL占位符`?`号,因此不存在注入问题,不必使用`微笑表达式`来预防.

##  SQL IN

### 使用"?"索引方式
```java
@Query("select * from UserInfo where name in (?1)")
List<UserInfo> findByNameIn(String...names);

@Query("select * from UserInfo where name in (?1) and id > ?2")
List<UserInfo> findByNameListIn(List<String> names,Integer id);
```

参数如果是一个空集合或空数组,那么`in`中的`?`所对应的值是`null`. `not in`结果集中若含有`null`,则,查询结果为`null`. `in` 结果集含有`null`不会影响正常查询.

```sql
id not in (1,2,null) -- 查不出
id in (null)         -- 并不会把id为null的记录查出来,id是null与否,最终查不出.
id in(1,2,null)      -- id为1或为2的结果会被查询出来
```

### 使用冒号表达式
```java
@Query("select * from student where sex = :sex and age > :age and name in(:names)")
List<Student> findByIn(@Param("sex")String sex,@Param("age")Integer age,@Param("names")Set<String> names);
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

假如在 XML 文档中放置了类似 `<` 或 `&` 字符,那么这个文档会产生一个错误,这是因为 XML 解析器会把 `<` 解释为新元素的开始,为了避免此类错误,可以将模板代码片段定义为CDATA. XML 解析器会把CDATA所包含的内容当作字符串处理.CDATA 部分由`<![CDATA[` 开始,由 `]]>`结束.   
若不用CDATA,那么有些字符必须采用**命名实体**的方式引入. 在 XML 中有 5 个预定义的实体引用:

| 字符 | 命名实体 | 实体编码 | 说明 |
|:-----:|:----:|:----:|:----:|
|  &lt;   | &amp;lt;  | &amp;#60; | 小于号 |
|  &gt;   | &amp;gt;  | &amp;#62; | 大于号 |
|  &amp;  | &amp;amp; | &amp;#38; | 与符号 |
|  &apos; | &amp;apos;| &amp;#39; | 单引号 |
|  &quot; | &amp;quot;| &amp;#34; | 双引号 |

如果想把一些公用的SQL代码片段提取出来,以便重用,通过定义`<parts>`元素(零件集)就可以做到. 在`<value>`,`<countQuery>`元素中,可以通过`#{#name}`表达式引用到名称相匹配的零件.如:`#{#condition}`表示引用name="condition"的零件.  
若`<parts>`元素跟`<query>`保持并列关系,那么该零件集是全局的.当前文件里的`<query>`都能引用它.  
一个非分页的函数,如果绑定的模板中包含`<countQuery>`,那么这个函数只会提取`<query>`语句,而不会提取计数语句.

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

当然,采用`@QueryByNamed`同样适应于改操作,例如:

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

`@QueryByNamed` 中的value值如果没有指定,默认是当前方法名.

```java
@QueryByNamed
public List<Student> findSomeStudent();
```

等效于 `@QueryByNamed("findSomeStudent")`  

`@QueryByNamed` 中的`render`属性,表示是否启用模板引擎对配置文件进行渲染,默认是`true`表示开启. 如果`<query>`节点中没有使用到任何模板语法,仅用于存储目的,那么建议设置为`false`.`:expression`,`?N`,`$expression`这些都不依赖模板引擎.  

**注意**: `$name`和`:name`这两种表达式的主要区别是——`$name`表示引用的是参数源值,可用于在模板中做逻辑判断,而`:name`用于标记参数位,SQL解析器会将其翻译成`?`号.  

在模板中`:expression`表达式或`?N`表达式可以作为`SQL`函数的逻辑判断表达式,如跟这些函数一起参与运算:`IF(expr1,expr2,expr3)`,`IFNULL(expr1,expr2)`,`NULLIF(expr1,expr2)`,`ISNULL(expr)`.  

```sql
-- 方法的第1个参数的值可以影响where的条件
select t.A from (select 11 as A,22 as B,33 as C) as T where if(?1 > 10,t.B>10,t.C>100)
-- 方法的第2个参数的值可以影响查询集
select if(?2 > 10,'大于10','不大于10') as msg
-- 名称为"number"的参数,其值可以影响where条件
select t.A from (select 11 as A,22 as B,33 as C) as T where if(:number > 10,t.B>10,t.C>100)
-- 名称为"number"的参数,其值可以影响查询集
select if(:number > 10,'大于10','不大于10') as msg
```

允许多个方法绑定同一个模板id. 在模板中使用`${_method}`可以引用到当前方法的`org.fastquery.core.MethodInfo`对象,该对象是反射`java.lang.reflect.Method`的缓存.  
例: 根据当前方法名称的不同取不同的`SQL`语句

```java
public interface QueryByNamedDBExtend extends QueryRepository {
	@QueryByNamed(render = false)
	JSONArray findUAll();
	
	// 两个方法指定同一个模板id值
	@QueryByNamed("findSome")
	JSONArray findLittle();
	@QueryByNamed("findSome")
	JSONArray findSome();
}
```

org.fastquery.dao.QueryByNamedDBExtend.queries.xml 模板文件的内容: 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xml> 
<queries>
	<!-- 定义全局 parts -->
	<parts>
		<part name="feids">id,name,age</part>
	</parts>
	<query id="findUAll">
		<value>select #{#feids} from UserInfo limit 3</value>
	</query>

	<query id="findSome">
		<![CDATA[
		## 如果当前方法的名称等于 "findLittle"
		#if( ${_method.getName()} == "findLittle" )
			## 查3条  
			select #{#feids} from UserInfo limit 3
		#else 
		   select `no`, `name` from Student limit 5
		#end  
		]]>
	</query>	
</queries>
```

其中 `${_method.getName()}` 可简写成 `${_method.name}`. 在`Velocity`里调用对象或方法,不是本文的重点,点到为止.

## QueryBuilder
上面介绍了`SQL`不仅可以绑定在`@Query`里, 也可以写到`XML`里. 还有另一种方式,**通过构造QueryBuilder对象**构建`Query`语句.  
用法举例:

```java
@Query
Page<Map<String, Object>> pageByQueryBuilder(QueryBuilder queryBuilder,Pageable pageable);
```

如果分页不要求得到总页数,在接口的方法上加`@NotCount`便可(谁说分页一定要执行count语句?).

不用去实现那个接口,直接调用:

```java
// 获取Repository实例
UserInfoDBService db = FQuery.getRepository(UserInfoDBService.class);

String query = "select id,name,age from userinfo #{#where}";
String countQuery = "select count(name) from userinfo #{#where}";
ConditionList conditions = ConditionList.of("age > :age","and id < :id");
Map<String, Object> parameters = new HashMap<>();
parameters.put("age", 18);
parameters.put("id", 50);

QueryBuilder queryBuilder = new QueryBuilder(query, countQuery, conditions, parameters);
Page<Map<String, Object>> page = db.pageByQueryBuilder(queryBuilder,new PageableImpl(1, 3));
List<Map<String, Object>> content = page.getContent();
content.forEach(map -> {
	Integer age = (Integer) map.get("age");
	Integer id = (Integer) map.get("id");
	assertThat(age, greaterThan(18));
	assertThat(id, lessThan(50));
});

List<String> executedSQLs = rule.getExecutedSQLs();
assertThat("断言：执行过的sql有两条",executedSQLs.size(), is(2));
assertThat(executedSQLs.get(0), equalTo("select id,name,age from userinfo where age > ? and id < ? limit 0,3"));
assertThat(executedSQLs.get(1), equalTo("select count(name) from userinfo where age > ? and id < ?"));
```

引用问号表达式(?expression) , 冒号表达式(:expression), 其中?1表示方法的第一个参数,`:age`表示匹配`@Param("age")`那个参数,采用问号或冒号表达式不会有注入问题.

**如果要查的表是一个变量(甚至表是自动生成的),要查的字段也是变量,条件单元的可选范围也是个变量,整个 SQL 都是动态生成的,在这种情形就只能用 `QueryBuilder`, 使用`@Query`模板,就无能为力了,`QueryBuilder`有不可取代的功能**.不过,能用`@Query`模板解决问题,就尽量使用它,因为它的设计,只用写一个抽象方法,零实现,让你没办法去写第二行 Java 代码,从设计上让你无法犯错.

## 支持存储过程

只支持in(输入)参数,不支持out(输出参数), 如果想输出存储过程的处理结果,在过程内部使用`select`查询输出.  
举例:  
插入一条学生,返回学生的总记录数和当前编码,存储过程语句:

```sql
delimiter $$
drop procedure if exists `xk`.`addStudent` $$
create procedure `xk`.`addStudent` (in no char(7), in name char(10), in sex char(2), in age tinyint(4), 
  in dept char(20))
begin

   -- 定义变量
   -- 总记录数
  declare count_num int default 0;
  -- 编码
  declare pno varchar(7) default '';
  
  insert into `student` (`no`, `name`, `sex`, `age`, `dept`) values(no, name, sex, age, dept);
  select count(`no`) into count_num from student;
  select `no` from student where `no`=no limit 0,1 into pno;
  -- 输出结果
  select count_num, pno;  
end $$
delimiter ;
```

调用存储过程:

```java
@Query("call addStudent(?1,:name,?3,?4,:dept)")
JSONObject callProcedure(String no,@Param("name")String name,String sex,int age,@Param("dept")String dept);
```

## 分页
要处理查询语句的参数,只需定义方法参数,为了在运行时对参数名称可见就额外加上`@Param`,上面有很多示例.另外,方法的设计还能识别某些特殊的类型,如`QueryBuilder`,`Pageable`,以便核心能智能地将动态构建查询和分页应用于查询中.

- 通过`@QueryByNamed`实现分页

```java
@QueryByNamed("findPage") // 引用id为"findPage"的分页模板
Page<Student> findPage(Pageable pageable, @Param("name") String name,@Param("age") Integer age);
```

模板文件:

```xml
<query id="findPage">
	<!-- 查询主体语句 -->
	<value>
		select no, name, sex from Student #{#condition} #{#order}
	</value>

	<!-- count语句 -->
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
- `#{#limit}`是分页模板的内置零件,表示分页区间. `#{#limit}`默认是放在尾部,在符合`SQL`语法的前提下也可以把它放在`SQL`语句中的其他地方
- 动态条件部分若用`<where>`元素进行包裹,会自动处理好条件连接符问题(避免出现where紧接`or`或`and`)
- `<value>`和`<countQuery>`节点引用的零件若包含`<where>`元素,零件解析成字符串后会自动加上 *"where"* ,请不要在引入切口处重复追加 *"where"* 字符串

- 通过@Query实现分页

```java
public interface UserInfoDBService extends QueryRepository {

	// Pageable 用做描述当前页的索引和每页条数.
    
	// countField : 明确指定用来统计总行数的字段,count(countField)中的countField默认值是"id"
	@Query(value="select id,name,age from `userinfo` where 1",countField="id")
	Page<Map<String, Object>> findAll(Pageable pageable);
	
	// 如果没有指定count语句,那么由fastquery分析出最优的count语句
	@Query("select id,name,age from `userinfo` #{#where}")
	@Condition("age > ?1")     // 若age的值传递null,该条件将不参与运算
	@Condition("and id < ?2")  // 若id的值传递null,该条件将不参与运算
	Page<UserInfo> find(Integer age,Integer id,Pageable pageable);
	
	// countQuery : 指定自定义count语句
	@Query(value = "select id,name,age from `userinfo` #{#where}", 
	       countQuery = "select count(id) from `userinfo` #{#where}")
	@Condition("age > ?1")        // 若age的值传递null,该条件将不参与运算
	@Condition("and id < ?2")     // 若id的值传递null,该条件将不参与运算
	Page<UserInfo> findSome(Integer age,Integer id,Pageable pageable);
}
```

### @PageIndex和@PageSize
`@PageIndex` 用来指定当前页索引,从1开始计数,如果传递的值小于1,依然视为1   
`@PageSize`  用来指定当前页应该显示多少条数据,如果传递的值小于1,依然视为1   
**注意**: 该注解组合不能和`Pageable`一起使用  
例如:

```java
@NotCount // 分页不统计总行数
@Query(value = "select id,name,age from `userinfo`")
Page<Map<String,Object>> findSome(Integer age,Integer id,@PageIndex int pageIndex,@PageSize int pageSize);
```


### 使用分页     
`Page`是分页的抽象,通过它可以获取分页中的各种属性,并且不用开发者去实现.

```java
int p = 1;    // 指定访问的是第几页(不是从0开始计数)
int size = 3; // 设定每一页最多显示几条记录
Integer age=10,id = 50;
Pageable pageable = new PageableImpl(p, size);
Page<UserInfo> page  = userInfoDBService.findSome(age, id,pageable);
List<UserInfo> userInfos = page.getContent(); // 获取这页的数据
Slice slice = page.getNextPageable();         // 下一页
int number = page.getNumber();                // 当前页数(当前是第几页)
// 更多 page.? 不妨亲自去试试看
```

`Page`转换成`JSON`后的结构如下:

```js
{
    "content":[                 // 这页的数据
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
    "previousPageable": {    	// 上一页的基本属性
        "number": 0,         	// 定位的页码
        "size": 15           	// 期望每页多少条数据
    },
    "nextPageable": {        	// 下一页的基本属性
        "number": 1,         	// 定位的页码
        "size": 15           	// 期望每页多少条数据
    },
    "number": 1,             	// 当前页码,从1开始
    "size": 15,              	// 期望每页行数(numberOfElements表示真正查出的条数)
    "numberOfElements": 6,  	// 当前页的真实记录行数
    "totalElements": 188,    	// 总行数
    "totalPages": 13         	// 总页数
}
```

### 注意:
- 如果在分页函数上标识`@NotCount`,表示在分页中不统计总行数.那么分页对象中的`totalElements`的值为-1L,`totalPages`为-1.其他属性都有效并且真实.    
- 如果明确指定不统计行数,那么设置`countField`和`countQuery`就会变得无意义.    
- `#{#limit}`不仅能使用在 XML 文件里,也可以使用在`@Query`里,无特殊要求,建议不要指定`#{#limit}`.

### 扩展分页实现
目前该框架默认支持分页的数据库有`MySQL`,`Microsoft SQL Server`,`PostgreSQL`,因此,扩展的空间非常大,并且非常容易.实现`org.fastquery.page.PageDialect`类,有针对性地重写相关方法,解决`SQL`中的差异.欲了解更多细节请参考`org.fastquery.dialect.MySQLPageDialect`,`org.fastquery.dialect.PostgreSQLPageDialect`.

## JavaScript分页插件
[PJAXPage](https://gitee.com/xixifeng.com/pjaxpage)分页插件,完美支持`Page`数据结构.        
项目地址: https://gitee.com/xixifeng.com/pjaxpage      
使用例子: http://xixifeng.com.oschina.io/pjaxpage/example/   

## 执行SQL文件
```java
String sqlName = "update.sql";
int[] effects = studentDBService.executeBatch(sqlName);
```

- sqlName 指定基准目录下的SQL文件. 注意: 基准目录在fastquery.json里配置,sqlName 为绝对路径也行
- 返回 `int[]`类型,用于记录SQL文件被执行后所影响的行数.若,effects[x] = m 表示第x行SQL执行后影响的行数是m; effects[y] = n 表示第y行SQL执行后所影响的行数是n
- 判定SQL文件里有多少条语句,依据以分号分割的结果作为标准
- 只支持整行注释,以`#`或`--`开头的行将视为注释

一个数据源可能管理着多个数据库,执行的SQL文件也有可能需要根据参数的不同而服务于不同的数据库.或者说SQL文件里有动态的部分,需要根据传递的参数加以区分.那么,可以使用`executeBatch(String sqlName,String[] quotes)`,第二个参数可以被SQL文件所用,引用方式为`$[N]`,表示引用数组的第`N`个元素.

```sql
drop table if exists $[0].demo_table;
```

## 动态适配数据源
### 创建数据源
如果想在项目运行期间动态创建一个新数据源,那么请使用`FQuery.createDataSource`.

```java
// 数据源名称
String dataSourceName = "xk1";

// 连接池配置
Properties properties = new Properties();
properties.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
properties.setProperty("jdbcUrl", "jdbc:mysql://db.fastquery.org:3306/xk1");
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

## 扩展支持数据连接池
默认已经支持的连接池有,`c3p0`,`druid`,`hikari`...当然,开发者很容易在此基础上进行扩展.  
示例,让`FastQuery`支持自定义的连接池.实现过程如下:  
步骤1: 自定义类实现`org.fastquery.core.ConnectionPoolProvider`接口

```java
public class MyPoolProvider implements ConnectionPoolProvider {

	@Override
	public DataSource getDataSource(Resource resource, String dataSourceName) {
		// 读取配置文件
		InputStream inputStream = resource.getResourceAsStream(name);
		.... ...

		Properties props = new Properties();
		props.setProperty(k, v);
		... ...
		
		// 创建数据源实例
		return new MyDataSource(props);
	}

}
```
步骤2: 在`pool-extend.xml`里注册

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xml>
<pools name="providers">
	<pool name="mypool" class="org.fastquery.<your.domain>.MyPoolProvider" />
</pools>
```

步骤3: 使用自定义的连接池`mypool`  
配置`fastquery.json`文件

```js
{
    "scope": [
        {
            "config": "mypool", // 用这个池提供数据源 
            "dataSourceName": "hiworld",
            "basePackages": [
                "<your.domain>.XxxDBService"
            ]
        }
    ]
}
```

## @Before拦截器
在执行方法之前拦截  
- 准备一个BeforeFilter

```java
 /**
  * @author xixifeng (fastquery@126.com)
  */
 public class MyBeforeFilter1 extends BeforeFilter<QueryRepository> {

 	@Override
 	public void doFilter(QueryRepository repository, Method method, Object[] args) {
 	
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
public class MyAfterFilter extends AfterFilter<QueryRepository> {

	@Override
	public Object doFilter(QueryRepository repository, Method method, Object[] args, Object returnVal) {
		
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
@SkipFilter // 标识该方法将不受"自定义Filter"的约束
@Query("select no from `course` limit 1")
String findOneCourse();
```

### 注意:
- `@Before`和`@After`不仅可以标注在接口类上,也可以标注在方法上
- 标识在类的上方:表示其拦截的作用范围是整个类的方法
- 标识在方法上:表示其拦截的作用范围是当前方法
- 一个方法的拦截器总和=它的所属类的拦截器+自己的拦截器

## 应用在 Spring 环境

配置扫描范围：

```xml
<context:component-scan base-package="org.fastquery.service" />
```

或者

```java
@ComponentScan("org.fastquery.service")
```

使用 db 接口处, 注入得到实例对象

```java
@javax.annotation.Resource
private UserInfoDB userInfoDB;
```

## 测试FastQuery
FastQuery提供的测试方式能轻松解决如下问题.
- 运行时获取SQL和它的参数值,以便开发者验证生成的SQL是否跟期望值一致.
- 运行DB方法后自动回滚数据库事务.

`FastQueryTestRule` 实现了Junit中的 `TestRule` 类,用来扩展测试用例.可以在测试方法中获取执行过的SQL语句及SQL所对应的参数值,以便做断言.加上`@Rollback`注解,可以用来控制测试方法执行完毕之后是否让数据事务回滚或提交.测试方法结束后默认自动回滚,既可以达到测试效果,又不影响数据库(可回滚到改之前状态). 如下是例子,请留意注释.

```java
// junit fastquery的扩展
@org.junit.Rule
public FastQueryTestRule rule = new FastQueryTestRule();

// 获取DB接口
private StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);

@Rollback(true) // 当该方法执行完毕之后自动回滚事务
@Test
public void update() {
	String no = "9512101";
	String name = "清风习习";
	int age = 17;
	int effect = studentDBService.update(no, name, age);
	// 断言: 影响的行数是1
	assertThat(effect, is(1));
	// 获取DB操作所绑定的SQL
	List<SQLValue> sqlValues = rule.getListSQLValue();
	// 断言: studentDBService.update 执行后产生的SQL为一条
	assertThat(sqlValues.size(), is(1));
	SQLValue sqlValue = sqlValues.get(0);
	// 断言: 所产生的SQL等于"update student s set s.age=?,s.name=? where  s.no=?"
	assertThat(sqlValue.getSql(), equalTo("update student s set s.age=?,s.name=? where  s.no=?"));
	// 获取SQL参数列表
	List<Object> values = sqlValue.getValues();
	// 断言: 这条SQL语句中一共有3个参数
	assertThat(values.size(), is(3));
	// 断言: SQL的第一个参数是Integer类型,并且他的值等于age
	assertThat(values.get(0).getClass() == Integer.class && values.get(0).equals(age), is(true));
	// 断言: SQL的第二个参数是String类型,并且他的值等于name
	assertThat(values.get(1).getClass() == String.class && values.get(1).equals(name), is(true));
	// 断言: SQL的第三个参数是String类型,并且他的值等于no
	assertThat(values.get(2).getClass() == String.class && values.get(2).equals(no), is(true));
}
```

并不是绑定了多少条`SQL`就一定执行多少条.就拿分页来说,并不是总会执行`count`,查不到数据时,就没有必要发出`count`语句.使用`rule.getExecutedSQLs()`可以取得已被执行过的`SQL`.每个`DB`方法执行之前都会清除历史记录从新统计.

```java
assertThat(db.findPageWithWhere(id, cityAbb, 6,pageSize).isHasContent(), is(true));
//  获取上行执行后,所执行过的sql
List<String> executedSQLs = rule.getExecutedSQLs();
// 断言已经执行了2条sql语句
assertThat(executedSQLs.size(), is(2));
// 断言第二条sql是...
assertThat(executedSQLs.get(1), equalTo("select count(id) from City where id > ? and cityAbb like ?"));

assertThat(db.findPageWithWhere(id, cityAbb, 7,pageSize).isHasContent(), is(false));
// 获取上行执行后,所执行过的sql
executedSQLs = rule.getExecutedSQLs();
assertThat(executedSQLs.size(), is(1));
assertThat(executedSQLs.get(0), not(containsString("count")));
```

`FastQuery`已经迭代了很久,每次发布新版本是如何保证之前的功能不受影响的呢?那是因为`FastQuery`的每个功能特性都有非常缜密的断言测试,发布时把能否通过所有断言做为先决条件,当然也得益于深思熟虑的设计.`Junit`是众多Java框架中,真正有用的为数不多的其中之一,`FastQuery`乐此不疲.

## fastquery.json其他可选配置选项:

| 属性名 | 类型 | 默认值 | 作用 | 示例 |
|:-----:|:-----:|:-----:|:-----|:-----|
| basedir | string | 无 | 基准目录,注意: 后面记得加上 "/" <br> 该目录用来放SQL文件,需要执行SQL文件时,指定其名称就够了 | "/tmp/sql/" |
| debug | boolean | false | 在调试模式下,可以动态装载xml里的SQL语句,且不用重启项目<br>默认是false,表示不开启调试模式.提醒:在生产阶段不要开启该模式 | false |
| queries | array | [ ] | 指定*.queries.xml(SQL模板文件)可以放在classpath目录下的哪些文件夹里.<br>默认:允许放在classpath根目录下<br>注意:配置文件的位置不一定基于classpath目录,也可以通过`"fastquery.config.dir"`另行指定,上文已经提及到.每个目录前不用加"/",目录末尾需要加"/" | ["queries/","tpl/"] |
| slowQueryTime | int | 0 | 设置慢查询的时间值(单位:毫秒; 默认:0,表示不开启慢查询功能), 如果 `QueryRepository` 中的方法执行超过这个时间,则会警告输出那些执行慢的方法,以便优化 | 50 |

## 源码

- https://gitee.com/xixifeng.com/fastquery
- https://github.com/xixifeng/fastquery

## 开发环境
仅仅是建议,并不局限于此         
  IDE: IntellIJ IDEA          
build: maven 

## 微信交流
![FastQuery 微信交流](file/wx.png "微信交流群,与作者交流FastQuery.")  
与作者一起探讨FastQuery(加入时请标注java,谢谢).

## 反馈问题
https://gitee.com/xixifeng.com/fastquery/issues  
FastQuery秉承自由、开放、分享的精神,本项目每次升级之后,代码和文档手册都会在第一时间完全开源,以供大家查阅、批评、指正.笔者技术水平有限,bug或不周之处在所难免,所以,遇到有问题或更好的建议时,还请大家通过[issue](https://gitee.com/xixifeng.com/fastquery/issues)来向我们反馈.  

