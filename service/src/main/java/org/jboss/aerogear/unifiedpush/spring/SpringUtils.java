package org.jboss.aerogear.unifiedpush.spring;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

/**
 * SpringUtils is a utility class encapsulating common functionality on objects
 * and other class types.
 */
public abstract class SpringUtils {

	/* (non-Javadoc) */
	public static String defaultIfEmpty(String value, String defaultValue) {
		return (StringUtils.hasText(value) ? value : defaultValue);
	}

	/* (non-Javadoc) */
	public static <T> T defaultIfNull(T value, T defaultValue) {
		return Optional.ofNullable(value).orElse(defaultValue);
	}

	/* (non-Javadoc) */
	public static <T> T defaultIfNull(T value, Supplier<T> supplier) {
		return Optional.ofNullable(value).orElseGet(supplier);
	}

	/* (non-Javadoc) */
	public static String dereferenceBean(String beanName) {
		return String.format("%1$s%2$s", BeanFactory.FACTORY_BEAN_PREFIX, beanName);
	}

	/* (non-Javadoc) */
	public static boolean equalsIgnoreNull(Object obj1, Object obj2) {
		return (obj1 == null ? obj2 == null : obj1.equals(obj2));
	}

	/* (non-Javadoc) */
	public static boolean nullOrEquals(Object obj1, Object obj2) {
		return (obj1 == null || obj1.equals(obj2));
	}

	/* (non-Javadoc) */
	public static boolean nullSafeEquals(Object obj1, Object obj2) {
		return (obj1 != null && obj1.equals(obj2));
	}

	/* (non-Javadoc) */
	public static <T> T safeGetValue(Supplier<T> supplier) {
		return safeGetValue(supplier, null);
	}

	/* (non-Javadoc) */
	public static <T> T safeGetValue(Supplier<T> supplier, T defaultValue) {
		try {
			return supplier.get();
		} catch (Throwable ignore) {
			return defaultValue;
		}
	}

	/**
	 * Null-safe operation returning the given {@link Set} if not
	 * {@literal null} or an empty {@link Set} if {@literal null}.
	 *
	 * @param <T>
	 *            Class type of the {@link Set} elements.
	 * @param set
	 *            {@link Set} to evaluate.
	 * @return the given {@link Set} if not null or an empty {@link Set}.
	 * @see java.util.Collections#emptySet()
	 * @see java.util.Set
	 */
	public static <T> Set<T> nullSafeSet(Set<T> set) {
		return (set != null ? set : Collections.emptySet());
	}
}