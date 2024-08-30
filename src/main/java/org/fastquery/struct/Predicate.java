package org.fastquery.struct;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import org.fastquery.core.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author xixifeng (fastquery@126.com)
 */
@SuppressWarnings("unchecked")
public abstract class Predicate<E>
{
    @Getter
    private SQLValue sqlValue;
    private final StringBuilder builder = new StringBuilder();
    private final List<Object> values = new ArrayList<>();

    private static final String AND = " and ";
    private static final String OR = " or ";
    @Transient
    private Boolean existsNulOpt = false;

    public <T> E and(Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        return this.condition(AND,left,operator,right);
    }

    public <T> E or(Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        return this.condition(OR,left,operator,right);
    }

    private <T> E condition(String booleanOperator, Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        if (right != null && !right.toString().equals(""))
        {
            if (!builder.toString().endsWith("( "))
            {
                builder.append(booleanOperator);
            }
            builder.append(left.get().getName());
            builder.append(operator.getOperator());

            addValue(right);
        }

        return (E) this;
    }

    private void addValue(Object value)
    {
        if(value instanceof Enum || value instanceof JSON)
        {
            values.add(value.toString());
        }
        else
        {
            values.add(value);
        }
    }

    public <T> E and(Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(AND, left,operator,collection);
    }

    public <T> E or(Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(OR, left,operator,collection);
    }

    private  <T> E condition(String booleanOperator, Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        if (collection != null && !collection.isEmpty())
        {
            collection = collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!collection.isEmpty())
            {
                if (!builder.toString().endsWith("( "))
                {
                    builder.append(booleanOperator);
                }
                builder.append(left.get().getName());

                String op = operator.getOperator();
                op = op.replace("?", mark(collection.size()));
                builder.append(op);

                for (T t : collection)
                {
                    addValue(t);
                }
            }
        }
        else
        {
            if (!builder.toString().endsWith("( "))
            {
                builder.append(booleanOperator);
            }
            builder.append(left.get().getName());

            String op = operator.getOperator();
            builder.append(op);
            addValue(null);
        }

        return (E) this;
    }

    public <T> E and(Supplier<Chip<T,E>> left, NulOperator operator)
    {
        return this.condition(AND,left,operator);
    }

    public <T> E or(Supplier<Chip<T,E>> left, NulOperator operator)
    {
        return this.condition(OR,left,operator);
    }

    private <T> E condition(String booleanOperator, Supplier<Chip<T,E>> left, NulOperator operator)
    {
        if (!builder.toString().endsWith("( "))
        {
            builder.append(booleanOperator);
        }
        if(values.isEmpty() && !Boolean.TRUE.equals(existsNulOpt))
        {
            builder.append("where ");
            existsNulOpt = true;
        }
        builder.append(left.get().getName());
        builder.append(operator.getOperator());

        return (E) this;
    }


    /**
     * 默认根据 id 降序排序
     * @return 当前实例
     */
    public E orderBy()
    {
        builder.append(" order by id desc");
        finish();
        return (E) this;
    }

    /**
     * 自定义排序， order by 可以省略
     * @param sql 排序规则
     * @return 当前实例
     */
    public E orderBy(String sql)
    {
        Objects.requireNonNull(sql, "sql must not be null");
        builder.append(" order by ");
        builder.append(sql);
        finish();
        return (E) this;
    }

    public E and(UnaryOperator<E> function)
    {
        return group(function,AND);
    }

    public E or(UnaryOperator<E> function)
    {
        return group(function,OR);
    }

    private E group(UnaryOperator<E> function, String booleanOperator)
    {
        int groupStart = builder.length();

        builder.append(booleanOperator);
        builder.append(" ( ");

        function.apply((E) this);

        int len = builder.length();

        if (len - groupStart <= 8) // 表明 function 没有内容
        {
            builder.delete(groupStart, len - 1);
        }
        else
        {
            builder.append(" )");
        }

        return (E) this;
    }

    public void finish()
    {
        SQLValue sv = new SQLValue();
        String sql = builder.toString().trim();

        sv.setSql(' ' + sql);
        sv.setValues(values);
        this.sqlValue = sv;
    }

    private static String mark(int size)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++)
        {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
