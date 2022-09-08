package org.fastquery.bean;

/**
 * @author xixifeng (fastquery@126.com)
 */
public enum AreaType {
    国家("country"),
    一级("first"),
    二级("second"),
    三级("third"),
    四级("fourth"),
    五级("fifth");

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

