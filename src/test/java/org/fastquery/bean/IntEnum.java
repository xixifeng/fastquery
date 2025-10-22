package org.fastquery.bean;

/**
 * @author xixifeng (fastquery@126.com)
 */
public interface IntEnum
{
    int getValue();

    static <E extends IntEnum> E fromValue(Class<E> enumClass, int value) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant in " + enumClass.getSimpleName() + " with value " + value);
    }
}
