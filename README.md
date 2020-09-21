## mybatis-extension
mybatis扩展库，纯mybatis原生支持，可用于辅助mybatis-plus、tk-mybatis或者mybatis-generator使用。

作者：RexSheng，版权所有，违者必究。博客：https://www.cnblogs.com/RexSheng/

环境依赖:
* mybatis>=3.5.2
* jdk>=1.8
* 无其他依赖包及要求

#### features
1. 支持多表自定义join关联查询<font size="1">（since1.0.0）</font>
2. 支持自定义AND/OR混合条件<font size="1">（since1.0.0）</font>
3. 支持GROUPBY/HAVING聚合查询<font size="1">（since1.0.0）</font>
4. 支持自定义sql查询<font size="1">（since1.0.0）</font>
5. 内置多种mybatis generator常用插件，例如批量新增、分页等<font size="1">（since1.0.0）</font>

#### 使用说明（springboot示例)
- 1. 配置mybatis的mapper依赖包：在启动类或者配置类上加入注解

``` java
@MapperScan(basePackages = {"com.github.rexsheng.mybatis.mapper"})
```
- 2. 配置mybatis拦截器

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
- 3. 开始使用，可直接注入DynamicMapper使用

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
		userQuery.or().like(TUser::getFirstName, "%管理员%").like(TUser::getLastName, "%管理员%");
		//执行查询，返回新的结构
		List<UserRoleQueryDto> userList=dao.selectByBuilder(userQuery.build(UserRoleQueryDto.class));
		log.info("用户角色列表:{}",userList);
	}
}
```

##### v<font size="3">1.0.0</font>  date: <font size="3">2020/09/01</font>
1. 第一版发布，提供springboot示例程序下载