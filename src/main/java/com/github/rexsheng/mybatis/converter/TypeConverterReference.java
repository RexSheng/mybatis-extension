package com.github.rexsheng.mybatis.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:41:39
 */
public abstract class TypeConverterReference<T> {

	  private final Type rawType;

	  protected TypeConverterReference() {
	    rawType = getSuperclassTypeParameter(getClass());
	  }

	  Type getSuperclassTypeParameter(Class<?> clazz) {
	    Type genericSuperclass = clazz.getGenericSuperclass();
	    if (genericSuperclass instanceof Class) {
	      // try to climb up the hierarchy until meet something useful
	      if (TypeConverterReference.class != genericSuperclass) {
	        return getSuperclassTypeParameter(clazz.getSuperclass());
	      }

	      throw new RuntimeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
	        + "Remove the extension or add a type parameter to it.");
	    }

	    Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
	    // TODO remove this when Reflector is fixed to return Types
	    if (rawType instanceof ParameterizedType) {
	      rawType = ((ParameterizedType) rawType).getRawType();
	    }

	    return rawType;
	  }

	  public final Type getRawType() {
	    return rawType;
	  }

	  @Override
	  public String toString() {
	    return rawType.toString();
	  }

	}
