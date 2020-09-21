package com.github.rexsheng.mybatis.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * @author https://github.com/RexSheng ©2019
 * @author RexSheng
 * 2019年11月11日 上午11:08:17
 */
public class OrderByPlugin extends PluginAdapter {
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(properties.get("type")==null || (properties.get("type")!=null && String.valueOf(properties.get("type")).equalsIgnoreCase("string"))) {
			addOrderBy(topLevelClass, introspectedTable);
		}
		else if(properties.get("type")!=null && String.valueOf(properties.get("type")).equalsIgnoreCase("lambda")) {
			addOrderByLambda(topLevelClass, introspectedTable);
		}
		else if(properties.get("type")!=null && String.valueOf(properties.get("type")).equalsIgnoreCase("all")) {
			addOrderBy(topLevelClass, introspectedTable);
			addOrderByLambda(topLevelClass, introspectedTable);
		}
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}
	
	private void addOrderBy(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		introspectedTable.getAllColumns().forEach(column->{
			String fieldName=column.getJavaProperty();
			Method ascMethod = new Method("setOrderBy" + fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1)+"Asc");
			ascMethod.setVisibility(JavaVisibility.PUBLIC);
			ascMethod.setReturnType(topLevelClass.getType());
			ascMethod.addBodyLine("this.orderByClause=this.orderByClause==null?\"" + column.getActualColumnName() + "\":(this.orderByClause+\"," +column.getActualColumnName()+ "\");");
			ascMethod.addBodyLine("return this;");
			topLevelClass.addMethod(ascMethod);
			
			Method descMethod = new Method("setOrderBy" + fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1)+"Desc");
			descMethod.setVisibility(JavaVisibility.PUBLIC);
			descMethod.setReturnType(topLevelClass.getType());
			descMethod.addBodyLine("this.orderByClause=this.orderByClause==null?\"" + column.getActualColumnName() + " desc\":(this.orderByClause+\"," +column.getActualColumnName()  + " desc\");");
			descMethod.addBodyLine("return this;");
			topLevelClass.addMethod(descMethod);
		});
	}
	
	private void addOrderByLambda(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Map<String,String> columnMapping=new HashMap<>();
		introspectedTable.getAllColumns().forEach(column->{
			String fieldName=column.getJavaProperty();
			String columnName=column.getActualColumnName();
			columnMapping.put(fieldName, columnName);
		});
		
		Method method = new Method("setOrderBy");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType param1=new FullyQualifiedJavaType("com.sanlly.common.models.input.EntityFunction");
		
		if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            param1.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType()));
        } else {
        	param1.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        }
		
		param1.addTypeArgument(FullyQualifiedJavaType.getObjectInstance());
		method.addParameter(new Parameter(param1,"fn"));
		method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(),"asc"));
		method.setReturnType(topLevelClass.getType());
		
		method.addBodyLine("String columnName=asc?columnMapping.get(EntityFunctionUtil.fnToFieldName(fn)):(columnMapping.get(EntityFunctionUtil.fnToFieldName(fn))+\" desc\");");
		method.addBodyLine("this.orderByClause=this.orderByClause==null?columnName:(this.orderByClause+\",\"+columnName);");
		method.addBodyLine("return this;");
		topLevelClass.addMethod(method);
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.sanlly.common.utils.EntityFunctionUtil"));
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.sanlly.common.models.input.EntityFunction"));
		
		FullyQualifiedJavaType mapType=FullyQualifiedJavaType.getNewMapInstance();
		mapType.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		mapType.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		Field field = new Field("columnMapping",mapType);
		field.setVisibility(JavaVisibility.PRIVATE);
		
		field.setStatic(true);
		String initValue="new HashMap<String,String>(){private static final long serialVersionUID = 1L;{";
		for(String key:columnMapping.keySet()) {
			initValue+="put(\""+key+"\",\""+columnMapping.get(key)+"\");";
		}
		initValue+="}}";
		field.setInitializationString(initValue);
		topLevelClass.addImportedType(FullyQualifiedJavaType.getNewMapInstance());
		topLevelClass.addImportedType(FullyQualifiedJavaType.getNewHashMapInstance());
		topLevelClass.addField(field);
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
