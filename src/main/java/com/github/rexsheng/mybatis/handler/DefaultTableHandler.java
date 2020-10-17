package com.github.rexsheng.mybatis.handler;

import com.github.rexsheng.mybatis.annotation.TableName;
import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.2.0
 */
public class DefaultTableHandler implements ITableHandler{

	@Override
	public String getName(Class<?> clazz,BuilderConfiguration configuration) {
		TableName tableName=clazz.getAnnotation(TableName.class);
		if(tableName!=null) {
			return composeFullyQualifiedTableName(tableName.catalog(),tableName.schema(),tableName.table(),tableName.value(),'.');
		}
		else {
			return StringUtils.capitalToUnderLine(clazz.getSimpleName());
		}
	}
	
	public String composeFullyQualifiedTableName(String catalog,
            String schema, String tableName, String bakTableName, char separator) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.hasValue(catalog)) {
            sb.append(catalog);
            sb.append(separator);
        }

        if (StringUtils.hasValue(schema)) {
            sb.append(schema);
            sb.append(separator);
        } else {
            if (sb.length() > 0) {
                sb.append(separator);
            }
        }

        if (StringUtils.hasValue(tableName)) {
            sb.append(tableName);
        } 
        else if (StringUtils.hasValue(bakTableName)) {
        	sb.append(bakTableName);
        }
        else {
        	throw new RuntimeException("必须指定表名");//$NON-NLS-1$
        }

        return sb.toString();
    }
}
