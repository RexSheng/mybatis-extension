package com.github.rexsheng.mybatis.plugin;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * @author RexSheng
 * 2020年10月12日 下午11:14:02
 */
public class ExtensionCommentGenerator extends DefaultCommentGenerator{

	 private Properties properties;

	    private boolean suppressDate;

	    private boolean suppressAllComments;

	    /** If suppressAllComments is true, this option is ignored. */
	    private boolean addRemarkComments;

	    private SimpleDateFormat dateFormat;
	    
	    private SimpleDateFormat defaultDateFormat;

	    public ExtensionCommentGenerator() {
	        super();
	        properties = new Properties();
	        suppressDate = false;
	        suppressAllComments = false;
	        addRemarkComments = false;
	        defaultDateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    }

	    @Override
	    public void addJavaFileComment(CompilationUnit compilationUnit) {
	        // add no file level comments by default
	    }

	    /**
	     * Adds a suitable comment to warn users that the element was generated, and
	     * when it was generated.
	     *
	     * @param xmlElement the xml element
	     */
	    @Override
	    public void addComment(XmlElement xmlElement) {
	        //xml不生成注释
	    }

	    @Override
	    public void addRootComment(XmlElement rootElement) {
	        // add no document level comments by default
	    }

	    @Override
	    public void addConfigurationProperties(Properties properties) {
	        this.properties.putAll(properties);

	        suppressDate = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));

	        suppressAllComments = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));

	        addRemarkComments = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS));

	        String dateFormatString = properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT);
	        if (StringUtility.stringHasValue(dateFormatString)) {
	            dateFormat = new SimpleDateFormat(dateFormatString);
	        }
	    }

	    /**
	     * This method adds the custom javadoc tag for. You may do nothing if you do not
	     * wish to include the Javadoc tag - however, if you do not include the Javadoc
	     * tag then the Java merge capability of the eclipse plugin will break.
	     *
	     * @param javaElement       the java element
	     * @param markAsDoNotDelete the mark as do not delete
	     */
	    protected void addJavadocTag(JavaElement javaElement, boolean markAsDoNotDelete) {
	        javaElement.addJavaDocLine(" *"); //$NON-NLS-1$
	        StringBuilder sb = new StringBuilder();
	        sb.append(" * "); //$NON-NLS-1$
	        sb.append(MergeConstants.NEW_ELEMENT_TAG);
	        if (markAsDoNotDelete) {
	            sb.append(" do_not_delete_during_merge"); //$NON-NLS-1$
	        }
	        String s = getDateString();
	        if (s != null) {
	            sb.append(' ');
	            sb.append(s);
	        }
	        javaElement.addJavaDocLine(sb.toString());
	    }

	    /**
	     * Returns a formated date string to include in the Javadoc tag and XML
	     * comments. You may return null if you do not want the date in these
	     * documentation elements.
	     * 
	     * @return a string representing the current timestamp, or null
	     */
	    protected String getDateString() {
	        if (suppressDate) {
	            return null;
	        } else if (dateFormat != null) {
	            return dateFormat.format(new Date());
	        } else {
	            return defaultDateFormat.format(new Date());
	        }
	    }

	    @Override
	    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
	        if (suppressAllComments) {
	            return;
	        }

	        StringBuilder sb = new StringBuilder();

	        innerClass.addJavaDocLine("/**"); //$NON-NLS-1$
	        
	        sb.append(" * 内部条件 "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        innerClass.addJavaDocLine(sb.toString());

	        addJavadocTag(innerClass, false);

	        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    @Override
	    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
	        if (suppressAllComments) {
	            return;
	        }

	        StringBuilder sb = new StringBuilder();

	        innerClass.addJavaDocLine("/**"); //$NON-NLS-1$
	        
	        sb.append(" * 扩展条件.");
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        innerClass.addJavaDocLine(sb.toString());

	        addJavadocTag(innerClass, markAsDoNotDelete);

	        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    /**
	     * entity生成的时候
	     */
	    @Override
	    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
	        if (suppressAllComments) {
	            return;
	        }

	        topLevelClass.addJavaDocLine("/**"); //$NON-NLS-1$

	        String remarks = introspectedTable.getRemarks();
	        if (StringUtility.stringHasValue(remarks)) {
	            String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	            for (String remarkLine : remarkLines) {
	                topLevelClass.addJavaDocLine(" *   " + remarkLine); //$NON-NLS-1$
	            }
	        }

	        StringBuilder sb = new StringBuilder();
	        sb.append(" * "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        topLevelClass.addJavaDocLine(sb.toString());

	        topLevelClass.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    @Override
	    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
	        
	    }

	    @Override
	    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
	            IntrospectedColumn introspectedColumn) {
	        if (suppressAllComments) {
	            return;
	        }

	        field.addJavaDocLine("/**"); //$NON-NLS-1$

	        String remarks = introspectedColumn.getRemarks();
	        if (StringUtility.stringHasValue(remarks)) {
	            String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	            for (String remarkLine : remarkLines) {
	                field.addJavaDocLine(" *   " + remarkLine); //$NON-NLS-1$
	            }
	        }

	        StringBuilder sb = new StringBuilder();
	        sb.append(" * "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        sb.append('.');
	        sb.append(introspectedColumn.getActualColumnName());
	        field.addJavaDocLine(sb.toString());

	        addJavadocTag(field, false);

	        field.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    /**
	     * example中的字段
	     */
	    @Override
	    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
	        if (suppressAllComments) {
	            return;
	        }

	        StringBuilder sb = new StringBuilder();

	        field.addJavaDocLine("/**"); //$NON-NLS-1$
	        if(field.getName().equalsIgnoreCase("pageIndex")) {
	        	sb.append(" * 页码 从1开始，默认-1 "); //$NON-NLS-1$
	        }
	        else if(field.getName().equalsIgnoreCase("pageSize")) {
	        	sb.append(" * 页大小 默认-1 "); //$NON-NLS-1$
	        }
	        else if(field.getName().equalsIgnoreCase("orderByClause")) {
	        	sb.append(" * 排序字段"); //$NON-NLS-1$
	        }
	        else if(field.getName().equalsIgnoreCase("distinct")) {
	        	sb.append(" * 是否去重"); //$NON-NLS-1$
	        }
	        else if(field.getName().equalsIgnoreCase("oredCriteria")) {
	        	sb.append(" * 查询条件"); //$NON-NLS-1$
	        }
	        else if(field.getName().equalsIgnoreCase("columnMapping")) {
	        	sb.append(" * 字段名与列名映射关系"); //$NON-NLS-1$
	        }
	        else {
	        	sb.append(" * 对应表 "); //$NON-NLS-1$
		        sb.append(introspectedTable.getFullyQualifiedTable());
	        }
	        field.addJavaDocLine(sb.toString());

	        addJavadocTag(field, false);

	        field.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    /**
	     * example中的方法
	     */
	    @Override
	    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
	        if (suppressAllComments) {
	            return;
	        }

	        StringBuilder sb = new StringBuilder();

	        method.addJavaDocLine("/**"); //$NON-NLS-1$
	        if(method.getName().equalsIgnoreCase("AuthUserExample")) {
	        	sb.append(" * 构造函数"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("setOrderByClause")) {
	        	sb.append(" * 设置排序字段"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("getOrderByClause")) {
	        	sb.append(" * 获取排序字段"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("setDistinct")) {
	        	sb.append(" * 是否去重"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("isDistinct")) {
	        	sb.append(" * 是否去重"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("getOredCriteria")) {
	        	sb.append(" * 获取查询条件"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("or")) {
	        	sb.append(" * 创建新的查询条件，用or关联旧的条件"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("createCriteria")) {
	        	sb.append(" * 创建新的查询条件"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("createCriteriaInternal")) {
	        	sb.append(" * 创建新的查询条件（内部）"); //$NON-NLS-1$ createCriteriaInternal
	        }
	        else if(method.getName().equalsIgnoreCase("clear")) {
	        	sb.append(" * 清空查询条件"); //$NON-NLS-1$ createCriteriaInternal
	        }
	        else if(method.getName().equalsIgnoreCase("setPageIndex")) {
	        	sb.append(" * 设置要查询的页码"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("getPageIndex")) {
	        	sb.append(" * 获取要查询的页码"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("setPageIndex")) {
	        	sb.append(" * 设置要查询的页码"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("setPageSize")) {
	        	sb.append(" * 设置页大小"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("getPageSize")) {
	        	sb.append(" * 获取页大小"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("setPage")) {
	        	sb.append(" * 设置分页参数"); //$NON-NLS-1$
	        }
	        else if(method.getName().equalsIgnoreCase("getStartIndex")) {
	        	sb.append(" * 获取分页数据起始位置"); //$NON-NLS-1$
	        }
	        else {
	        	sb.append(" * 对应表 "); //$NON-NLS-1$
		        sb.append(introspectedTable.getFullyQualifiedTable());
	        }
	        method.addJavaDocLine(sb.toString());

	        addJavadocTag(method, false);

	        method.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    @Override
	    public void addGetterComment(Method method, IntrospectedTable introspectedTable,
	            IntrospectedColumn introspectedColumn) {
	        if (suppressAllComments) {
	            return;
	        }

	        StringBuilder sb = new StringBuilder();

	        method.addJavaDocLine("/**"); //$NON-NLS-1$
	        
	        String remarks = introspectedColumn.getRemarks();
	        if (StringUtility.stringHasValue(remarks)) {
	            String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	            for (String remarkLine : remarkLines) {
	            	method.addJavaDocLine(" * " + remarkLine); //$NON-NLS-1$
	            }
	        }

	        sb.setLength(0);
	        sb.append(" * @return "); //$NON-NLS-1$
	        sb.append(introspectedTable.getFullyQualifiedTable());
	        sb.append('.');
	        sb.append(introspectedColumn.getActualColumnName());
	        method.addJavaDocLine(sb.toString());

	        addJavadocTag(method, false);

	        method.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    @Override
	    public void addSetterComment(Method method, IntrospectedTable introspectedTable,
	            IntrospectedColumn introspectedColumn) {
	        if (suppressAllComments) {
	            return;
	        }

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

	        addJavadocTag(method, false);

	        method.addJavaDocLine(" */"); //$NON-NLS-1$
	    }

	    @Override
	    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
	            Set<FullyQualifiedJavaType> imports) {
	        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated")); //$NON-NLS-1$
	        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString(); //$NON-NLS-1$
	        method.addAnnotation(getGeneratedAnnotation(comment));
	    }

	    @Override
	    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
	            IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
	        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated")); //$NON-NLS-1$
	        String comment = "Source field: " //$NON-NLS-1$
	                + introspectedTable.getFullyQualifiedTable().toString() + "." //$NON-NLS-1$
	                + introspectedColumn.getActualColumnName();
	        method.addAnnotation(getGeneratedAnnotation(comment));
	    }

	    @Override
	    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
	            Set<FullyQualifiedJavaType> imports) {
	        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated")); //$NON-NLS-1$
	        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString(); //$NON-NLS-1$
	        field.addAnnotation(getGeneratedAnnotation(comment));
	    }

	    @Override
	    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
	            IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
	        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated")); //$NON-NLS-1$
	        String comment = "Source field: " //$NON-NLS-1$
	                + introspectedTable.getFullyQualifiedTable().toString() + "." //$NON-NLS-1$
	                + introspectedColumn.getActualColumnName();
	        field.addAnnotation(getGeneratedAnnotation(comment));

	        if (!suppressAllComments && addRemarkComments) {
	            String remarks = introspectedColumn.getRemarks();
	            if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
	                field.addJavaDocLine("/**"); //$NON-NLS-1$
	                field.addJavaDocLine(" * Database Column Remarks:"); //$NON-NLS-1$
	                String[] remarkLines = remarks.split(System.getProperty("line.separator")); //$NON-NLS-1$
	                for (String remarkLine : remarkLines) {
	                    field.addJavaDocLine(" *   " + remarkLine); //$NON-NLS-1$
	                }
	                field.addJavaDocLine(" */"); //$NON-NLS-1$
	            }
	        }
	    }

	    @Override
	    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable,
	            Set<FullyQualifiedJavaType> imports) {
	        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated")); //$NON-NLS-1$
	        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString(); //$NON-NLS-1$
	        innerClass.addAnnotation(getGeneratedAnnotation(comment));
	    }

	    private String getGeneratedAnnotation(String comment) {
	        StringBuilder buffer = new StringBuilder();
	        buffer.append("@Generated("); //$NON-NLS-1$
	        if (suppressAllComments) {
	            buffer.append('\"');
	        } else {
	            buffer.append("value=\""); //$NON-NLS-1$
	        }

	        buffer.append(MyBatisGenerator.class.getName());
	        buffer.append('\"');

	        if (!suppressDate && !suppressAllComments) {
	            buffer.append(", date=\""); //$NON-NLS-1$
	            buffer.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
	            buffer.append('\"');
	        }

	        if (!suppressAllComments) {
	            buffer.append(", comments=\""); //$NON-NLS-1$
	            buffer.append(comment);
	            buffer.append('\"');
	        }

	        buffer.append(')');
	        return buffer.toString();
	    }

	    @Override
	    public void addFileComment(KotlinFile kotlinFile) {
	        if (suppressAllComments) {
	            return;
	        }

	        kotlinFile.addFileCommentLine("/*"); //$NON-NLS-1$
	        kotlinFile.addFileCommentLine(" * Auto-generated file. Created by MyBatis Generator"); //$NON-NLS-1$
	        if (!suppressDate) {
	            kotlinFile.addFileCommentLine(" * Generation date: " //$NON-NLS-1$
	                    + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
	        }
	        kotlinFile.addFileCommentLine(" */"); //$NON-NLS-1$
	    }
}
