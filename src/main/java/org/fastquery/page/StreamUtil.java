package org.fastquery.page;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.struct.AttributeConverter;
import org.fastquery.struct.Convert;
import org.fastquery.struct.Converter;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class StreamUtil
{
    /**
     * 从集合中收集指定类型的数据, 装入集合并返回
     *
     * @param collection collection
     * @param function   指定收集的目标
     * @param isDistinct 是否去重
     * @param <E>        集合中类型
     * @param <R>        需要收集的数据的类型
     * @return 收集之后的数据集合
     */
    public static <E, R> List<R> collected(Collection<E> collection, Function<E, R> function, boolean isDistinct)
    {
        Assert.notNull(collection, "collection must not be null");
        Stream<R> stream = collection.stream().map(function).filter(Objects::nonNull);
        if (isDistinct)
        {
            stream = stream.distinct();
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * 左表把右表的一些数据合并进来
     *
     * @param left                    左表
     * @param leftFriendshipKey       左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey      右表的关系键
     * @param leftSetRight            自定义左表是如何合并右表的数据
     * @param <E>                     左表元素的类型
     * @param <K>                     键的类型
     * @param <T>                     右表元素的类型
     * @return 得到右表数据之后的左表
     */
    public static <E, K, T> List<E> join(List<E> left, Function<E, List<K>> leftFriendshipKey, Function<List<K>,
            List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey, BiConsumer<E, List<T>> leftSetRight)
    {
        Assert.notNull(left, "left must not be null");
        Assert.notNull(leftFriendshipKey, "leftFriendshipKey must not be null");
        Assert.notNull(byLeftFriendshipKeyData, "byLeftFriendshipKeyData must not be null");
        Assert.notNull(rightFriendshipKey, "rightFriendshipKey must not be null");
        Assert.notNull(leftSetRight, "leftSetRight must not be null");

        // 收集左表的关系键
        List<List<K>> ids = StreamUtil.collected(left, leftFriendshipKey, true);

        //将左表的关系键合并成一个大集合
        List<K> leftKeys = new ArrayList<>();
        for (List<K> id : ids)
        {
            leftKeys.addAll(id);
        }
        // 根据左表的关系键查询出右表数据
        Map<K, List<T>> map = getKtListMap(leftKeys, byLeftFriendshipKeyData, rightFriendshipKey);

        // 将 entities.id 和  this.content. 指定的外键 合并
        left.forEach(target -> {
            List<K> leftKeyIds = leftFriendshipKey.apply(target); // 左表的关系键
            if (leftKeyIds != null)
            {
                List<T> list = new ArrayList<>();// 用于存放当前单元项要合并的数据
                for (K leftKey : leftKeyIds)
                {
                    List<T> t = map.get(leftKey);
                    if (t != null)
                    {
                        list.addAll(t);
                    }
                }

                leftSetRight.accept(target, list);
            }
        });

        return left;
    }

    private static <K, T> Map<K, List<T>> getKtListMap(List<K> leftKeys, Function<List<K>, List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey)
    {
        // 把右表转化成 Map 右表的关系键作为 key
        return byLeftFriendshipKeyData.apply(leftKeys).stream().collect(Collectors.groupingBy(rightFriendshipKey));
    }

    private static <K, T> Map<K, T> getKtMap(List<K> leftKeys, Function<List<K>, List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey)
    {
        List<T> entities = byLeftFriendshipKeyData.apply(leftKeys);

        // 把右表转化成 Map 右表的关系键作为 key
        Map<K, T> map = entities.stream().collect(HashMap::new, (m, entity) -> m.put(rightFriendshipKey.apply(entity), entity), HashMap::putAll);
        log.debug("子查询数据：{}", map);
        return map;
    }

    /**
     * 左表把右表的一些数据合并进来
     *
     * @param left                    左表
     * @param leftFriendshipKey       左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey      右表的关系键
     * @param leftSetRight            自定义左表是如何合并右表的数据
     * @param copy                    针对右表与左表相同的字段，右表的字段值是否覆盖左表字段值. 如果为 false，ignoreProperties 字段就失去意义
     * @param ignoreProperties        自定义哪些字段不参与字段值的覆盖运算，copy 为 true 才有效
     * @param <E>                     左表元素的类型
     * @param <K>                     键的类型
     * @param <T>                     右表元素的类型
     */
    public static <E, K, T> void join(List<E> left, Function<E, K> leftFriendshipKey, Function<List<K>,
            List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey, @Nullable BiConsumer<E, T> leftSetRight, boolean copy, String... ignoreProperties)
    {
        Assert.notNull(left, "left must not be null");
        Assert.notNull(byLeftFriendshipKeyData, "ByForeignKeyFindData must not be null");
        Assert.notNull(rightFriendshipKey, "PrimaryKey must not be null");
        Assert.notNull(leftFriendshipKey, "ForeignKey must not be null");
        if (!copy && leftSetRight == null)
        {
            throw new IllegalArgumentException("copy 为 false 时，leftSetRight 不能为 null，该方法将失去意义");
        }

        // 统计的数据, ids 不会收集到 null 值
        if (left.isEmpty())
        {
            return;
        }
        List<K> ids = StreamUtil.collected(left, leftFriendshipKey, true);

        Map<K, T> map = getKtMap(ids, byLeftFriendshipKeyData, rightFriendshipKey);

        // 将 entities.id 和  this.content. 指定的外键 合并
        left.forEach(target -> {
            K foreignId = leftFriendshipKey.apply(target); // 主表的关联字段 (外键)
            T source = map.get(foreignId); // 根据外键从从表中查
            if (source != null)
            {
                // copy = false , leftSetRight = null
                if (copy)
                {
                    copyProperties(source, target, ignoreProperties);
                }
                if (leftSetRight != null)
                {
                    leftSetRight.accept(target, source);
                }
            }
            else
            {
                log.info("没有什么数据跟 {} 合并 ", target);
            }
        });
    }

    /**
     * 模型：<br>
     * 部门表 对 员工表 <br>
     * 定义：部门表为【左表】; 员工表为为【右表】<br>
     * 【员工表引用了部门表的id】这个id，称之为【关系键】<br>
     * <p>
     * 左表把右表的一些数据合并进来 <br>
     * oneToManyJoin, one：相当于左表的一条记录，Many：对应右表的多条记录
     *
     * @param left                    左表
     * @param leftFriendshipKey       左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey      右表的关系键
     * @param leftSetRight            自定义左表是如何合并右表的数据
     * @param <E>                     左表元素的类型
     * @param <K>                     键的类型
     * @param <B>                     右表元素的类型
     */
    public static <E, K, B> void oneToManyJoin(List<E> left, Function<E, K> leftFriendshipKey, Function<List<K>,
            List<B>> byLeftFriendshipKeyData, Function<B, K> rightFriendshipKey, BiConsumer<E, List<B>> leftSetRight)
    {
        Assert.notNull(leftFriendshipKey, "leftFriendshipKey must not be null");
        Assert.notNull(byLeftFriendshipKeyData, "byLeftFriendshipKeyData must not be null");
        Assert.notNull(rightFriendshipKey, "rightFriendshipKey must not be null");

        List<K> ids = StreamUtil.collected(left, leftFriendshipKey, true);
        Map<K, List<B>> classifyB = byLeftFriendshipKeyData.apply(ids).stream().collect(Collectors.groupingBy(rightFriendshipKey));

        left.forEach(s -> {
            K k = leftFriendshipKey.apply(s);
            List<B> employees = classifyB.get(k);
            if (employees == null)
            { // 当前分组的成员, 如果为 null 那么, 给他一个空集合
                employees = new ArrayList<>();
            }
            leftSetRight.accept(s, employees);
        });
    }

    /**
     * 讲源对象浅复制到目标对象。如果目标字段标记了类型转换器 @Convert，就依据其规则转换
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 指定不参与复制的字段名称
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties)
    {
        BeanUtils.copyProperties(source,target,ignoreProperties);
        Converter.fit(source,target);
    }
}
