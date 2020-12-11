package com.github.rexsheng.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * 
 * @author https://github.com/RexSheng ©2019 2019年11月8日 下午2:55:14
 */
public class TableColumnNamePlugin extends PluginAdapter {

	/**
	 * ALL,TABLE,COLUMN
	 */
	private String mode="ALL";
	
	/**
	 * ALL,NONE,FIELD,METHOD
	 */
	private String remarkType="ALL";
	
	
	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		super.initialized(introspectedTable);
		if(properties.containsKey("type")) {
			mode=properties.getProperty("type");
		}
		if(properties.containsKey("remark")) {
			remarkType=properties.getProperty("remark");
		}
	}
	
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(mode.equalsIgnoreCase("ALL") || mode.equalsIgnoreCase("TABLE")) {
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
				if(!remarkType.equalsIgnoreCase("NONE")) {
					String remarks = introspectedTable.getRemarks();
			        if (StringUtility.stringHasValue(remarks)) {
			        	sb.append(",");
						sb.append("desc=\"");
						sb.append(remarks);
						sb.append("\"");
			        }
				}
			}
			else {
				if(!remarkType.equalsIgnoreCase("NONE")) {
					String remarks = introspectedTable.getRemarks();
			        if (StringUtility.stringHasValue(remarks)) {
			        	sb.append("table=\"");
						sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
						sb.append("\"");
						
			        	sb.append(",");
						sb.append("desc=\"");
						sb.append(remarks);
						sb.append("\"");
			        }
			        else {
			        	sb.append("\"");
						sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
						sb.append("\"");
			        }
				}
				else {
					sb.append("\"");
					sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
					sb.append("\"");
				}
			}
			topLevelClass.addAnnotation("@TableName("+sb.toString()+")");
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		Boolean isPrimayKey=false;
		for(IntrospectedColumn col:introspectedTable.getPrimaryKeyColumns()) {
			if(col.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
				isPrimayKey=true;
				break;
			}
		}
		
		if(mode.equalsIgnoreCase("ALL") || mode.equalsIgnoreCase("COLUMN")) {
			topLevelClass.addImportedType(new FullyQualifiedJavaType("com.github.rexsheng.mybatis.annotation.ColumnName"));
			String columnName=MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
			StringBuilder sb=new StringBuilder();
			if(isPrimayKey) {
				topLevelClass.addImportedType(new FullyQualifiedJavaType("com.github.rexsheng.mybatis.core.ColumnType"));
				sb.append("type=ColumnType.PK");
			}
			if(remarkType.equalsIgnoreCase("ALL") || remarkType.equalsIgnoreCase("FIELD")) {
				String remarks = introspectedColumn.getRemarks();
		        if (StringUtility.stringHasValue(remarks)) {
		        	if(sb.length()>0) {
		        		sb.append(",");
					}
		        	sb.append("desc=\"");
		        	sb.append(remarks);
		        	sb.append("\"");
		        }
			}
			if(sb.length()>0) {
	    		sb.insert(0, ",");
	    		sb.insert(0, "\"");
	    		sb.insert(0, columnName);
	    		sb.insert(0, "value=\"");
	    		field.addAnnotation("@ColumnName("+sb.toString()+")");
			}
			else {
				field.addAnnotation("@ColumnName(\""+columnName+"\")");
			}			
		}
		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}
	
	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		if(remarkType.equalsIgnoreCase("ALL") || remarkType.equalsIgnoreCase("METHOD")) {
			method.addJavaDocLine("/**"); //$NON-NLS-1$
	        
	        String remarks = introspectedColumn.getRemarks();
	        if (StringUtility.stringHasValue(remarks)) {
	            String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	            for (String remarkLine : remarkLines) {
	            	method.addJavaDocLine(" * " + remarkLine); //$NON-NLS-1$
	            }
	        }
	        StringBuilder sb = new StringBuilder();
	        sb.append(" * @return "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        sb.append('.');
	        sb.append(introspectedColumn.getActualColumnName());
	        method.addJavaDocLine(sb.toString());

	        method.addJavaDocLine(" */"); //$NON-NLS-1$
		}
		return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}
	
	@Override
	public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		if(remarkType.equalsIgnoreCase("ALL") || remarkType.equalsIgnoreCase("METHOD")) {
			method.addJavaDocLine("/**"); //$NON-NLS-1$

	        String remarks = introspectedColumn.getRemarks();
	        if (StringUtility.stringHasValue(remarks)) {
	            String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	            for (String remarkLine : remarkLines) {
	            	method.addJavaDocLine(" * " + remarkLine); //$NON-NLS-1$
	            }
	        }

	        Parameter parm = method.getParameters().get(0);
	        StringBuilder sb = new StringBuilder();
	        sb.append(" * @param "); //$NON-NLS-1$
	        sb.append(parm.getName());
	        sb.append(" "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        sb.append('.');
	        sb.append(introspectedColumn.getActualColumnName());
	        method.addJavaDocLine(sb.toString());

	        method.addJavaDocLine(" */"); //$NON-NLS-1$
		}
		return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
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
