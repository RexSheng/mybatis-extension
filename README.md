## mybatis-extension
mybatis扩展库，纯mybatis原生支持，可用于辅助mybatis-plus、tk-mybatis或者mybatis-generator使用。

作者：RexSheng，版权所有，违者必究。博客：https://www.cnblogs.com/RexSheng/

环境依赖:
* mybatis>=3.5.2
* jdk>=1.8
* 无其他依赖包及要求

#### features
1. 支持多表自定义join关联查询
2. 支持自定义AND/OR混合条件
3. 支持GROUPBY/HAVING聚合查询
4. 支持自定义sql查询
5. 内置多种mybatis generator常用插件，例如批量新增、分页等
6. 支持批量插入<font size="1">（since1.0.1）</font>，更新，删除<font size="1">（since1.2.0）</font>

#### 使用说明（springboot示例)
- 1. pom.xml中添加maven依赖包

``` java
<!-- https://mvnrepository.com/artifact/com.github.rexsheng/mybatis-extension -->
<dependency>
    <groupId>com.github.rexsheng</groupId>
    <artifactId>mybatis-extension</artifactId>
    <version>1.5.0</version>
</dependency>

```
- 2. 配置mybatis的mapper依赖包：在启动类或者配置类上加入注解

``` java
@MapperScan(basePackages = {"com.github.rexsheng.mybatis.mapper"})
```
- 3. 配置mybatis拦截器

```
import com.github.rexsheng.mybatis.interceptor.ResultTypeInterceptor;

@Configuration
public class InterceptorConfig {
	@Bean
	public ResultTypeInterceptor resultTypeInterceptor() {
		//默认mysql方言
		return new ResultTypeInterceptor();
		//ResultTypeInterceptor interceptor=new ResultTypeInterceptor();
		//interceptor.setConfig(BuilderConfigurationFactory.builder().dialect(new MySqlDialect()).build());
		//return interceptor;
	}
}
```
- 4. 开始使用，可直接注入接口DynamicMapper使用

```

import com.github.rexsheng.mybatis.extension.TableQueryBuilder;
import com.github.rexsheng.mybatis.mapper.DynamicMapper;
import com.github.rexsheng.mybatis.test.dto.UserRoleQueryDto;
import com.github.rexsheng.mybatis.test.entity.TUser;
import com.github.rexsheng.mybatis.test.entity.UserRole;

@SpringBootTest
public class MapperTest {
	@Autowired
	private DynamicMapper dao;
	
	/**
     * 单表简单查询
     */
	@Test
	public void simpleSelect() {
		//定义要查询的表的构建器
		TableQueryBuilder<TUser> userQuery=TableQueryBuilder.from(TUser.class);
		//定义要查询的字段
		userQuery.select(TUser::getUserId,TUser::getUserName).where().like(TUser::getUserName, "%王二小%");
		//执行查询
		List<TUser> userList=dao.selectByBuilder(userQuery.build());
		log.info("用户列表:{}",userList);
	}

	/**
     * 多表简单关联查询
     */
	@Test
	public void simpleJoin() {
		//定义要查询的主表的构建器
		TableQueryBuilder<UserRole> userRoleQuery=TableQueryBuilder.from(UserRole.class);
		//定义要查询的从表的构建器
		TableQueryBuilder<TUser> userQuery=TableQueryBuilder.from(TUser.class);
		//定义where中的条件
		userQuery.selectAll().or().like(TUser::getFirstName, "%管理员%").like(TUser::getLastName, "%管理员%");
		//定义要查询主表的字段，指定关联条件
		userRoleQuery.distinct().select(UserRole::getRoleId).innerJoin(userQuery).on(UserRole::getUserId, TUser::getUserId);		
		//执行查询，定义新的返回类
		List<UserRoleQueryDto> userList=dao.selectByBuilder(userRoleQuery.build(UserRoleQueryDto.class));
		log.info("用户角色列表:{}",userList);
	}
}
```
##### v<font size="3">1.5.1</font>  date: <font size="3">2021/02/21</font>
1. 修复：update多表时可更新多个表字段

##### v<font size="3">1.5.0</font>  date: <font size="3">2020/12/22</font>
1. 新增：totalCountDisabled()方法
2. 新增：@ColumnIgnore注解用于忽略字段查询
3. 修复：oracle分页异常问题

##### v<font size="3">1.4.3</font>  date: <font size="3">2020/12/20</font>
1. 修复：selectAll()时字段乱序问题
2. 修复：字段重载时偶尔无效问题

##### v<font size="3">1.4.2</font>  date: <font size="3">2020/12/20</font>
1. 修复：排除static与transient修饰的字段

##### v<font size="3">1.4.1</font>  date: <font size="3">2020/12/18</font>
1. 修复：TableColumnNamePlugin中BLOB类无注解问题
2. 修复：TableColumnNamePlugin中备注字符串换行问题
3. 优化：selectAll()字段时，按照优先父类字段的顺序获取列名

##### v<font size="3">1.4.0</font>  date: <font size="3">2020/12/11</font>
1. 新增：TableColumnNamePlugin中ColumnName注解 新增字段类型，可指定字段为主键类型
2. 修复：totalCountEnabled(Boolean)参数为true时不继续执行查询的问题
3. 优化：精简selectByBuilder生成的sql，减少不必要的AS关键字
4. 优化：sql中jdbcType增强支持

##### v<font size="3">1.3.1</font>  date: <font size="3">2020/12/06</font>
1. 修复：ResultTypeInterceptor.setConfig(BuilderConfiguration)调用时的日志显示问题
2. 优化：传入sql参数时支持jdbcType形式。例如： 

```java
selectBySqlWithParams("select id from t_user where create_time>#{createTime,jdbcType=TIMESTAMP}",...);
```

##### v<font size="3">1.3.0</font>  date: <font size="3">2020/11/22</font>
1. 新增：IDatabaseDialect数据库方言接口，针对配置不同数据库的分页及特性
2. 新增（变更）： 初始化时使用BuilderConfiguration.setDatabaseDialect(IDatabaseDialect)配置，废弃setDbType(String dbType)、setBeginDelimiter(String beginDelimiter)、setEndDelimiter(String endDelimiter)
3. 新增：totalCountEnabled(Boolean)，当某个查询计算总条数为0时，Boolean参数可自定义配置是否继续执行原有查询，优先级高于全局配置IDatabaseDialect.skipSelectIfCountZero()
4. 优化：totalCountEnabled()生成的sql语句

##### v<font size="3">1.2.2</font>  date: <font size="3">2020/11/15</font>
1. 优化：增强多字段的查询方法selectField,selectExcept
2. 优化：精简生成的查询sql：当返回类型的属性与sql字段相同时，取消语句中的字段AS部分
3. 修复：多表关联 selectCount(*)时异常问题

##### v<font size="3">1.2.1</font>  date: <font size="3">2020/10/20</font>
1. 修复：修改sqlserver查询时强制分页问题
2. 修复：修改查询时select使用as关键字问题
3. 优化：selectByBuilder无select列时抛出异常

##### v<font size="3">1.2.0</font>  date: <font size="3">2020/10/17</font>
1. 新增：DynamicMapper批量更新方法updateByBuilder，批量删除方法deleteByBuilder，查询总条数方法countBySql、countBySqlWithParams
2. 新增：tablename注解新增catalog,schema配置
3. 新增：TableColumnNamePlugin新增catalog,schema配置，支持传入属性：注解位置type=ALL/TABLE/COLUMN,备注类型remark=ALL,NONE,FIELD,METHOD
4. 新增：selectByBuilder支持distinct
5. 新增：where条件notIn,notLike
6. 新增：BuilderConfiguration配置maxInLength，自动拆分列表值为or连接的条件，用于解决oracle中in最多1000个值的问题
7. 新增：mbg 注释实现类ExtensionCommentGenerator
        
        <commentGenerator type="com.github.rexsheng.mybatis.plugin.ExtensionCommentGenerator">
            <property name="suppressDate" value="true" />
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="false" />
        </commentGenerator>
        
8. 优化（变更）：BuilderConfiguration中默认beginDelimiter和endDelimiter为空白字符串
9. 优化：BuilderConfiguration中配置ITableHandler，IColumnHanler来获取数据表及列配置
10. 优化：支持sqlserver2012以上使用offset fetch next分页

##### v<font size="3">1.1.1</font>  date: <font size="3">2020/10/08</font>
1. 新增：DynamicMapper接口sql传参方法selectBySqlWithParams，selectByMapWithParams。
   sql里不必再手动拼接参数，写法与xml语法保持一致，支持参数直接传入list自动拆解
2. 优化：删除调试日志

##### v<font size="3">1.1.0</font>  date: <font size="3">2020/10/05</font>
1. 新增：TableQueryBuilder类新增设置分页方法setPage,新增开启计算总条数方法totalCountEnabled()
2. 新增：mbg分页插件，支持传入page参数，支持只设置pageSize，pageIndex改为非必须
3. 新增：BuilderConfiguration中配置dbType，默认值为"mysql"
4. 修复：批量插入方法不使用配置方法获取列名的问题
5. 优化：调整部分注释

##### v<font size="3">1.0.1</font>  date: <font size="3">2020/09/28</font>
1. 新增：DynamicMapper接口批量插入方法batchInsert，支持全局配置，示例程序同步增加测试用例

##### v<font size="3">1.0.0</font>  date: <font size="3">2020/09/01</font>
1. 第一版发布，提供springboot示例程序下载