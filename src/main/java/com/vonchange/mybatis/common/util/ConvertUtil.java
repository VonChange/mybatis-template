package com.vonchange.mybatis.common.util;


import com.vonchange.mybatis.common.util.bean.convert.Converter;

import java.math.BigDecimal;

/**
 * 简单方式(效率高)实现类型转换,细节部分会不如ConvertUtils :ConvertUtils一次类型转换需要69ms 有点代价过高
 * 不过多个也大概是这个时间?
 *
 * @author vonchange@163.com
 */
public class ConvertUtil {
	private static final String NULLSTR = "NULL";

	private static Object toNull(Object value) {
		if (null == value) {
			return null;
		}
		if (value instanceof String) {
			value = value.toString().trim();
			if (NULLSTR.equals(value)) {
				return null;
			}
			if("".equals(value)){
				return null;
			}
		}
		return value;
	}

	public static Integer toInteger(Object value) {
		value = toNull(value);
		return Converter.get().toInteger(value);
	}

	public static String toString(Object value) {
		if (null == value) {
			return null;
		}
		return Converter.get().toString(value);
	}

	public static Long toLong(Object value) {
		value = toNull(value);
		return Converter.get().toLong(value);
	}

	public static Boolean toBoolean(Object value) {
		value = toNull(value);
		return Converter.get().toBoolean(value);
	}

	public static Double toDouble(Object value) {
		value = toNull(value);
		return Converter.get().toDouble(value);
	}

	public static Float toFloat(Object value) {
		value = toNull(value);
		return Converter.get().toFloat(value);
	}

	public static Short toShort(Object value) {
		value = toNull(value);
		return Converter.get().toShort(value);
	}

	public static Byte toByte(Object value) {
		value = toNull(value);
		return Converter.get().toByte(value);
	}



	public static BigDecimal toBigDecimal(Object value) {
		value = toNull(value);
		return Converter.get().toBigDecimal(value);
	}
	@SuppressWarnings("unchecked")
	public static <T> T toObject(Object value, Class<?> targetType) {
		if (null == value) {
			return null;
		}
		if(value.getClass().isAssignableFrom(targetType)){
			return (T) value;
		}
		String targetTypeName = targetType.getSimpleName();
		return toObject(value,targetTypeName);
	}
	@SuppressWarnings("unchecked")
	public static <T> T toObject(Object value, String targetTypeName) {
		if (null == value) {
			return null;
		}
		if (targetTypeName.equalsIgnoreCase("string")) {
			return (T) ConvertUtil.toString(value);
		}
		if (targetTypeName.equalsIgnoreCase("Integer")) {
			return  (T)ConvertUtil.toInteger(value);
		}
		if (targetTypeName.equalsIgnoreCase("Long")) {
			return  (T)ConvertUtil.toLong(value);
		}
		if (targetTypeName.equalsIgnoreCase("Boolean")) {
			return  (T)ConvertUtil.toBoolean(value);
		}
		if (targetTypeName.equalsIgnoreCase("Float")) {
			return  (T)ConvertUtil.toFloat(value);
		}
		if (targetTypeName.equalsIgnoreCase("Double")) {
			return  (T)ConvertUtil.toDouble(value);
		}
		if (targetTypeName.equalsIgnoreCase("Short")) {
			return  (T)ConvertUtil.toShort(value);
		}
		if (targetTypeName.equalsIgnoreCase("Byte")) {
			return  (T)ConvertUtil.toByte(value);
		}
		if (targetTypeName.equalsIgnoreCase("BigDecimal")) {
			return  (T)ConvertUtil.toBigDecimal(value);
		}
		return  (T)value;
	}
	public static void main(String[] args) {
		toInteger("");
	}

}
