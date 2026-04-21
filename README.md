## 1. FastQuery 是什么

FastQuery 是一个基于 Java 的数据持久层框架。它的目标不是把数据库访问“包装得更复杂”，而是让开发者以尽可能简单、清晰、稳定的方式操作数据层。框架强调几个核心方向：少量注解、接口式编程、初始化阶段完成大量校验、运行期尽量轻量，以及通过 `@Query`、`@Condition`、`@Set`、`@QueryByNamed` 等机制来表达查询与动态 SQL。

FastQuery 不是传统意义上那种依赖大量 XML、约定隐蔽、运行期才暴露错误的框架。它更希望把许多原本会在运行期才发生的问题，尽量提前到项目初始化阶段发现并阻断。这样做的结果是：开发阶段暴露问题更早，运行阶段的执行路径更直接，代码维护成本也更低。

## 2. FastQuery 的设计目标

如果只用一句话概括 FastQuery，可以这样说：

> **让开发者只面对接口、注解与 SQL，本身不再承担实现类、样板代码和大量重复判断的负担。**

- DAO 只需要定义接口，不需要编写实现类；
- 框架会在初始化阶段为接口生成实现；
- SQL 绑定、方法返回值、分页参数、模板调用等都会在初始化阶段进行检查；
- 如果存在明显问题，项目应当直接无法启动，从而把错误尽可能留在开发阶段解决。

这意味着 FastQuery 的价值，不只是“省代码”，更在于：

- 降低出错面；
- 强迫接口设计更清晰；
- 把动态能力建立在受控规则之上，而不是随意拼接字符串；
- 让查询、分页、事务、多数据源等能力都保持统一风格。

## 3. 适用场景

FastQuery 适合以下类型的项目：

- 以 SQL 为核心表达方式的业务系统；
- 希望接口层清晰、实现层自动生成的数据访问场景；
- 既需要静态 SQL，又需要一定动态 SQL 能力的项目；
- 希望通过注解风格完成绝大部分数据访问定义的项目；
- 对分页、事务、批量更新、多数据源、命名式查询有明确需求的项目。

如果你的项目更希望把 SQL 完整掌握在开发者手里，而不是把查询逻辑大量隐藏在方法名解析或复杂的 Criteria API 中，那么 FastQuery 会更直接。

## 4. 运行环境与依赖

FastQuery 的运行环境要求是 **Java 8+**。

### Maven

```xml
<dependency>
    <groupId>org.fastquery</groupId>
    <artifactId>fastquery</artifactId>
    <version>1.0.149</version> <!-- fastquery.version -->
</dependency>
```

### Gradle

```groovy
compile 'org.fastquery:fastquery:1.0.149'
```

## 5. 第一个 FastQuery 程序

第一次使用 FastQuery，需要理解 5 个角色：

- 配置数据源 DataSource
- 配置`fastquery.json`
- 实体类
- DAO 接口
- 通过 `FQuery.getRepository(...)` 获取接口实现并调用

### 5.1 配置数据库数据源

几乎支持所有的主流连接池，这里用 `HikariCP` 做示例:

druid.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
  <!-- 数据源名称 -->
	<bean name="xkdb">
    <!-- 数据库连接地址 -->
		<property name="jdbcUrl" value="jdbc:mysql://localhost:3306/xk" />
    <!-- 数据库用户名 -->
		<property name="dataSource.user" value="xk" />
    <!-- 数据库密码 -->
		<property name="dataSource.password" value="abc123" />	
    <!-- HikariCP 的其他配置选项请参照其官网 -->
    
    <!-- 若有需要，也可以配置多个 bean 节点，提供多个数据源 -->
</beans>
```

详细配置选项请参照 https://github.com/brettwooldridge/HikariCP

### 5.2 配置`fastquery.json`

```json 
// 配置必须遵循标准的 json 语法.
{
"scope":[
      {
          "config": "hikari", // 连接池厂商,如，"hikari","druid","c3p0" 等等
          "dataSourceName": "xkdb", // 这里指定由上个步骤配置的数据源名称
          "basePackages": [         // 该数据源的作用范围
              "org.fastquery.example",              // 包地址
              "org.fastquery.dao.UserInfoDBService" // 完整类名称 
          ]
      }
    ] 
}
```

### 5.3 定义实体

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

这里有一个非常关键的约定：**实体属性与数据库字段映射时，应使用包装类型，而不是基本类型；否则会被忽略。** 如果某个字段不参与映射，可以在属性上加 `@Transient`。

### 5.4 定义 Repository 接口

```java
 public interface StudentDBService extends org.fastquery.core.Repository {
    @Query("select no, name, sex from student")
    JSONArray findAll();
    @Query("select no,name,sex,age,dept from student")
    Student[] find();      
 }
```

### 5.5 获取并调用

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

需要特别说明：

1. **不要手动实现 Repository 接口**
2. `FQuery.getRepository(...)` 返回的实例是单例且不可变的

在 FastQuery 中，接口的实现类由框架在**项目启动阶段自动生成**。在生成过程中，框架会对接口方法进行静态分析，包括：

- SQL 语句是否合法
- 参数绑定是否正确
- 返回值类型是否匹配
- 是否正确使用分页等特性

如果存在问题，应用将无法启动，并在开发阶段直接报错，从而避免将错误带入运行期。这种设计将大量运行期校验前移到初始化阶段，在保证安全性的同时，也提升了运行时性能。

从使用角度来看，开发者只需定义接口和 SQL，无需关注底层实现细节。框架会自动完成 SQL 执行和结果映射，使代码更加简洁、低耦合。

这种基于接口的编程方式，使系统更容易扩展，同时减少人为实现带来的错误风险。

## 6. 配置文件

### 6.1 配置文件位置

默认情况下，FastQuery 会从 `classpath` 目录加载配置文件。

你也可以通过以下方式自定义配置目录：

```java
System.setProperty("fastquery.config.dir", "/data/fastquery/configs");
```

该设置会覆盖 `classpath` 中的同名配置文件。

如果项目以 JAR 方式启动，可以通过 JVM 参数指定：

```bash
java -Dfastquery.config.dir=/data/fastquery/configs -jar Start.jar
```

------

### 6.2 连接池配置示例

FastQuery 支持多种主流连接池（如 HikariCP、c3p0、Druid），并允许**多数据源共存**。

------

#### 1）c3p0 配置示例

FastQuery 完全兼容 c3p0 官方配置：

命名：c3p0.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
    <named-config name="xk-c3p0">  
        <property name="driverClass">com.mysql.cj.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://localhost:3306/xk</property>  
        <property name="user">xk</property>  
        <property name="password">abc123</property>  
        <property name="initialPoolSize">100</property>  
        <property name="minPoolSize">50</property>  
        <property name="maxPoolSize">1000</property>
    </named-config>

    <!-- 可配置多个数据源 -->
    <named-config name="name-x"> ... </named-config>
</c3p0-config>
```

------

#### 2）Druid 配置示例

命名：druid.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
	 <bean name="xkdb1" class="com.alibaba.druid.pool.DruidDataSource" 
           init-method="init" destroy-method="close"> 
	     <property name="url" value="jdbc:mysql://localhost:3306/xk" />
	     <property name="username" value="xk" />
	     <property name="password" value="abc123" />
	     <property name="maxActive" value="20" />
	 </bean>
</beans>
```

------

👉 支持多个连接池同时存在（例如：HikariCP + Druid）。

------

### 6.3 fastquery.json 配置说明

```json
{
  "scope":[
    {
      "config": "hikari",
      "dataSourceName": "xkdb",
      "basePackages": [
        "org.fastquery.example",
        "org.fastquery.dao.UserInfoDBService"
      ]
    },
    {
      "config": "mySQLDriver",
      "dataSourceName": "world-mysql",
      "basePackages": ["org.fastquery.sqlserver.dao"]
    }
  ],

  "debug": true,

  "queries": ["queries/", "tpl/"],

  "slowQueryTime": 50,

  "convert": [
    {
      "sourceType": "java.lang.Integer",
      "targetType": "org.fastquery.bean.Option",
      "converter": "org.fastquery.bean.IntToOptionConverter"
    }
  ]
}
```

#### 配置项说明

##### 1）scope（数据源作用域）

定义“**哪个数据源作用于哪些接口/包**”：

- `config`：连接池类型（hikari / c3p0 / druid）
- `dataSourceName`：数据源名称（可选）
- `basePackages`：作用范围（包或类）

👉 规则：

- 如果指定了 `dataSourceName` → 必须存在且正确
- 如果未指定 → 调用时必须手动指定数据源

------

##### 2）debug（调试模式）

- `true`：支持 SQL 模板热加载（无需重启）
- `false`：默认关闭

⚠️ **生产环境禁止开启**

------

##### 3）queries（SQL 模板路径）

指定 `*.queries.xml` 的扫描目录：

```json
"queries": ["queries/", "tpl/"]
```

表示：

```text
classpath/queries/
classpath/tpl/
```

------

##### 4）slowQueryTime（慢查询阈值）

- 单位：毫秒
- 超过该时间将记录警告日志

------

##### 5）convert（类型转换器）

用于自定义结果映射：

```json
{
  "sourceType": "java.lang.Integer",
  "targetType": "org.fastquery.bean.Option",
  "converter": "xxxConverter"
}
```

------

### 6.5 关键说明

数据源的初始化流程如下：

1. 读取 `fastquery.json`
2. 解析 `scope`
3. 根据 `dataSourceName` 查找对应连接池配置
4. 创建数据源并建立映射关系

## 7. Repository 与 QueryRepository

FastQuery 中有两个内置基础接口需要理解：

### 7.1 Repository 

如果你的 DAO 仅仅需要声明式的查询或更新，不需要内置 CRUD 能力，那么继承 `Repository` 就够了。它更轻量。

### 7.2 QueryRepository

如果你希望直接使用框架提供的内置方法，例如：

- 根据主键查询
- 保存实体
- 更新实体
- 保存或更新
- 批量更新
- 统计记录数
- 执行事务函数

那么应继承 `QueryRepository`。它包含若干内置函数，例如 `find`、`insert`、`save`、`saveToId`、`update`、`saveOrUpdate`、`tx`、`count` 等。

一个简单例子：

```java
public interface UserInfoDBService extends QueryRepository {
}
```

这样你就可以直接调用：

```java
UserInfo u1 = new UserInfo(36, "Dick", 23);
userInfoDBService.save(u1);

UserInfo u2 = new UserInfo(36, "Dick", null);
userInfoDBService.update(u2);
```

当实体某些属性为 `null` 时，更新时这些字段默认不会参与 `set` 运算。

## 8. `@Query`：最核心的查询入口

`@Query` 是 `FastQuery` 中最基础、最重要的注解。它的职责非常纯粹：**承载 SQL**。

### 设计说明

在 `FastQuery` 中，没有区分 `@Select`、`@Insert`、`@Update` 等多种注解，而是统一使用 `@Query`。

原因如下：

- `SQL` 本身已经具备完整语义（`SELECT` / `INSERT` / `UPDATE` / `DELETE`）
- 过多注解会增加学习成本和维护复杂度
- 统一入口可以降低接口设计复杂度

对于“写操作”（`INSERT` / `UPDATE` / `DELETE`），只需额外标注：`@Modifying` 即可。

------

### 对比说明

例如：

```java
@Query("insert into table (name) values ('Tom')")
```

相比：

```java
@Insert("insert into...")
```

👉 FastQuery 更倾向于：

- 减少注解种类
- 保持模型简单
- 将语义交给 SQL 本身

### 8.1 基本查询

```java
@Query("select no, name, sex from student")
JSONArray findAll();
```

### 8.2 带参数查询

```java
@Query("select no,name,sex,age,dept from student s where s.sex=:sex and s.age > ?1")
Student[] find(Integer age, @Param("sex") String sex);
```

`Query`中的`SQL`语句支持两类参数引用方式：

- `?1`、`?2`、`?N`：按方法参数位置绑定
- `:name`：按参数名绑定，需要配合 `@Param("name")`

当参数较多时，更推荐 `:name` 方式，因为它与顺序解耦，更利于维护。

## 9. 返回值类型

`Repository` 接口的方法支持多种返回值映射方式，用于将查询结果自动转换为目标类型。

```java
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

------

### 9.1 常见返回类型

#### 1）JSON 类型

- `JSONArray`
- `JSONObject`

------

#### 2）对象类型

- 单个对象：`UserInfo`
- 对象数组：`Student[]`
- 集合：`List<UserInfo>`

------

#### 3）通用结构

- `List<Map<String, Object>>`

适用于不固定字段或动态查询场景。

------

### 9.2 单字段返回（标量查询）

当 `SQL` 只查询**一个字段**时，支持以下类型：

- 单值：`String`、`Integer`、`Long`、`Double` 等
- 数组：`String[]`、`Integer[]` 等
- 集合：`List<String>`、`List<Integer>` 等

👉 所有类型均为**包装类型（Wrapper）**

------

### 9.3 重要规则

  #### ❗ 规则 1：优先使用包装类型（Wrapper）

  ```java
int ❌
Integer ✔
  ```

  **原因：**

  - `SQL` 查询结果可能为 `NULL`
  - `Java` 基本类型无法表示 `null`

------

  #### ✅ 例外：`COUNT` 查询

  当 `@Query` 明确用于统计行数（如 `COUNT(*)`）时，可以使用基本类型：

  ```java
@Query("select count(*) from student")
int count();
  ```

  **说明：**

  - `COUNT` 函数在 `SQL` 中**始终返回非 NULL 值**
  - 因此可以安全映射为基本类型（如 `int`、`long`）

#### ❗ 规则 2：包装类型返回 `null` 的含义

当返回值为单个对象（如 `Integer`、`String`）时：

```text
返回 null 可能表示：
1. 查询结果为空（没有记录）
2. 查询字段值为 NULL
```

👉 两种情况不会区分（需自行处理）

------

#### ❗ 规则 3：集合返回值不会为 null

```java
@Query("select name from Student limit 3")
List<String> findNames();
```

返回结果：

- 有数据 → `["A", "B", "C"]`
- 无数据 → `[]`（空集合）

👉 **不会返回 null**

------

### 9.4 返回类型选择建议

| 场景            | 推荐类型                   |
| --------------- | -------------------------- |
| 多行多列        | `List<实体>`               |
| 单行            | `实体`                     |
| 单字段多行      | `List<T>`                  |
| 单字段单值      | `T`                        |
| 不确定结构      | `List<Map<String,Object>>` |
| 调试 / 快速开发 | `JSONArray`                |

## 10. 空结果约定

FastQuery 采用如下统一约定：**当查询结果为空时，不返回 `null`，而是返回“空对象”。**

------

### 10.1 行为规则

当查询没有匹配数据时：

| 返回类型                           | 返回结果        |
| ---------------------------------- | --------------- |
| `List`                             | 空集合（`[]`）  |
| `Map`                              | 空对象（`{}`）  |
| 数组（如 `Student[]`）             | 长度为 0 的数组 |
| JSON（`JSONArray` / `JSONObject`） | 空 JSON 对象    |

------

### 10.2 示例

```java
@Query("sql statements")
Student[] find(Integer age, String sex);

@Query("sql statements")
Map<String, Object> find(Integer id);

@Query("sql statements")
List<Map<String, Object>> find(String sex);
```

当查询不到结果时：

- `Student[]` → `new Student[0]`
- `Map` → 空 `Map`
- `List` → 空 `List`

------

### 10.3 设计目的

这种设计可以：

- 避免 `NullPointerException`
- 减少空值判断（`if (x != null)`）
- 提高代码稳定性与可读性

------

### 10.4 设计依据

这一约定与 Java 社区的最佳实践一致。

Joshua Bloch 在《Effective Java》中建议：

> “Return empty collections or arrays, not nulls.”（返回空集合或空数组，而不是返回 null）

他同时指出：

> “There is no reason ever to return null from an array- or collection-valued method.”（对于返回集合或数组的方法，没有理由返回 null）

------

### 10.5 关于性能的说明

有观点认为：返回空对象（如 `new ArrayList<>()`）会带来额外开销。

但实践表明：

- 这类开销通常可以忽略
- 相比之下，避免 `NullPointerException` 带来的收益更大

除非经过性能分析证明这是瓶颈，否则无需优化。

## 11. 参数绑定：`?N`、`:name` 与 `$name`

FastQuery 支持三种参数处理方式：

| 类型       | 语法                | 机制                   | 是否防 SQL 注入 |
| ---------- | ------------------- | ---------------------- | --------------- |
| 位置参数   | `?N`                | 预编译参数绑定         | ✅               |
| 命名参数   | `:name`             | 预编译参数绑定         | ✅               |
| 模板表达式 | `$name` / `${name}` | SQL 生成阶段字符串替换 | ❌（需自行控制） |

### 11.1 `?N`（位置参数）

`?1` 表示方法第一个参数，`?2` 表示第二个参数，以此类推。

```java
@Query("select * from UserInfo where name in (?1) and id > ?2")
List<UserInfo> findByNameListIn(List<String> names, Integer id);
```

👉 特点：

- 简单直接
- 参数较多时可读性较差

------

### 11.2 `:name`（命名参数）

命名参数需配合 `@Param` 使用。其中：`:name` 对应方法参数中：`@Param("name")` 所绑定的实际值。

基本示例：

```java
@Query("select name,age from UserInfo u where u.name = :name or u.age = :age")
UserInfo[] findUserInfoByNameOrAge(
        @Param("name") String name,
        @Param("age") Integer age);
```

说明：

- `:name` → 绑定参数 `name`
- `:age` → 绑定参数 `age`

机制说明：

命名参数在解析阶段会被转换为 JDBC 占位符：`:name` 转换为：`?`，最终通过 `PreparedStatement` 完成安全绑定。即：SQL 结构与参数值分离，避免字符串拼接式注入风险。

优点：

- 与参数顺序解耦
- SQL 可读性更强
- 参数含义明确
- 更适合多参数、复杂查询场景

**冒号转义规则**：

在 `@Query` 中，冒号 `:` 并不一定表示命名参数，有时它只是普通字符。例如，你希望 SQL 中保留字面量：":name"，而不是把它识别成参数表达式。此时可使用：`::name` 表示转义后的字面字符串。

示例：

```java
@Query("... where name like '%::name%'")
```

实际含义为：

```sql
where u.name like '%:name%'
```

即：

- `::name` → 字面文本 `:name`
- 不参与参数绑定
- 不会被替换为 `?`

------

### 11.3 `$name` / `${name}`（模板表达式）

`$` 表达式属于 **template expression（模板表达式）**，其行为是：**在 SQL 生成阶段进行字符串替换，而不是参数绑定**。

```java
@Query("select * from userinfo where ${one} $orderby")
UserInfo findUserInfo(@Param("orderby") String orderby,
                      @Param("one") int i);
```

若 `orderby = "order by age desc"`，`i = 1`，最终生成 SQL：

```sql
select * from userinfo where 1 order by age desc
```

若 `orderby = null`，`i = 1`，最终生成 SQL：

```sql
select * from userinfo where 1
```

#### ⚠️ 安全约束

由于 `$` 表达式是**直接拼接 SQL**，因此：

- 传递 `null` 值，模板变量默认取空字符串`""`
- 不经过预编译
- 不进行参数转义
- 存在 SQL 注入风险

#### 使用规范

- **仅用于 SQL 结构片段**（如 `ORDER BY`、列名、表名等）
- **禁止拼接用户输入**
- 推荐采用**白名单控制**

对于必须使用 `$` 表达式的场景，可通过：`@Safe`标识参数，由框架执行严格校验，以降低风险。

------

#### `$` 表达式的边界规则

`$` 表达式支持两种形式：`$name` 或 `${name}`

##### 1）可省略 `{}` 的场景

当变量与后续内容之间存在**明确分隔符**（如空格、关键字、符号）时：

```sql
where $name and age > 18 $orderby
```

👉 解析结果：

- `$name` → 变量 `name`
- `$orderby` → 变量 `orderby`

##### 2）必须使用 `{}` 的场景

当变量与后续字符**无分隔（连续拼接）**时：

```sql
${field}where   ✔
$fieldwhere     ❌
```

👉 否则会被解析为变量：`fieldwhere`

##### 3）解析规则

`$` 表达式采用：**最长匹配原则（longest match）进行变量解析**

具体表现为：

- 连续字符会被尽可能解析为变量名
- 不存在分隔符时，会发生“吞并”

##### 4）推荐实践

| 场景     | 建议           |
| -------- | -------------- |
| 简单拼接 | `$name`        |
| 紧邻文本 | `${name}`      |
| 动态 SQL | 优先使用 `${}` |

## 12. 实体字段与表字段的映射

FastQuery **不要求实体属性名与数据库字段名保持一致**。字段映射关系由 SQL 决定，同时也支持通过注解进行配置。

### 12.1 默认映射规则

在未使用注解或 SQL 别名时，FastQuery 采用默认约定：

- 实体类名 → 数据库表名
- 实体属性名 → 数据库字段名

👉 即：**同名自动映射**

------

### 12.2 使用 `@Table` 自定义表映射

当实体类名与数据库表名不一致时，可以通过 `@Table` 指定表名：

```java
@Table("user_info")
public class UserInfo {
}
```

### 12.3 使用 SQL 别名进行字段映射

当字段名不一致时，可以通过 SQL `AS` 别名显式指定映射关系：

```java
public class UserInformation {
    private Integer uid;
    private String myname;
    private Integer myage;
}

@Query("select id as uid, name as myname, age as myage from user_info where id = ?1")
UserInformation findUserInfoById(Integer id);
```

### 12.4 多表查询场景说明

在单表场景下，实体字段与数据库字段保持一致确实有助于简化映射。但在实际业务中，多表关联查询非常常见。例如：

```sql
select a.name, b.name from A a join B b on a.id = b.a_id
```

此时：

- 表 A 中存在字段 `name`
- 表 B 中同样存在字段 `name`

👉 查询结果中会出现**字段名冲突**，无法直接映射到同一个实体属性。

因此，必须通过 SQL 别名进行区分：

```sql
select a.name as aName, b.name as bName from A a join B b on a.id = b.a_id
```

再映射到实体：

```java
public class ABView {
    private String aName;
    private String bName;
}
```

👉 这也是 FastQuery 不强制字段名一致的重要原因：**在多表查询场景下，字段映射必须由 SQL 显式控制，而无法依赖默认命名规则。**

### 12.5 映射优先级

当多种映射方式同时存在时，优先级如下：

```text
SQL 别名（最高）
↓
@Table（表级映射）
↓
默认映射（同名规则）
```

### 12.6 命名解耦原则

FastQuery 采用“**SQL 映射驱动**”的设计：

- 数据库字段名 **无需** 与实体属性名一致
- 数据库表名 **无需** 与实体类名一致

👉 映射关系最终由 SQL（或显式配置）控制。

### 12.7 设计优势

这种设计带来的好处：

- **降低耦合**：数据库结构变化不强制修改实体
- **提高灵活性**：支持复杂查询与多表结果映射
- **避免冲突**：多表查询时可通过别名解决字段重名问题

## 13. 动态条件查询：`@Condition`

`@Condition` 是 FastQuery 中用于构建动态查询条件的核心机制。可以理解为：**在注解层面声明式构建动态 WHERE 条件**。


### 13.1 基本用法

```java
@Query("select no, name, sex from Student #{#where} order by age desc")
@Condition("no like ?1")
@Condition("and name like ?2")
@Condition(value = "and age > ?3", ignoreNull = false)
@Condition("and name not like ?4")
@Condition("or age between ?5 and ?6")
Student[] findAllStudent(... args ...);
```

执行模型

- `#{#where}`：动态 WHERE 占位符
- 每个 `@Condition`：定义一个**条件单元（condition fragment）**
- 框架根据参数值判断是否拼接该条件单元

👉 最终 SQL = 基础 SQL + 动态拼接的条件单元


### 13.2 条件参与规则

默认情况下，`@Condition` 是否参与拼接，取决于其**所有相关参数**：

| 参数情况                    | 行为           |
| --------------------------- | -------------- |
| 任一参数为 `null`           | 移除该条件单元 |
| 任一参数为 `""`（空字符串） | 移除该条件单元 |

#### 多参数条件说明

当一个条件单元包含多个参数时（如 `between`）：

```java
@Condition("or age between ?5 and ?6")
```

👉 规则：

- 只要任一参数为 `null` 或 `""`
- 整个 `@Condition` 将被移除

#### 控制选项

```java
@Condition(value = "...", ignoreNull = false)
@Condition(value = "...", ignoreEmpty = false)
```

- `ignoreNull = false` → `null` 参数仍参与条件
- `ignoreEmpty = false` → 空字符串仍参与条件

👉 默认值：

```java
ignoreNull = true
ignoreEmpty = true
```

### 13.3 `$` 模板表达式的特殊规则

当 `@Condition` 中使用 `$` 表达式（`$name` / `${name}`）时，条件参与规则发生变化：**`$` 表达式不参与参数判空逻辑，不受 `ignoreNull` 和 `ignoreEmpty` 控制**。

#### 行为说明

- `$` 表达式属于**模板替换（template substitution）**
- 不属于参数绑定（不会转为 `?`）
- 即使参数为 `null` 或 `""`，条件仍然保留

#### 示例

```java
@Query("select * from userinfo #{#where}")
@Condition("age between $age1 and ${age2}")
List<Map<String, Object>> between(@Param("age1") Integer age1,
                                  @Param("age2") Integer age2);
```

当：

```java
age1 = null
age2 = 30
```

生成 SQL（示意）：

```sql
age between  and 30
```

#### 设计说明

这是因为：

- `$` 表达式仅进行**字符串替换**
- `null` 默认会被替换为 `""`（空字符串）
- 框架不会基于 `$` 表达式的值判断条件是否移除

#### 结论

`?N` / `:name` → 影响条件是否参与，`$表达式` → 只影响 SQL 内容，不影响条件存在。

### 13.4 `null` 语义转换

当 `ignoreNull = false` 时，`null` 参数不会导致条件移除，而会参与 SQL 生成。此时框架会进行语义转换：

#### 等值判断

```java
@Condition(value = "name = ?1", ignoreNull = false)
```

当参数为 `null`：

```sql
name is null
```

#### 不等判断

```java
@Condition(value = "name != ?1", ignoreNull = false)
```

当参数为 `null`：

```sql
name is not null
```

#### 设计说明

这是因为：

- SQL 中 `NULL` 不能参与 `=`、`!=` 等比较运算
- 必须使用 `IS NULL` / `IS NOT NULL`

👉 框架会自动完成该语义转换


### 13.5 补充说明

`@Condition` 适用于中等复杂度的动态条件拼接。

对于更复杂的场景（如嵌套条件、分支逻辑、条件复用等），建议使用 `@QueryByNamed`。通过将 SQL 定义在模板文件中，可以在其中进行更灵活的逻辑控制，从而实现更高复杂度的动态查询能力。

## 14. `@Condition` 的高级控制

在基础的“参数为空则移除条件”之上，FastQuery 提供了更灵活的条件控制机制：

- 基于脚本的控制（`ignoreScript`）
- 条件分支控制（`if$ / else$`）
- 自定义判定逻辑（`Judge`）

👉 本质：**将“条件是否参与拼接”的决策逻辑外部化**


### 14.1 `ignoreScript`（脚本控制）

通过 `ignoreScript` 属性，可以使用一段 Java 表达式决定条件是否被移除。

```java
@Query("select id,name,age from userinfo #{#where}")
@Condition("age > :age")
@Condition(value="and name like :name",
           ignoreScript=":age > 18 && :name != null && :name.contains(\"Rex\")")
Page<UserInfo> find(@Param("age") int age,
                    @Param("name") String name,
                    Pageable pageable);
```


#### 行为规则

- 脚本返回 `true` → **移除条件**
- 脚本返回 `false` → **保留条件**


#### 执行机制

- 脚本在**初始化阶段编译为字节码**
- 运行时直接执行，不引入额外解析开销


#### 参数引用

脚本中通过 `:name` 形式引用方法参数：

```text
:age  → @Param("age")
:name → @Param("name")
```


#### 注意事项

- 表达式必须返回 `boolean`，否则启动失败
- 脚本内不支持自动拆箱（unboxing），需显式调用：

```text
:age.intValue() > 18
```


#### 使用建议

- 适用于简单逻辑判断
- 不建议编写过长或复杂脚本（影响可读性和维护性）


### 14.2 `if$` / `else$`（条件分支）

用于在同一个条件位置，根据不同情况选择不同 SQL 片段。

```java
@Query("select id,name,age from userinfo #{#where}")
@Condition(value="age > :age",
           if$=":age < 18",
           else$="name = :name")
Page<UserInfo> findPage(@Param("age") int age,
                        @Param("name") String name,
                        Pageable pageable);
```


#### 行为规则

- `if$ = true` → 使用原始条件（`value`）
- `if$ = false` → 使用 `else$` 中的 SQL
- 若未提供 `else$` → 移除该条件


#### 示例说明

```text
age < 18  → 使用：age > :age
age ≥ 18  → 使用：name = :name
```


### 14.3 自定义 `Judge`（复杂逻辑控制）

当条件逻辑较复杂，不适合使用脚本表达时，可以通过自定义类进行控制。


#### 定义方式

继承 `org.fastquery.where.Judge` 并实现 `ignore()` 方法：

```java
public class LikeNameJudge extends Judge {
    @Override
    public boolean ignore() {
        int age = this.getParameter("age", int.class);
        String name = this.getParameter("name", String.class);
        return age > 18 && name != null && name.contains("Rex");
    }
}
```


#### 使用方式

```java
@Condition(value="and name like :name", ignore=LikeNameJudge.class)
```


#### 行为规则

- `ignore()` 返回 `true` → 移除条件
- 返回 `false` → 保留条件


#### 参数获取

在 `Judge` 内部可获取所有方法参数：

```java
this.getParameter("name", String.class);
```


#### 默认实现

- 默认使用 `DefaultJudge`
- 默认行为：始终保留条件（不做处理）


### 14.4 使用建议

| 场景         | 推荐方式       |
| ------------ | -------------- |
| 简单条件控制 | `ignoreScript` |
| 分支逻辑     | `if$ / else$`  |
| 复杂业务判断 | 自定义 `Judge` |

## 15. SQL `IN` 查询

在 `IN` 查询中使用集合、数组或可变参数，并自动展开为 SQL 列表。

### 15.1 使用 `?N`（位置参数）

```java
@Query("select * from UserInfo where name in (?1)")
List<UserInfo> findByNameIn(String... names);

@Query("select * from UserInfo where name in (?1) and id > ?2")
List<UserInfo> findByNameListIn(List<String> names, Integer id);
```

#### 参数规则

- 支持类型：
  - 数组（如 `String[]`）
  - 集合（如 `List` / `Set`）
  - 可变参数（`varargs`）

👉 框架会自动展开为：

```sql
name in (?, ?, ?)
```

#### 空集合处理

当参数为：空集合 / 空数组

👉 框架会将其转换为：

```sql
name in (null)
```

#### SQL 语义说明

在 SQL 中，`NULL` 参与 `IN / NOT IN` 运算具有特殊语义：

```sql
id in (null)          -- 永远为 UNKNOWN，因此条件不会成立，查询结果为空。换言之，无论 id 是否为 NULL，都不会匹配到任何记录。
id in (1, 2, null)    -- null 不参与匹配，不影响已有值（1、2）的查询结果，但不会匹配 id IS NULL 的行
id not in (1,2,null)  -- 表达式结果始终为 UNKNOWN，因此不会返回任何记录
```

#### 结论

- `IN` 中包含 `NULL`：
  - 不影响已有匹配值（如 `1,2`）
- `NOT IN` 中包含 `NULL`：
  - 会导致整个条件失效（返回空结果）

### 15.2 使用 `:name`（命名参数）

```java
@Query("select * from student where sex = :sex and age > :age and name in (:names)")
List<Student> findByIn(@Param("sex") String sex,
                       @Param("age") Integer age,
                       @Param("names") Set<String> names);
```

行为说明

- 命名参数与 `?N` 规则一致
- 集合参数会自动展开为多个占位符

### 15.3 使用建议

为避免 SQL 语义陷阱，建议：

- **避免传入空集合**
- 在业务层提前判断：

```java
if (names.isEmpty()) {
    return Collections.emptyList();
}
```

- 避免在 `NOT IN` 中使用可能包含 `null` 的集合

## 16. `@Modifying`：改操作

当 SQL 为 **插入（INSERT）**、**更新（UPDATE）** 或 **删除（DELETE）** 时，需要配合 `@Modifying` 使用。

👉 **默认开启事务（自动事务管理）**

### 16.1 返回值类型

`@Modifying` 支持多种返回方式，用于表达不同的业务语义。

#### 1）返回影响行数（推荐）

```java
@Query("update student s set s.age=?3, s.name=?2 where s.no=?1")
@Modifying
int update(String no, String name, int age);
```

**说明：**

- 返回值表示受影响的行数
- 返回 `0` 不代表失败，仅表示未命中任何数据

#### 2）返回布尔值

```java
@Modifying
@Query("delete from userinfo where id=?1")
boolean deleteUserinfoById(int id);
```

**说明：**

- 内部基于影响行数判断
- 影响行数 ≥ 0 → 返回 `true`

⚠️ 注意：

> 返回 `true` 并不意味着一定修改了数据，仅表示 SQL 执行成功

### 16.2 插入或修改后返回结果

支持在插入或修改操作后回查并返回数据（Post-Insert Fetch）。

```java
@Modifying(id="id", table="userinfo")
@Query("insert into #{#table} (name, age) values (?1, ?2)")
UserInfo saveUserInfo(String name, Integer age);
```

### 16.3 `@Modifying` 参数说明

| 参数           | 说明                 |
| -------------- | -------------------- |
| `table`        | 指定表名（用于回查） |
| `id`           | 主键字段             |
| `selectFields` | 回查字段列表         |

**`selectFields` 示例**

```java
@Modifying(id="id", table="userinfo", selectFields="name,age")
@Query("insert into #{#table} (name, age) values (?1, ?2)")
UserInfo addUserInfo(String name, Integer age);
```

默认行为：`selectFields = "*"`

👉 默认查询所有字段

### 16.4 主键说明

当满足以下条件时：

- 主键不是自增字段
- 主键值由外部传入
- 且操作后需要进行回查

👉 必须使用 `@Id` 标识主键参数：

```java
Student addStudent(@Id String no, ...);
```

**说明：**

- `@Id` 用于标识主键来源
- 框架将基于该值构造回查条件

👉 若未指定，框架将无法确定回查依据

## 17. `@Set`：动态更新字段

`@Set` 是 `@Condition` 在更新语句中的对应机制，用于**动态构建 `SET` 子句**。

> 可以理解为：**根据参数情况，声明式控制哪些字段参与更新**

### 17.1 基本用法

```java
@Modifying
@Query("update Course #{#sets} where no = ?5")
@Set("name = ?1")
@Set("credit = ?2")
@Set("semester = ?3")
@Set("period = ?4")
int updateCourse(String name, Integer credit, Integer semester, Integer period, String no);
```

执行模型：

- `#{#sets}`：动态 `SET` 占位符
- 每个 `@Set`：定义一个**设置单元（set fragment）**
- 框架根据参数决定是否拼接该设置单元

👉 最终 SQL：

```text
update ... set 动态拼接的字段列表 where ...
```

------

### 17.2 默认参与规则

默认情况下，`@Set` 是否参与拼接，取决于其参数值：

| 参数情况          | 行为           |
| ----------------- | -------------- |
| 任一参数为 `null` | 移除该设置单元 |
| 任一参数为 `""`   | 移除该设置单元 |

#### 多参数说明

当一个 `@Set` 包含多个参数时：

```java
@Set("name = ?1, credit = ?2")
```

👉 规则：

- 任一参数为 `null` 或 `""`
- 整个设置单元将被移除

#### 控制选项

```java
@Set(value="...", ignoreNull=false, ignoreEmpty=false)
```

- `ignoreNull = false` → `null` 参数仍参与更新
- `ignoreEmpty = false` → 空字符串仍参与更新

👉 默认值：

```text
ignoreNull = true  
ignoreEmpty = true
```

### 17.3 空 `SET` 风险

当所有 `@Set` 被移除时，可能生成非法 SQL：

```sql
update Course set where no = ?
```

解决方案

**方式一：添加保底设置（推荐）**

```java
@Set("name = name")
```

👉 永不移除，且不会影响数据

**方式二：调用前进行参数校验**

确保至少有一个字段参与更新

### 17.4 高级控制能力

与 `@Condition` 保持一致，`@Set` 支持以下控制方式：

- `ignoreScript`
- `if$ / else$`
- 自定义 `Judge`

#### 1）`ignoreScript`（脚本控制）

```java
@Set(value="name = :name",
     ignoreScript=":name!=null && :name.startsWith(\"计算\") && :credit!=null && :credit.intValue() > 2")
```

规则：

- 返回 `true` → 移除该设置单元
- 返回 `false` → 保留

注意事项：

- 表达式必须返回 `boolean`
- 不支持自动拆箱：

```text
:credit.intValue() > 2
```

执行机制：

- 脚本在**初始化阶段编译**
- 运行期无额外解析开销

#### 2）`if$ / else$`（条件分支）

```java
@Set(value="name = :name", if$="!:name.contains(\"root\")", else$="name = name")
```

规则：

- `if$ = true` → 使用原始设置
- `if$ = false` → 使用 `else$`，若未提供 `else$` → 移除该设置单元

#### 3）自定义 `Judge`（复杂逻辑）

```java
public class NameJudge extends Judge {
    @Override
    public boolean ignore() {
        String name = this.getParameter("name", String.class);
        Integer credit = this.getParameter("credit", Integer.class);
        return name.startsWith("计算") && credit != null && credit > 2;
    }
}
```

使用：

```java
@Set(value="name = :name", ignore=NameJudge.class)
```

规则：

- `ignore()` 返回 `true` → 移除
- 返回 `false` → 保留

### 17.5 组合生效规则

一个 `@Set` 被移除的条件包括：

- 参数为 `null`
- 参数为空字符串
- `ignoreScript` 返回 `true`
- `if$` 判断失败且未提供 `else$`
- 自定义 `Judge` 返回 `true`

👉 满足任一条件，该设置单元都会被移除

### 17.6 其他实现方式

动态更新字段还可以通过以下方式实现：

#### 1）实体更新

```java
int executeUpdate(E entity)
```

👉 值为 `null` 的字段不会参与更新

#### 2）SQL 模板

使用 `@QueryByNamed` 实现更复杂逻辑

#### 3）模板表达式

使用 `$` 表达式实现动态 SQL（需自行控制安全性）

### 17.7 统一模型说明

FastQuery 提供统一的动态 SQL 构建模型：

```text
WHERE → @Condition  
SET   → @Set
```

## 18. Predicate：单表条件构建器

### 18.1 什么是 Predicate？

`Predicate` 是 FastQuery 提供的**条件构建器（Condition Builder）**，用于：

- 构建 SQL 的 `WHERE` 条件
- 支持 `AND / OR` 组合
- 支持动态条件（参数为空自动忽略）
- 支持函数式表达（避免字符串拼接 SQL）

👉 核心定位：

> 面向**单表查询**，基于**实体 + 函数式表达式**构建查询条件

### 18.2 核心设计思想

#### 18.2.1 不再手写 SQL 字符串

传统方式：

```java
"where age > 18 and name like ?"
```

Predicate：

```java
user.and(User::age, GT, 18)
    .and(User::name, LIKE, "Tom%");
```

👉 SQL 由框架自动生成

#### 18.2.2 列名使用 Chip（避免字符串）

```java
public static Chip<Integer, User> age() {
    return new Chip<>("age");
}
```

调用：

```java
user.and(User::age, GT, 18);
```

👉 优势：

- 编译期安全（避免拼错字段名）
- 重构安全（字段改名自动同步）

#### 18.2.3 条件链式累加

```java
user.and(...).or(...).and(...);
```

👉 生成：

```sql
where ... and ... or ... and ...
```

### 18.3 快速开始

#### 第1步 定义实体（必须继承 Predicate）

```java
public class User extends Predicate<User> {

    private Long id;
    private String name;
    private Integer age;

    public static Chip<Long,User> id() {
        return new Chip<>("id");
    }

    public static Chip<String,User> name() {
        return new Chip<>("name");
    }

    public static Chip<Integer,User> age() {
        return new Chip<>("age");
    }
}
```

每个字段都要提供一个 `Chip` 方法

#### 第2步 定义 Repository

```java
public interface UserDBService extends QueryRepository {
}
```

#### 第3步 构建条件

```java
User user = new User();

user.and(User::age, SQLOperator.GT, 18)
    .and(User::name, SQLOperator.LIKE, "Tom%")
    .orderBy();
```

#### 第4步 执行查询

```java
@Resource
UserDBService db;

User result = db.findOne(user, false);
```

#### `QueryRepository` 中的如下内置方法支持`Predicate`特性：

- `executeUpdate`
- `count`
- `findOne`
- `exists`
- `existsEachOn`
- `findPageByPredicate`

### 18.4 条件来源模型

支持两种条件构建模式：

#### 18.4.1 模式一：实体字段自动生成条件（默认）

当出现以下任一情况时，框架会根据实体字段值自动生成 `WHERE` 条件：

- 实体**没有继承** `Predicate`
- 或者实体**继承了** `Predicate`，但**没有调用** `and / or` 等方法显式构建条件

规则：

- 字段值不为 `null` → 参与条件（默认 `=`）
- 字段值为 `null` → 自动忽略
- 条件之间默认使用 `AND` 连接

示例：

```java
user.setName("Tom");
user.setAge(18);
```

会生成 SQL：`where name = ? and age = ?`

#### 18.4.2 模式二：Predicate 显式构建条件

当实体继承 `Predicate`，且调用 `and / or` 等方法显式添加条件时：

```java
user.and(User::age, GT, 18)
    .and(User::name, LIKE, "Tom%");
```

支持：

- 比较操作（`> < like in is null`）
- AND / OR
- 分组（括号）

#### 18.4.3 使用建议

你可以把条件构建理解为两层机制：

| 层级   | 方式                        | 适用场景 |
| :----- | :-------------------------- | :------- |
| 第一层 | 实体字段（自动 equal 条件） | 简单查询 |
| 第二层 | `Predicate`（显式条件构建） | 复杂查询 |

建议做法：

- 简单查询：直接 `set` 字段值（更简洁）
- 复杂查询：使用 `Predicate` 构建条件（更灵活）
- 为了可读性，避免混用两种模式

重要说明：

- 模式一与模式二**不能同时生效**，两者之间不存在交织与叠加。

一句话总结：

> 即使不显式使用 `Predicate`，实体本身也可以作为“天然的 WHERE 条件构建器”。

### 18.5 条件构建详解

#### 基础条件（最常用）

```java
user.and(User::age, SQLOperator.GT, 18);
```

生成 SQL：

```sql
age > ?
```

#### 默认等于（简写）

```java
user.and(User::age, 18);
```

等价于：

```java
user.and(User::age, SQLOperator.EQ, 18);
```

#### OR 条件

```java
user.or(User::age, 18);
```

#### LIKE 模糊查询

```java
user.and(user::name, SQLOperator.LIKE, "Tom%");
```

#### IN 查询

```java
user.and(User::age, SQLOperator.IN, List.of(18, 20, 25));
```

生成 SQL：

```sql
age in (?, ?, ?)
```

注意（非常重要），如果集合为空：

```java
List.of()
```

会生成 SQL：

```sql
age in (NULL)
```

建议你自己判断：

```java
if (!list.isEmpty()) {
    user.and(User::age, IN, list);
}
```

#### NULL 判断

```java
user.and(user::name, NulOperator.ISNULL);
user.and(user::name, NulOperator.ISNOTNULL);
```

生成 SQL：

```sql
name is null
name is not null
```

### 18.6 复杂条件

```java
user.and(User::age, SQLOperator.GE, 18)
    .and(p -> p
        .or(user::name, LIKE, "Tom%")
        .or(user::name, LIKE, "Jerry%")
    )
    .orderBy();
```

生成 SQL：

```sql
where age >= ?
and ( name like ? or name like ? )
order by id desc
```

### 18.7 排序（orderBy）

#### 默认排序

```java
user.orderBy();
```

生成 SQL：`order by id desc`

#### 自定义排序

```java
user.orderBy("age desc, id asc");
```

强烈建议：

❌ 不要直接拼用户输入

✅ 使用白名单：

```java
String sort = switch(param) {
    case "age" -> "age desc";
    case "name" -> "name asc";
    default -> "id desc";
};
user.orderBy(sort);
```

### 18.8 更新能力

#### set 更新字段

```java
user.set(User::name, "Tom");
```

生成 SQL：

```sql
set name = ?
```

#### increment 自增

```java
user.increment(User::age, 1);
```

生成 SQL：

```sql
set age = age + 1
```

注意：

- `set()` 会自动忽略 `null`
- `increment()` 不允许 null

### 18.9 重要行为

#### 18.9.1 null 自动忽略

```java
user.and(User::age, null);
```

👉 不生成条件，非常适合搜索接口

#### 18.9.2 延迟生成

SQL 在以下时机生成：

```java
orderBy() 或 finish()
```

#### 18.9.3 自动参数绑定

```sql
age > ?
```

👉 防 SQL 注入

## 19. `@QueryByNamed`：命名式查询（模板 SQL）

`@QueryByNamed` 用于将 SQL 定义在外部模板文件中，并通过注解按名称进行绑定与执行。

👉 核心能力：

- 将 SQL 与 Java 代码解耦
- 支持复杂动态 SQL（基于模板引擎）
- 支持 SQL 片段复用
- 适用于复杂查询与动态逻辑场景

### 19.1 基本概念

使用方式：

```java
@QueryByNamed("findUserInfoAll")
JSONArray findUserInfoAll();
```

👉 表示绑定模板文件中 `id="findUserInfoAll"` 的 SQL

模板文件命名规则：

```text
类全限定名 + .queries.xml
```

例如：

```text
org.fastquery.dao.QueryByNamedDBExample.queries.xml
```

👉 要求：

- 必须位于 `classpath` 下
- 可通过 `fastquery.json` 中的 `queries` 属性配置模板目录

### 19.2 模板文件结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<queries>

    <query id="findUserInfoAll">
        select id,name,age from UserInfo
    </query>

    <query id="findUserInfoOne">
        <value>
            select id,name,age from UserInfo where id = :id
        </value>
    </query>

</queries>
```

`<query>` 元素说明

| 元素           | 说明         |
| -------------- | ------------ |
| `id`           | 唯一标识     |
| `<value>`      | SQL 主体     |
| `<parts>`      | SQL 片段定义 |
| `<countQuery>` | 分页统计 SQL |

👉 说明：若 `<query>` 仅包含一段 SQL 文本，可直接作为节点内容；当存在多结构元素（如 `<parts>`）时，建议使用 `<value>` 明确 SQL 主体。

### 19.3 模板引擎（Velocity）

模板文件中的 SQL 会经过 **Apache Velocity** 模板引擎渲染（Template Rendering）。

👉 支持：

- 条件控制：`#if / #else / #end` 等 Velocity 控制指令
- 变量引用：`${}`（用于模板变量与上下文数据）
- 动态拼接 SQL（基于模板逻辑生成最终语句）

示例：动态条件

```xml
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
```

### 19.4 表达式说明（重要）

模板中存在三类表达式：

| 表达式              | 作用     | 是否参与预编译 |
| ------------------- | -------- | -------------- |
| `:name`             | 参数绑定 | ✅              |
| `?N`                | 位置参数 | ✅              |
| `$name` / `${name}` | 模板变量 | ❌              |

关键区别

参数绑定（`:name` / `?N`）

```sql
where id = :id
```

👉 转换为：

```sql
where id = ?
```

👉 使用 `PreparedStatement`，防止 SQL 注入

模板变量（`${name}`）

```xml
#if(${name})
```

👉 用于：

- 参与模板阶段的逻辑判断
- 控制 SQL 结构的动态生成
- 在模板中直接引用参数源值

👉 特性说明：

- 仅作用于**模板渲染阶段**
- 不参与 `PreparedStatement` 参数绑定
- 不属于 SQL 预编译参数

一句话总结

> `:name` / `?N` 用于参数绑定，`${name}` 用于模板渲染与逻辑控制

### 19.5 SQL 片段复用（parts）

```xml
<parts>
    <part name="fields">id,name,age</part>
</parts>
```

使用：

```sql
select #{#fields} from UserInfo
```

作用范围

- `<query>` 内定义 → 局部作用域
- `<queries>` 下定义 → 全局作用域

### 19.6 XML 特殊字符处理

XML 中以下字符需要转义：

| 字符 | 实体     |
| ---- | -------- |
| `<`  | `&lt;`   |
| `>`  | `&gt;`   |
| `&`  | `&amp;`  |
| `'`  | `&apos;` |
| `"`  | `&quot;` |

推荐方式：使用 CDATA

```xml
<![CDATA[
    select * from UserInfo where age < 18
]]>
```

👉 推荐使用 CDATA 避免转义问题

### 19.7 方法绑定规则

```java
@QueryByNamed("findUserInfoOne")
UserInfo findUserInfoOne(@Param("id") Integer id);
```

默认行为

```java
@QueryByNamed
List<Student> findSomeStudent();
```

👉 等价：

```java
@QueryByNamed("findSomeStudent")
```

### 19.8 是否启用模板引擎（render）

```java
@QueryByNamed(render = false)
```

说明

- `render = true`（默认） → 启用模板引擎
- `render = false` → 不进行模板渲染

👉 注意：

- `:name`、`?N` 参数绑定 **不依赖模板引擎**
- `${name}` 在参与逻辑运算（如 `#if` 判断）时依赖模板引擎（需设置 `render = true`）；若仅用于引用参数源值（简单替换），则不涉及模板逻辑执行，可关闭模板引擎（`render = false`）

使用建议

| 场景                     | 建议             |
| ------------------------ | ---------------- |
| 存在模板逻辑（`#if` 等） | `render = true`  |
| 仅作为 SQL 存储          | `render = false` |

### 19.9 改操作支持

```java
@Modifying
@QueryByNamed("updateUserInfoById")
int updateUserInfoById(@Param("id") int id,
                       @Param("name") String name,
                       @Param("age") int age);
```

模板：

```xml
<query id="updateUserInfoById">
    update UserInfo set name = :name, age = :age where id = :id
</query>
```

### 19.10 SQL 中的参数逻辑控制

参数不仅可以绑定，还可以参与 SQL 逻辑：

```sql
select if(?1 > 10, '大于10', '不大于10')
select if(:number > 10, t.B > 10, t.C > 100)
```

👉 支持常见函数：

- `IF`
- `IFNULL`
- `NULLIF`
- `ISNULL`

⚠️ 注意：上述函数依赖具体数据库实现（如 MySQL 的 `IF`），不同数据库可能存在语法差异。

### 19.11 多方法复用同一模板

```java
@QueryByNamed("findSome")
JSONArray findLittle();

@QueryByNamed("findSome")
JSONArray findSome();
```

`_method` 上下文变量，模板中可访问当前方法：

```xml
#if(${_method.name} == "findLittle")
    select ... limit 3
#else
    select ... limit 5
#end
```

👉 `_method` 类型：

```text
org.fastquery.core.MethodInfo
```

👉 说明：

- `${_method.getName()}` 可简写为 `${_method.name}`
- 该能力用于根据调用上下文动态选择 SQL。⚠️ 建议谨慎使用，避免在模板中引入过多分支逻辑，影响可读性与维护性。

### 19.12 使用建议

适用于：

- 复杂动态 SQL（多条件、多分支）
- SQL 需要复用
- SQL 过长，不适合写在注解中
- 需要模板逻辑控制

不推荐使用场景

- 简单 CRUD
- SQL 简短且固定
- 无动态逻辑

👉 优先使用 `@Query`

### 19.13 一句话总结

`@QueryByNamed` 是 FastQuery 的模板化 SQL 方案，用于解决复杂动态 SQL 场景，是 `@Query` 的增强形态。

## 20. 分页

FastQuery 内置分页能力。当方法返回值为 `Page<T>`，且参数中声明了分页参数（如 `Pageable` 或 `@PageIndex` / `@PageSize`）时，框架会自动识别为分页查询，并完成以下工作：

- 查询当前页数据
- 统计总记录数（可关闭）
- 生成分页元信息
- 按数据库方言自动拼接分页语句（如 `LIMIT / OFFSET`）

### 20.1 分页查询的两种实现方式

FastQuery 支持两种分页写法：

| 方式            | 适用场景            |
| --------------- | ------------------- |
| `@Query`        | 简单、固定 SQL 分页 |
| `@QueryByNamed` | 复杂动态 SQL 分页   |

### 20.2 使用 `@Query` 实现分页

适用于结构清晰、逻辑较简单的分页查询。

#### 示例 1：指定统计字段

```java
@Query(value = "select id,name,age from userinfo", countField = "id")
Page<Map<String,Object>> findAll(Pageable pageable);
```

说明：

- `countField` 用于生成统计语句：

```sql
select count(id) ...
```

- 默认值通常为：`id`

#### 示例 2：自动推导统计 SQL

```java
@Query("select id,name,age from userinfo #{#where}")
@Condition("age > ?1")
@Condition("and id < ?2")
Page<UserInfo> find(Integer age, Integer id, Pageable pageable);
```

说明：

若未显式指定 `countQuery`，框架会尝试自动生成统计 SQL。

⚠️ 自动推导属于便利能力，适合中低复杂度 SQL；复杂查询建议显式指定 `countQuery`。

#### 示例 3：自定义统计 SQL

```java
@Query(
    value = "select id,name,age from userinfo #{#where}",
    countQuery = "select count(id) from userinfo #{#where}"
)
@Condition("age > ?1")
@Condition("and id < ?2")
Page<UserInfo> findSome(Integer age, Integer id, Pageable pageable);
```

适用于：

- 多表关联
- 子查询
- `group by`
- `distinct`
- 自动推导不准确的场景

### 20.3 使用 `@QueryByNamed` 实现分页

适用于复杂动态 SQL、条件较多、模板复用场景。

```java
@QueryByNamed("findPage")
Page<Student> findPage(Pageable pageable, @Param("name") String name, @Param("age") Integer age);
```

对应模板文件

```xml
<query id="findPage">

    <value>
        select no, name, sex from Student #{#condition} #{#order}
    </value>

    <countQuery>
        select count(no) from Student #{#condition}
    </countQuery>

    <parts>

        <part name="condition">
        <![CDATA[
        <where>

        #if($name)
            and name like :name
        #end

        #if($age)
            and age > :age
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

### 20.4 分页模板内置能力

#### 1）`#{#limit}`：分页片段

```sql
#{#limit}
```

表示分页区间，由框架按数据库方言自动生成，例如：

```sql
limit ?, ?
offset ? rows fetch next ? rows only
```

说明：

- 默认自动追加到 SQL 尾部
- 如语法特殊，也可手动放置在合法位置

#### 2）`<where>`：动态条件整理器

```xml
<where>
    #if(...)
        and ...
    #end
</where>
```

作用：

- 自动追加 `where`
- 自动去除首个 `and / or`
- 避免非法 SQL

例如：

```sql
where and age > 10
```

不会出现。

### 20.5 分页参数方式

FastQuery 提供两种传参方式。

#### 方式一：`Pageable`（推荐）

```java
Pageable pageable = new PageableImpl(1, 10);
```

含义：

- 第 1 页
- 每页 10 条

推荐原因：

- 接口语义清晰
- 扩展性更好
- 统一分页模型

#### 方式二：`@PageIndex` + `@PageSize`

```java
@Query("select id,name,age from userinfo")
Page<Map<String,Object>> findSome(@PageIndex int pageIndex, @PageSize int pageSize);
```

规则：

- `@PageIndex`：页码，从 **1** 开始
- 小于 1 按 1 处理
- `@PageSize`：每页数量
- 小于 1 按 1 处理

⚠️ 注意：

> `@PageIndex / @PageSize` 不能与 `Pageable` 同时使用。

适用于简单接口、快速接收前端 `page/size` 参数场景。

### 20.6 使用分页结果对象 `Page<T>`

```java
Pageable pageable = new PageableImpl(1, 10);
Page<UserInfo> page = userInfoDBService.findSome(18, 100, pageable);
```

常用方法:

```java
page.getContent();        // 当前页数据
page.getNumber();         // 当前页码
page.getSize();           // 每页大小
page.hasNext();           // 是否有下一页
page.hasPrevious();       // 是否有上一页
page.getTotalElements();  // 总记录数
page.getTotalPages();     // 总页数
page.isFirst();           // 是否第一页
page.isLast();            // 是否最后一页
```

### 20.7 `@NotCount`：关闭总数统计

表示分页查询时不执行 `count(id)` 统计。

适用于：

- 大表分页
- 深分页
- 无限滚动
- 只关心当前页数据
- 对性能敏感的场景

返回值约定

| 字段            | 值   |
| --------------- | ---- |
| `totalElements` | `-1` |
| `totalPages`    | `-1` |

其他分页信息仍然有效。

说明：

若使用 `@NotCount`，则：`countField` `countQuery` 将失去意义。

### 20.8 JSON 输出结构（示意）

```json
{
  "content": [...],
  "number": 1,
  "size": 15,
  "hasNext": true,
  "hasPrevious": false,
  "first": true,
  "last": false,
  "totalElements": 188,
  "totalPages": 13
}
```

### 20.9 数据库方言支持

当前内置支持：

- MySQL
- Microsoft SQL Server
- PostgreSQL

框架会自动生成对应分页语法。

### 20.10 扩展分页方言

如需支持其他数据库，可实现：

```text
org.fastquery.page.PageDialect
```

参考实现：

- `MySQLPageDialect`
- `PostgreSQLPageDialect`

### 20.11 使用建议

推荐优先使用 `Pageable`

```java
Page<T> find(..., Pageable pageable)
```

作为标准分页方式，更利于维护与扩展。

count 查询优化建议，复杂 SQL 建议显式指定：

```java
countQuery = "..."
```

避免自动推导失准或性能下降。

大表分页建议使用：`@NotCount` 减少统计成本。

### 20.12 一句话总结

FastQuery 将分页查询、总数统计、分页元信息与数据库方言统一封装，让分页开发保持简洁，同时具备工程级扩展能力。

## 21. 事务（Transaction）

事务用于保证一组数据库操作具备 **原子性（Atomicity）**：要么全部成功提交，要么发生异常时全部回滚。

FastQuery 提供两种事务使用方式：

- 注解式事务：`@Transactional`
- 函数式事务：`tx(...)`

### 21.1 使用 `@Transactional` 声明事务

当一个方法包含多条改操作（`INSERT / UPDATE / DELETE`）时，可通过 `@Transactional` 将其纳入同一事务。

```java
@Transactional
@Modifying
@Query("update userinfo set name = ?1 where id = ?3")
@Query("update userinfo set age = ?2 where id = ?3")
@Query("update userinfo set id = 1 where id = ?3")
int updateBatch(String name, Integer age, Integer id);
```

以上三条 SQL 会作为一个事务执行：

1. 修改姓名
2. 修改年龄
3. 修改主键

若第三条语句因主键冲突失败（例如 `id=1` 已存在），则：

- 前两条修改也会回滚
- 整个事务失败
- 数据保持原状

即：三条语句要么全部成功，要么全部失效。

### 21.2 返回值规则

返回 `int`：

```java
int updateBatch(...)
```

表示事务成功提交后，**所有改操作影响行数之和**。

例如：

```text
update1 → 1 行
update2 → 1 行
update3 → 1 行
```

返回：`3`

返回 `int[]`:

```java
int[] updateBatch(...)
```

表示事务成功提交后，每条 SQL 各自影响的行数。

例如事务内有三条语句：

- U1 → 1 行
- U2 → 2 行
- U3 → 0 行

返回：

```java
new int[]{1, 2, 0}
```

适用于需要了解每一步执行结果的场景。

### 21.3 使用函数式事务 `tx(...)`

除注解方式外，FastQuery 还提供内置事务函数：

```java
int effect = userInfoDBService.tx(() -> {

    // update 1
    // update 2
    // update 3

    return 3;
});
```

执行语义：

传入 Lambda 中的代码块会在一个事务中执行：

- 全部成功 → 提交事务
- 任意异常 → 回滚事务

即：Lambda 代码块中的数据库操作具备原子性。

### 21.4 适用场景对比

| 方式             | 适用场景                     |
| ---------------- | ---------------------------- |
| `@Transactional` | 固定事务流程、声明式编程     |
| `tx(...)`        | 动态流程、条件判断、循环处理 |

### 21.5 Lambda 变量捕获说明（Java 语言规则）

在 Java Lambda 中，只能捕获 **final 或 effectively final** 的局部变量。

例如：

```java
int sum = 0;

tx(() -> {
    sum = sum + 1;   // 编译错误
    return 1;
});
```

原因：

- Lambda 不能修改外部局部变量的值
- 这是 Java 语言规范，与 FastQuery 无关

可修改对象内容。若外部变量是对象引用，则可修改对象内部状态：

```java
Map<String,Object> map = new HashMap<>();

tx(() -> {
    map.put("ok", true);   // 合法
    return 1;
});
```

原因：

- 引用本身未变化
- 修改的是对象内部数据

### 21.6 如何从事务块中带出结果

推荐通过返回值或外部对象承载结果。

方式一：使用返回值（推荐）

```java
int total = tx(() -> {
    return 5;
});
```

方式二：使用对象承载

```java
AtomicInteger total = new AtomicInteger();

tx(() -> {
    total.set(5);
    return 1;
});
```

### 21.7 使用建议

#### 推荐 `@Transactional`

适用于：

- 固定业务流程
- 多条 SQL 原子提交
- 方法级事务边界清晰

#### 推荐 `tx(...)`

适用于：

- 循环插入
- 条件分支事务
- 运行期动态决定执行步骤

### 21.8 注意事项

- 事务主要用于改操作（增删改）
- 查询通常不需要事务（除锁定读取等特殊场景）
- 事务范围应尽量小，避免长事务占用连接资源
- 不建议在事务中执行耗时 IO（网络请求、文件操作等）

### 21.9 一句话总结

FastQuery 同时提供声明式事务与函数式事务，既适合固定业务流程，也适合复杂动态事务场景。

## 22. 类型映射

FastQuery 的类型映射采用 **宽兼容策略（compatible mapping）**，并非严格的一一强类型绑定。

其核心原则是：**只要目标 Java 类型能够安全容纳 SQL 字段值，即可完成映射。**

换句话说：

- Java 类型“装得下” SQL 值 → 可以映射
- Java 类型“装不下” SQL 值 → 不建议映射，可能产生溢出、截断或转换异常

这意味着 FastQuery 更关注：

- 数据是否可承载
- 转换是否合理
- 使用是否便利

而不是机械地要求 JDBC 类型完全对应。

### 22.1 常见映射关系

| Java 类型   | 常见 SQL 类型                     |
| ----------- | --------------------------------- |
| `Boolean`   | `BIT(1)`、`TINYINT(1)`、`CHAR(1)` |
| `Byte`      | `TINYINT`                         |
| `Short`     | `SMALLINT`                        |
| `Integer`   | `INT`                             |
| `Long`      | `BIGINT`                          |
| `Float`     | `FLOAT`                           |
| `Double`    | `DOUBLE`                          |
| `Character` | `CHAR(1)`                         |
| `Enum`      | `ENUM`                            |
| `EnumSet`   | `SET`                             |
| `String`    | 几乎所有常见类型（按字符串读取）  |

### 22.2 设计特点

#### 宽类型可接收窄类型

例如：

```text
SMALLINT → Integer
INT      → Long
FLOAT    → Double
```

通常是可行的，因为目标类型容量更大。

#### 窄类型接收宽类型需谨慎

例如：

```text
BIGINT → Integer
DOUBLE → Float
```

可能出现：

- 数值溢出
- 精度损失
- 转换异常

因此不推荐。

### 22.3 `String` 的特殊性

`String` 具备较强兼容性，可用于读取多种 SQL 类型，例如：

- 数值型
- 字符型
- 枚举型
- 日期型（按字符串形式）

例如：

```sql
INT        → "100"
DOUBLE     → "3.14"
DATETIME   → "2015-06-20 10:30:00"
```

因此：`String` 常作为通用接收类型使用，但会失去原始类型语义。

### 22.4 布尔值映射说明

以下字段常被映射为 `Boolean`：

| SQL 值类型   | 常见解释                          |
| ------------ | --------------------------------- |
| `BIT(1)`     | `0 / 1`                           |
| `TINYINT(1)` | `0 / 1`                           |
| `CHAR(1)`    | `Y/N`、`T/F`、`0/1`（视规则而定） |

### 22.5 枚举映射

`Enum`，适用于数据库 `ENUM` 字段：

```java
enum Status {
    ENABLED, DISABLED
}
```

`EnumSet`，适用于数据库 `SET` 字段（如 MySQL）。

例如：

```sql
SET('READ','WRITE','EXECUTE')
```

映射为：

```java
EnumSet<Permission>
```

### 22.6 使用建议

#### 推荐使用语义明确的类型

```java
Integer age
Long id
Boolean enabled
```

优于：

```java
String age
String enabled
```

#### 数值字段优先使用包装类型

```java
Integer ✔
Long ✔
int ✘（查询字段可能为 null）
```

原因：

- SQL 字段允许 `NULL`
- 包装类型可表示 `null`

#### 超大数值谨慎选择类型

若数据库字段可能超出 `Integer` 范围，建议直接使用：`Long`

### 22.7 一句话总结

FastQuery 的类型映射采用“能安全承载即可映射”的宽兼容策略，强调实用性与开发效率，而非僵硬的一一强绑定。

## 23. 动态适配数据源

在运行时动态切换数据源，使同一 `Repository` 接口可连接不同数据库实例。

适用于：

- 多租户系统（tenant isolation）
- 读写分离
- 分库分表后的库级路由
- 多环境数据库切换
- 同构数据库集群访问

### 23.1 使用 `@Source` 指定数据源

通过 `@Source` 可动态指定当前方法执行时所使用的数据源。

```java
@Query("select id,name,age from userinfo u where u.age > ?1")
Map<String,Object> findOne(Integer age, @Source String dataSourceName);
```

调用示例：

```java
findOne(18, "tenant_a");
findOne(18, "tenant_b");
```

表示同一查询逻辑，分别连接不同数据源执行。

### 23.2 典型应用场景：多租户系统

若系统采用：

- 每个租户独立数据库
- 表结构一致
- 业务逻辑一致

则可通过 `@Source` 动态切换租户数据库，而无需复制 DAO 接口。

例如：

```text
tenant_a → db_a
tenant_b → db_b
tenant_c → db_c
```

这种模式在 SaaS 系统中非常实用。

### 23.3 使用规则

参数类型限制

当 `@Source` 标注在方法参数上时，该参数类型必须为：`String`，用于表示数据源名称。

例如：

```java
@Source String dataSourceName
```

### 23.4 数据源优先级

若同时存在以下两种配置：

#### 1）`fastquery.json` 中已配置默认数据源作用域

```json
{
  "scope": [...]
}
```

#### 2）方法参数使用 `@Source`

则优先级为：

```text
@Source 指定的数据源
    >
fastquery.json 默认作用域数据源
```

即：显式指定优先于配置默认值。

### 23.5 使用建议

推荐用于“同结构多库”场景，例如：

- 多租户独立库
- 按地区拆库
- 灰度数据库切换

不建议用于频繁随机切换，若每次请求都随机切库，需注意：

- 连接池数量
- 连接复用率
- 初始化成本
- 运维复杂度

### 23.6 一句话总结

`@Source` 提供运行时数据源路由能力，使同一 Repository 可服务多个数据库实例，非常适合多租户与多库场景。

## 24. 扩展支持连接池

FastQuery 已内置支持常见连接池，例如：

- HikariCP
- Druid
- c3p0

同时支持扩展自定义连接池。

### 24.1 扩展步骤总览

实现流程：

1. 实现 `ConnectionPoolProvider`
2. 在 `pool-extend.xml` 注册
3. 在 `fastquery.json` 中使用

### 24.2 第一步：实现连接池提供者

实现接口：

```text
org.fastquery.core.ConnectionPoolProvider
```

示例：

```java
public class MyPoolProvider implements ConnectionPoolProvider {

    @Override
    public DataSource getDataSource(
            Resource resource,
            String dataSourceName) {

        // 读取配置文件
        InputStream in =
            resource.getResourceAsStream(...);

        Properties props = new Properties();

        // 初始化配置
        props.setProperty(...);

        // 创建并返回数据源
        return new MyDataSource(props);
    }
}
```

说明：

该接口职责是：根据配置创建并返回 `DataSource` 实例。

### 24.3 第二步：注册连接池

创建配置文件：

```text
pool-extend.xml
```

内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<pools name="providers">
    <pool name="mypool" class="org.fastquery.xxx.MyPoolProvider" />
</pools>
```

说明：

- `name`：连接池名称
- `class`：实现类全限定名

### 24.4 第三步：在 `fastquery.json` 中使用

```json
{
  "scope": [
    {
      "config": "mypool",
      "dataSourceName": "hiworld",
      "basePackages": [
        "your.domain.XxxDBService"
      ]
    }
  ]
}
```

说明：

```text
config = mypool
```

即引用你注册的连接池实现。

### 24.5 设计价值

该扩展机制使 FastQuery 不依赖固定连接池生态，能够接入：

- 企业内部自研连接池
- 云厂商专用数据源
- 特殊代理连接池
- 新兴 JDBC Pool 方案

具备较好的开放性。

## 25. 内置 CRUD、统计查询与批量更新

如果你的 Repository 接口继承的是 `QueryRepository`，FastQuery 提供了一组内置数据访问能力。
对于许多常见场景（保存、更新、统计、存在性判断、分页等），可以**无需手写 SQL**，直接调用接口方法完成。

### 25.1 为什么有这一章

并不是所有操作都值得写：

```java
@Query("insert ...")
@Query("update ...")
@Query("select count(*) ...")
```

对于大量重复、固定模式的数据库操作，框架直接内置实现更高效。

因此 FastQuery 提供：

- 保存实体
- 更新实体
- 保存或更新
- 按主键查询
- 按主键删除
- 条件统计
- 是否存在
- 条件查询单条记录
- 批量更新
- 条件分页查询
- 函数式事务

### 25.2 保存数据（Insert）

#### 保存并返回实体

```java
UserInfo saved = userInfoDBService.save(entity);
```

行为：

1. 执行插入
2. 获取主键
3. 回查数据库
4. 返回完整实体对象

适用于：

- 自增主键场景
- 插入后希望立即获得数据库最终值

#### 仅返回主键

```java
BigInteger id = userInfoDBService.saveToId(entity);
```

适用于：

- 只关心主键值
- 高性能插入场景

#### 批量保存

```java
int effect = userInfoDBService.save(false, list);
```

参数说明：

```java
save(ignoreRepeat, entities)
```

- `ignoreRepeat=true`：忽略唯一键冲突记录
- `ignoreRepeat=false`：按正常规则执行

### 25.3 更新数据（Update）

#### 按主键更新实体

```java
UserInfo updated = userInfoDBService.update(entity);
```

行为：

1. 根据实体主键执行更新
2. 更新成功后回查
3. 返回最新实体

说明：

- 默认 `null` 字段通常不参与更新（动态更新风格）
- 若影响行数不是 1，可能抛异常

#### 仅执行更新并返回影响行数

```java
int effect = userInfoDBService.executeUpdate(entity);
```

适用于：

- 不需要回查实体
- 更关注执行效率

#### 保存或更新（UPSERT 风格）

```java
UserInfo result = userInfoDBService.saveOrUpdate(entity);
```

逻辑：

- 存在主键记录 → 更新
- 不存在 → 插入

前提：

- 实体包含主键字段

### 25.4 查询数据（Find）

#### 按主键查询

```java
UserInfo user = userInfoDBService.find(UserInfo.class, 1001);
```

#### 指定包含字段 / 排除字段

```java
UserInfo user = userInfoDBService.find(UserInfo.class, 1001, true, "name", "age");
```

参数：

```text
contain = true   → 仅查询这些字段
contain = false  → 排除这些字段
```

适用于大字段裁剪场景。

### 25.5 删除数据（Delete）

#### 按主键删除

```java
int effect = userInfoDBService.delete("userinfo", "id", 1001);
```

参数：

- 表名
- 主键字段名
- 主键值

### 25.6 count 与 exists

FastQuery 对这类高频需求做了直接支持。

#### count

```java
@Query("select count(no) from student")
long count();
```

也支持按实体条件统计：

```java
long total = userInfoDBService.count(entity);
```

规则：

- 实体中 `null` 字段不参与条件
- 非 null 字段默认按 `=` + `and` 参与查询

#### exists

```java
@Query("select no from student where no=?1")
boolean exists(String no);
```

也支持实体条件：

```java
boolean yes = userInfoDBService.exists(entity);
```

#### existsEachOn

逐字段检测是否存在，命中即返回字段名：

```java
String field = userInfoDBService.existsEachOn(entity);
```

适用于：

- 唯一字段冲突提示
- 用户名 / 手机号 / 邮箱重复检查

### 25.7 条件查询一条记录

```java
UserInfo one = userInfoDBService.findOne(entity, false);
```

规则：

- 实体中非 null 字段作为查询条件
- 条件关系默认 `and`

例如：

```java
UserInfo q = new UserInfo();
q.setName("Tom");
q.setAge(18);
```

等价逻辑：

```sql
where name = 'Tom' and age = 18
```

### 25.8 批量更新（高亮能力）

这是 FastQuery 很有价值的一项能力。你可以提交多条不同内容的数据，框架自动生成：

```sql
case when
```

批量更新 SQL。

示例：

```java
List<UserInfo> list = new ArrayList<>();

list.add(new UserInfo(77,"Alice",18));
list.add(new UserInfo(88,"Bob",null));
list.add(new UserInfo(99,null,16));

int effect = userInfoDBService.update(list);
```

可能生成：

```sql
update UserInfo set
name = case id
  when 77 then 'Alice'
  when 88 then 'Bob'
  else name end,

age = case id
  when 77 then 18
  when 99 then 16
  else age end

where id in (77,88,99)
```

价值：相比逐条 update：N 次 SQL 往返，该方案可优化为：1 次批量 SQL。

适用于：

- 批量改状态
- 导入修复数据
- 多行不同值更新

### 25.9 条件分页查询（零 SQL）

```java
Page<UserInfo> page =
    userInfoDBService.findPageByPredicate(entity, false, 1, 20, false);
```

说明：

- 实体非 null 字段作为条件
- 自动分页
- 自动统计（可关闭）
- 返回 `Page<T>`

适用于后台快速查询页。

### 25.10 内置事务函数

```java
long effect = userInfoDBService.tx(() -> {
    // 多个写操作
    return 3L;
});
```

说明：

- Lambda 内操作处于同一事务
- 成功提交
- 异常回滚

## 26. 注解总览

FastQuery 采用 **注解驱动（Annotation-Driven）** 的设计方式。开发者通过在接口、方法、参数或实体类上标注注解，即可完成查询、更新、分页、事务、动态 SQL、实体映射等功能。

本章用于快速总览 FastQuery 的全部核心注解，帮助建立整体认知。

### 26.1 按功能分类

| 分类     | 注解             | 说明                     |
| -------- | ---------------- | ------------------------ |
| 查询     | `@Query`         | 在注解中直接编写 SQL     |
| 查询     | `@QueryByNamed`  | 引用外部模板文件中的 SQL |
| 写操作   | `@Modifying`     | 标识插入、更新、删除操作 |
| 参数绑定 | `@Param`         | 指定命名参数             |
| 动态 SQL | `@Condition`     | 动态构建 `WHERE` 条件    |
| 动态 SQL | `@Set`           | 动态构建 `SET` 子句      |
| 实体映射 | `@Table`         | 指定实体对应表名         |
| 实体映射 | `@Id`            | 标识主键字段或主键参数   |
| 实体映射 | `@Transient`     | 标识非持久化字段         |
| 多数据源 | `@Source`        | 动态切换数据源           |
| 分页     | `@NotCount`      | 分页时不统计总记录数     |
| 分页     | `@PageIndex`     | 指定页码参数             |
| 分页     | `@PageSize`      | 指定每页条数参数         |
| 事务     | `@Transactional` | 声明式事务控制           |

### 26.2 查询类注解

#### `@Query`

用于在方法上直接声明 SQL。

```java
@Query("select * from userinfo where id = ?1")
UserInfo findById(Integer id);
```

适合：

- SQL 简短明确
- 常规 CRUD
- 固定查询逻辑

#### `@QueryByNamed`

引用 XML 模板文件中的 SQL。

```java
@QueryByNamed("findUserById")
UserInfo findById(Integer id);
```

适合：

- SQL 很长
- 多条件动态查询
- 模板复用
- 复杂业务逻辑

### 26.3 写操作类注解

#### `@Modifying`

标识当前方法执行的是：

- `INSERT`
- `UPDATE`
- `DELETE`

```java
@Modifying
@Query("delete from userinfo where id=?1")
int deleteById(Integer id);
```

### 26.4 参数绑定类注解

#### `@Param`

为参数命名，供 `:name` 使用。

```java
@Query("select * from userinfo where name=:name")
UserInfo find(@Param("name") String name);
```

### 26.5 动态 SQL 类注解

#### `@Condition`

动态拼接 `WHERE` 条件。

```java
@Condition("and age > ?1")
```

适合搜索页、多条件筛选。

#### `@Set`

动态拼接 `UPDATE ... SET ...`

```java
@Set("name = ?1")
```

适合部分字段更新。

### 26.6 实体映射类注解

#### `@Table`

指定实体映射的表名。

```java
@Table("userinfo")
public class UserInfo {
}
```

#### `@Id`

标识主键。

可用于：实体字段

```java
@Id
private Integer id;
```

方法参数（插入回查）

```java
save(@Id String no, String name);
```

#### `@Transient`

标识该字段不参与数据库映射。

```java
@Transient
private String tempValue;
```

适合：

- 临时字段
- 计算字段
- 页面展示字段

### 26.7 多数据源类注解

#### `@Source`

动态指定当前方法使用的数据源。

```java
findById(1001, @Source String dsName);
```

适合：

- 多租户系统
- 主从切换
- 分库分表

### 26.8 分页类注解

#### `@NotCount`

分页时跳过 `count(*)`

```java
@NotCount
Page<UserInfo> findAll(Pageable pageable);
```

适合：

- 大表分页
- 无限滚动
- 性能优先场景

#### `@PageIndex`

指定页码参数（从 1 开始）

```java
@PageIndex int page
```

#### `@PageSize`

指定每页条数参数

```java
@PageSize int size
```

⚠️ 建议优先使用 `Pageable`

### 26.9 事务类注解

#### `@Transactional`

声明当前方法在事务中执行。

```java
@Transactional
@Modifying
@Query(...)
@Query(...)
int updateBatch(...);
```

特性：

- 全部成功提交
- 任一失败回滚

## 27. 应用于 Spring 环境

FastQuery 可以与 Spring Framework / Spring Boot 无缝集成。
只需将 Repository 接口所在包纳入 Spring 扫描范围，即可通过依赖注入直接使用 FastQuery 自动生成的实例对象。

### 27.1 配置扫描范围

确保 Spring 能扫描到 FastQuery 的 DB 接口或相关组件。

XML 配置方式：

```xml
<context:component-scan base-package="org.fastquery.service" />
```

Java Config 方式：

```java
@ComponentScan("org.fastquery.service")
```

### 27.2 注入 Repository 实例

在业务类中，可直接注入 FastQuery 生成的 Repository 实例。

使用 `@Resource`

```java
@javax.annotation.Resource
private UserInfoDB userInfoDB;
```

使用 `@Autowired`

```java
@Autowired
private UserInfoDB userInfoDB;
```

### 27.3 使用示例

```java
@Service
public class UserService {

    @Resource
    private UserInfoDB userInfoDB;

    public UserInfo findById(Integer id) {
        return userInfoDB.findById(id);
    }
}
```

### 27.4 说明

Spring 注入的对象并不是你手写的实现类，而是 FastQuery 在初始化阶段自动生成并注册到容器中的代理实例。

这意味着：

- 无需手写 DAO 实现类
- 可直接面向接口编程
- 与 Spring IoC 生命周期统一管理
- 易于测试与维护

## 28. 测试 FastQuery

FastQuery 提供专用测试支持，可帮助开发者高效验证 Repository 方法的执行结果与 SQL 生成行为。

核心能力包括：

- 在运行时获取最终执行的 SQL 与参数值
- 验证动态 SQL 是否符合预期
- 测试结束后自动回滚事务
- 避免测试数据污染数据库
- 对分页、条件拼接、批量更新等行为做精确断言

### 28.1 为什么需要专用测试支持

对于 SQL 驱动框架，仅验证返回结果通常不够。

例如：

```text
查询结果正确，但 SQL 性能很差
结果正常，但条件拼接错误
分页可用，但 count SQL 多执行了一次
```

因此，FastQuery 测试能力不仅测试“结果”，还测试：SQL 是否正确生成、是否正确执行、是否符合预期。

### 28.2 `FastQueryTestRule`

`FastQueryTestRule` 它基于 JUnit 的 `TestRule` 扩展机制，用于增强单元测试能力。

作用：

- 捕获执行 SQL
- 捕获绑定参数
- 记录实际执行语句
- 配合事务回滚测试

### 28.3 基本使用方式

```java
@org.junit.Rule
public FastQueryTestRule rule = new FastQueryTestRule();
```

Repository 获取方式：

```java
private StudentDBService studentDBService =
        FQuery.getRepository(StudentDBService.class);
```

### 28.4 自动回滚测试事务

默认：`@Rollback(true)` 可让测试方法执行完成后自动回滚事务。这样既能验证写操作，又不会污染数据库数据。

### 28.5 完整示例

```java
@Rule
public FastQueryTestRule rule = new FastQueryTestRule();

private StudentDBService studentDBService =
        FQuery.getRepository(StudentDBService.class);

@Rollback(true)
@Test
public void update() {

    String no = "9512101";
    String name = "清风习习";
    int age = 17;

    int effect = studentDBService.update(no, name, age);

    assertThat(effect, is(1));

    List<SQLValue> sqlValues = rule.getListSQLValue();

    assertThat(sqlValues.size(), is(1));

    SQLValue sqlValue = sqlValues.get(0);

assertThat(
        sqlValue.getSql(),
        equalTo(
          "update student s set s.age=?,s.name=? where s.no=?"
        ));
}
```

### 28.6 获取生成 SQL 与参数

获取 SQL 记录

```java
List<SQLValue> list = rule.getListSQLValue();
```

每个 `SQLValue` 包含：

- SQL 文本
- 参数列表

获取参数值：`sqlValue.getValues();`

适合断言：

- 参数顺序是否正确
- 参数类型是否正确
- 参数值是否正确绑定

### 28.7 获取实际执行 SQL

需要区分两个概念：已绑定 SQL，表示框架生成的 SQL。已执行 SQL，表示真正发送到数据库执行的 SQL。

获取方式：`rule.getExecutedSQLs();`

### 28.8 分页优化测试案例

分页查询并不一定总会执行 `count(*)`。

例如：

- 当前页已有数据 → 可能执行 count
- 当前页无数据 → 可跳过 count
- 当前页行数不足 →  是最后一页，没必要检查是否有一下页
- 检查是否有下一页  →  只查一条数据就够了

示例

```java
db.findPage(...);
List<String> sqls = rule.getExecutedSQLs();
```

断言：

```java
assertThat(sqls.size(), is(3));
```

说明执行了：

1. 查询当前页数据
2. 查询总数
3. 检测是否有下一页

若当前页无数据：

```java
assertThat(sqls.size(), is(1));
```

说明框架自动跳过 count 查询。这类测试非常有价值，因为它验证的是：框架是否真正做了性能优化。
