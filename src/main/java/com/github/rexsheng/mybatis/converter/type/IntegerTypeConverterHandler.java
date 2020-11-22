package com.github.rexsheng.mybatis.converter.type;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:46:46
 */
public class IntegerTypeConverterHandler extends BaseTypeConverterHandler<Integer>{

	@SuppressWarnings("unchecked")
	@Override
	public <U> U convert(Class<U> clazz) {
		System.out.println("convert:convertype:"+converterType.toString()+",Integer:"+(U)getValue().toString());
		return (U)getValue();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
