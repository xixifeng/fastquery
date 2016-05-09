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

package org.fastquery.example;

import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author 
 */
public interface DataAcquireDbService extends QueryRepository{
	
	@Query("select id from ParkCardInLog order by id desc limit 1 ")
	JSONObject findTheLastestParkingInLog();
	
	@Query("select id from ParkCardOutLog order by id desc limit 1")
	JSONObject findTheLastestParkingOutLog();

	@Query("select sum(totalCount) as totalCount,sum(emptyCount) as emptyCount  from ParkGarage")
	JSONObject findParkLots();
	
	@Query("select isOnLine from device where isOnLine=0")
	JSONArray isDevicesOffline();
	
	@Query("select i.id,i.plateNum,i.inTime,i.cardTypeId,i.carTypeId,i.realName,i.state,d.dName from parkcardinlog i left join device d on i.controlId=d.tempId order by id desc")
	JSONArray findAllParkingInLog();
	
	@Query("select o.id,o.plateNum,o.inTime,o.outTime,o.cardTypeId,o.carTypeId,o.realName,o.state,d.dName from parkcardoutlog o left join device d on o.controlId=d.id order by id desc")
	JSONArray findAllParkingOutLog();
	
	@Query("select c.inTime,c.chargeTime,c.cardTypeId,c.carTypeId,c.plateNum,c.money,c.saleMoney,c.vipMoney,c.chargeMoney,c.feeType,c.passType,c.realName,d.dname from parkcardchargelog c left join device d on c.cpcId=d.id order by c.id desc")
	JSONArray findAllChargeLog();
	
	@Query("select * from (select id,inTime as outintime,cardTypeId,plateNum,0 as inouttype from ParkCardInLog union all select id,outTime as outintime,cardTypeId,plateNum,1 as inouttype from ParkCardOutLog ) d order by d.id desc")
	JSONArray findAllParkingData();
	
}
