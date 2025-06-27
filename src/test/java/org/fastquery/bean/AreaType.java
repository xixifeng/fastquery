package org.fastquery.bean;

/**
 * @author xixifeng (fastquery@126.com)
 */
public enum AreaType {
    COUNTRY("country"),
    FIRST("first"),
    SECOND("second"),
    THIRD("third"),
    FOURTH("fourth"),
    FIFTH("fifth");

    public static final String CN_NAME = "类型";
    public static final String EN_NAME = "Type";

    private String enName;


    AreaType(String enName) {
        this.enName = enName;
    }

    public String getEnName() {
        return enName;
    }
}

