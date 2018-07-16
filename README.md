### Apache Maven
```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>fastquery</artifactId>
    <version>1.0.51</version> <!-- fastquery.version -->
</dependency>
```

### Gradle/Grails
```xml
compile 'org.fastquery:fastquery:1.0.51'
```

# FastQuery 数据持久层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.<br />
提供少许`Annotation`,消费者只用关心注解的含义,这就使得框架的核心便于重构,便于持续良性发展.<br />

## FastQuery 主要特性如下:
1. 遵循非侵入式原则,设计优雅或简单,极易上手
2. 在项目初始化阶段采用ASM生成好字节码,因此支持编译前预处理,可最大限度减少运行期的错误,显著提升程序的强壮性
3. 支持安全查询,防止SQL注入
4. 支持与主流数据库连接池框架集成,如集成c3p0,dbcp等等
5. 支持 `@Query` 查询,使用 `@Condition`,可实现动态 `where` 条件查询
6. 支持查询结果集以JSON类型返回
7. 拥有非常优雅的`Page`(分页)设计
8. 支持`AOP`,注入拦截器只需要标识几个简单的注解,如: `@Before` , `@After`
9. 使用`@Source`可实现动态适配数据源.这个特性特别适合多租户系统中要求数据库彼此隔离其结构相同的场景里
10. 支持`@QueryByNamed`命名式查询,SQL动态模板
11. 支持存储过程
12. 支持批量更新集合实体(根据主键,批量更新不同字段,不同内容).

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
        <property name="driverClass">com.mysql.cj.jdbc.Driver</property>  
        <property name="url">jdbc:mysql://192.168.1.1:3306/xk?user=xk&amp;password=abc123</property>
        </named-config>

        <!-- 配置第二个数据源 -->
        <named-config name="shtest_db">  
        <property name="driverClass">com.mysql.cj.jdbc.Driver</property>  <!-- jdbc 驱动 -->
        <property name="databaseName">dbname</property>                   <!-- 数据库的名称 -->
        <property name="user">username</property>                         <!-- 数据库用户名称 -->
        <property name="password">userpasswd</property>                   <!-- 数据库用户的密码 --> 
        <property name="portNumber">3306</property>                       <!-- 端口 -->
        <property name="serverName">192.168.1.1</property>                <!-- 数据库主机地址 -->
    </named-config>
</jdbc-config>
```

### c3p0-config.xml
完全支持c3p0官方配置,详情配置请参照c3p0官网的说明.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
    <named-config name="xk-c3p0">  
        <property name="driverClass">com.mysql.cj.jdbc.Driver</property>  
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
		            "org.fastquery.dao.UserInfoDBService" // 完整类名称 
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
		  ] 
}
```
**注意**: 在fastquery.json中配置作用域,其中"dataSourceName"不是必须的,"dataSourceName"要么不指定,要指定的话那么必须正确.如果没有指定"dataSourceName",那么在调用接口的时候必须指定数据源的名称.下面的适配数据源章节会讲到."basePackages"若配置了包地址,那么对应的数据源会作用这个包的所有类,及所有子包中的类.  
fastquery.json其他可选配置选项:

| 属性名 | 类型 | 默认值 | 作用 | 示例 |
|:-----:|:-----:|:-----:|:-----|:-----|
| basedir | string | 无 | 基准目录,注意: 后面记得加上 "/" <br> 该目录用来放SQL文件,需要执行SQL文件时,指定其名称就够了 | "/tmp/sql/" |
| debug | boolean | false | 在调试模式下,可以动态装载xml里的SQL语句,且不用重启项目<br>默认是false,表示不开启调试模式.提醒:在生产阶段不要开启该模式 | false |
| queries | array | [ ] | 指定*.queries.xml(SQL模板文件)可以放在classpath目录下的哪些文件夹里.<br>默认:允许放在classpath根目录下<br>注意:每个目录前不用加"/",目录末尾需要加"/" | ["queries/","tpl/"] |
| slowQueryTime | int | 0 | 设置慢查询的时间值(单位:毫秒; 默认:0,表示不开启慢查询功能), 如果 `QueryRepository` 中的方法执行超过这个时间,则会警告输出日志,以便优化 | 50 |


## 入门例子
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
      // 实际应用中不能省略(getter/setter占篇幅较多,为了文档经凑,因此没列举)
 } 
```

- DAO接口

```java
 public interface StudentDBService extends QueryRepository {
    @Query("select no, name, sex from student")
    JSONArray findAll();
    @Query("select no,name,sex,age,dept from student")
    Student[] find();      
 }
```

- 使用DAO接口.

 **注意**:不用去实现StudentDBService接口.

```java
   // 获取实现类
   StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
   // 调用 findAll 方法
   JSONArray jsonArray = studentDBService.findAll();
   // 调用 find 方法
   Student[] students = studentDBService.find(); 
```

一个接口不实现,它的`public abstract`方法就毫无作用可言,因此,与之对应的实例对象是必须的,只不过是FastQuery内部替用户实现了,用户可通过`FQuery.getRepository`获取DB接口对应的实例.读者可能会问,这个自动生成的实例在什么时候生成? 动态生成的效率如何保持高效? 为此, 笔者做了相当多的功课:让所有DB实现类在项目初始化阶段进行,并且尽可能地对接口方法做静态分析,把有可能在运行期发生的错误尽最大努力提升到初始化阶段,生成代码前会检测SQL绑定是否合法有效、检测方法返回值是否符合常规、方法的参数是否满足模版的调用、是否正确使用分页...诸如此类问题.这些潜在问题一旦暴露,项目都启动不起来,迫使开发者必须朝正确的道路走.项目进入运行期,大量的校验就没必要写了,从而最大限度保证快速执行(至少在往这个方向不懈努力中).  

唯一的出路,只能引用接口,这就使得开发者编程起来不得不简单,因为面对的是一个高度抽象的模型,而不必去考虑细枝末节.接口可以看成是一个能解析SQL并能自动执行的模型,方法的参数、绑定的模版和标识的注解无不是为了实现一个目的:执行SQL,返回结果.  

这种不得不面向接口的编程风格,有很多好处:耦合度趋向0,天然就是**对修改封闭,对扩展开放**,不管是应用层维护还是对框架增加新特性,这些都变得特别容易.隐藏实现,可以减少bug或者是能消灭bug,就如**解决问题,不如消灭问题**一般,解决问题的造诣远远落后于消灭问题,原因在于问题被解决后,不能证明另一个潜在问题不再出现,显然消灭问题更胜一筹.应用层只用写声明抽象方法和标识注解,试问bug从何而来?该框架最大的优良之处就是让开发者没办法去制造bug(当然,排除有心为之,令提别论),至少说很难搞出问题来.不得不简便,没法造bug,显然是该项目所追求的核心目标之一.  

不管用不用这个项目,笔者都期望,读者能快速检阅一下该文档,有很多设计是众多同类框架所不具备的,希望读者从中得到正面启发或反面启发,哪怕一点点,都会使你收益.  

## 针对本文@Query的由来
该项目开源后,有些习惯于繁杂编码的开发者表示,"*使用`@Query`语义不强,为何不用@SQL,@Select,@Insert,@Update...?*". SQL的全称是 Structured Query Language,本文的 `@Query` 就是来源于此. `@Query`只作为运行SQL的载体,要做什么事情由SQL自己决定.因此,不要片面的认为Query就是select操作. 针对数据库操作的注解没有必要根据SQL的四种语言(DDL,DML,DCL,TCL)来定义,定义太多,只会增加复杂度,并且毫无必要,如果是改操作加上`@Modifying`注解,反之,都是"查",这样不更简洁实用吗? 诸如此类:`@Insert("insert into table (name) values('Sir.Xi')")`,`@Select("select * from table")`,SQL表达能力还不够吗? 就不觉得多出`@insert`和`@Select`有画蛇添足之嫌? SQL的语义本身就很强,甚至连`@Query`和`@Modifying`都略显多余,但是毕竟SQL需要有一个载体和一个大致的分类.

## 带条件查询

```java
// sql中的?1 表示对应当前方法的第1个参数
// sql中的?2 表示对应当前方法的第2个参数
//       ?N 表示对应当前方法的第N个参数
	
// 查询返回数组格式
@Query("select no,name,sex,age,dept from student s where s.sex=?2 and s.age > ?1")
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

若返回`List<Map<String, String>>`或`Map<String, String>`,表示把查询出的字段值(value)包装成字符串.   

**注意**: 在没有查询到数据的情况下,如果返回值是集合类型或`JSON`类型或者是数组类型,返回具体的值不会是`null`,而是一个空对象(empty object)集合或空对象`JSON`或者是长度为0的数组.   
使用空对象来代替返回`null`,它与有意义的对象一样,并且能避免`NullPointerException`,可以减少运行期错误.反对者一般都从性能的角度来考虑,认为`new`一个空对象替代`null`,会增加系统的开销.可是,&lt;&lt;Effective Java&gt;&gt;的作者**Josh Bloch**说,在这个级别上担心性能问题是不明智的,除非有分析表明,返回空对象来替代返回null正是造成性能问题的源头.      
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

## 动态条件查询

### 采用`Annotation`实现简单动态条件  
看到这里,可别认为`SQL`只能写在Annotation(注解)里.`FastQuery`还提供了另二种方案:① 采用`@QueryByNamed`(命名式查询),将`SQL`写入到模板文件中,并允许在模板文件里做复杂的逻辑判断,相当灵活.② 通过`BuilderQuery`函数式接口构建`SQL`.下面章节有详细描述. 

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
若`@Condition`的值使用了`${表达式}`,`$表达式`,不管方法的参数传递了什么都不会使条件移除,因为`$`表达式(或称之为EL表达式)仅作为简单模版使用,传null,默认会替换为""(空字符串).举例:

```
@Query("select * from `userinfo` #{#where}")
@Condition("age between $age1 and ${age2}")
List<Map<String, Object>> between(@Param("age1") Integer age1,@Param("age2") Integer age2);	
```
该例中`@Condition`使用到了`$`表达式,`$age1`,`$age1`仅作为模板替换,age1为null,即便设置`ignoreNull=true`也不会影响条件的增减.**总之,`$` 表达式不会动摇条件的存在**.  

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

**注意**:
- 改操作返回int类型:表示影响的行数,没有找到可以修改的,那么影响行数为0,并不能视为改失败了
- 改操作返回boolean类型:表示是否改正确,依据是,影响行数若大于或等于0都会返回true,反之,返回false

## Annotation
针对FastQuery中的所有注解,做个说明:

| Annotation | 作用 |
|:---|:---|
|`@Id`|用来标识表主键|
|`@Modifying`|标识改操作|
|`@Param`|标识参数名称,便于运行期获取|
|`@Query`|标识查询语句|
|`@QueryByNamed`|标识根据命名式查询(语句放在配置文件中)|
|`@Source`|标识用来适配数据源的参数|
|`@Transactional`|事务|
|`@Transient`|标识实体中的属性是临时的(例如:save对象时,该属性不存储到数据库里)|
|`@NotCount`|标识分页中不统计总行数|
|`@PageIndex`|标识页索引对应哪个参数|
|`@PageSize`|标识页行数对应哪个参数|
|`@Condition`|标识条件单元|
|`@Before`|标识函数执行前|
|`@After`|标识函数执行后|
|`@SkipFilter`|标识跳过拦截器|

## QueryRepository的内置方法
凡是继承`QueryRepository`的接口,都可以使用它的方法,并且不用写实现类.

| 方法 | 描述 |
|:---|:---|
| `<E> E find(Class<E> entityClass,long id)` | 根据主键查询实体 |
| `<E> int insert(E entity)` | 插入一个实体(主键字段的值若为null,那么该字段将不参与运算),返回影响行数 |
| `<B> int save(boolean ignoreRepeat,Collection<B> entities)` | 保存一个集合实体,是否忽略重复主键记录 |
| `int saveArray(boolean ignoreRepeat,Object...entities)` | 保存一个可变数组实体,是否忽略重复主键记录 |
| `BigInteger saveToId(Object entity)` | 保存实体后,返回主键值.**注意**:主键类型必须为数字且自增长,不支持联合主键 |
| `<E> E save(E entity)` | 保存实体后,返回实体 |
| `<E> int executeUpdate(E entity)` | 更新一个实体,返回影响行数.**注意**:实体的成员属性如果是null,那么该属性将不会参与改运算 |
| `<E> E update(E entity)` | 更新一个实体,返回被更新的实体 |
| `<E> int executeSaveOrUpdate(E entity)` | 不存在就保存,反之更新(前提条件:这个实体必须包含主键值),返回影响行数 |
| `<E> E saveOrUpdate(E entity)` | 不存在就保存,反之更新,返回被更新的实体或返回已存储的实体 |
| `int update(Object entity,String where)` | 更新实体时,自定义条件(有时候不一定是根据主键来修改),若给where传递null或"",默认按照主键修改,返回影响行数 |
| `<E> int update(Collection<E> entities)` | 更新集合实体,成员属性如果是null,那么该属性将不会参与改运算,每个实体必须包含主键 |
| `int delete(String tableName,String primaryKeyName,long id)` | 根据主键删除实体,返回影响行数 |
| `int[] executeBatch(String sqlFile)` | 根据指定的SQL文件名称或绝对路径,执行批量操作SQL语句,返回int[],数组中的每个数对应一条SQL语句执行后所影响的行数 |

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
update `UserInfo`
set `name` = case `id`
	when 77 then '茝若'
	when 88 then '芸兮'
	else `name`
end, `age` = case `id`
	when 77 then '18'
	when 99 then '16'
	else `age`
end
where `id` in (77, 88, 99)
```

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

**SQL中使用冒号表达式**
 
```java
@Query("select name,age from UserInfo u where u.name = :name or u.age = :age")
UserInfo[] findUserInfoByNameOrAge(@Param("name") String name, @Param("age")Integer age);
```

其中`:name`对应`@Param("name")`所指定的方法变量值;`:age`对应`@Param("age")`所指定的方法变量值.当然SQL中的变量也可以用`?N`(N={正整数})的形式来表达,且不用标识`@Param`.  
如:`select name,age from UserInfo u where u.name = :name or u.age = :age`以防SQL注入问题,在执行语句之前,最终会被编译成`select name,age from UserInfo u where u.name=? or u.age=?`


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

### 参数格式解释器
`@Param`中的`format`属性,可以对参数进行某种格式化,默认为`""`(空字符串),表示无格式处理. 该实现依赖于`String.format(String format, Object... args)`, 其中`args`为当前方法的参数集. 格式化语法请查阅[java.util.Formatter](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html).  
用法举例:  
需求,给一个参数首位加上 `%` 符号

```java
@Param(value="name",format="%%%1$s%%") String name
```
`%` 是格式化语法的关键字, 要输出`%`本身,那么就需要连续写两个`%`来转义.  
`format` 中支持 `$` 表达式引用参数:

```java
@Param(value="name",format="%%${name}%%") String name
```
可见, **通过`$`表达式引用参数值比采取百分号索引引用参数值更优越.方法的参数顺序倘若被改变,引用的索引号也要同步修改.而,`$`表达式,跟参数顺序没关系.**

## 微笑表达式
定义: **以<code>\`-</code> 作为开始,以<code>-\`</code>作为结尾,包裹着若干字符,因为<code>\`- -\`</code>酷似微笑表情,因此将这样的表达式称之为`微笑表达式`.** <br>例如: <code> \`-%${name}%-\` </code>. **\`** 反撇号的位置如下图所示:<br>
![反撇号](https://xixifeng.github.io/pjaxpage/example/img/fanpie.png "反撇号")    
作用:  
1.可以作为实参的模板,举例: 查询出姓"张"的用户.没有`微笑表达式`时的写法:
```java
db.findLikeName(name + "%");
```
这种写法不好,需要手动拼接模糊搜索关键字.  
现在有`微笑表达式`了,在模板中,可以配置name实参的模板.假设模板中通过<code>\`-:name%-\`</code>引用这个实参,那么<code>\`-:name%-\`</code>将会作为这个实参的模板. name的值为"张",实际上传递的是"张%".   

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
id not in (1,2,null) -- 结果会为null
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
若`<parts>`元素跟`<query>`保持并列关系,那么该零件集是全局的.当前文件里的`<query>`都能引用它.一个非分页的函数,如果绑定的模板中包含`<countQuery>`,那么这个函数只会提取查询语句,而不会提取计数语句.

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

允许多个方法绑定同一个模板id.当然,采用`@QueryByNamed`同样适应于改操作,例如:

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

`@QueryByNamed` 中的render属性,表示是否启用模板引擎对配置文件进行渲染,默认是true表示开启. 如果`<query>`节点中没有使用到任何模板语法,仅用于存储目的,那么建议设置为false.  

**注意**: `$name`和`:name`这两种表达式的主要区别是——`$name`表示引用的是参数源值,可用于在模板中做逻辑判断,而`:name`用于标记参数位,SQL解析器会将其翻译成`?`号.

## BuilderQuery
上面介绍了`SQL`不仅可以绑定在`@Query`里, 也可以写到`XML`里. 还有另一种方式,**通过函数式构建SQL语句**.  
用法举例:

```java
@Query
public interface DefaultDBService extends QueryRepository {
   // 分页, 查询语句 和 count语句 通过 builderQuery 构建出来
   @Query
   Page<Map<String,Object>> findPage(Integer id,@Param("age")Integer age,Pageable p,BuilderQuery builder);
}
```

如果分页不要求得到总页数,在接口的方法上加`@NotCount`便可(谁说分页一定要执行count语句?).

不用去实现那个接口,直接调用:

```java
DefaultDBService db = FQuery.getRepository(DefaultDBService.class);
Pageable pageable = new PageableImpl(1, 3);
Integer id = 500;
Integer age = 18;
Page<Map<String, Object>> page = db.findPage(id, age, pageable, m -> {
	m.setQuery("select id,name,age from `userinfo`");// 设置查询语句
	m.setWhere("where id < ?1 and age > :age");// 设置条件(这样设计可以让条件复用)
	m.setCountQuery("select count(`id`) from `userinfo`");// 设置count语句
});
```

引用问号表达式(?expression) , 冒号表达式(:expression), 其中?1表示方法的第一个参数,`:age`表示匹配`@Param("age")`那个参数,采用问号或冒号表达式不会有注入问题.

## 支持存储过程

只支持in(输入)参数,不支持out(输出参数), 如果想输出存储过程的处理结果,在过程内部使用`select`查询输出.  
举例:  
插入一条学生，返回学生的总记录数和当前编码,存储过程语句:

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
JSONObject callProcedure(String no,@Param("name") String name,String sex,int age,@Param("dept") String dept);
```

## 分页

- 通过`@QueryByNamed`实现分页

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
- `<value>`和`<countQuery>`节点引用的零件中已经包含`<where>`元素,那么该节点中禁止出现where字符串

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
`Page`是分页的抽象,通过它可以获取分页中的各种属性,并且开发者不用去实现.

```java
int p = 1;    // 指定访问的是第几页(不是从0开始计数)
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

## JavaScript分页插件
[PJAXPage](https://gitee.com/xixifeng.com/pjaxpage)分页插件,完美支持`Page`数据结构.        
项目地址: https://gitee.com/xixifeng.com/pjaxpage      
使用例子: http://xixifeng.com.oschina.io/pjaxpage/example/   

## 执行SQL文件
```java
String sqlFile = "update.sql";
int[] effects = studentDBService.executeBatch(sqlFile);
```

- sqlFile 指定基准目录下的SQL文件. 注意: 基准目录在fastquery.json里配置,sqlFile 为绝对路径也行. 
- 返回 `int[]`类型,用于记录SQL文件被执行后所影响的行数.若,effects[x] = m 表示第x行SQL执行后影响的行数是m; effects[y] = n 表示第y行SQL执行后所影响的行数是n.

## 动态适配数据源
### 创建数据源
如果想在项目运行期间动态创建一个新数据源,那么请使用`FQuery.createDataSource`.

```java
// 数据源名称
String dataSourceName = "xk1";

// 连接池配置
Properties properties = new Properties();
properties.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
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

## WEB 支持
### 应用在 Jersey 环境

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

FastQuery支持JAX-RS注解,不需实现类,便能构建极简的RESTful.不得不简单的设计,可见一斑.

```java
@Path("userInfo")
public interface UserInfoDBService extends QueryRepository {

	// 查询并实现分页
	@Path("findAll")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Query(value = "select id,name,age from `userinfo` where 1", countField = "id")
	Page<Map<String, Object>> findAll(@QueryParam("pageIndex") @PageIndex int pageIndex,
			                          @QueryParam("pageSize")  @PageSize  int pageSize);
   
}
```

没错, **不用去写任何实现类**, 访问 `http://<your host>/rest/userInfo/findAll?pageIndex=1&pageSize=5`, 就可以看到效果.  
**DB接口不仅能当做WEB Service(服务),同时也是一个DB接口**.  

### 配置支持HttpSign
[HttpSign](https://github.com/xixifeng/httpsign) 是一种RESTful接口签名认证的实现.  

```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>httpsign</artifactId>
    <!-- 请从 https://gitee.com/xixifeng.com/httpsign 或 maven 中央仓库中查阅最新版本 -->
    <version>1.0.3</version>
</dependency>
```

用法很简单,在方法上标识`@Authorization`便可.

```java
@org.fastquery.httpsign.Authorization
@Path("findById")
@GET
@Produces(MediaType.APPLICATION_JSON)
@Query("select id,name,age from UserInfo where id = :id")
JSONObject findById(@QueryParam("id") @Param("id") Integer id);
```
 
当然,如果不喜欢太简单,可以把DB接口注入到JAX-RS Resource类中:

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
- 运行时获取SQL和它的参数值,以便开发者验证生成的SQL是否跟期望值一致.
- 运行DB方法后自动回滚数据库事务.

`FastQueryTestRule` 实现了Junit中的 `TestRule` 类,用来扩展测试用例.可以在测试方法中获取执行过的SQL语句及SQL所对应的参数值,以便做断言.加上`@Rollback`注解,可以用来控制测试方法执行完毕之后是否让数据事务回滚或提交.测试方法结束后默认自动回滚,既可以达到测试效果,又不影响数据库(可回滚到改之前状态). 如下是例子,请留意注释,细节就不再赘述了.

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
	// 获取DB操作后所产生的SQL
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

- https://gitee.com/xixifeng.com/fastquery
- https://github.com/xixifeng/fastquery

## 开发环境
仅仅是建议,并不局限于此         
  IDE: eclipse          
build: maven 

## 交流
![FastQuery QQ群号:621656696](https://gitee.com/uploads/images/2017/0705/115519_b9f971c8_788636.png "FastQuery QQ群号:621656696，发现更多内容")    
FastQuery QQ交流群号(621656696) 由支持者自由发起,非常感谢!

## 反馈问题
https://gitee.com/xixifeng.com/fastquery/issues  
地球人都知道,FastQuery秉承自由、开放、分享的精神,本项目每次升级之后,代码和文档手册都会在第一时间完全开源,以供大家查阅、批评、指正.笔者技术水平有限,bug或不周之处在所难免,所以,遇到有问题或更好的建议时,还请大家通过[issue](https://gitee.com/xixifeng.com/fastquery/issues)来向我们反馈.  

## 关于作者
@习习风 fastquery#126.com  
欢迎批评指正.
