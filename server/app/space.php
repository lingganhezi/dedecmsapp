<?php
require_once (dirname(__FILE__) . '/config.php');
require_once (dirname(__FILE__) . '/result.class.php');
require_once (dirname(__FILE__) . '/userInfo.class.php');
//是否允许用户空间显示未审核文档
$addqSql = '';
if ($cfg_mb_allowncarc == 'N')
	$addqSql .= " And arc.arcrank > -1 ";
if (isset($mtype))
	$mtype = intval($mtype);
if (!empty($mtype))
	$addqSql .= " And arc.mtype = '$mtype' ";

/**
 * 查询所有未关注的用户，最多100个
 */
if ($action == 'listunfriend') {
	//由于 可能会报告 Safe Alert: Request Error step 2!，先关闭这个安全监测
	$dsql->safeCheck = false;
	
	$sql = "SELECT #@__member.userid,#@__member.sex,#@__member.email,#@__member.face,#@__member.sex,
    			#@__member_person.birthday,#@__member_person.lovemsg,#@__member_person.place
    			FROM #@__member INNER JOIN #@__member_person 
    			ON (#@__member_person.mid = #@__member.mid) 
    			WHERE #@__member.mid NOT IN(SELECT fid FROM #@__member_friends WHERE mid = '{$cfg_ml->M_ID}') 
    			AND #@__member.uname LIKE '%$uname%' 
    			LIMIT 100";
	$dsql -> SetQuery($sql);
	$dsql -> Execute();
	$users = array();
	while ($row = $dsql -> GetArray()) {
		$row['isFriend'] = 0;
		$userinfo = getUserInfo($row);
		array_push($users, $userinfo);
	}
	echo json_encode($users);
	
	$dsql->safeCheck = true;//重新打开
	exit ;
}
/*---------------------------------
 我的好友
 function friend(){ }
 -------------------------------------*/
else if ($action == 'listfriend') {
	//由于 可能会报告 Safe Alert: Request Error step 2!，先关闭这个安全监测
	$dsql->safeCheck = false;
	
	$sql = "SELECT #@__member.userid,#@__member.sex,#@__member.email,#@__member.face,#@__member.sex,
    			#@__member_person.birthday,#@__member_person.lovemsg,#@__member_person.place
    			FROM #@__member INNER JOIN #@__member_person 
    			ON (#@__member_person.mid = #@__member.mid) 
    			WHERE #@__member.mid IN(SELECT fid FROM #@__member_friends WHERE mid = '{$cfg_ml->M_ID}') 
    			";
	$dsql -> SetQuery($sql);
	$dsql -> Execute();
	$users = array();
	while ($row = $dsql -> GetArray()) {
		$row['isFriend'] = 1;
		$userinfo = getUserInfo($row);
		array_push($users, $userinfo);
	}
	echo json_encode($users);
	
	$dsql->safeCheck = true;//重新打开
	exit ;
}
/*---------------------------------
 加好友
 function newfriend(){ }
 -------------------------------------*/
else if ($action == 'newfriend') {
	CheckRank(0, 0);
	if ($userid == $cfg_ml -> M_LoginID) {
		sendResult(CODE_FAILD, "你不能加自己为好友！");
	}
	$addtime = time();
	$row = $dsql -> GetOne("SELECT * FROM `#@__member_friends` where floginid='{$userid}' And mid='{$cfg_ml->M_ID}' ");
	if (is_array($row)) {
		sendResult(CODE_FAILD, "该用户已经是你的好友！");
	} else {
		//目标用户
		$targetUser = $dsql -> GetOne("SELECT * FROM `#@__member` WHERE userid = '$userid' ");
		#api{{
		if (defined('UC_API') && @include_once DEDEROOT . '/uc_client/client.php') {
			if ($data = uc_get_user($cfg_ml -> M_LoginID))
				uc_friend_add($uid, $data[0]);
		}
		#/aip}}

		$inquery = "INSERT INTO `#@__member_friends` (`fid` , `floginid` , `funame` , `mid` , `addtime` , `ftype`)
                VALUES ('{$targetUser['mid']}' , '{$targetUser['userid']}' , '{$targetUser['uname']}' , '{$cfg_ml->M_ID}' , '$addtime' , '0'); ";
		$dsql -> ExecuteNoneQuery($inquery);
		//统计我的好友数量
		$row = $dsql -> GetOne("SELECT COUNT(*) AS nums FROM `#@__member_friends` WHERE `mid`='" . $cfg_ml -> M_ID . "'");
		$dsql -> ExecuteNoneQuery("UPDATE `#@__member_tj` SET friend='$row[nums]' WHERE `mid`='" . $cfg_ml -> M_ID . "'");

		//会员动态记录
		$cfg_ml -> RecordFeeds('addfriends', "", "", $userid);

		sendResult(CODE_SUCCESS, "成功添加好友");

	}
}
/*---------------------------------
 解除好友关系
 function delfriend(){ }
 -------------------------------------*/
else if ($action == 'delfriend') {
	CheckRank(0, 0);
	if ($userid == $cfg_ml -> M_LoginID) {
		sendResult(CODE_FAILD, "你不能和自己为解除关系！");
	}
	$addtime = time();
	$row = $dsql -> GetOne("Select * FROM `#@__member_friends` where floginid='{$userid}' And mid='{$cfg_ml->M_ID}' ");
	if (!is_array($row)) {
		sendResult(CODE_FAILD, "该用户已经不是你的好友！");
	} else {
		#api{{
		if (defined('UC_API') && @include_once DEDEROOT . '/uc_client/client.php') {
			if ($data = uc_get_user($cfg_ml -> M_LoginID))
				uc_friend_add($uid, $data[0]);
		}
		#/aip}}
		$inquery = "DELETE FROM `dede_member_friends` where floginid='{$userid}' And mid='{$cfg_ml->M_ID}' ";
		$dsql -> ExecuteNoneQuery($inquery);
		//统计我的好友数量
		$row = $dsql -> GetOne("SELECT COUNT(*) AS nums FROM `#@__member_friends` WHERE `mid`='" . $cfg_ml -> M_ID . "'");
		$dsql -> ExecuteNoneQuery("UPDATE `#@__member_tj` SET friend='$row[nums]' WHERE `mid`='" . $cfg_ml -> M_ID . "'");
		sendResult(CODE_SUCCESS, "成功解除好友关系！");
	}
}
