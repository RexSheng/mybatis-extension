package com.github.rexsheng.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class PaginationPlugin extends PluginAdapter {
	
	private String pageInputClassName="com.github.rexsheng.mybatis.core.PageInput";
	
	
	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		super.initialized(introspectedTable);
		if(properties.containsKey("inputClass")) {
			pageInputClassName=properties.getProperty("inputClass");
		}
	}
	
	
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// addfield, getter, setter for limit clause
		addLimit(topLevelClass, introspectedTable, "limitStart");
		addLimit(topLevelClass, introspectedTable, "limitEnd");
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}
	
	@Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
//		String tableName=introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
//		if(tableName.equals("t_user")){
//			topLevelClass.addImportedType("java.util.ArrayList");
//			topLevelClass.setSuperClass("ArrayList");
//		}
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		// LIMIT5,10; // 检索记录行 6-15
		XmlElement isNotNullElement = new XmlElement("if");//$NON-NLS-1$
		isNotNullElement.addAttribute(new Attribute("test", "limitStart != null and limitStart >=0"));//$NON-NLS-1$ //$NON-NLS-2$
		isNotNullElement.addElement(new TextElement("limit ${limitStart} , ${limitEnd}"));
		element.addElement(isNotNullElement);
		// LIMIT 5;//检索前 5个记录行
		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}
	
	@Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(
        XmlElement element, IntrospectedTable introspectedTable) {
		// LIMIT5,10; // 检索记录行 6-15
		XmlElement isNotNullElement = new XmlElement("if");//$NON-NLS-1$
		isNotNullElement.addAttribute(new Attribute("test", "limitStart != null and limitStart >=0"));//$NON-NLS-1$ //$NON-NLS-2$
		isNotNullElement.addElement(new TextElement("limit ${limitStart} , ${limitEnd}"));
		element.addElement(isNotNullElement);
		// LIMIT 5;//检索前 5个记录行
		return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }
	
	

	private void addLimit(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field(name,FullyQualifiedJavaType.getIntInstance());
		field.setVisibility(JavaVisibility.PROTECTED);
		field.setInitializationString("-1");
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
		char c = name.charAt(0);
		String camel = Character.toUpperCase(c) + name.substring(1);
		Method method = new Method("set" + camel);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), name));
		method.addBodyLine("this." + name + "=" + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		method = new Method("get" + camel);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.addBodyLine("return " + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
	}
	
	/**
	 * 暂时不用此方法
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param name
	 */
	@SuppressWarnings("unused")
	private void addPage(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
		topLevelClass.addImportedType(new FullyQualifiedJavaType(pageInputClassName));
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field(name,new FullyQualifiedJavaType(pageInputClassName));
		field.setVisibility(JavaVisibility.PROTECTED);
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
		char c = name.charAt(0);
		String camel = Character.toUpperCase(c) + name.substring(1);
		Method method = new Method("set" + camel);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(new FullyQualifiedJavaType(pageInputClassName), name));
		method.addBodyLine("this." + name + "=" + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		method = new Method("get" + camel);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.addBodyLine("return " + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
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
