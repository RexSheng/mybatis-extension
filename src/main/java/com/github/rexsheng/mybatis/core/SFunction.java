package com.github.rexsheng.mybatis.core;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author RexSheng
 * 2020年8月8日 上午11:51:02
 */
@FunctionalInterface
public interface SFunction<T,R> extends Function<T,R>,Serializable {
    
}
