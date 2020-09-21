package com.github.rexsheng.mybatis.plugin;

import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

/**
 * insertList扩展（mysql）v1.0
 * 
 * @author RexSheng 2019年4月10日上午11:42:13
 */
public class BulkInsertPlugin extends PluginAdapter {

	/**
	 * 
	 * This plugin is always valid -no properties are required
	 * 
	 */
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 添加自定义方法
	 * 
	 * @param method
	 * @param introspectedTable
	 * @return
	 */
	private Method generateBulkListMethod(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		interfaze.addImportedType(FullyQualifiedJavaType.getNewListInstance());
		return generateBulkListMethod(method, introspectedTable);
	}

	@SuppressWarnings("unused")
	private Method generateBulkListMethod(Method method, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
		return generateBulkListMethod(method, introspectedTable);
	}

	private Method generateBulkListMethod(Method method, IntrospectedTable introspectedTable) {

		Method m = new Method("insertList");

		m.setVisibility(method.getVisibility());
		m.setReturnType(FullyQualifiedJavaType.getIntInstance());
		m.setAbstract(true);
		FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
		paramType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());// 泛型类型
		m.addParameter(new Parameter(paramType, "records", "@Param(\"records\")"));

		context.getCommentGenerator().addGeneralMethodComment(m, introspectedTable);
		return m;
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();// 数据库表名

		XmlElement rootElement = document.getRootElement();

		XmlElement insertRootElement = new XmlElement("insert");
		insertRootElement.addAttribute(new Attribute("id", "insertList"));
		insertRootElement.addAttribute(new Attribute("parameterType", "java.util.List"));

		GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
//        	<selectKey keyProperty="prodEdiPoolId" order="AFTER" resultType="java.lang.Integer">
//            /*FORCE_MASTER*/ SELECT LAST_INSERT_ID()
//          </selectKey>
            introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
                // if the column is null, then it's a configuration error. The
                // warning has already been reported
                if (gk.isJdbcStandard()) {
                	insertRootElement.addAttribute(new Attribute(
                            "useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                    insertRootElement.addAttribute(new Attribute(
                            "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                    insertRootElement.addAttribute(new Attribute(
                            "keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
                } else {
                	insertRootElement.addElement(getSelectKey(introspectedColumn, gk));
                }
            });
        }
        
		
		StringBuilder tableColumnText = new StringBuilder();
		tableColumnText.append("insert into " + tableName + " (");
		
		StringBuilder valueText = new StringBuilder();
		valueText.append("(");
		Iterator<IntrospectedColumn> iterator = introspectedTable.getAllColumns().iterator();
		while (iterator.hasNext()) {
			IntrospectedColumn column = iterator.next();
//			MyBatis3FormattingUtilities.getEscapedColumnName(column);
			tableColumnText.append(MyBatis3FormattingUtilities.getEscapedColumnName(column));
			valueText.append(MyBatis3FormattingUtilities.getParameterClause(column, "item."));
			if (iterator.hasNext()) {
				tableColumnText.append(",");
				valueText.append(",");
			}
		}
		valueText.append(")");
		tableColumnText.append(") values ");
		insertRootElement.addElement(new TextElement(tableColumnText.toString()));

		XmlElement foreachElement = new XmlElement("foreach");
		foreachElement.addAttribute(new Attribute("item", "item"));
		foreachElement.addAttribute(new Attribute("index", "index"));
		foreachElement.addAttribute(new Attribute("collection", "records"));
		foreachElement.addAttribute(new Attribute("separator", ","));

		foreachElement.addElement(new TextElement(valueText.toString()));
		insertRootElement.addElement(foreachElement);

		rootElement.addElement(insertRootElement);

		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}
	
	protected XmlElement getSelectKey(IntrospectedColumn introspectedColumn,
            GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn
                .getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultType", identityColumnType)); //$NON-NLS-1$
        answer.addAttribute(new Attribute(
                "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("order", //$NON-NLS-1$
                generatedKey.getMyBatis3Order())); 
        
        answer.addElement(new TextElement(generatedKey
                        .getRuntimeSqlStatement()));

        return answer;
    }

	@Override
	public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		interfaze.addMethod(generateBulkListMethod(method, interfaze, introspectedTable));
		return super.clientInsertMethodGenerated(method, interfaze, introspectedTable);
	}

//	@Override
//	public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass,
//			IntrospectedTable introspectedTable) {
//		topLevelClass.addMethod(generateBulkListMethod(method, topLevelClass, introspectedTable));
//		return super.clientInsertMethodGenerated(method, topLevelClass, introspectedTable);
//	}
}
