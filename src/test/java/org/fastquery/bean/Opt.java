package org.fastquery.bean;

/**
 * @author xixifeng (fastquery@126.com)
 */
public enum Opt implements IntEnum
{
    可选1(1,"opt1", "opt 1"),
    可选3(3,"opt3", "opt 3"),
    可选5(5,"opt5", "opt 5");

    private int value;
    private String message;
    private String enMessage;

    Opt(Integer value, String message, String enMessage)
    {
        this.value = value;
        this.message = message;
        this.enMessage = enMessage;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return Integer.toString(value);
    }

    public String getEnMessage()
    {
        return enMessage;
    }

    public Option option()
    {
        return new Option(this.ordinal() + 1, this.message, this.enMessage);
    }

    @Override
    public int getValue()
    {
        return value;
    }
}
