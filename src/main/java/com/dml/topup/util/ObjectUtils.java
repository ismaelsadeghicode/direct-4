package com.dml.topup.util;

/**
 * @author Ismael Sadeghi
 */
public abstract class ObjectUtils {
    public ObjectUtils() {
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean isNotNull(Object o) {
        return o != null;
    }

    public static boolean isAllNull(Object... objects) {
        Object[] var1 = objects;
        int var2 = objects.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Object o = var1[var3];
            if (isNotNull(o)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isExistInArray(Object src, Object... des) {
        Object[] var2 = des;
        int var3 = des.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Object o = var2[var4];
            if (src.equals(o)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNotExistInArray(Object src, Object... des) {
        return !isExistInArray(src, des);
    }

    public static boolean isAnyNull(Object... objects) {
        Object[] var1 = objects;
        int var2 = objects.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Object o = var1[var3];
            if (isNull(o)) {
                return true;
            }
        }

        return false;
    }
}
