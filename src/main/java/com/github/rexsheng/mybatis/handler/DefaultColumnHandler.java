package com.github.rexsheng.mybatis.handler;

import com.github.rexsheng.mybatis.annotation.ColumnName;
import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.core.ColumnType;
import com.github.rexsheng.mybatis.core.SqlReservedWords;
import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.2.0
 */
public class DefaultColumnHandler implements IColumnHandler{

	@Override
	public String getName(ColumnQueryBuilder<?> columnBuilder,BuilderConfiguration configuration) {
		String col=null;
		if(columnBuilder.getField()!=null) {
			ColumnName columnName=columnBuilder.getField().getAnnotation(ColumnName.class);
			if(columnName!=null) {
				col=columnName.value();
			}
			else {
				col=StringUtils.camelCaseToUnderLine(columnBuilder.getFieldName());
			}
		}
		else {
			col=StringUtils.camelCaseToUnderLine(columnBuilder.getFieldName());
		}
		if(SqlReservedWords.containsWord(col)) {
			return configuration.getDatabaseDialect().getProperty().getBeginDelimiter()+col+configuration.getDatabaseDialect().getProperty().getEndDelimiter();
		}
		else {
			return col;
		}
	}
	
	@Override
	public Boolean isPrimaryKey(ColumnQueryBuilder<?> columnBuilder, BuilderConfiguration configuration) {
		if(columnBuilder.getField()!=null) {
			ColumnName columnName=columnBuilder.getField().getAnnotation(ColumnName.class);
			if(columnName!=null) {
				return ColumnType.PK.equals(columnName.type());
			}
		}
		return false;
	}
}
