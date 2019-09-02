package org.fastquery.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TreeStructure2 {
	
	/**
	 
	模拟一些数据
	 
	 id, parentID, name
	 
	 1，0	顶级1
	 2  1  	顶级1>1
	 3  2  	顶级1>1>1
	 4  3  	顶级1>1>1>1
	 
	 5	0	顶级2
	 6	5 	顶级2>1
	 7	5 	顶级2>2
	 8	5 	顶级2>3
	 
	 9 	 7	顶级2>2>1
	 10  7	顶级2>2>2
	 11  7	顶级2>2>3
	 */
	
	private static List<CommonGroupTreeVO> allDataItem = new ArrayList<>();
	
	static {
		CommonGroupTreeVO v1 = new CommonGroupTreeVO(1, 0, "顶级1");
		CommonGroupTreeVO v2 = new CommonGroupTreeVO(2, 1, "顶级1>1");
		CommonGroupTreeVO v3 = new CommonGroupTreeVO(3, 2, "顶级1>1>1");
		CommonGroupTreeVO v4 = new CommonGroupTreeVO(4, 3, "顶级1>1>1>1");
		CommonGroupTreeVO v5 = new CommonGroupTreeVO(5, 0, "顶级2");
		CommonGroupTreeVO v6 = new CommonGroupTreeVO(6, 5, "顶级2>1");
		CommonGroupTreeVO v7 = new CommonGroupTreeVO(7, 5, "顶级2>2");
		CommonGroupTreeVO v8 = new CommonGroupTreeVO(8, 5, "顶级2>3");
		CommonGroupTreeVO v9 = new CommonGroupTreeVO(9, 7, "顶级2>2>1");
		CommonGroupTreeVO v10 = new CommonGroupTreeVO(10, 7, "顶级2>2>2");
		CommonGroupTreeVO v11 = new CommonGroupTreeVO(11, 7, "顶级2>2>3");
		
		allDataItem.add(v1);
		allDataItem.add(v2);
		allDataItem.add(v3);
		allDataItem.add(v4);
		allDataItem.add(v5);
		allDataItem.add(v6);
		allDataItem.add(v7);
		allDataItem.add(v8);
		allDataItem.add(v9);
		allDataItem.add(v10);
		allDataItem.add(v11);
	}

	
	public static void main(String[] args) {
		List<CommonGroupTreeVO> rootDataItem = new ArrayList<CommonGroupTreeVO>();
		List<CommonGroupTreeVO> rootList = allDataItem.stream().filter(p -> p.getParentID().equals(0))
				.collect(Collectors.toList());
		rootList.forEach(item -> {
			item.setChildren(getChild(item.getId(), allDataItem));
			if (item.getChildren().size() == 0) {
				item.setChildren(null);
			}
			rootDataItem.add(item);
		});
		
		
		JSONArray json = (JSONArray) JSON.toJSON(rootDataItem);
		System.out.println(JSON.toJSONString(json, true));
		
	}
	
	private static List<CommonGroupTreeVO> getChild(int parentId, List<CommonGroupTreeVO> allDataItem) {

		List<CommonGroupTreeVO> childrenList = allDataItem.stream().filter(p -> p.getParentID().equals(parentId))
				.collect(Collectors.toList());
		// 子节点
		List<CommonGroupTreeVO> childList = new ArrayList<CommonGroupTreeVO>();
		childrenList.forEach(item -> {
			item.setChildren(getChild(item.getId(), allDataItem));
			if (item.getChildren().size() == 0) {
				item.setChildren(null);
			}
			childList.add(item);
		});
		return childList;
	}
	
	private static class CommonGroupTreeVO {
		private Integer id;
		private Integer parentID;
		private String name;
		
		private List<CommonGroupTreeVO> children;

		public List<CommonGroupTreeVO> getChildren() {
			return children;
		}
		public void setChildren(List<CommonGroupTreeVO> children) {
			this.children = children;
		}
		
		public CommonGroupTreeVO(Integer id, Integer parentID, String name) {
			this.id = id;
			this.parentID = parentID;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public Integer getParentID() {
			return parentID;
		}

		public String getName() {
			return name;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public void setParentID(Integer parentID) {
			this.parentID = parentID;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		
		
	}
}







































































