package com.github.rexsheng.mybatis.handler;

import com.github.rexsheng.mybatis.annotation.ColumnName;
import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.core.SqlReservedWords;
import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.1.2
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
			return configuration.getBeginDelimiter()+col+configuration.getEndDelimiter();
		}
		else {
			return col;
		}
	}
}
