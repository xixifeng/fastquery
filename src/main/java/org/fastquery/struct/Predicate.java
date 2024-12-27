package org.fastquery.struct;

import com.alibaba.fastjson.JSON;
import lombok.Getter;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author xixifeng (fastquery@126.com)
 */
public abstract class Predicate<E>
{
    @Getter
    private SQLValue sqlValue;
    private final StringBuilder builder = new StringBuilder();
    private final List<Object> values = new ArrayList<>();

    private static final String AND = " and ";
    private static final String OR = " or ";
    private static final String WHERE = " where ";
    private static final String SET = " set ";

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName != ? 这类表达式。与上一个条件单元的关系是 and
     * @param left 表达式左边的字段名称
     * @param operator 运算符
     * @param right 右边的值
     * @return 当前对象，便于链式操作
     * @param <T> 字段值的类型
     */
    public <T> Predicate<E> and(Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        return this.condition(AND,left,operator,right);
    }

    @Deprecated
    public <T> Predicate<E> and(Supplier<Chip<T,E>> left, Collection<T> collection)
    {
        return this.condition(AND, left,SQLOperator.IN,collection);
    }

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName != ? 这类表达式。与上一个条件单元的关系是 or
     * @param left 表达式左边的字段名称
     * @param operator 运算符
     * @param right 右边的值
     * @return 当前对象，便于链式操作
     * @param <T>  字段值的类型
     */
    public <T> Predicate<E> or(Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        return this.condition(OR,left,operator,right);
    }

    private <T> Predicate<E> condition(String booleanOperator, Supplier<Chip<T,E>> left, SQLOperator operator, T right)
    {
        if (right != null && !right.toString().isEmpty())
        {
            if (!builder.toString().endsWith("( "))
            {
                builder.append(booleanOperator);
            }

            if (SQLOperator.JSON_CONTAINS.equals(operator))
            {
                builder.append('(')
                        .append(operator.getOperator())
                        .append(left.get().getName())
                        .append(", ?)");
            }
            else
            {
                builder.append(left.get().getName());
                builder.append(operator.getOperator());
            }
            addValue(right);
        }

        return this;
    }

    private void addValue(Object value)
    {
        if(value instanceof Enum || value instanceof JSON)
        {
            values.add(value.toString());
        }
        else if(value instanceof EnumSet)
        {
            values.add(toEnumSetString(value));
        }
        else
        {
            values.add(value);
        }
    }

    private static String toEnumSetString(Object enumSet)
    {
        return enumSet.toString().replace("[","").replace("]", "").replace(" ", "");
    }

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName != ? 这类表达式。与上一个条件单元的关系是 and
     * @param left 表达式左边的字段名称
     * @param operator 运算符
     * @param collection 右边的值
     * @return 当前对象，便于链式操作
     * @param <T> 字段值的类型
     */
    public <T> Predicate<E> and(Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(AND, left,operator,collection);
    }

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName != ? 这类表达式。与上一个条件单元的关系是 or
     * @param left 表达式左边的字段名称
     * @param operator 运算符
     * @param collection 右边的值
     * @return 当前对象，便于链式操作
     * @param <T> 字段值的类型
     */
    public <T> Predicate<E> or(Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        return this.condition(OR, left,operator,collection);
    }

    private  <T> Predicate<E> condition(String booleanOperator, Supplier<Chip<T,E>> left, SQLOperator operator, Collection<T> collection)
    {
        if (collection != null)
        {
            collection = collection.isEmpty() ? collection : collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
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
        }

        return this;
    }

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName is not null 这类表达式。与上一个条件单元的关系是 and
     * @param left 表达式左边的字段名称
     * @param operator 运算符, 可选项 is not null, is null
     * @return 当前对象，便于链式操作
     * @param <T> 字段值的类型
     */
    public <T> Predicate<E> and(Supplier<Chip<T,E>> left, NulOperator operator)
    {
        return this.condition(AND,left,operator);
    }

    /**
     * 用于构建 sql 查询条件单元。例如，构建 fieldName is not null 这类表达式。与上一个条件单元的关系是 and
     * @param left 表达式左边的字段名称
     * @param operator 运算符, 可选项 is not null, is null
     * @return 当前对象，便于链式操作
     * @param <T> 字段值的类型
     */
    public <T> Predicate<E> or(Supplier<Chip<T,E>> left, NulOperator operator)
    {
        return this.condition(OR,left,operator);
    }

    private <T> Predicate<E> condition(String booleanOperator, Supplier<Chip<T,E>> left, NulOperator operator)
    {
        if (!builder.toString().endsWith("( "))
        {
            builder.append(booleanOperator);
        }

        builder.append(left.get().getName());
        builder.append(operator.getOperator());

        return this;
    }


    /**
     * 默认根据 id 降序排序
     */
    public void orderBy()
    {
        builder.append(" order by id desc");
        finish();
    }

    /**
     * 自定义排序， order by 可以省略
     * @param sql 排序规则
     */
    public void orderBy(String sql)
    {
        Objects.requireNonNull(sql, "sql must not be null");
        builder.append(" order by ");
        builder.append(sql);
        finish();
    }

    /**
     * 将多个条件单元包装在小括号()里. ()与前面条件的关系是 and
     * @param function 条件单元集
     * @return 当前对象，便于链式操作
     */
    public Predicate<E> and(UnaryOperator<Predicate<E>> function)
    {
        return group(function,AND);
    }

    /**
     * 将多个条件单元包装在小括号()里. ()与前面条件的关系是 or
     * @param function 条件单元集
     * @return 当前对象，便于链式操作
     */
    public Predicate<E> or(UnaryOperator<Predicate<E>> function)
    {
        return group(function,OR);
    }

    private Predicate<E> group(UnaryOperator<Predicate<E>> function, String booleanOperator)
    {
        int groupStart = builder.length();

        builder.append(booleanOperator);
        builder.append(" ( ");

        function.apply(this);

        int len = builder.length();

        if (len - groupStart <= 8) // 表明 function 没有内容
        {
            builder.delete(groupStart, len - 1);
        }
        else
        {
            builder.append(" )");
        }

        return this;
    }

    public void finish()
    {
        SQLValue sv = new SQLValue();
        String sql = builder.toString().trim();

        sql = sql.replace(", and ",WHERE).replace(", or ",WHERE);

        sv.setSql(' ' + sql);
        sv.setValues(values);
        this.sqlValue = sv;

        builder.setLength(0);
        values.clear();
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

    /**
     * 将指定字段自增 Z (Z 表示整数集)
     * @param fieldName 字段名称
     * @param t 自增的量（正整数或负整数）
     * @return 当前对象，便于链式操作
     * @param <T> 增量值的类型
     */
    public <T extends Number> Predicate<E> increment(Supplier<Chip<T,E>> fieldName,T t)
    {
        Objects.requireNonNull(fieldName, "fieldName cannot be null");
        Objects.requireNonNull(t, "t cannot be null");
        if(builder.indexOf(SET) == -1)
        {
            builder.append(SET);
        }
        String name = fieldName.get().getName();
        builder.append(name).append('=').append(name).append('+').append(t).append(',');
        return this;
    }

    /**
     * 修改指定字段的值
     * @param fieldName 字段名称
     * @param t 设置的值
     * @return  当前对象，便于链式操作
     * @param <T> 设置值的类型
     */
    public <T> Predicate<E> set(Supplier<Chip<T,E>> fieldName,T t)
    {
        Objects.requireNonNull(fieldName, "fieldName cannot be null");
        Objects.requireNonNull(t, "t cannot be null");
        if(builder.indexOf(SET) == -1)
        {
            builder.append(SET);
        }
        String name = fieldName.get().getName();
        builder.append(name).append('=').append("?,");
        addValue(t);
        return this;
    }
}
