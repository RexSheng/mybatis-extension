package com.github.rexsheng.mybatis.converter;

import java.io.Serializable;

/**
 * @author RexSheng
 * 2020年8月8日 上午11:51:02
 */
@FunctionalInterface
public interface Converter<T> extends Serializable {
    void apply(T t);
}
