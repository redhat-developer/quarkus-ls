package io.quarkus.deployment.bean;

/**
 * <p>
 * This class is a copy/paste of
 * https://github.com/quarkusio/quarkus/blob/99a773f3a096a36c72caa152245093d10703519e/core/deployment/src/main/java/io/quarkus/deployment/bean/JavaBeanUtil.java
 * </p>
 */
public class JavaBeanUtil {

    private static final String GET = "get";
    private static final String IS = "is";

    public static String getGetterName(String name, String type) {
        String prefix = type.equals("Z") ? IS : GET;
        return prefix + capitalize(name);
    }

    public static String getSetterName(String name) {
        return "set" + capitalize(name);
    }

    // See conventions expressed by https://docs.oracle.com/javase/7/docs/api/java/beans/Introspector.html#decapitalize(java.lang.String)
    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
                return name;
            } else {
                char[] chars = name.toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                return new String(chars);
            }
        } else {
            return name;
        }
    }

    // See conventions expressed by https://docs.oracle.com/javase/7/docs/api/java/beans/Introspector.html#decapitalize(java.lang.String)
    public static String decapitalize(String name) {
        if (name != null && name.length() != 0) {
            if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
                return name;
            } else {
                char[] chars = name.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        } else {
            return name;
        }
    }

    /**
     * Returns the corresponding property name for a getter method name
     * 
     * @throws IllegalArgumentException if the method name does not follow the getter name convention
     */
    public static String getPropertyNameFromGetter(String methodName) {
        if (methodName.startsWith(GET)) {
            return decapitalize(methodName.substring(GET.length()));
        } else if (methodName.startsWith(IS)) {
            return decapitalize(methodName.substring(IS.length()));
        } else {
            throw new IllegalArgumentException(methodName + " is not a getter");
        }
    }
}