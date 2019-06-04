# COMMENT1
DELETE FROM `product` WHERE `pid` = 1 and `lid` = 2;
DELETE FROM `product` WHERE `pid` = 2 and `lid` = 1;
-- COMMENT2
DELETE FROM `product` WHERE `pid` = 1 and `lid` = 3;
INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (1,2,'電風扇','效果很好');
INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (2,1,'杯子','喝茶使用');
INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (1,3,'電腦','寫代碼');
DELETE FROM `product` WHERE `pid` = 1 and `lid` = 2;
-- #COMMENT3
DELETE FROM `product` WHERE `pid` = 2 and `lid` = 1;
DELETE FROM `product` WHERE `pid` = 1 and `lid` = 3;


update `UserInfo` set 
`name` = case `id` when 77 then '茝若' when 88 then '芸兮' when 99 then '梓' else `name` end,
`age` = case `id` when 77 then '18' when 99 then '16' else `age` end
where `id` in(77,88,99,66);