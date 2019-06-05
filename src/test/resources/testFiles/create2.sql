-- 如果存在就先删除
$[0]
drop 
$[1] if exists 
$[2]
;
# 创建表
CREATE TABLE 
demo_$[3] (
	id 
	int 
	primary key $[4] NOT
	NULL
)

;DROP TABLE
demo_table $[5];

