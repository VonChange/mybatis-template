package com.vonchange.mybatis.tpl.clazz;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 */
public class ClazzUtils {
	private ClazzUtils() {
		throw new IllegalStateException("Utility class");
	}
	/**
	 * isBaseType
	 */
	public static boolean isBaseType(Class<?> clazz) {
		return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(Character.class) || clazz.equals(Short.class) || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class) || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
	}


}
