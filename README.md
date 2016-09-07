# FastQuery 数据持久层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.<br />
做为一个开发者, **仅仅只需要设计DAO接口即可**,其内部采用ASM动态生成实现,执行快. 因此,代码简洁而优雅.从而,大幅度提升开发效率.<br />
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
9. 使用`@Source`可实现动态适配数据源.这个特性特别适合多租户系统中要求数据库彼此隔离其结构相同的场景里.

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
支持c3p0配置,详情配置请参照c3p0官网的说明.

```xml
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
  "basedir" : "/root/git/fastquery/fastquery/tmp/"
}
```
**注意**: 在fastquery.json中配置作用域,其中"dataSourceName"不是必须的,"dataSourceName"要么不指定,要指定的话那么必须正确.      
如果没有指定"dataSourceName",那么在调用接口的时候必须指定数据源的名称.下面的适配数据源章节会讲到.

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

## 动态条件查询

### 采用`Annotation`实现简单动态条件
```java
@Query("select no, name, sex from Student #{#where} order by age desc")
// 增加若干个条件
@Condition(l="no",o=Operator.LIKE,r="?1")                   // ?1的值,如果是null, 该行条件将不参与运算
@Condition(c=COperator.AND,l="name",o=Operator.LIKE,r="?2") // 参数 ?2,如果接收到的值为null,该条件不参与运算
// 通过 ignoreNull=false 开启条件值即使是null也参与运算
// 下行?3接收到的值若为null,该条件也参与运算.
@Condition(c=COperator.AND,l="age",o=Operator.GT,r="?3",ignoreNull=false)
@Condition(c=COperator.OR,l="dept",o=Operator.IN,r="(?4,?5,?6)")           // dept in(?4,?5,?6)
@Condition(c=COperator.AND,l="name",o={Operator.NOT,Operator.LIKE},r="?7") // 等效于 name not like ?7
@Condition(c=COperator.OR,l="age",o=Operator.BETWEEN,r="?8 and ?9")        // 等效于 age between ?8 and ?9
Student[] findAllStudent(... args ...);
```

### 采用`NativeSpec`实现动态构建语句
`QueryRepository`接口中提供了若干个方法,用来动态构建查询语句.            
凡是继承自`QueryRepository`的接口,都能直接使用`QueryRepository`中的所用方法.                
举例:StudentDBService接口的实例使用`QueryRepository`接口中的find方法

```java
// 1). 准备一个 NativeSpec
NativeSpec spec = new NativeSpec() {
	@Override
	public Predicate toPredicate(SelectQuery selectQuery) {
		// 待查询的列
		selectQuery.addCustomColumns("s.name,s.sex,s.age,s.dept,c.name as courseName");
		// 待查询的表
		selectQuery.addCustomFromTable("student s");
		// 增加一个自定义关联
		selectQuery.addCustomJoin(" JOIN sc on s.no = sc.studentNo");
		// 再增加一个自定义关联
		selectQuery.addCustomJoin(" JOIN course c on c.no = sc.courseNo");
		// 增加一个自定义条件,下行等价于: (s.age >= ?1) AND (s.sex = ?2) 
		selectQuery.addCondition(ComboCondition.and(
		                         BinaryCondition.greaterThan("s.age", "?1", true),
		                         BinaryCondition.equalTo("s.sex", "?2"))
		                         );
		// 注意:build(SelectQuery selectQuery,Object... parameters),其中parameters表示SQL语句中所需的参数.
		// 参数通常都是有外界传递进来的,在此采用prepared模式,目的是为了防止SQL注入.
		// 3 对应 ?1
		// "男" 对应 ?2
		return this.build(selectQuery,3,"男"); 
	}
};

// 2). 调用find
String countField = "s.no"; // 求和字段,默认是"id"
String countsql = null;     // 求和语句
Page<Map<String, Object>> maps = studentDBService.find(spec, new PageableImpl(1, 5), countField, countsql, false);
System.out.println(JSON.toJSONString(maps, true));
```


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
@Query("update student s set s.age=?3,s.name=?2 where  s.no=?1")
@Modifying
int update(String no,String name,int age); // 返回修改之后所影响的行数

@Modifying
@Query("DELETE FROM `userinfo` WHERE id=?1")
boolean deleteUserinfoById(int id);
	
@Query("update student s set s.age=?2 where  s.no=?1")
@Modifying
int update(String no,int age);

// 以实体bean格式,返回当前保存的数据
@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
@Modifying(table="student",id="no")
// 注意: student的主键是字符串不会自增长,在此处需要用@Id标识
Student addStudent(@Id String no,String name,String sex,int age,String dept);
	
// 以Map格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Map<String, Object> addUserInfo(String name,Integer age);

// 以JSON格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
JSONObject saveUserInfo2(String name,Integer age);

// 返回当前保存的数据的主键信息.
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Primarykey saveUserInfo(String name,Integer age);

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

```java
@Query("select * from `userinfo` where {one} {orderby}")
UserInfo findUserInfo(@Param("orderby") String orderby, @Param("one") int i);
// String orderby 这个形参接受到的值会取代掉 "{orderby}"
// int i 接受到的值会取代掉 "{one}"

// 假设: orderby的值为: "order by age desc", i的值为:1
// 则: 最终的SQL为: "select * from `userinfo` where 1 order by age desc"
```

### 注意 
- 参数模板仅仅用来辅助开发者构建SQL语句
- 请堤防使用不当,引发SQL注入问题
- 请避免模板参数的值完全来源于用户层的输入
- 请确保参数值可控.


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

```java
public interface UserInfoDBService extends QueryRepository {

     // Pageable 用做描述当前页的索引和每页条数.
    
	// countField : 明确指定用来统计总行数的字段,count(countField)中的countField默认值是"id"
	@Query(value="select id,name,age from `userinfo` where 1",countField="id")
	Page<Map<String, Object>> findAll(Pageable pageable);
	
	// 如果没有指定求和语句,那么由fastquery分析出最优的求和语句
	@Query("select id,name,age from `userinfo` #{#where}")
	@Condition(l="age",o=Operator.GT,r="?1")                // age > ?1 若age的值传递null,该条件将不参与运算
	@Condition(c=COperator.AND,l="id",o=Operator.LT,r="?2") // id < ?2 若id的值传递null,该条件将不参与运算
	Page<UserInfo> find(Integer age,Integer id,Pageable pageable);
	
	// countQuery : 指定自定义求和语句
	@Query(value = "select id,name,age from `userinfo` #{#where}", 
	       countQuery = "select count(id) from `userinfo` #{#where}")
	@Condition(l = "age", o = Operator.GT, r = "?1")        // age > ?1 若age的值传递null,该条件将不参与运算
	@Condition(c=COperator.AND,l="id",o=Operator.LT,r="?2") // id < ?2 若id的值传递null,该条件将不参与运算
	Page<UserInfo> findSome(Integer age,Integer id,Pageable pageable);
}
```

### @PageIndex和@PageSize
`@PageIndex` 用来指定当前页索引   
`@PageSize`  用来指定当前页应该显示多少条数据   
**注意**: 该注解组合不能和`Pageable`一起使用  
例如:

```java
@NotCount // 分页不统计总行数. 上百万的数据里求和会让人感觉慢
@Query(value = "select id,name,age from `userinfo`")
Page<Map<String,Object>> findSome(Integer age, Integer id,@PageIndex int pageIndex, @PageSize int pageSize);
```


### 使用分页     
`Page`是分页的抽象.通过它可以获取分页中的各种属性. 并且开发者不用去实现.

```java
int p = 1;    // 指定访问的是第几页
int size = 3; // 设定每一页最多显示几条记录
Pageable pageable = new PageableImpl(p, size);
Page<UserInfo> page  = userInfoDBService.findSome(10, 50,pageable);
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
    "totalPages": 13         	// 总页数(指明一共有多少页)
}
```

### 注意:
- 如果在分页函数上标识`@NotCount`,表示在分页中不统计总行数.那么分页对象中的`totalElements`的值为-1L,`totalPages`为-1.其他属性都有效并且真实.    
- 如果明确指定不统计行数,那么设置`countField`和`countQuery`就会变得无意义.    
- 通常分页的 **区间控制** 默认是放在`SQL`语句的末尾. 在符合`SQL`语法的前提下,通过`#{#limit}`可以把分页区间放在`SQL`里的任何地方.

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
在多租户系统中,数据库彼此隔离,数据库结构一样.那么使用这个特性是非常方便的.    
**注意:** `@Source`如果标识在参数前面,那么该参数只能是字符串类型.

```java
@Query("select id,name,age from `userinfo` as u where u.age>?1")
Map<String, Object> findOne(Integer age,@Source String dataSourceName);
```

### 适配数据源的优先级
如果在fastquery.json中明确指定了数据源的作用域,同时接口函数也存在`@Source`,那么`@Source`指定的优先,其次是配置文件.

## @Before拦截器
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
 		// 中断后立马返回,当前方法后面的所有Filter将不会执行
		
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
@SkipFilter
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
- https://code.aliyun.com/xixifeng/fastquery

## 开发环境
仅仅是建议,并不局限于此         
  IDE: eclipse          
build: maven 

## 反馈问题
http://git.oschina.net/xixifeng.com/fastquery/issues

## 联系作者
fastquery#126.com
欢迎批评指正.
