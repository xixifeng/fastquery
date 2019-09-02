package org.fastquery.test;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构工具类
 * 
 * @author mei.sir
 */
public class TreeStructure {
	
	
	/**
	 * 将任意属于Collection集合类型的集合，转换成树结构
	 * 
	 * @author mei.sir
	 * @param <ITEM> 集合的项,它是一个可变类型
	 * @param <R> 任意继承自Collection的集合类型
	 * @param items 项集合
	 * @param getIDFun 获取项id逻辑
	 * @param getParentIDFun 获取项parentID逻辑
	 * @param storageChildrenFun 存储子集逻辑
	 * 
	 * @return 树结构
	 */
	public static <ITEM,R extends Collection<ITEM>> R toListTree(R items, Function<ITEM, Integer> getIDFun,Function<ITEM, Integer> getParentIDFun, BiConsumer<ITEM, Collection<ITEM>> storageChildrenFun) {
		return interate(0, items, getIDFun, getParentIDFun, storageChildrenFun);
	}
	
	/**
	 * 将任意属于Collection集合类型的树形集合，遍历出来
	 * @param <ITEM> 集合的项,它是一个可变类型
	 * @param <R> 任意继承自Collection的集合类型
	 * @param items 项集合
	 * @param getChildrenFun 获取 children 逻辑
	 * @param consumerItem 消费项数据
	 */
	public static <ITEM,R extends Collection<ITEM>> void interateTree(R items, Function<ITEM, R> getChildrenFun, Consumer<ITEM> consumerItem) {
		items.forEach(item -> {
			consumerItem.accept(item);
			interateTree(getChildrenFun.apply(item),getChildrenFun,consumerItem);
		});
	}


	private static <ITEM,R extends Collection<ITEM>> R getChildren(int pid, R items,Function<ITEM, Integer> getIDFun,Function<ITEM, Integer> getParentIDFun,BiConsumer<ITEM,Collection<ITEM>> storageChildrenFun) {
		return interate(pid, items, getIDFun, getParentIDFun, storageChildrenFun);
	}
	
	@SuppressWarnings("unchecked")
	private static <ITEM,R extends Collection<ITEM>> R interate(int pid, R items, Function<ITEM, Integer> getIDFun, Function<ITEM, Integer> getParentIDFun,
			BiConsumer<ITEM, Collection<ITEM>> storageChildrenFun) {
		
		R collection = null;
		try {
			collection = (R) items.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(items.getClass() + " 不能实例化对象",e);
		}		
		R list = collection;		
		items.stream().filter(item -> { // 过滤 
			Integer parentID = getParentIDFun.apply(item);
			return parentID.equals(pid);
		}).collect(Collectors.toList()).forEach(item -> { // 转换成list后遍历
			Integer id = getIDFun.apply(item);
			storageChildrenFun.accept(item, getChildren(id, items, getIDFun, getParentIDFun,storageChildrenFun));
			list.add(item);
		});
		
		return list;
	}
}







































































