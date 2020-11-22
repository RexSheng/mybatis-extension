package com.github.rexsheng.mybatis.converter;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.rexsheng.mybatis.converter.type.BigDecimalTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.BigIntegerTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.BooleanTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ByteArrayTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ByteObjectArrayTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ByteTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.CharacterTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.DateTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.DoubleTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.FloatTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.InputStreamTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.InstantTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.IntegerTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.LocalDateTimeTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.LocalDateTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.LocalTimeTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.LongTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.MonthTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ObjectTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.OffsetDateTimeTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.OffsetTimeTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ReaderTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ShortTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.SqlDateTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.SqlTimeTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.SqlTimestampTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.StringTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.YearMonthTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.YearTypeConverterHandler;
import com.github.rexsheng.mybatis.converter.type.ZonedDateTimeTypeConverterHandler;

/**
 * @author RexSheng 2020年11月16日 下午11:38:38
 */
public class TypeConverterRegistry {
	
	private final Map<Type, Map<ConverterType, TypeConverterHandler<?>>> typeHandlersMap = new HashMap<>();

	public TypeConverterRegistry() {
	    register(Boolean.class, new BooleanTypeConverterHandler());
	    register(boolean.class, new BooleanTypeConverterHandler());
	    
	    register(Byte.class, new ByteTypeConverterHandler());
	    register(byte.class, new ByteTypeConverterHandler());

	    register(Short.class, new ShortTypeConverterHandler());
	    register(short.class, new ShortTypeConverterHandler());

	    register(Integer.class, new IntegerTypeConverterHandler());
	    register(int.class, new IntegerTypeConverterHandler());

	    register(Long.class, new LongTypeConverterHandler());
	    register(long.class, new LongTypeConverterHandler());

	    register(Float.class, new FloatTypeConverterHandler());
	    register(float.class, new FloatTypeConverterHandler());

	    register(Double.class, new DoubleTypeConverterHandler());
	    register(double.class, new DoubleTypeConverterHandler());

	    register(Reader.class, new ReaderTypeConverterHandler());
	    register(String.class, new StringTypeConverterHandler());

	    register(Object.class, new ObjectTypeConverterHandler());

	    register(BigInteger.class, new BigIntegerTypeConverterHandler());
	    register(BigDecimal.class, new BigDecimalTypeConverterHandler());
	    
	    register(InputStream.class, new InputStreamTypeConverterHandler());
	    register(Byte[].class, new ByteObjectArrayTypeConverterHandler());
	    register(byte[].class, new ByteArrayTypeConverterHandler());

	    register(Date.class, new DateTypeConverterHandler());

	    register(java.sql.Date.class, new SqlDateTypeConverterHandler());
	    register(java.sql.Time.class, new SqlTimeTypeConverterHandler());
	    register(java.sql.Timestamp.class, new SqlTimestampTypeConverterHandler());

	    register(Instant.class, new InstantTypeConverterHandler());
	    register(LocalDateTime.class, new LocalDateTimeTypeConverterHandler());
	    register(LocalDate.class, new LocalDateTypeConverterHandler());
	    register(LocalTime.class, new LocalTimeTypeConverterHandler());
	    register(OffsetDateTime.class, new OffsetDateTimeTypeConverterHandler());
	    register(OffsetTime.class, new OffsetTimeTypeConverterHandler());
	    register(ZonedDateTime.class, new ZonedDateTimeTypeConverterHandler());
	    register(Month.class, new MonthTypeConverterHandler());
	    register(Year.class, new YearTypeConverterHandler());
	    register(YearMonth.class, new YearMonthTypeConverterHandler());

	    register(Character.class, new CharacterTypeConverterHandler());
	    register(char.class, new CharacterTypeConverterHandler());
	}

	public <T> void register(Class<T> javaType, TypeConverterHandler<? extends T> typeHandler) {
		register((Type) javaType, typeHandler);
	}

	public <T> void register(TypeConverterReference<T> javaTypeReference, TypeConverterHandler<? extends T> handler) {
		register(javaTypeReference.getRawType(), handler);
	}

	private <T> void register(Type javaType, TypeConverterHandler<? extends T> typeHandler) {
		register(javaType,ConverterType.TOSTRING,typeHandler);
	}
	
	private <T> void register(Type javaType,ConverterType converterType, TypeConverterHandler<? extends T> typeHandler) {
		if (javaType != null) {
			Map<ConverterType, TypeConverterHandler<?>> map = typeHandlersMap.get(javaType);
		      if (map == null) {
		        map = new HashMap<>();
		      }
		      map.put(converterType, typeHandler);
		      typeHandlersMap.put(javaType, map);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> TypeConverterHandler<T> getHandler(Type javaType, ConverterType converterType) {
		Map<ConverterType, TypeConverterHandler<?>> map =typeHandlersMap.get(javaType);
		if(map!=null) {
			return (TypeConverterHandler<T>)map.get(converterType);
		}
		return null;
	}
	
	public <T> TypeConverterHandler<T> getHandler(T value, ConverterType converterType) {
		if(value==null) {
			return null;
		}
		TypeConverterHandler<T> handler= getHandler(value.getClass(),converterType);
		if(handler!=null) {
			handler.setValue(value,converterType);
		}
		return handler;
	}
	
	public <T> TypeConverterHandler<T> getHandler(T value) {
		return getHandler(value,ConverterType.TOSTRING);
	}


	@SuppressWarnings("unchecked")
	public <T> TypeConverterHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
		if (javaTypeClass != null) {
			try {
				Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
				return (TypeConverterHandler<T>) c.newInstance(javaTypeClass);
			} catch (NoSuchMethodException ignored) {
				// ignored
			} catch (Exception e) {
				throw new RuntimeException("Failed invoking constructor for handler " + typeHandlerClass, e);
			}
		}
		try {
			Constructor<?> c = typeHandlerClass.getConstructor();
			return (TypeConverterHandler<T>) c.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to find a usable constructor for " + typeHandlerClass, e);
		}
	}

}
