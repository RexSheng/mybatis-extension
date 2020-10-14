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
6. 支持批量插入<font size="1">（since1.0.1）</font>

#### 使用说明（springboot示例)
- 1. pom.xml中添加maven依赖包

``` java
<!-- https://mvnrepository.com/artifact/com.github.rexsheng/mybatis-extension -->
<dependency>
    <groupId>com.github.rexsheng</groupId>
    <artifactId>mybatis-extension</artifactId>
    <version>1.1.2</version>
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
		return new ResultTypeInterceptor();
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
		TableQueryBuilder<TUser> userQuery=TableQueryBuilder.from(TUser.class);
		//定义要查询的从表的构建器
		TableQueryBuilder<UserRole> userRoleQuery=TableQueryBuilder.from(UserRole.class);
		//定义要查询主表的所有字段，并且使用主表左关联从表，指定关联条件
		userQuery.selectAll().leftJoin(userRoleQuery).on(TUser::getUserId, UserRole::getUserId);
		//定义where中的条件
		userQuery.where().like(TUser::getFirstName, "%管理员%").like(TUser::getLastName, "%管理员%");
		//执行查询，定义新的返回类
		List<UserRoleQueryDto> userList=dao.selectByBuilder(userQuery.build(UserRoleQueryDto.class));
		log.info("用户角色列表:{}",userList);
	}
}
```
##### v<font size="3">1.1.2</font>  date: <font size="3">2020/10/17</font>
1. 新增：DynamicMapper批量更新方法updateByBuilder，批量删除方法deleteByBuilder，查询总条数方法countBySql、countBySqlWithParams
2. 新增：tablename注解新增catalog,schema配置，TableColumnNamePlugin插件更新
3. 新增：selectByBuilder支持distinct
4. 新增：where条件notIn,notLike
5. 新增：BuilderConfiguration配置maxInLength，自动拆分列表值为or连接的条件，用于解决oracle中in最多1000个值的问题
6. 优化：BuilderConfiguration中默认beginDelimiter和endDelimiter为空白字符串

##### v<font size="3">1.1.1</font>  date: <font size="3">2020/10/08</font>
1. 新增：DynamicMapper接口sql传参方法selectBySqlWithParams，selectByMapWithParams。
   sql里不必再手动拼接参数，写法与xml语法保持一致，支持参数直接传入list自动拆解
2. 优化：删除调试日志

##### v<font size="3">1.1.0</font>  date: <font size="3">2020/10/05</font>
1. 新增：TableQueryBuilder类新增设置分页方法setPage,新增开启计算总条数方法totalCountEnabled()
2. 新增：mbg分页插件，支持传入page参数，支持只设置pageSize，pageIndex改为非必须
3. 新增：BuilderConfiguration中配置dbType，默认值为"mysql"
4. bug：批量插入方法不使用配置方法获取列名的问题
5. 优化：调整部分注释

##### v<font size="3">1.0.1</font>  date: <font size="3">2020/09/28</font>
1. 新增：DynamicMapper接口批量插入方法batchInsert，支持全局配置，示例程序同步增加测试用例

##### v<font size="3">1.0.0</font>  date: <font size="3">2020/09/01</font>
1. 第一版发布，提供springboot示例程序下载