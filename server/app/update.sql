#更新数据库结构用

#修改userid最长到36
ALTER TABLE `dede_member` CHANGE `userid` `userid` CHAR(36) DEFAULT '' NOT NULL;
ALTER TABLE `dede_member_pms` CHANGE `floginid` `floginid` VARCHAR(36) CHARACTER DEFAULT '' NOT NULL, CHANGE `tologinid` `tologinid` CHAR(36) CHARACTER DEFAULT '' NOT NULL;

#添加  消息会话表 dede_member_pms_session
CREATE TABLE `reled`.`dede_member_pms_session`(     `formid` INT ,     `toid` INT ,     `floginid` VARCHAR(255) ,     `tologinid` VARCHAR(255) ,     `id` INT NOT NULL AUTO_INCREMENT,     `lasttime` INT ,     PRIMARY KEY (`id`)  );

#增加pms表 sessionid字段
ALTER TABLE `reled`.`dede_member_pms`     ADD COLUMN `sessionid` INT NULL AFTER `message`;