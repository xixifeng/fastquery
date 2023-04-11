package org.fastquery.struct;

import org.fastquery.core.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author xixifeng (fastquery@126.com)
 */
@SuppressWarnings("unchecked")
public abstract class Predicate<E>
{
    private SQLValue sqlValue;
    private final StringBuilder builder = new StringBuilder();
    private final List<Object> values = new ArrayList<>();

    @Transient
    private Integer groupStart = -1;

    public <T> E and(Supplier<Chip<T>> left, SQLOperator operator, T right)
    {
        return this.condition(BooleanOperator.AND,left,operator,right);
    }

    public <T> E or(Supplier<Chip<T>> left, SQLOperator operator, T right)
    {
        return this.condition(BooleanOperator.OR,left,operator,right);
    }

    private <T> E condition(BooleanOperator booleanOperator, Supplier<Chip<T>> left, SQLOperator operator, T right)
    {
        if (right != null && !right.toString().equals(""))
        {
            if (!builder.toString().endsWith("( "))
            {
                builder.append(booleanOperator != BooleanOperator.EMPTY ? booleanOperator.getName() : "");
            }
            builder.append(left.get().getName());
            builder.append(operator.getOperator());

            values.add(right);
        }

        return (E) this;
    }

    public <T> E and(Supplier<Chip<T>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(BooleanOperator.AND, left,operator,collection);
    }

    public <T> E or(Supplier<Chip<T>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(BooleanOperator.OR, left,operator,collection);
    }

    private  <T> E condition(BooleanOperator booleanOperator, Supplier<Chip<T>> left, SQLOperator operator, Collection<T> collection)
    {
        if (collection != null && !collection.isEmpty())
        {
            collection = collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!collection.isEmpty())
            {
                if (!builder.toString().endsWith("( "))
                {
                    builder.append(booleanOperator != BooleanOperator.EMPTY ? booleanOperator.getName() : "");
                }
                builder.append(left.get().getName());

                String op = operator.getOperator();
                op = op.replace("?", mark(collection.size()));
                builder.append(op);

                for (T t : collection)
                {
                    values.add(t.toString());
                }
            }
        }

        return (E) this;
    }

    public E orderBy()
    {
        builder.append(" order by id desc");
        return (E) this;
    }

    public E orderBy(String sql)
    {
        Objects.requireNonNull(sql, "sql must not be null");
        builder.append(" order by ");
        builder.append(sql);
        return (E) this;
    }

    public E groupStart(BooleanOperator booleanOperator)
    {
        groupStart = builder.length();
        if (booleanOperator == BooleanOperator.EMPTY)
        {
            throw new IllegalArgumentException("booleanOperator cannot be EMPTY!");
        }
        else
        {
            builder.append(booleanOperator.getName());
            builder.append(" ( ");
            return (E) this;
        }
    }

    public E groupEnd()
    {
        if (groupStart == -1)
        {
            throw new IllegalArgumentException("groupEnd can't find the '(' from the front");
        }

        if (builder.substring(groupStart).trim().endsWith("("))
        {
            builder.delete(groupStart, builder.length() - 1);
        }
        else
        {
            builder.append(" )");
            groupStart = -1; // 标识（）一对已经结束
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

    public SQLValue getSqlValue()
    {
        return sqlValue;
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
