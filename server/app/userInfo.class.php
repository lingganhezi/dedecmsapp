<?php

class UserInfo {
	var $id = 0;
	var $name = '';
	var $email;
	var $protrait;
	var $birthday;
	var $city;
	var $description;
	var $sex;
	var $isFriend = 0;
	
	//php5构造函数
	function __construct($id, $name, $email, $protrait, $birthday, $city, $description, $sex,$isFriend) {
		$this -> id = $id;
		$this -> name = $name;
		$this -> email = $email;
		$this -> protrait = $protrait;
		$this -> birthday = $birthday;
		$this -> city = $city;
		$this -> description = $description;
		$this -> sex = $sex;
		if(empty($isFriend)){
			$isFriend = 0;
		}
		$this -> isFriend = $isFriend;
	}

	function UserInfo($id, $name, $email, $protrait, $birthday, $city, $description, $sex,$isFriend) {
		$this -> __construct($id, $name, $email, $protrait, $birthday, $city, $description, $sex,$isFriend);
	}

}

/***
 * 根据 数据库查询得到的$row 来生成UserInfo
 */
function getUserInfo($row) {
	global $cfg_basehost;
	//性别处理
	switch($row['sex']) {
		case '男' :
			$row['sex'] = 0;
			break;
		case '女' :
			$row['sex'] = 1;
			break;
		case '保密' :
			$row['sex'] = -1;
			break;
		default:
			$row['sex'] = -1;
			break;
	}

	$userinfo = new UserInfo($row['userid'], $row['userid'], $row['email'], $cfg_basehost . $row['face'], $row['birthday'], $row['place'], $row['lovemsg'], $row['sex'],$row['isFriend']);
	return $userinfo;
}
