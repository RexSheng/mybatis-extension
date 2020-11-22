package com.github.rexsheng.mybatis.converter.type;

import java.util.Date;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:46:46
 */
public class DateTypeConverterHandler extends BaseTypeConverterHandler<Date>{

	@SuppressWarnings("unchecked")
	@Override
	public <U> U convert(Class<U> clazz) {
		// TODO Auto-generated method stub
		System.out.println("convert:convertype:"+converterType.toString()+",date:"+(U)getValue().toString());
		return (U)getValue().toString();
	}

	@Override
	public String toString() {
		System.out.println("toString:convertype:"+converterType.toString()+",date:"+getValue().toString());
		return getValue().toString();
	}
}
