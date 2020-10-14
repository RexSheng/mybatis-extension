package com.github.rexsheng.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * 
 * @author https://github.com/RexSheng ©2019 2019年11月8日 下午2:55:14
 */
public class TableColumnNamePlugin extends PluginAdapter {

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.github.rexsheng.mybatis.annotation.TableName"));
		StringBuilder sb=new StringBuilder();
		if(StringUtils.hasValue(introspectedTable.getTableConfiguration().getCatalog())) {
			sb.append("catalog=\"");
			sb.append(introspectedTable.getTableConfiguration().getCatalog());
			sb.append("\",");
		}
		if(StringUtils.hasValue(introspectedTable.getTableConfiguration().getSchema())) {
			sb.append("schema=\"");
			sb.append(introspectedTable.getTableConfiguration().getSchema());
			sb.append("\",");
		}
		if(sb.length()>0) {
			sb.append("table=\"");
			sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
			sb.append("\"");
		}
		else {
			sb.append("\"");
			sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
			sb.append("\"");
		}
		topLevelClass.addAnnotation("@TableName("+sb.toString()+")");
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.github.rexsheng.mybatis.annotation.ColumnName"));
		String columnName=MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
		field.addAnnotation("@ColumnName(\""+columnName+"\")");
		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	@Override
	public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.clientGenerated(interfaze, introspectedTable);
	}
	
	/**
	 * 
	 * This plugin is always valid -no properties are required
	 * 
	 */
	public boolean validate(List<String> warnings) {
		return true;
	}
}
