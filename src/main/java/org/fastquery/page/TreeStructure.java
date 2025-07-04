package org.fastquery.page;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 树形结构工具类
 *
 */
@UtilityClass
public class TreeStructure
{
    /**
     * 将任意属于 Collection 集合类型的集合，转换成树结构
     *
     * @param <E>                 集合的项, 它是一个可变类型
     * @param <R>                 任意继承自 Collection 的集合类型
     * @param newInstanceSupplier 创建 R 实例的方式，用于放新的树形结构
     * @param topLevelParentId    顶级项的父级 id
     * @param items               准备转换成树的集合
     * @param getIdFun            获取项 id 逻辑
     * @param getParentIDFun      获取项 parentID 逻辑
     * @param storageChildrenFun  存储子集逻辑
     * @return 树结构
     */
    public static <E, R extends Collection<E>, K> R toTree(R items, Supplier<R> newInstanceSupplier, K topLevelParentId, Function<E, K> getIdFun, Function<E, K> getParentIDFun, BiConsumer<E, R> storageChildrenFun)
    {
        return iterate(topLevelParentId, items, newInstanceSupplier, getIdFun, getParentIDFun, storageChildrenFun);
    }

    private static <E, R extends Collection<E>, K> R getChildren(K pid, R items, Supplier<R> newInstanceSupplier, Function<E, K> getIDFun, Function<E, K> getParentIDFun, BiConsumer<E, R> storageChildrenFun)
    {
        return iterate(pid, items, newInstanceSupplier, getIDFun, getParentIDFun, storageChildrenFun);
    }

    private static <E, R extends Collection<E>, K> R iterate(K pid, R items, Supplier<R> newInstanceSupplier, Function<E, K> getIDFun, Function<E, K> getParentIDFun,
                                                             BiConsumer<E, R> storageChildrenFun)
    {
        R collection = newInstanceSupplier.get();
        items.stream().filter(item -> { // 过滤
            K parentID = getParentIDFun.apply(item);
            return parentID.equals(pid);
        }).forEach(item -> { // 转换成 collection 后遍历
            K id = getIDFun.apply(item);
            storageChildrenFun.accept(item, getChildren(id, items, newInstanceSupplier, getIDFun, getParentIDFun, storageChildrenFun));
            collection.add(item);
        });

        return collection;
    }
}