<p align="center"><img src="file/logo.png" alt="FastQuery logo"></p>

### Apache Maven
```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>fastquery</artifactId>
    <version>1.0.100</version> <!-- fastquery.version -->
</dependency>
```

### Gradle/Grails
```
compile 'org.fastquery:fastquery:1.0.100'
```

# FastQuery 数据持久层框�???
FastQuery 基于Java语言.他的使命�???:�???化Java操作数据�???.<br />
提供少许`Annotation`,消费者只用关心注解的含义,这就使得框架的核心便于重�???,便于持续良�?�发�???.<br />

## FastQuery 主要特�?�如�???:
1. 遵循非侵入式原则,设计优雅或简�???,极易上手
2. 在项目初始化阶段采用ASM生成好字节码,因此支持编译前预处理,可最大限度减少运行期的错�???,显著提升程序的强壮�??
3. 支持安全查询,防止SQL注入
4. 支持与主流数据库连接池框架集�???
5. 支持 `@Query` 查询,使用 `@Condition`,可实现动�??? `where` 条件查询
6. 支持查询结果集以JSON类型返回
7. 拥有非常优雅的`Page`(分页)设计
8. 支持`AOP`,注入拦截器只�???要标识几个简单的注解,�???: `@Before` , `@After`
9. 使用`@Source`可实现动态�?�配数据�???.这个特�?�特别�?�合多租户系统中要求数据库彼此隔离其结构相同的场景里
10. 支持`@QueryByNamed`命名式查�???,SQL动�?�模�???
11. 支持存储过程
12. 支持批量更新集合实体(根据主键,批量更新不同字段,不同内容).

## 运行环境要求
JRE 8+

## 配置文件

### 配置文件的存放位�???

默认从`classpath`目录下去寻找配置文件. 配置文件的存放位置支持自定义, �???: `System.setProperty("fastquery.config.dir","/data/fastquery/configs");`, 它将会覆盖`classpath`目录里的同名配置文件.  如果项目是以jar包的形式启动,那么可以通过java命令�??? `-D` 参数指定配置文件的目�???, �???: `java -jar Start.jar -Dfastquery.config.dir=/data/fastquery/configs`. 

### c3p0-config.xml
完全支持c3p0官方配置,详情配置请参照c3p0官网的说�???.

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
     <!-- 可以配置多个named-config节点,多个数据�??? -->
    <named-config name="name-x"> ... ... </named-config>
</c3p0-config>
```

### druid.xml
用于配置支持Druid连接�???,详细配置请参�??? https://github.com/alibaba/druid

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
用于配置支持HikariCP连接�???,详细配置选项请参�??? https://github.com/brettwooldridge/HikariCP  
连接MySQL,为了得到更好的�?�能,推荐配置

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
	<!-- 可以配置多个bean节点,提供多个数据�??? -->
	<bean name="name-x"> ... ... </bean>
</beans>
```

支持多连接池共存,�???,同时让Druid,HikariCP工作,并配置多个数据源.

### fastquery.json
配置数据源的作用范围

```js
// @author xixifeng (fastquery@126.com)
// 配置必须遵循标准的json语法.
{
  "scope":[
		    // config 用于指定由谁来提供数据源,�???,"c3p0","druid","hikari"等等
		    {
		        "config": "c3p0",            // 表示由c3p0负责提供数据�???
		        "dataSourceName": "xk-c3p0", // 数据源的名称
		        "basePackages": [            // 该数据源的作用范�???
		            "org.fastquery.example",              // 包地�???
		            "org.fastquery.dao.UserInfoDBService" // 完整类名�??? 
		            // 在这可以配置多个DB接口或包地址,�???","号隔�???
		            // 提醒:在json结构�???,数组的最后一个元素的后面不能�???","
		        ]
		    },
		    
		     /*
		      再配置一个数据源作用�???
		     */
		     {
		        "config" : "mySQLDriver",      // 表示由mySQLDriver负责提供数据�???
		        "dataSourceName": "shtest_db", // 数据源的名称
		        "basePackages": [              // 该数据源的作用范�???
		            "org.fastquery.example.DataAcquireDbService"
		             // 在这可以配置多个DB接口,�???","号隔�???
		        ]
		     },
		    
		     {
		        "config": "c3p0",              // 表示由c3p0负责提供数据�???
		        "basePackages": [   
		             "org.fastquery.dao2.UserInfoDBService2"
		        ]
		     }
		  ] 
}
```
**注意**: 在fastquery.json中配置作用域,其中"dataSourceName"不是必须�???,"dataSourceName"要么不指�???,要指定的话那么必须正�???.如果没有指定"dataSourceName",那么在调用接口的时�?�必须指定数据源的名�???.下面的�?�配数据源章节会讲到."basePackages"若配置了包地�???,那么对应的数据源会作用这个包的所有类,及所有子包中的类.  

数据源的初始化是�???"fastquery.json"�???始的,根据从里面读�???"dataSourceName"的�??,取相应的配置,继�?�完成数据源的创�???.�???,创建�???个名�???"rex-db"的数据源:

```js   
{
    "config": "c3p0",           
    "dataSourceName": "rex-db"
}
```
在这�???,"basePackages"不是必须�???,该数据源可以当做是一个服�???,供没有明确指定数据源的Repository使用.

## 入门例子
当看到一个例子时,切勿断章取义,多看�???�???,�???�???会有意想不到的结�???.  
- 准备�???个实�???

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

**实体属�?�跟数据库映射的字段必须为包装类�???,否则被忽�???**. 在实体属性上标识`@Transient`,表示该字段不参与映射.   

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
	// 获取实现�???
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

**注意**:不用去实现StudentDBService接口.通过`FQuery.getRepository`获取DAO接口对应的实�???,虽然每次获取实例消�?�的性能微乎其微可以忽略不计,但是,作为�???个接口并且频繁被调用,因此,建议把获取到的实例赋值给类成员变�???,�???好是用`static`修饰.`FQuery.getRepository`获得的实例是唯一�???,不可变的.  

�???个接口不实现它的`public abstract`方法就毫无作用可�???,因此,与之对应的实例对象是必须�???,只不过是FastQuery内部替用户实现了.读�?�可能会�???,这个自动生成的实例在�???么时候生�???? 动�?�生成的效率如何保持高效? 为此, 笔�?�做了相当多的功�???:让所有DB实现类在项目初始化阶段进�???,并且尽可能地对接口方法做静�?�分�???,把有可能在运行期发生的错误尽�???大努力提升到初始化阶�???,生成代码前会�???测SQL绑定是否合法有效、检测方法返回�?�是否符合常规�?�方法的参数是否满足模版的调用�?�是否正确地使用了分�???...诸如此类问题.这些潜在问题�???旦暴�???,项目都启动不起来,错误信息将在�???发阶段详细输�???,并且必须干掉这些本该在生产环境才发生的错�???,才能继续�???�???,迫使�???发�?�必须朝正确的道路走,或�?�说框架的优良设计其核心理念引导�???发�?�不得不写出稳健的程�???.项目进入运行�???,大量的校验就没必要写�???,从�?�最大限度保证快速执�???.  

唯一的出�???,只能引用接口,这就使得�???发�?�编程起来不得不�???�???,因为面对的是�???个高度抽象的模型,而不必去考虑细枝末节.接口可以看成是一个能解析SQL并能自动执行的模�???,方法的参数�?�绑定的模版和标识的注解无不是为了实现一个目�???:执行SQL,返回结果.  

这种不得不面向接口的编程风格,有很多好�???:耦合度趋�???0,天然就是**对修改封�???,对扩展开�???**,不管是应用层维护还是对框架增加新特�??,这些都变得特别容�???.隐藏实现,可以减少bug或�?�是能消灭bug,就如**解决问题,不如消灭问题**�???�???,解决问题的�?�诣远远落后于消灭问�???,原因在于问题被解决后,不能证明另一个潜在问题在解决代码中不再出�???,显然消灭问题更胜�???�???.应用层只用写声明抽象方法和标识注�???,试问bug从何而来?该框架最大的优良之处就是让开发�?�没办法去制造bug,至少说很难搞出问题来.不得不简�???,没法造bug,显然是该项目�???追求的核心目标之�???.  

不管用不用这个项�???,笔�?�期望读者至少能快�?�地�???阅一下该文档,有很多设计是众多同类框架�???不具备的,希望读�?�从中得到正面启发或反面启发,哪�?�一点点,都会使你收益.  

## 针对本文@Query的由�???
该项目开源后,有些习惯于繁杂编码的�???发�?�表�???,"*使用`@Query`语义不强,为何不用@SQL,@Select,@Insert,@Update...?*". SQL的全称是 Structured Query Language,本文�??? `@Query` 就是来源于此. `@Query`只作为运行SQL的载�???,要做�???么事情由SQL自己决定.因此,不要片面的认为Query就是select操作. 针对数据库操作的注解没有必要根据SQL的四种语�???(DDL,DML,DCL,TCL)来定�???,定义太多,只会增加复杂�???,并且毫无必要,如果是改操作加上`@Modifying`注解,反之,都是"�???",这样不更�???洁实用吗? 诸如此类:`@Insert("insert into table (name) values('Sir.Xi')")`,`@Select("select * from table")`,SQL的表达能力还不够�???? 就不觉得多出`@insert`和`@Select`有拖泥带水之�???? SQL的语义本身就很强,甚至连`@Query`和`@Modifying`都略显多�???,但是毕竟SQL�???要有�???个载体和�???个大致的分类.

## 带条件查�???

```java
// sql中的?1 表示对应当前方法的第1个参�???
// sql中的?2 表示对应当前方法的第2个参�???
//       ?N 表示对应当前方法的第N个参�???
	
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
参数较多时不建议使用问号(?)引用参数,因为它跟方法的参数顺序有�???,不便维护,可以使用冒号(:)表达�???,跟顺序无�???, ":name" 表示引用标记有@Param("name")的那个参�???.  
若返回`List<Map<String, String>>`或`Map<String, String>`,会把查询出的字段�???(value)包装成字符串.   

**注意**: 在没有查询到数据的情况下,如果返回值是集合类型或`JSON`类型或�?�是数组类型,返回具体的�?�不会是`null`,而是�???个空对象(empty object)集合或空对象`JSON`或�?�是长度�???0的数�???.   
使用空对象来代替返回`null`,它与有意义的对象�???�???,并且能避免`NullPointerException`,阻止`null`肆意传播,可以减少运行期错�???.反对者一般都从�?�能的角度来考虑,认为`new`�???个空对象替代`null`,会增加系统的�???�???.可是,&lt;&lt;Effective Java&gt;&gt;的作�???**Josh Bloch**�???,在这个级别上担心性能问题是不明智�???,除非有分析表�???,返回空对象来替代返回`null`正是造成性能问题的源�???.细心的人可能已经发现JDK新版本的API都在努力避免返回`null`.  
举例说明: 

```java
// 针对该方�???,如果没有查询到数�???,返回值的结果是一个长度为0的Student[]
@Query("sql statements")
Student[] find(Integer age,String sex); 

// 针对该方�???,如果没有查询到数�???,返回值的结果是一个空Map(非null)
@Query("sql statements")
Map<String,Object> find(Integer id);

// 针对该方�???,如果没有查询到数�???,返回值的结果是一个空List<Map>(非null)
@Query("sql statements")
List<Map<String, Object>> find(String sex);
```

**注意**: 查询单个字段,还支持返回如下类�???:
- `List<String>`,`String[]` �??? `String`
- `List<Byte>`,`Byte[]` �??? `Byte`
- `List<Short>`,`Short[]` �??? `Short`
- `List<Integer>`,`Integer[]` �??? `Integer`
- `List<Long>`,`Long[]` �??? `Long`
- `List<Float>`,`Float[]` �??? `Float`
- `List<Double>`,`Double[]` �??? `Double`
- `List<Character>`,`Character[]` �??? `Character`
- `List<Boolean>`,`Boolean[]` �??? `Boolean`  

除了改操作或count�???,查单个字段不能返回基本类�???,因为:基本类型不能接受`null`�???,而SQL表字段可以为`null`.
返回类型若是基本类型的包装类�???,若返回null, 表示:没有查到或查到的值本身就是null.
例如: 

```java
// 查询单个字段,若没有查�???,就返回空List<String>(非null)
@Query("select name from Student limit 3")
List<String> findNames(); 
```

## 类属性名称与表字段不�???致时,如何映射?  
为了说明这个问题先准备一个实�???  

```java
public class UserInformation {
	private Integer uid;
	private String myname;
	private Integer myage;
	// getters / setters
	// ... ...
}
```

而数据库中的表字段分别是id,name,age,通过`SQL`别名的方�???,可以解决类属性名称与表字段不�???致的映射问题.如下:  

```java
// 把查询到的结果映射给UserInformation
@Query("select id as uid,name as myname,age as myage from UserInfo u where u.id = ?1")
UserInformation findUserInfoById(Integer id);
```

## 动�?�条件查�???

### 采用`Annotation`实现�???单动态条�???  
看到这里,可别认为`SQL`只能写在Annotation(注解)�???.`FastQuery`还提供了另二种方�???: �??? 采用`@QueryByNamed`(命名式查�???),将`SQL`写入到模板文件中,并允许在模板文件里做复杂的�?�辑判断,相当灵活. �??? 通过`QueryBuilder`构建`SQL`.下面章节有详细描�???. 

```java
@Query("select no, name, sex from Student #{#where} order by age desc")
// 增加若干个条�???
@Condition("no like ?1")                            // ?1的�??,如果是null, 该行条件将不参与运算
@Condition("and name like ?2")                      // 参数 ?2,如果接收到的值为null,该条件不参与运算
// 通过 ignoreNull=false �???启条件�?�即使是null也参与运�???
@Condition(value = "and age > ?3",ignoreNull=false) // ?3接收到的值若为null,该条件将保留
@Condition("and name not like ?4") 
@Condition("or age between ?5 and ?6")
Student[] findAllStudent(... args ...);
```

**注意**:  
- 如果参数是`String`类型,值若为`null`�???""(空字符串),在默认情况下,都会使条件移�???
- `ignoreNull=false` : 参数值即便为null,条件也参�???
- `ignoreEmpty=false` : 参数值即使为"",条件也保�???

`@Condition(value="name = ?1",ignoreNull=false)`表示`?1`接受到的值若是`null`,该条件也参与运算,�???终会翻译成`name is null`.`SQL`中的`null`无法跟比较运算符(如`=`,`<`,或�?�`<>`)�???起运�???,但允许跟`is null`,`is not null`,`<=>`操作符一起运�???,�???,将`name = null`想表达的意�??,解释成`name is null`.  
`@Condition(value="name != ?1",ignoreNull=false)` 若`?1`的�?�为`null`,�???终会解释成`name is not null`. 

在`In`查询作为�???个条件单元时,请忽略null判断,如`@Condition("or dept in(?4,?5,?6)"`其中的一个参数为`null`就将条件移除显然不太合理.

### 结构包装
有时候我们需要返回如下结构的数据�???
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
			"name":"李�??",
			"id":3
		}
	]
}
```

举例说明，部门对应员工是 1：N 关系，查询某�???个部门下面的员工，可以这样写�???
```java
@Query("select d.id as departmentId, d.name as departmentName, emps[e.id, e.name] from `department` d left join employee e on d.id = e.departmentId where d.id = :departmentId")
Department findDepartment(@Param("departmentId") Long departmentId);
```

其中 `emps[e.id, e.name]` 是关键，`[]` 确定集合 `emps` 里的元素，`Department` 类中�???要有 emps 成员属�?��??


### 通过JAVA脚本控制条件增减
`@Condition`中的`ignoreScript`属�?�可以绑定一个JAVA脚本(不是JS),根据脚本运行后的布尔结果,来决定是否保留条件项.脚本运行后的结果如果是`true`,那么就删除该条件�???,反之,保留条件�???,默认脚本是`false`,表示保留该条件项. 注意: 脚本执行后得到的结果必须是布尔类�???,否则,项目都启动不起来.  
举例:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition("age > :age")
@Condition(value="and name like :name",ignoreScript=":age > 18 && :name!=null && :name.contains(\"Rex\")")
Page<UserInfo> find(@Param("age")int age,@Param("name")String name,Pageable pageable);
```
其中, `:age`引用的是`@Param("age")int age`的实参�??.`:name`是`@Param("name")String name`的实参�??.这个脚本要表达的意�?�不�???而喻. 不过脚本的解析能力还不能自动**拆箱**(unboxing),�???要调用拆箱方�???,在这里age变量如果是`Integer`类型,要想如上脚本能正确编�???,必须这么�???: `":age.intValue() > 18 && :name!=null && :name.contains(\"Rex\")"`, 请留意住`:age.intValue()`. 其他包装类型`Short`, `Long`, `Byte`, `Boolean`, `Character`, `Float`, `Double` 以此类推.  

### �???么是JAVA脚本?
在这里将�???段承载着程序的字符串称之为JAVA脚本.脚本在初始化阶段被解释成能在`JVM`里运行的字节�???,在脚本里能�?�过`:expression`(冒号表达�???)获取当前方法在运行时�???接受到的�???有参�???,引用的参数可以是�???个复杂对�???,完全可以把`:expression`当成是对象的引用句柄.虽然允许把脚本写得很�???,更支持写出较为复杂的逻辑,但是,不建议这么做,因为那样可读性极�???,不便迭代维护.做再庞杂的程�???,都应该拆分成若干小�?�简单的功能,然后以优良的设计将其串联起来.`FastQuery`自始自终会遵守简�???,严谨,清晰的编程风�???.

### @Condition 中的 if...else
条件是否保留可以通过`if`条件来确�???,`if`绑定的JAVA脚本运行后的结果若为`true`就保留该`Condition`,反之就取`else`的捆绑�??,`else`如果没有值或者是空�??,表示移除该`Condition`.  
举例:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition(value="age > :age",if$=":age < 18", else$="name = :name")
Page<UserInfo> findPage(@Param("age")int age,@Param("name")String name,Pageable pageable);
```
如果`age`的实参�?�小�???18,�???,保留该`Condition`,否则,该`Condition`的�?�变为`name = :name`.当然,`else`不是必须�???,如果`if`运算为假,直接删除该行`SQL`条件.

### 自定义类控制条件增减
决定�???个条件是否参与运�???,有时候需要根据多个不同的参数进行某种计算来决�???, 并且这种计算逻辑用JAVA脚本(非JS)难以表达或�?�不太乐意让JAVA脚本登场. 那么就使用`@Condition`中的`ignore`选项,指定�???个类,它叫`Judge`,是一个裁判员,条件是否去除的决定权可以理所当然地委托给自定义的`Judge`类来处理.   
举例: �???:年龄大于18及姓名不为空且包�???"Rex".�???,剔除条件`and name like :name`.  
定制�???个决定条件存活的�???,�???要遵循一些约�???: 继承`org.fastquery.where.Judge`,当完成这�???�???,IDE就会提示�???发�?�必须实现ignore方法, 否则,面对的是红叉. 这样的设计可以减少犯错的可能. 当`ignore`方法�???终返回`true`�???,�???,删除相对应的条件;当最后返回`false`�???,�???,保留条件.

```java
public class LikeNameJudge extends Judge {
	@Override
	public boolean ignore() {
		// 获取方法中名称为"age"的参数�??
		int age = this.getParameter("age", int.class);
		// 获取方法中名称为"name"的参数�??
		String name = this.getParameter("name", String.class);
		return age > 18 && name!=null && name.contains("Rex");
	}
}
```
在`LikeNameJudge`的`this`范围内可以获得当前DB方法的所有实�???.这些参数都有资格决定条件的存�???.   
指定 `LikeNameJudge`:

```java
@Query("select id,name,age from `userinfo` #{#where}")
@Condition("age > :age")
@Condition(value="and name like :name",ignore=LikeNameJudge.class)
Page<UserInfo> find(@Param("age")int age,@Param("name")String name,Pageable pageable);
```

其中,`ignore`选项默认指定`DefaultJudge`,它是�???个无�???事事的裁判员,当它是空气好�???.

若`@Condition`的�?�使用了`${表达式}`,`$表达式`,不管方法的参数传递了�???么都不会使条件移�???,因为`$`表达�???(或称之为EL表达�???)仅作为简单模版使�???,传null,默认会替换为""(空字符串).举例:

```java
@Query("select * from `userinfo` #{#where}")
@Condition("age between $age1 and ${age2}")
List<Map<String, Object>> between(@Param("age1") Integer age1,@Param("age2") Integer age2);	
```
该例中`@Condition`使用到了`$`表达�???,`$age1`,`${age2}`仅作为模板替�???,age1为null,即便设置`ignoreNull=true`也不会影响条件的增减.**总之,`$` 表达式不会动摇条件的存在**.  
单个`@Condition`针对出现多个`SQL`参数的情�???,�??? `@Condition("or age between ?5 and ?6")` �??? `@Condition("or age between :age1 and :age2")` 参数 `?5`、`?6`、`:age1`�??? `:age2`中的任意�???个为`null`都会导致该行条件移除.

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

## 改操�???
```java
// 返回修改之后�???影响的行�???
@Query("update student s set s.age=?3,s.name=?2 where  s.no=?1")
@Modifying
int update(String no,String name,int age); 

// 改成功了返回true,反之,false
@Modifying
@Query("delete from `userinfo` where id=?1")
boolean deleteUserinfoById(int id);

// 以实体bean格式,返回当前保存的数�???
@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
@Modifying(table="student",id="no")
// 注意: // 注意: student的主键是字符�???,因此不会自增�???,在此处需要用@Id标识哪个就是主键字段
Student addStudent(@Id String no,String name,String sex,int age,String dept);

// 以JSON格式,返回当前保存的数�???
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
JSONObject saveUserInfo2(String name,Integer age);

// 返回当前保存的数据的主键信息
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Primarykey saveUserInfo(String name,Integer age);
```

新增�???条记�???,返回实体,可以通过`@Modifying`中的`selectFields`配置项明确指定待查询的字�???. �???:

```java
// 以Map格式,返回当前保存的数�???
@Modifying(id="id",table="userinfo",selectFields="name,age")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Map<String, Object> addUserInfo(String name,Integer age);
```

其中,`selectFields` 默认�??? **<code>\*</code>**,字段与字段之间请用英文�?�号隔开.  

**注意**:
- 改操作返回int类型:表示影响的行�???,没有找到可以修改�???,那么影响行数�???0,并不能视为改失败�???
- 改操作返回boolean类型:表示是否改正�???,依据�???,影响行数若大于或等于0都会返回true,反之,返回false

## Annotation
针对FastQuery中的�???有注�???,做个说明:

| Annotation | 作用 |
|:---|:---|
|`@Id`|用来标识表主键|
|`@Table`|用来指定表名称|
|`@Modifying`|标识改操作|
|`@Param`|标识参数名称,便于运行期获取|
|`@Query`|标识查询语句|
|`@QueryByNamed`|标识根据命名式查�???(语句放在配置文件�???)|
|`@Source`|标识用来适配数据源的参数|
|`@Transactional`|事务|
|`@Transient`|标识实体中的属�?�是临时�???(例如:save对象�???,该属性不存储到数据库�???)|
|`@NotCount`|标识分页中不统计总行数|
|`@PageIndex`|标识页索引|
|`@PageSize`|标识页行数|
|`@Condition`|标识条件单元|
|`@Set`|标识设置字段单元|
|`@Before`|标识函数执行前|
|`@After`|标识函数执行后|
|`@SkipFilter`|标识跳过拦截器|

## QueryRepository的内置方�???
凡是继承`QueryRepository`的接�???,都可以使用它的方�???,并且不用写实现类.

| 方法 | 描述 |
|:---|:---|
| `<E> E find(Class<E> entityClass,long id)` | 根据主键查询实体 |
| `<E> int insert(E entity)` | 插入�???个实�???(主键字段的�?�若为null,那么该字段将不参与运�???),返回影响行数 |
| `<B> int save(boolean ignoreRepeat,Collection<B> entities)` | 保存�???个集合实�???,是否忽略已经存在的唯�???key(有可能是多个字段构成的唯�???key)记录 |
| `int saveArray(boolean ignoreRepeat,Object...entities)` | 保存�???个可变数组实�???,是否忽略已经存在的唯�???key(有可能是多个字段构成的唯�???key)记录 |
| `BigInteger saveToId(Object entity)` | 保存实体�???,返回主键�???.**注意**:主键类型必须为数字且自增�???,不支持联合主�??? |
| `<E> E save(E entity)` | 保存实体�???,返回实体 |
| `<E> int executeUpdate(E entity)` | 更新�???个实�???,返回影响行数.**注意**:实体的成员属性如果是null,那么该属性将不会参与改运�??? |
| `<E> E update(E entity)` | 更新�???个实�???,返回被更新的实体 |
| `<E> int executeSaveOrUpdate(E entity)` | 不存在就保存,反之更新(前提条件:这个实体必须包含主键字段,主键值若是null,直接�???),返回影响行数 |
| `<E> E saveOrUpdate(E entity)` | 不存在就保存,反之更新(前提条件:这个实体必须包含主键字段,主键值若是null,直接�???),返回被更新的实体或返回已存储的实�??? |
| `int update(Object entity,String where)` | 更新实体�???,自定义条�???(有时候不�???定是根据主键来修�???),若给where传�?�null�???"",默认按照主键修改,返回影响行数 |
| `<E> int update(Collection<E> entities)` | 更新集合实体,成员属�?�如果是null,那么该属性将不会参与改运�???,每个实体必须包含主键 |
| `int delete(String tableName,String primaryKeyName,long id)` | 根据主键删除实体,返回影响行数 |
| `int[] executeBatch(String sqlName)` | 根据指定的SQL文件名称或绝对路�???,执行批量操作SQL语句,返回int[],数组中的每个数对应一条SQL语句执行后所影响的行�??? |
| `int tx(Supplier<Integer> fun)` | 事务函数.fun的返回�?�等于tx的返回�??.fun返回null,-1或向上抛异常,tx会被回滚,并返�???-1 |
| `<E> long count(E entity)` | 根据指定的条件统计�?�记录数，实体属性若�??? null 值，则，该属性不参与运算，反之，参与 and 运算 |

举例说明:  
先准备一个实�???  

```java
public class UserInfo {
	@Id
	private Integer id;
	private String name;
	private Integer age;
	// getter /setter 省略...	
}
```

使用QueryRepository的内置函�???,必须要继承它:

```java
public interface StudentDBService extends QueryRepository {
   ... ...
}
```

**提醒**: 继承`Repository`适合应用于不使用内置函数的场�???,显然更加轻量�???.   

保存实体,更新实体,保存或更新实体示例如�???:

``` java
UserInfo u1 = new UserInfo(36,"Dick", 23);

// 保存实体
studentDBService.save(u1)

Integer id = 36;
String name = "Dick";
Integer age = null;
UserInfo u2 = new UserInfo(id,name,age);
// age是null�???, age就不会参与修改运算了.
studentDBService.update(u2); // 更新语句�???: update UserInfo set name = ? where id = ?

// 保存或更新实�???
studentDBService.saveOrUpdate(u1);
```

使用update�???,同时自定义条件的例子:

```java
Integer id = 1;
String name = "可馨";
Integer age = 3;
UserInfo entity = new UserInfo(id,name,age);
// 会解析成:update `UserInfo` set `id`=?, `age`=? where name = ?
int effect = studentDBService.update(entity,"name = :name");
// 断言: 影响的行数大�???0�???
assertThat(effect, greaterThan(0));

// 不想让id字段参与改运�???,那么就把它的值设置为null
entity.setId(null);
// 会解析成:update `UserInfo` set `age`=? where name = ?
effect = studentDBService.update(entity,"name = :name");
assertThat(effect, greaterThan(0));
```

批量更新(update),如果是把多条记录更新成相同的内容,没有�???么好说的.在此主要解决:批量更新不同字段,不同内容.  
举例:  
假设�???求是:

- 把id=77的用户的姓名修改�???"茝若",年龄修改�???18
- 把id=88的用户的姓名修改�???"芸兮",注意:不修改年�???
- 把id=99的用户的年龄修改�???16

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

�???终会解释成一条SQL语句:

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

## @Set 实现动�?�修改不同字�???

�???�???只需要修改表的中的个别字�???: A处需要修改table.x字段,B处要修改table.y字段,C处同时改x,y字段,. 设计`@Set`就是为了满足诸如此类的需�???. 根据传�?�参数的不同动�?�地增减�???要set的字�???,让一条SQL尽可能地满足多个要求.

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?5")
@Set("`name` = ?1") // ?1 若是 null 或是 "" , �???, 该行set移除
@Set("`credit` = ?2")
@Set("`semester` = ?3")
@Set("`period` = ?4")
int updateCourse(String name,Integer credit, Integer semester, Integer period, String no);
```

`#{#sets}` 用于引用设置选项. `@Set(value="name = ?1" , ignoreNull=true , ignoreEmpty=true)` 中的可�?�配置项,顾名思义.    

方法上的�???有`@Set`有可能全部被移除,那么就会得到�???个错误的SQL`update Course set where no = ?5`,避免此错误有两个方法: 1). 加一条不含有SQL参数的`@set`,�???: `@set("name = name")`,它永远不会被删除,并且不会对原有数据�?�成任何影响; 2).调用方法前对参数做校�???,以排除因为参数导致全部`@set`被丢弃的可能.  

单个`@Set`针对出现多个`SQL`参数的情�???,�??? `@Set("name = ?1","credit = ?2")` �??? `@Set("name = :name","credit = :credit")` 参数 `?1`、`?2`、`:name`�??? `:credit`中的任意�???个为`null`都会导致该行设置项被移除.  

### 通过JAVA脚本控制设置项增�???
`@Set`中的`ignoreScript`属�?�可以绑定一个JAVA脚本(非JS),根据脚本运行后的布尔结果,来决定是否保留设置项.脚本运行后的结果如果是`true`,那么就删除该设置�???,反之,保留设置�???,默认脚本是`false`,表示保留该设置项. 注意: 脚本执行后得到的结果必须是布尔类�???,否则,项目都启动不起来.  
举例:

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?3")
@Set(value="`name` = :name",
     ignoreScript=":name!=null && :name.startsWith(\"计算\") && :credit!=null && :credit.intValue() > 2")
@Set("`credit` = :credit")
int updateCourse(@Param("name") String name,@Param("credit") Integer credit,String no);
```
其中, `:credit`引用的是`@Param("credit") Integer credit`的实参�??.`:name`是`@Param("name")String name`的实参�??.这个脚本要表达的意�?�不�???而喻. 不过脚本的解析能力还不能自动**拆箱**(unboxing),�???要调用拆箱方�???,请留意住`:credit.intValue() > 2`. 若写成`:credit > 2`是编译不了的. 其他包装类型`Short`, `Long`, `Byte`, `Boolean`, `Character`, `Float`, `Double` 以此类推.  
脚本的编译工作在项目初始化阶段完�???,因此不存在�?�能问题.建议不要把脚本写得太�???,那样会破坏可读�??.

### @Set 中的 if...else
这个`SQL`设置项是否保�???,可以通过`if`...`else`...来确�???.`if`的表达式用`=`号与之绑�???.`if`成立,�???,保留当前设置�???,反之,就取`else`�???指定的�??.当然,`else`在语法上不是必须�???,若不写`else`,`if`条件不成�???,�???,直接删除当前`@Set`.  
举例:

```java
@Modifying
@Query("update `User` #{#sets} where id = ?3")
@Set(value="`name` = :name",if$="!:name.contains(\"root\")",else$="`name` = name")
int updateUser(@Param("name") String name,int id);
```
其中,如果`name`的�?�不包含"root",就保留`"name = :name"`这个设置选项,否则,设置选项为`name = name`(表示`name`的�?�保持原�???).

### 自定义类控制设置项增�???
决定�???个Set项是否参与运�???,可以根据多个参数进行某种计算来决�???,`@Set`允许关联�???个自定义的`Judge`�???,作为这种计算的载�???.  
举例: �???: name值的前缀�???"*计算*" 并且 credit的�?�大�???2, �???,删除`name = :name`这条设置�???.  
NameJudge �???:

```java
public class NameJudge extends Judge {
	@Override
	public boolean ignore() {
		// 获取方法中名称为"name"的参数�??
		String name = this.getParameter("name", String.class);
		// 获取方法中名称为"credit"的参数�??
		Integer credit = this.getParameter("credit", Integer.class);
		return name.startsWith("计算") && credit!=null && credit > 2;
	}
}
```
设置项绑�??? NameJudge: 

```java
@Modifying
@Query("update `Course` #{#sets} where no = ?3")
@Set(value="`name` = :name",ignore=NameJudge.class)
@Set("`credit` = :credit")
int updateCourse(@Param("name") String name,@Param("credit") Integer credit,String no);
```
关于修改`name`的那个设置项, 有三种可能使它作�???: �??? name的�?�是null; �??? name的�?�是""; �??? NameJudge类的ignore方法返回了`true`.

根据参数动�?�增减set不同字段,除了用`@Set`实现之外,别忘了还有其他几种解决办�???: a.调用内置方法`int executeUpdate(E entity)`,实体的字段若是`null`�???,那么,该字段将不会参与set运算; b.使用SQL模版,在里头做逻辑判断; c.采用`QueryBuilder`; d.采用`$表达式`. �???发�?�将会发现很难不能�?�择出�?�合的解决方�???.

## 事务

### 用`@Transactional`实现�???单事�???

```java
// 将三条改操作纳入到一个事务中.
@Transactional
@Modifying
@Query("update `userinfo` set `name`=?1 where id=?3")
@Query("update `userinfo` set `age`=?2 where id=?3")
// 把主键id修改�???1,目前主键id=1是存在的.这行会报�???.那么前两行所做的操作全部失效.
@Query("update `userinfo` set `id`=1 where `id`=?3")
int updateBatch(String name,Integer age,Integer id);
// 注意: 
// 1).返回值如果是int类型,表示这个事务成功提交后所有改操作�???影响的行数�?�和.
// 2).返回值如果是int[]类型,表示这个事务成功提交�???,每个�???小修改单元所影响行数的集�???.
//    举例说明: 若有个事务T,它里面有3条改操作,分别叫U1,U2,U3. T成功提交�???,U1,U2,U3�???影响的数据行数分别为N1,N2,N3.
//    �???: 返回值为: new int[]{N1,N2,N3}
```

### 事务函数式接�???
在`QueryRepository`中提供了�???个内置事务函数`tx`.支持多个数据源加入到同一个事务里.

```java
int effect = userInfoDBService.tx(() -> {
	// 把需要纳入到�???个事务内的改操作放入到这里来
	// update1
	// to do...
	// update2
	// return 影响行数;
});
```

以上`Lambda`表达�???,`()->{}`中的`{}`里的�???有操作是原子性的,要么统统成功,要么全部失败回滚.在`{}`里抛出异常或`return null`或返�???-1,都会导致`{}`全体回滚并返�???-1.`Lambda`表达式对**�???**封闭,�???**变量**�???�???(Lambda expressions close over values,not variables),正因为这个特�???,不能在`{}`中修改外界的�???,但是可以给外界的对象设置�???. 

```java
... ...
Map<String, Object> map = new HashMap<>();
int sum = 0;
tx(() -> {
     sum = sum + 1; // 编译报错,不能修改sum的�??(Illegal, close over values)
     map.put(K, V); // 这是允许�???(Legal, open over variables)
});
```
因此,要想把`{}`中处理的数据拿出来使�???,将其设置给一个外界的对象就行�???. `tx`方法被回滚后会返�???-1.

## @Param参数

**SQL中使用冒号表达式**

```java
@Query("select name,age from UserInfo u where u.name = :name or u.age = :age")
UserInfo[] findUserInfoByNameOrAge(@Param("name") String name, @Param("age")Integer age);
```

其中`:name`对应`@Param("name")`�???指定的方法变量�??;`:age`对应`@Param("age")`�???指定的方法变量�??.当然SQL中的变量也可以用`?N`(N={正整数})的形式来表达,且不用标识`@Param`.  
�???:`select name,age from UserInfo u where u.name = :name or u.age = :age`以防SQL注入问题,在执行语句之�???,�???终会被编译成`select name,age from UserInfo u where u.name=? or u.age=?`

> **注意**: 有时候在`@Query`中使用`:`不一定是表达�???,而是字面字符.为了避开跟冒号表达式冲突,请额外加�???个`:`以起到转义作�???.

**SQL中的变量采用${name}表达�???**  
实现原样替换,当然,也可以写成`$name`.不过请注意避免SQL注入问题.   

```java
@Query("select * from `userinfo` where ${one} ${orderby}")
UserInfo findUserInfo(@Param("orderby") String orderby, @Param("one") int i);
// String orderby 这个形参接受到的值会原样取代�??? "${orderby}", orderby 如果接受到的值为null,那么${orderby}默认�???""
// int i 接受到的值会取代�??? "${one}"

// 假设: orderby的�?�为: "order by age desc", i的�?�为:1
// �???: �???终的SQL�???: "select * from `userinfo` where 1 order by age desc"
```

### 采用${name}时请注意: 
- 传�?�null�???,模板变量默认�???""
- 参数模板仅仅用来辅助�???发�?�构建SQL语句
- 请提防使用不�???,引发SQL注入问题
- 请避免模板参数的值完全来源于用户层的输入
- 请确保参数�?�可�???.  

通过`defaultVal`属�?�指�???:若参数接受到null�???,应该采用的默认�??(该属性不是必须的,默认�???"").例如:

```java
@Query("select * from `userinfo` ${orderby}")
// orderby 若为null, 那么 ${orderby}的�??,就取defaultVal的�??
JSONArray findUserInfo(@Param(value="orderby",defaultVal="order by age desc") String orderby);
```

## 微笑表达�???
定义: **�???<code>\`-</code> 作为�???�???,�???<code>-\`</code>作为结尾,包裹�???若干字符,因为<code>\`- -\`</code>酷似微笑表情,因此将这样的表达式称之为`微笑表达式`.** <br>例如: <code> \`-%${name}%-\` </code>. **\`** 反撇号的位置如下图所�???:<br>
![反撇号示意图](file/fanpie.png "反撇号示意图")    
作用:  
1.可以作为实参的模�???,举例: 查询出姓"�???"的用�???.没有`微笑表达式`时的写法:
```java
db.findLikeName(name + "%");
```
这种写法不好,实参和模糊关键字`%`被融在一起了.实参是程序语�???特�??,而`%`是`SQL`特�??,把`%`放在`@Query`里或`SQL`模板里更为�?�合.  
现在有`微笑表达式`�???,在模板中,可以配置name实参的模�???.假设模板中�?�过<code>\`-:name%-\`</code>引用这个实参,那么<code>\`-:name%-\`</code>将会作为这个实参的模�???. name的�?�为"�???",实际上传递的�???"�???%".   
举例:

```java
@Query("select * from UserInfo where id > :id and age > 18 or name like `-%:name%-`")
```

�???

```xml
<?xml version="1.0" encoding="UTF-8"?>
<queries>
	<query id="findUserInfo">
		select * from UserInfo where id > :id and age > 18 or name like `-'%:name%'-`
	</query>
</queries>
```

2.采用`微笑表达式`的片�???,会过滤敏感关键字,严格防止SQL注入. 建议将其用在`$表达式`/`${表达式}`�???,因为 **$表达式的存在仅仅是为了开发�?�方便构建SQL**,使用不当很危�???,加上`微笑表达式`可以防止由于�???发�?�的疏忽而引发的SQL注入问题.**注意**: 冒号表达�???,如`:name`�???终会解释成SQL占位符`?`�???,因此不存在注入问�???,不必使用`微笑表达式`来预�???.

##  SQL IN

### 使用"?"索引方式
```java
@Query("select * from UserInfo where name in (?1)")
List<UserInfo> findByNameIn(String...names);

@Query("select * from UserInfo where name in (?1) and id > ?2")
List<UserInfo> findByNameListIn(List<String> names,Integer id);
```

参数如果是一个空集合或空数组,那么`in`中的`?`�???对应的�?�是`null`. `not in`结果集中若含有`null`,�???,查询结果为`null`. `in` 结果集含有`null`不会影响正常查询.

```sql
id not in (1,2,null) -- 查不�???
id in (null)         -- 并不会把id为null的记录查出来,id是null与否,�???终查不出.
id in(1,2,null)      -- id�???1或为2的结果会被查询出�???
```

### 使用冒号表达�???
```java
@Query("select * from student where sex = :sex and age > :age and name in(:names)")
List<Student> findByIn(@Param("sex")String sex,@Param("age")Integer age,@Param("names")Set<String> names);
```

## @QueryByNamed命名式查�???
就是把`SQL`语句写在配置文件�???(在配置文件中可以进行逻辑判断),然后用`@QueryByNamed`绑定配置文件中的id�???,以便引用到解析后的`SQL`.       
配置文件的命名格�???: `类的长名�???(包含包地�???).queries.xml`,每个类文件对应一个配置文�???,请放到`classpath`目录�???.  
配置文件里的SQL代码�???,会被**Velocity**的模板引擎所渲染,因此,很方便写出复杂的动�?�SQL语句.    
例如: `org.fastquery.dao.QueryByNamedDBExample.queries.xml`  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<queries>
	<query id="findUserInfoAll">
		select id,name,age from UserInfo
	</query>

	<query id="findUserInfoOne">
		<value>
			## :id �???终会替换�??? ?
			## ${id} 不会替换还成"?",引用的是参数源�??
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

假如�??? XML 文档中放置了类似 `<` �??? `&` 字符,那么这个文档会产生一个错�???,这是因为 XML 解析器会�??? `<` 解释为新元素的开�???,为了避免此类错误,可以将模板代码片段定义为CDATA. XML 解析器会把CDATA�???包含的内容当作字符串处理.CDATA 部分由`<![CDATA[` �???�???,�??? `]]>`结束.   
若不用CDATA,那么有些字符必须采用**命名实体**的方式引�???. �??? XML 中有 5 个预定义的实体引�???:

| 字符 | 命名实体 | 实体编码 | 说明 |
|:-----:|:----:|:----:|:----:|
|  &lt;   | &amp;lt;  | &amp;#60; | 小于�??? |
|  &gt;   | &amp;gt;  | &amp;#62; | 大于�??? |
|  &amp;  | &amp;amp; | &amp;#38; | 与符�??? |
|  &apos; | &amp;apos;| &amp;#39; | 单引�??? |
|  &quot; | &amp;quot;| &amp;#34; | 双引�??? |

如果想把�???些公用的SQL代码片段提取出来,以便重用,通过定义`<parts>`元素(零件�???)就可以做�???. 在`<value>`,`<countQuery>`元素�???,可以通过`#{#name}`表达式引用到名称相匹配的零件.�???:`#{#condition}`表示引用name="condition"的零�???.  
若`<parts>`元素跟`<query>`保持并列关系,那么该零件集是全�???�???.当前文件里的`<query>`都能引用�???.  
�???个非分页的函�???,如果绑定的模板中包含`<countQuery>`,那么这个函数只会提取`<query>`语句,而不会提取计数语�???.

```java
public interface QueryByNamedDBExample extends QueryRepository {

	// 从该类的配置文件里寻找id="findUserInfoAll"节点,然后绑定其SQL代码�???
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

`@QueryByNamed` 中的value值如果没有指�???,默认是当前方法名.

```java
@QueryByNamed
public List<Student> findSomeStudent();
```

等效�??? `@QueryByNamed("findSomeStudent")`  

`@QueryByNamed` 中的`render`属�??,表示是否启用模板引擎对配置文件进行渲�???,默认是`true`表示�???�???. 如果`<query>`节点中没有使用到任何模板语法,仅用于存储目�???,那么建议设置为`false`.`:expression`,`?N`,`$expression`这些都不依赖模板引擎.  

**注意**: `$name`和`:name`这两种表达式的主要区别是—�?�`$name`表示引用的是参数源�??,可用于在模板中做逻辑判断,而`:name`用于标记参数�???,SQL解析器会将其翻译成`?`�???.  

在模板中`:expression`表达式或`?N`表达式可以作为`SQL`函数的�?�辑判断表达�???,如跟这些函数�???起参与运�???:`IF(expr1,expr2,expr3)`,`IFNULL(expr1,expr2)`,`NULLIF(expr1,expr2)`,`ISNULL(expr)`.  

```sql
-- 方法的第1个参数的值可以影响where的条�???
select t.A from (select 11 as A,22 as B,33 as C) as T where if(?1 > 10,t.B>10,t.C>100)
-- 方法的第2个参数的值可以影响查询集
select if(?2 > 10,'大于10','不大�???10') as msg
-- 名称�???"number"的参�???,其�?�可以影响where条件
select t.A from (select 11 as A,22 as B,33 as C) as T where if(:number > 10,t.B>10,t.C>100)
-- 名称�???"number"的参�???,其�?�可以影响查询集
select if(:number > 10,'大于10','不大�???10') as msg
```

允许多个方法绑定同一个模板id. 在模板中使用`${_method}`可以引用到当前方法的`org.fastquery.core.MethodInfo`对象,该对象是反射`java.lang.reflect.Method`的缓�???.  
�???: 根据当前方法名称的不同取不同的`SQL`语句

```java
public interface QueryByNamedDBExtend extends QueryRepository {
	@QueryByNamed(render = false)
	JSONArray findUAll();
	
	// 两个方法指定同一个模板id�???
	@QueryByNamed("findSome")
	JSONArray findLittle();
	@QueryByNamed("findSome")
	JSONArray findSome();
}
```

org.fastquery.dao.QueryByNamedDBExtend.queries.xml 模板文件的内�???: 

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
		## 如果当前方法的名称等�??? "findLittle"
		#if( ${_method.getName()} == "findLittle" )
			## �???3�???  
			select #{#feids} from UserInfo limit 3
		#else 
		   select `no`, `name` from Student limit 5
		#end  
		]]>
	</query>	
</queries>
```

其中 `${_method.getName()}` 可简写成 `${_method.name}`. 在`Velocity`里调用对象或方法,不是本文的重�???,点到为止.

## QueryBuilder
上面介绍了`SQL`不仅可以绑定在`@Query`�???, 也可以写到`XML`�???. 还有另一种方�???,**通过构�?�QueryBuilder对象**构建`Query`语句.  
用法举例:

```java
@Query
Page<Map<String, Object>> pageByQueryBuilder(QueryBuilder queryBuilder,Pageable pageable);
```

如果分页不要求得到�?�页�???,在接口的方法上加`@NotCount`便可(谁说分页�???定要执行count语句?).

不用去实现那个接�???,直接调用:

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
assertThat("断言：执行过的sql有两�???",executedSQLs.size(), is(2));
assertThat(executedSQLs.get(0), equalTo("select id,name,age from userinfo where age > ? and id < ? limit 0,3"));
assertThat(executedSQLs.get(1), equalTo("select count(name) from userinfo where age > ? and id < ?"));
```

引用问号表达�???(?expression) , 冒号表达�???(:expression), 其中?1表示方法的第�???个参�???,`:age`表示匹配`@Param("age")`那个参数,采用问号或冒号表达式不会有注入问�???.

**如果要查的表是一个变�???(甚至表是自动生成�???),要查的字段也是变�???,条件单元的可选范围也是个变量,整个 SQL 都是动�?�生成的,在这种情形就只能�??? `QueryBuilder`, 使用`@Query`模板,就无能为力了,`QueryBuilder`有不可取代的功能**.不过,能用`@Query`模板解决问题,就尽量使用它,因为它的设计,只用写一个抽象方�???,零实�???,让你没办法去写第二行 Java 代码,从设计上让你无法犯错.

## 支持存储过程

只支持in(输入)参数,不支持out(输出参数), 如果想输出存储过程的处理结果,在过程内部使用`select`查询输出.  
举例:  
插入�???条学�???,返回学生的�?�记录数和当前编�???,存储过程语句:

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
要处理查询语句的参数,只需定义方法参数,为了在运行时对参数名称可见就额外加上`@Param`,上面有很多示�???.另外,方法的设计还能识别某些特殊的类型,如`QueryBuilder`,`Pageable`,以便核心能智能地将动态构建查询和分页应用于查询中.

- 通过`@QueryByNamed`实现分页

```java
@QueryByNamed("findPage") // 引用id�???"findPage"的分页模�???
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

	<!-- 定义零件�???,他们可以被value,countQuery节点引用,以达到复用的效果 -->
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
- `#{#limit}`是分页模板的内置零件,表示分页区间. `#{#limit}`默认是放在尾�???,在符合`SQL`语法的前提下也可以把它放在`SQL`语句中的其他地方
- 动�?�条件部分若用`<where>`元素进行包裹,会自动处理好条件连接符问�???(避免出现where紧接`or`或`and`)
- `<value>`和`<countQuery>`节点引用的零件若包含`<where>`元素,零件解析成字符串后会自动加上 *"where"* ,请不要在引入切口处重复追�??? *"where"* 字符�???

- 通过@Query实现分页

```java
public interface UserInfoDBService extends QueryRepository {

	// Pageable 用做描述当前页的索引和每页条�???.
    
	// countField : 明确指定用来统计总行数的字段,count(countField)中的countField默认值是"id"
	@Query(value="select id,name,age from `userinfo` where 1",countField="id")
	Page<Map<String, Object>> findAll(Pageable pageable);
	
	// 如果没有指定count语句,那么由fastquery分析出最优的count语句
	@Query("select id,name,age from `userinfo` #{#where}")
	@Condition("age > ?1")     // 若age的�?�传递null,该条件将不参与运�???
	@Condition("and id < ?2")  // 若id的�?�传递null,该条件将不参与运�???
	Page<UserInfo> find(Integer age,Integer id,Pageable pageable);
	
	// countQuery : 指定自定义count语句
	@Query(value = "select id,name,age from `userinfo` #{#where}", 
	       countQuery = "select count(id) from `userinfo` #{#where}")
	@Condition("age > ?1")        // 若age的�?�传递null,该条件将不参与运�???
	@Condition("and id < ?2")     // 若id的�?�传递null,该条件将不参与运�???
	Page<UserInfo> findSome(Integer age,Integer id,Pageable pageable);
}
```

### @PageIndex和@PageSize
`@PageIndex` 用来指定当前页索�???,�???1�???始计�???,如果传�?�的值小�???1,依然视为1   
`@PageSize`  用来指定当前页应该显示多少条数据,如果传�?�的值小�???1,依然视为1   
**注意**: 该注解组合不能和`Pageable`�???起使�???  
例如:

```java
@NotCount // 分页不统计�?�行�???
@Query(value = "select id,name,age from `userinfo`")
Page<Map<String,Object>> findSome(Integer age,Integer id,@PageIndex int pageIndex,@PageSize int pageSize);
```


### 使用分页     
`Page`是分页的抽象,通过它可以获取分页中的各种属�???,并且不用�???发�?�去实现.

```java
int p = 1;    // 指定访问的是第几�???(不是�???0�???始计�???)
int size = 3; // 设定每一页最多显示几条记�???
Integer age=10,id = 50;
Pageable pageable = new PageableImpl(p, size);
Page<UserInfo> page  = userInfoDBService.findSome(age, id,pageable);
List<UserInfo> userInfos = page.getContent(); // 获取这页的数�???
Slice slice = page.getNextPageable();         // 下一�???
int number = page.getNumber();                // 当前页数(当前是第几页)
// 更多 page.? 不妨亲自去试试看
```

`Page`转换成`JSON`后的结构如下:

```js
{
    "content":[                 // 这页的数�???
		{
			"name":"查尔斯·巴贝奇","id":2,"year":1792
		},
		{
			"name":"约翰·冯·诺依曼","id":3,"year":1903
		},
		{                     
			"name":"阿兰·麦席森·图�???","id":1,"year":1912
		},
		{
			"name":"约翰·麦卡�???","id":4,"year":1927
		},
		{
			"name":"丹尼斯·里�???","id":5,"year":1941
		},
		{
			"name":"蒂姆·伯纳斯·李","id":6,"year":1955
		}
    ],
    "first": true,           	// 是否是第�???�???
    "hasContent": true,      	// 这页是否有数�???
    "hasNext": true,         	// 是否有下�???�???
    "hasPrevious": false,    	// 是否有上�???�???
    "last": false,           	// 是否是最后一�???
    "previousPageable": {    	// 上一页的基本属�??
        "number": 0,         	// 定位的页�???
        "size": 15           	// 期望每页多少条数�???
    },
    "nextPageable": {        	// 下一页的基本属�??
        "number": 1,         	// 定位的页�???
        "size": 15           	// 期望每页多少条数�???
    },
    "number": 1,             	// 当前页码,�???1�???�???
    "size": 15,              	// 期望每页行数(numberOfElements表示真正查出的条�???)
    "numberOfElements": 6,  	// 当前页的真实记录行数
    "totalElements": 188,    	// 总行�???
    "totalPages": 13         	// 总页�???
}
```

### 注意:
- 如果在分页函数上标识`@NotCount`,表示在分页中不统计�?�行�???.那么分页对象中的`totalElements`的�?�为-1L,`totalPages`�???-1.其他属�?�都有效并且真实.    
- 如果明确指定不统计行�???,那么设置`countField`和`countQuery`就会变得无意�???.    
- `#{#limit}`不仅能使用在 XML 文件�???,也可以使用在`@Query`�???,无特殊要�???,建议不要指定`#{#limit}`.

### 扩展分页实现
目前该框架默认支持分页的数据库有`MySQL`,`Microsoft SQL Server`,`PostgreSQL`,因此,扩展的空间非常大,并且非常容易.实现`org.fastquery.page.PageDialect`�???,有针对�?�地重写相关方法,解决`SQL`中的差异.欲了解更多细节请参�?�`org.fastquery.dialect.MySQLPageDialect`,`org.fastquery.dialect.PostgreSQLPageDialect`.

## JavaScript分页插件
[PJAXPage](https://gitee.com/xixifeng.com/pjaxpage)分页插件,完美支持`Page`数据结构.        
项目地址: https://gitee.com/xixifeng.com/pjaxpage      
使用例子: http://xixifeng.com.oschina.io/pjaxpage/example/   

## 执行SQL文件
```java
String sqlName = "update.sql";
int[] effects = studentDBService.executeBatch(sqlName);
```

- sqlName 指定基准目录下的SQL文件. 注意: 基准目录在fastquery.json里配�???,sqlName 为绝对路径也�???
- 返回 `int[]`类型,用于记录SQL文件被执行后�???影响的行�???.�???,effects[x] = m 表示第x行SQL执行后影响的行数是m; effects[y] = n 表示第y行SQL执行后所影响的行数是n
- 判定SQL文件里有多少条语�???,依据以分号分割的结果作为标准
- 只支持整行注�???,以`#`或`--`�???头的行将视为注释

�???个数据源可能管理�???多个数据�???,执行的SQL文件也有可能�???要根据参数的不同而服务于不同的数据库.或�?�说SQL文件里有动�?�的部分,�???要根据传递的参数加以区分.那么,可以使用`executeBatch(String sqlName,String[] quotes)`,第二个参数可以被SQL文件�???�???,引用方式为`$[N]`,表示引用数组的第`N`个元�???.

```sql
drop table if exists $[0].demo_table;
```

## 动�?��?�配数据�???
### 创建数据�???
如果想在项目运行期间动�?�创建一个新数据�???,那么请使用`FQuery.createDataSource`.

```java
// 数据源名�???
String dataSourceName = "xk1";

// 连接池配�???
Properties properties = new Properties();
properties.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
properties.setProperty("jdbcUrl", "jdbc:mysql://db.fastquery.org:3306/xk1");
properties.setProperty("user", "xk1");
properties.setProperty("password", "abc1");

// 创建数据�???
FQuery.createDataSource(dataSourceName, properties);
```

### 适配数据�???
使用`@Source`动�?��?�配当前`Repository`的方法应该采用哪个数据源. 显然这个功能很有�???.      
在多租户系统�???,数据库彼此隔�???,表结构一�???.那么使用这个特�?�是非常方便�???.    
**注意:** `@Source`如果标识在参数前�???,那么该参数只能是字符串类�???.

```java
@Query("select id,name,age from `userinfo` as u where u.age>?1")
Map<String, Object> findOne(Integer age,@Source String dataSourceName);
```

### 适配数据源的优先�???
如果在fastquery.json文件里明确指定了数据源的作用�???,同时接口函数也存在`@Source`,那么以`@Source`指定的数据源优先,其次是配置文�???.

## 扩展支持数据连接�???
默认已经支持的连接池�???,`c3p0`,`druid`,`hikari`...当然,�???发�?�很容易在此基础上进行扩�???.  
示例,让`FastQuery`支持自定义的连接�???.实现过程如下:  
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
		
		// 创建数据源实�???
		return new MyDataSource(props);
	}

}
```
步骤2: 在`pool-extend.xml`里注�???

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
            "config": "mypool", // 用这个池提供数据�??? 
            "dataSourceName": "hiworld",
            "basePackages": [
                "<your.domain>.XxxDBService"
            ]
        }
    ]
}
```

## @Before拦截�???
在执行方法之前拦�???  
- 准备�???个BeforeFilter

```java
 /**
  * @author xixifeng (fastquery@126.com)
  */
 public class MyBeforeFilter1 extends BeforeFilter<QueryRepository> {

 	@Override
 	public void doFilter(QueryRepository repository, Method method, Object[] args) {
 	
 		// repository: 当前拦截到的实例
 		// method: 当前拦截到的方法
 		// args: 当前传�?�进来的参数�???,args[N]表示第N个参�???,从第0�???始计�???.
 		
 		// this.abortWith(returnVal); // 中断拦截�???,并指定返回�??
 		// 中断后立马返�???,针对当前方法后面的所有Filter将不会执�???
		
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

## @After拦截�???
在执行方法之�???,即将返回执行结果之前拦截  
```java
/**
 * @author xixifeng (fastquery@126.com)
 */
public class MyAfterFilter extends AfterFilter<QueryRepository> {

	@Override
	public Object doFilter(QueryRepository repository, Method method, Object[] args, Object returnVal) {
		
		// repository: 当前拦截到的实例
		// method: 当前拦截到的method
		// args: 当前传�?�进来的参数�???,args[N]表示第N个参�???,从第0�???始计�???.
		// returnVal 即将返回的�??
		
		// 在这里可以中途修�??? returnVal
		
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

## 控制拦截器的作用�???
�???: �???个拦截器继承自`F<T>`,那么:这个拦截器的作用范围只能在`T`类或`T`的子类里.<br />
举例:
```java
// 这个拦截器的作用范围�??? DataAcquireDbService里或在DataAcquireDbService子类�???.
// 换言�???: MyBeforeFilter3这个拦截器只能标注在DataAcquireDbService里或标注在DataAcquireDbService的子类里.
// 否则,程序不能顺利通过初始化阶�???.
public class MyBeforeFilter3 extends BeforeFilter<DataAcquireDbService> { 
     // some code ... ...
}
```

### @SkipFilter
跳过当前接口绑定的所有非默认的Filter(系统默认的Filter不会跳过).<br />
举例:

```java
@SkipFilter // 标识该方法将不受"自定义Filter"的约�???
@Query("select no from `course` limit 1")
String findOneCourse();
```

### 注意:
- `@Before`和`@After`不仅可以标注在接口类�???,也可以标注在方法�???
- 标识在类的上�???:表示其拦截的作用范围是整个类的方�???
- 标识在方法上:表示其拦截的作用范围是当前方�???
- �???个方法的拦截器�?�和=它的�???属类的拦截器+自己的拦截器

## WEB 支持
### 应用�??? Jersey 环境

```xml
<dependency>
	<groupId>org.glassfish.jersey.containers</groupId>
	<artifactId>jersey-container-servlet</artifactId>
	<version>2.27</version>
</dependency>

<dependency>
	<groupId>org.glassfish.jersey.inject</groupId>
	<artifactId>jersey-hk2</artifactId>
	<version>2.27</version>
</dependency>
```

让Jersey容器管理FastQuery:

```java
import javax.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class MyApplication extends ResourceConfig {
	public MyApplication() {
		// 绑定FastQuery	      
		org.fastquery.jersey.FQueryBinder.bind(this);
	}
}
```

FastQuery支持JAX-RS注解,不需实现�???,便能构建极简的RESTful.不得不简单的设计,可见�???�???.

```java
@Path("userInfo")
public interface UserInfoDBService extends QueryRepository {

	// 查询并实现分�???
	@Path("findAll")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Query(value = "select id,name,age from `userinfo` where 1", countField = "id")
	Page<Map<String, Object>> findAll(@QueryParam("pageIndex") @PageIndex int pageIndex,
			                          @QueryParam("pageSize")  @PageSize  int pageSize);
   
}
```

没错, **不用去写任何实现�???**, 访问 `http://<your host>/rest/userInfo/findAll?pageIndex=1&pageSize=5`, 就可以看到效�???.  
**DB接口不仅能当做WEB Service(服务),同时也是�???个DB接口**.除非逻辑是数据即服务,否则,不提倡`DAO`层跟`HTTP`服务融在�???�???.JAX-RS Resource的实现类,在WEB容器初始化之前就已经被`FastQuery`推导创建好了.

### 配置支持HttpSign
[HttpSign](https://github.com/xixifeng/httpsign) 是一种RESTful接口签名认证的实�???.  

```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>httpsign</artifactId>
    <!-- 请从 https://gitee.com/xixifeng.com/httpsign �??? maven 中央仓库中查阅最新版�??? -->
    <version>1.0.3</version>
</dependency>
```

用法很简�???,在方法上标识`@Authorization`便可.

```java
@org.fastquery.httpsign.Authorization
@Path("findById")
@GET
@Produces(MediaType.APPLICATION_JSON)
@Query("select id,name,age from UserInfo where id = :id")
JSONObject findById(@QueryParam("id") @Param("id") Integer id);
```

当然,如果不喜欢太�???�???,可以把DB接口注入到JAX-RS Resource类中:

```java
@Path("hi")
public class Hi {

	@javax.inject.Inject
	private UserInfoDBService db;
	
	@GET
	@Produces({"text/html"})
	public String hi() {
	      // use db...
	      return "hi";
	}
}
```

## 测试FastQuery
FastQuery提供的测试方式能轻松解决如下问题.
- 运行时获取SQL和它的参数�??,以便�???发�?�验证生成的SQL是否跟期望�?�一�???.
- 运行DB方法后自动回滚数据库事务.

`FastQueryTestRule` 实现了Junit中的 `TestRule` �???,用来扩展测试用例.可以在测试方法中获取执行过的SQL语句及SQL�???对应的参数�??,以便做断�???.加上`@Rollback`注解,可以用来控制测试方法执行完毕之后是否让数据事务回滚或提交.测试方法结束后默认自动回�???,既可以达到测试效�???,又不影响数据�???(可回滚到改之前状�???). 如下是例�???,请留意注�???.

```java
// junit fastquery的扩�???
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
	// 获取DB操作�???绑定的SQL
	List<SQLValue> sqlValues = rule.getListSQLValue();
	// 断言: studentDBService.update 执行后产生的SQL为一�???
	assertThat(sqlValues.size(), is(1));
	SQLValue sqlValue = sqlValues.get(0);
	// 断言: �???产生的SQL等于"update student s set s.age=?,s.name=? where  s.no=?"
	assertThat(sqlValue.getSql(), equalTo("update student s set s.age=?,s.name=? where  s.no=?"));
	// 获取SQL参数列表
	List<Object> values = sqlValue.getValues();
	// 断言: 这条SQL语句中一共有3个参�???
	assertThat(values.size(), is(3));
	// 断言: SQL的第�???个参数是Integer类型,并且他的值等于age
	assertThat(values.get(0).getClass() == Integer.class && values.get(0).equals(age), is(true));
	// 断言: SQL的第二个参数是String类型,并且他的值等于name
	assertThat(values.get(1).getClass() == String.class && values.get(1).equals(name), is(true));
	// 断言: SQL的第三个参数是String类型,并且他的值等于no
	assertThat(values.get(2).getClass() == String.class && values.get(2).equals(no), is(true));
}
```

并不是绑定了多少条`SQL`就一定执行多少条.就拿分页来说,并不是�?�会执行`count`,查不到数据时,就没有必要发出`count`语句.使用`rule.getExecutedSQLs()`可以取得已被执行过的`SQL`.每个`DB`方法执行之前都会清除历史记录从新统计.

```java
assertThat(db.findPageWithWhere(id, cityAbb, 6,pageSize).isHasContent(), is(true));
//  获取上行执行�???,�???执行过的sql
List<String> executedSQLs = rule.getExecutedSQLs();
// 断言已经执行�???2条sql语句
assertThat(executedSQLs.size(), is(2));
// 断言第二条sql�???...
assertThat(executedSQLs.get(1), equalTo("select count(id) from City where id > ? and cityAbb like ?"));

assertThat(db.findPageWithWhere(id, cityAbb, 7,pageSize).isHasContent(), is(false));
// 获取上行执行�???,�???执行过的sql
executedSQLs = rule.getExecutedSQLs();
assertThat(executedSQLs.size(), is(1));
assertThat(executedSQLs.get(0), not(containsString("count")));
```

`FastQuery`已经迭代了很�???,每次发布新版本是如何保证之前的功能不受影响的�????那是因为`FastQuery`的每个功能特性都有非常缜密的断言测试,发布时把能否通过�???有断�???做为先决条件,当然也得益于深�?�熟虑的设计.`Junit`是众多Java框架�???,真正有用的为数不多的其中之一,`FastQuery`乐此不疲.

## fastquery.json其他可�?�配置�?�项:

| 属�?�名 | 类型 | 默认�??? | 作用 | 示例 |
|:-----:|:-----:|:-----:|:-----|:-----|
| basedir | string | �??? | 基准目录,注意: 后面记得加上 "/" <br> 该目录用来放SQL文件,�???要执行SQL文件�???,指定其名称就够了 | "/tmp/sql/" |
| debug | boolean | false | 在调试模式下,可以动�?�装载xml里的SQL语句,且不用重启项�???<br>默认是false,表示不开启调试模�???.提醒:在生产阶段不要开启该模式 | false |
| queries | array | [ ] | 指定*.queries.xml(SQL模板文件)可以放在classpath目录下的哪些文件夹里.<br>默认:允许放在classpath根目录下<br>注意:配置文件的位置不�???定基于classpath目录,也可以�?�过`"fastquery.config.dir"`另行指定,上文已经提及�???.每个目录前不用加"/",目录末尾�???要加"/" | ["queries/","tpl/"] |
| slowQueryTime | int | 0 | 设置慢查询的时间�???(单位:毫秒; 默认:0,表示不开启慢查询功能), 如果 `QueryRepository` 中的方法执行超过这个时间,则会警告输出那些执行慢的方法,以便优化 | 50 |

## 源码

- https://gitee.com/xixifeng.com/fastquery
- https://github.com/xixifeng/fastquery

## �???发环�???
仅仅是建�???,并不�???限于�???         
  IDE: eclipse          
build: maven 

## 微信交流
![FastQuery 微信交流](file/wx.png "微信交流�???,与作者交流FastQuery.")  
与作者一起探讨FastQuery(加入时请标注java,谢谢).

## 反馈问题
https://gitee.com/xixifeng.com/fastquery/issues  
FastQuery秉承自由、开放�?�分享的精神,本项目每次升级之�???,代码和文档手册都会在第一时间完全�???�???,以供大家查阅、批评�?�指�???.笔�?�技术水平有�???,bug或不周之处在�???难免,�???�???,遇到有问题或更好的建议时,还请大家通过[issue](https://gitee.com/xixifeng.com/fastquery/issues)来向我们反馈.  

## 捐助
FastQuery 采用 Apache 许可的开源项�???, 使用完全自由, 免费.  如果 FastQuery  对你有帮�???, 可以用捐助来表示谢意.


