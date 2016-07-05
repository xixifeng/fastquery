/*
 * Copyright (c) 2016-2016, fastquery.org and/or its affiliates. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For more information, please see http://www.fastquery.org/.
 * 
 */

package org.fastquery.sqlbuilder;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.NotCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Example {

	// create default schema
	DbSpec spec = new DbSpec();
	DbSchema schema = spec.addDefaultSchema();

	// 设计顾客表
	// add table with basic customer info
	DbTable customerTable = schema.addTable("customer");
	DbColumn custIdCol = customerTable.addColumn("cust_id", "number", null);
	DbColumn custNameCol = customerTable.addColumn("name", "varchar", 255);

	// 设计订单表
	// add order table with basic order info
	DbTable orderTable = schema.addTable("order");
	DbColumn orderIdCol = orderTable.addColumn("order_id", "number", null);
	DbColumn orderCustIdCol = orderTable.addColumn("cust_id", "number", null);
	DbColumn orderTotalCol = orderTable.addColumn("total", "number", null);
	DbColumn orderDateCol = orderTable.addColumn("order_date", "timestamp", null);

	// 增加关联
	// add a join from the customer table to the order table (on cust_id)
	DbJoin custOrderJoin = spec.addJoin(null, "customer", null, "order", "cust_id");

	// 构建创建表语句
	@Test
	public void createCustomerTable() {
		String createCustomerTable = new CreateTableQuery(customerTable, true).validate().toString();
		// 打印出来分析下
		System.out.println(createCustomerTable);
		assertThat(createCustomerTable,
				equalToIgnoringCase("CREATE TABLE customer (cust_id number,name varchar(255))"));
	}
	
	// 构建创建表语句
	@Test
	public void createOrderTable() {
		String createOrderTable = new CreateTableQuery(orderTable, true).validate().toString();
		System.out.println(createOrderTable);
		assertThat(createOrderTable, equalToIgnoringCase("CREATE TABLE order (order_id number,cust_id number,total number,order_date timestamp)"));
	}
	
	@Test
	public void insert1(){
	    String insertCustomerQuery = new InsertQuery(customerTable)
	    	      .addColumn(custIdCol, 1) 
	    	      .addColumn(custNameCol, "bob")
	    	      .validate().toString();
	    	    System.out.println(insertCustomerQuery);
	    	    assertThat(insertCustomerQuery, equalToIgnoringCase("INSERT INTO customer (cust_id,name) VALUES (1,'bob')"));
	}
	
	@Test
	public void insert2(){
		 
	    String preparedInsertCustomerQuery =
	      new InsertQuery(customerTable)
	      .addPreparedColumns(custIdCol, custNameCol)
	      .validate().toString();
	    System.out.println(preparedInsertCustomerQuery);
	    assertThat(preparedInsertCustomerQuery, equalToIgnoringCase("INSERT INTO customer (cust_id,name) VALUES (?,?)"));
	}
	
	@Test
	public void insert3(){
		// JdbcEscape.timestamp(new Date()) --> "2016-06-29 14:50:21.05"
	    String insertOrderQuery =
	    	      new InsertQuery(orderTable)
	    	      .addColumn(orderIdCol, 37)
	    	      .addColumn(orderCustIdCol, 1)
	    	      .addColumn(orderTotalCol, 37.56)
	    	      .addColumn(orderDateCol, "2016-06-29 14:50:21.05")
	    	      .validate().toString();
	    	    System.out.println(insertOrderQuery);
	    	    assertThat(insertOrderQuery, equalToIgnoringCase("INSERT INTO order (order_id,cust_id,total,order_date) VALUES (37,1,37.56,'2016-06-29 14:50:21.05')"));
	}

	@Test
	public void query1(){
	    // find a customer name by id
	    String query1 =
	      new SelectQuery()
	      .addColumns(custNameCol)
	      // 增加一个条件
	      .addCondition(BinaryCondition.equalTo(custIdCol, 1))
	      .validate().toString();
	    System.out.println(query1);
	    assertThat(query1, equalToIgnoringCase("SELECT t0.name FROM customer t0 WHERE (t0.cust_id = 1)"));
	}
	
	@Test
	public void query2(){
	    // find all the orders for a customer, given name, order by date
	    String query2 =
	      new SelectQuery()
	      .addAllTableColumns(orderTable)
	      .addJoins(SelectQuery.JoinType.INNER, custOrderJoin)
	      .addCondition(BinaryCondition.equalTo(custNameCol, "bob"))
	      .addOrderings(orderDateCol)
	      .validate().toString();
	    System.out.println(query2);
	    assertThat(query2, equalToIgnoringCase("SELECT t1.* FROM customer t0 INNER JOIN order t1 ON (t0.cust_id = t1.cust_id) WHERE (t0.name = 'bob') ORDER BY t1.order_date"));
	}
	
	@Test
	public void query3(){
		
		// find the totals of all orders for people named bob who spent over $100
	    // this year, grouped by name
		
		// JdbcEscape.date(new Date(108, 0, 1)) -> '2008-01-01'
	    String query3 =
	    	      new SelectQuery()
	    	      .addCustomColumns(
	    	          custNameCol,
	    	          FunctionCall.sum().addColumnParams(orderTotalCol))
	    	      .addJoins(SelectQuery.JoinType.INNER, custOrderJoin)
	    	      .addCondition(BinaryCondition.like(custNameCol, "%bob%"))
	    	      // (t1.order_date >= '2008-01-01')
	    	      .addCondition(BinaryCondition.greaterThan(
	    	                        orderDateCol,"2008-01-01", true))
	    	      .addGroupings(custNameCol)
	    	      // (SUM(t1.total) > 100)
	    	      .addHaving(BinaryCondition.greaterThan(
	    	                     FunctionCall.sum().addColumnParams(orderTotalCol),
	    	                     100, false))
	    	      .validate().toString();
	    	    System.out.println(query3);
	    	    // => SELECT t0.name,SUM(t1.total)
	    	    //      FROM customer t0 INNER JOIN order t1 ON (t0.cust_id = t1.cust_id)
	    	    //      WHERE ((t0.name LIKE '%bob%') AND (t1.order_date >= {d '2008-01-01'}))
	    	    //      GROUP BY t0.name
	    	    //      HAVING (SUM(t1.total) > 100)
	    	    assertThat(query3, equalToIgnoringCase("SELECT t0.name,SUM(t1.total) FROM customer t0 INNER JOIN order t1 ON (t0.cust_id = t1.cust_id) WHERE ((t0.name LIKE '%bob%') AND (t1.order_date >= '2008-01-01')) GROUP BY t0.name HAVING (SUM(t1.total) > 100)"));
	}
	
	@Test
	public void customQuery1(){
        // find addresses for customers from PA,NJ,DE from table:
        //   address(cust_id, street, city, state, zip)
        String customQuery1 =
          new SelectQuery()
          .addCustomColumns(
              custNameCol,
              new CustomSql("a1.street"),
              new CustomSql("a1.city"),
              new CustomSql("a1.state"),
              new CustomSql("a1.zip"))
          .addCustomJoin(SelectQuery.JoinType.INNER, customerTable,
                         "address a1",
                         BinaryCondition.equalTo(custIdCol,
                                                 new CustomSql("a1.cust_id")))
          .addCondition(new InCondition("a1.state",
                                        "PA", "NJ", "DE"))
          .validate().toString();
        System.out.println(customQuery1);
     
        // => SELECT t0.name,a1.street,a1.city,a1.state,a1.zip
        //      FROM customer t0 INNER JOIN address a1 ON (t0.cust_id = a1.cust_id)
        //      WHERE ('a1.state' IN ('PA','NJ','DE') )
        assertThat(customQuery1, equalToIgnoringCase("SELECT t0.name,a1.street,a1.city,a1.state,a1.zip FROM customer t0 INNER JOIN address a1 ON (t0.cust_id = a1.cust_id) WHERE ('a1.state' IN ('PA','NJ','DE') )"));
	}
	
	// 一元条件
	// '?1' IS NOT NULL
	@Test
	public void unaryCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(UnaryCondition.isNotNull("?1"));
	    System.out.println(selectQuery.validate().toString()); // SELECT t0.name FROM customer t0 WHERE (t0.cust_id = 1)
	}
	
	// 二元条件
	@Test
	public void binaryCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(BinaryCondition.equalTo(custIdCol, 1));
	    System.out.println(selectQuery.validate().toString()); // SELECT t0.name FROM customer t0 WHERE (t0.cust_id = 1)
	}
	
	
	// 组合条件
	// where ((t0.name = '?1') and (t0.cust_id = '?2'))
	@Test
	public void comboCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(ComboCondition.and(BinaryCondition.equalTo(custNameCol, "?1"),BinaryCondition.equalTo(custIdCol, "?2")));
	    System.out.println(selectQuery.validate().toString()); 
	}
	
	// 自定义条件
	@Test
	public void customCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(new CustomCondition("im really snazzy"));
	    System.out.println(selectQuery.validate().toString()); 
	}
	
	// in 条件
	// 'cust_id' IN (1,2,3)
	@Test
	public void inCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(new InCondition("cust_id", 1,2,3));
	    System.out.println(selectQuery.validate().toString()); 
	}
	
	// Not 条件
	// NOT (t0.cust_id = 1)
	@Test
	public void notCondition(){
		SelectQuery selectQuery = new SelectQuery();
		// 增加列
		selectQuery.addColumns(custNameCol);
		// 增加条件
		selectQuery.addCondition(new NotCondition(BinaryCondition.equalTo(custIdCol, 1)));
	    System.out.println(selectQuery.validate().toString()); 
	}
	
	@Test
	public void comboCondition2(){
		
		// select * from student as s LEFT JOIN sc on sc.studentNo=s.no LEFT JOIN course as c on c.no=sc.courseNo;
		
		SelectQuery selectQuery = new SelectQuery();
		// 待查找的表
		selectQuery.addCustomFromTable("Student as s "); 
		// 待查找的列
		selectQuery.addCustomColumns("no,name,sex,age,dept");
		
		// 可以根据传递进来的参数进行增减条件
		// 增加条件
		selectQuery.addCondition(ComboCondition.and(BinaryCondition.equalTo("abc", "?1"),BinaryCondition.equalTo("bcd", "?2")));
		// 再增加条件
		selectQuery.addCondition(new CustomCondition("cust_id IN (1,2,3) )"));
		
		selectQuery.addCustomJoin("LEFT JOIN sc on sc.studentNo=s.no LEFT JOIN course as c on c.no=sc.courseNo");
		
		
		// 获取条件
		System.out.println("条件: "+selectQuery.getWhereClause());
		// 获取条件 End
		
		
		String sql = selectQuery.validate().toString();
		
		// 把''换成"
		sql = sql.replace("''", "\"");
		// 把 ' 换成空字符串
		sql = sql.replace("'", "");
		// 再把 " 换成 '
		sql = sql.replace("\"", "'");

	    System.out.println(sql); 
	}
}






































