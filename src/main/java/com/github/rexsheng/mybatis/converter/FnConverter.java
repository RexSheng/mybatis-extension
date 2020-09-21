package com.github.rexsheng.mybatis.converter;

import com.github.rexsheng.mybatis.core.SFunction;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng
 * 2020年8月8日 上午11:51:24
 */
public class FnConverter<T> {
    public String fnToFieldName(Converter<T> fn) {
        return ReflectUtil.fnToFieldName(fn);
    }
    public String functionToFieldName(SFunction<T,Object> fn) {
        return ReflectUtil.fnToFieldName(fn);
    }
    public static <A> FnConverter<A> of(Class<A> clazz) {
        return new FnConverter<A>();
    }
}
