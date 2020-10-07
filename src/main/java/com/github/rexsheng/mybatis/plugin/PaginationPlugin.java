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
		addLimit(topLevelClass, introspectedTable, "pageIndex");
		addLimit(topLevelClass, introspectedTable, "pageSize");
		addPage(topLevelClass, introspectedTable);
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
		XmlElement pageElement = new XmlElement("if");//$NON-NLS-1$
		pageElement.addAttribute(new Attribute("test", "pageSize != null and pageSize > 0"));//$NON-NLS-1$ //$NON-NLS-2$
		pageElement.addElement(new TextElement("limit #{startIndex},#{pageSize}"));		
		element.addElement(pageElement);
		
		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}
	
	@Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(
        XmlElement element, IntrospectedTable introspectedTable) {
		XmlElement pageElement = new XmlElement("if");//$NON-NLS-1$
		pageElement.addAttribute(new Attribute("test", "pageSize != null and pageSize > 0"));//$NON-NLS-1$ //$NON-NLS-2$
		pageElement.addElement(new TextElement("limit #{startIndex},#{pageSize}"));		
		element.addElement(pageElement);
		
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
	 *  设置page
	 * @param topLevelClass 类
	 * @param introspectedTable table
	 */
	private void addPage(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String pageType="com.github.rexsheng.mybatis.core.IPageInput";
		topLevelClass.addImportedType(new FullyQualifiedJavaType(pageType));
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Method method = new Method("setPage");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(new FullyQualifiedJavaType(pageType), "page"));
		method.addBodyLine("this.pageIndex=page.getPageIndex();");
		method.addBodyLine("this.pageSize=page.getPageSize();");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		
		Method method2 = new Method("setPage");
		method2.setVisibility(JavaVisibility.PUBLIC);
		method2.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "pageIndex"));
		method2.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "pageSize"));
		method2.addBodyLine("this.pageIndex=pageIndex;");
		method2.addBodyLine("this.pageSize=pageSize;");
		commentGenerator.addGeneralMethodComment(method2, introspectedTable);
		topLevelClass.addMethod(method2);
		
		Method startMethod = new Method("getStartIndex");
		startMethod.setVisibility(JavaVisibility.PUBLIC);
		startMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
		startMethod.addBodyLine("return this.pageIndex>0?(this.pageIndex-1)*this.pageSize:0;");
		commentGenerator.addGeneralMethodComment(startMethod, introspectedTable);
		topLevelClass.addMethod(startMethod);
	}

	/**
	 * 
	 * This plugin is always valid -no properties are required
	 * 
	 */
	public boolean validate(List<String> warnings) {
		return true;
	}


	public String getPageInputClassName() {
		return pageInputClassName;
	}
}
