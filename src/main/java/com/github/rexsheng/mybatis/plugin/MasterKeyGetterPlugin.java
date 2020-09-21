package com.github.rexsheng.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * https://github.com/RexSheng ©2019
 * @author RexSheng
 * 2019年11月8日 下午2:32:41
 */
public class MasterKeyGetterPlugin extends PluginAdapter {
	
	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		setMaster(element,introspectedTable);
		return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
	}
	
	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		setMaster(element,introspectedTable);
		return super.sqlMapInsertElementGenerated(element, introspectedTable);
	}
	
	
	
	private void setMaster(XmlElement element, IntrospectedTable introspectedTable) {
		if(introspectedTable.getGeneratedKey()!=null && introspectedTable.getGeneratedKey().isIdentity()) {
			for(VisitableElement childrenElement:element.getElements()) {
				if(XmlElement.class.isAssignableFrom(childrenElement.getClass())) {
					XmlElement childXml=(XmlElement)childrenElement;
					if(childXml.getName().equalsIgnoreCase("selectKey")) {
						 if(childXml.getElements().size()>0) {
							 TextElement text=(TextElement)childXml.getElements().get(0);
							 if(text.getContent().toUpperCase().startsWith("SELECT ")) {
								 String sql="/*FORCE_MASTER*/ "+text.getContent();
								 childXml.getElements().set(0, new TextElement(sql));
							 }
						 }
					}
				}
				
			}
		}
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
