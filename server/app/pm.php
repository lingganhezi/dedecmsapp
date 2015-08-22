<?php
require_once (dirname(__FILE__) . "/config.php");
require_once (dirname(__FILE__) . "/message.class.php");

/**
 * 获取 messageid
 */
function getMessageSession($floginid,$tologinid,$formid,$toid){
	if(empty($sessionid)){
		global $dsql;
		$sql = "INSERT INTO `#@__member_pms_session` set floginid = '$floginid' ,tologinid = '$tologinid' ,formid = '$formid' ,toid = '$toid';";
		$dsql -> ExecuteNoneQuery($sql);
		return $dsql->GetLastID();
	}else{
		return $sessionid;
	}
}

CheckRank(0, 0);
$menutype = 'mydede';
$menutype_son = 'pm';
$id = isset($id) ? intval($id) : 0;
if ($cfg_mb_lit == 'Y') {
	sendResult(CODE_FAILD, '由于系统开启了精简版会员空间，你不能向其它会员发短信息，不过你可以向他留言！');
}

#api{{
if (defined('UC_API') && @include_once DEDEROOT . '/uc_client/client.php') {
	if ($data = uc_get_user($cfg_ml -> M_LoginID))
		uc_pm_location($data[0]);
}
#/aip}}

if (!isset($action)) {
	$action = '';
}
//检查用户是否被禁言
CheckNotAllow();
$state = (empty($state)) ? "" : $state;
/*--------------------
 function __send(){  }
 ----------------------*/
if ($action == 'friends') {
	/** 好友记录 **/
	$sql = "SELECT * FROM `#@__member_friends` WHERE  mid='{$cfg_ml->M_ID}' AND ftype!='-1'  ORDER BY addtime DESC LIMIT 20";
	$friends = array();
	$dsql -> SetQuery($sql);
	$dsql -> Execute();
	while ($row = $dsql -> GetArray()) {
		$friends[] = $row;
	}

	include_once (dirname(__FILE__) . '/templets/pm-send.htm');
	exit();
}
/*-----------------------
 function __read(){  }
 ----------------------*/
else if ($action == 'read') {
	$sql = "SELECT * FROM `#@__member_friends` WHERE  mid='{$cfg_ml->M_ID}' AND ftype!='-1'  ORDER BY addtime DESC LIMIT 20";
	$friends = array();
	$dsql -> SetQuery($sql);
	$dsql -> Execute();
	while ($row = $dsql -> GetArray()) {
		$friends[] = $row;
	}
	$row = $dsql -> GetOne("SELECT * FROM `#@__member_pms` WHERE id='$id' AND (fromid='{$cfg_ml->M_ID}' OR toid='{$cfg_ml->M_ID}')");
	if (!is_array($row)) {
		sendResult(CODE_FAILD, '对不起，你指定的消息不存在或你没权限查看！');
	}
	$dsql -> ExecuteNoneQuery("UPDATE `#@__member_pms` SET hasview=1 WHERE id='$id' AND folder='inbox' AND toid='{$cfg_ml->M_ID}'");
	$dsql -> ExecuteNoneQuery("UPDATE `#@__member_pms` SET hasview=1 WHERE folder='outbox' AND toid='{$cfg_ml->M_ID}'");
	include_once (dirname(__FILE__) . '/templets/pm-read.htm');
	exit();
}
/*-----------------------
 function __savesend(){  }
 ----------------------*/
else if ($action == 'send') {
//	if ($subject == '') {
//		sendResult(CODE_FAILD, "请填写信息标题!");
//	}
	$subject = '';
	$msg = CheckUserID($msgtoid, "用户名", false);
	if ($msg != 'ok') {
		sendResult(CODE_FAILD, "此用户不接受消息");
	}
	$row = $dsql -> GetOne("SELECT * FROM `#@__member` WHERE userid LIKE '$msgtoid' ");
	if (!is_array($row)) {
		sendResult(CODE_FAILD, "你指定的用户不存在,不能发送信息!");
	}
	$subject = cn_substrR(HtmlReplace($subject, 1), 60);
	$message = cn_substrR(HtmlReplace($message, 0), 1024);
	$sendtime = $writetime = time();

	//发给收件人(收件人可管理)
	$inquery1 = "INSERT INTO `#@__member_pms` (`floginid`,`fromid`,`toid`,`tologinid`,`folder`,`subject`,`sendtime`,`writetime`,`hasview`,`isadmin`,`message`)
      VALUES ('{$cfg_ml->M_LoginID}','{$cfg_ml->M_ID}','{$row['mid']}','{$row['userid']}','inbox','$subject','$sendtime','$writetime','0','0','$message'); ";

	//保留到自己的发件箱(自己可管理)
	$inquery2 = "INSERT INTO `#@__member_pms` (`floginid`,`fromid`,`toid`,`tologinid`,`folder`,`subject`,`sendtime`,`writetime`,`hasview`,`isadmin`,`message`)
      VALUES ('{$cfg_ml->M_LoginID}','{$cfg_ml->M_ID}','{$row['mid']}','{$row['userid']}','outbox','$subject','$sendtime','$writetime','0','0','$message'); ";
	$dsql -> ExecuteNoneQuery($inquery1);
	$dsql -> ExecuteNoneQuery($inquery2);
	
	
	$lastid = $dsql->GetLastID();
	$query = "SELECT * FROM `#@__member_pms` WHERE `id`=$lastid ";
	$row = $dsql->GetOne($query);
	$msgData = new Message($row['id'], $row['floginid'], $row['fromid'], $row['toid'], $row['tologinid'], $row['folder'], $row['subject'], $row['sendtime'], $row['writetime'], $row['hasview'], $row['isadmin'], $row['message']);
	
	sendResult(CODE_SUCCESS, "成功发送一条信息!",$msgData);
}
/*-----------------------
 function __del(){  }
 ----------------------*/
else if ($action == 'del') {
	if(!empty($sessionid)){
		
	}
	
	$ids = preg_replace("#[^0-9,]#", "", $ids);
	if ($folder == 'inbox') {
		$boxsql = "SELECT * FROM `#@__member_pms` WHERE id IN($ids) AND folder LIKE 'inbox' AND toid='{$cfg_ml->M_ID}'";
		$dsql -> SetQuery($boxsql);
		$dsql -> Execute();
		$query = '';
		while ($row = $dsql -> GetArray()) {
			if ($row && $row['isadmin'] == 1) {
				$query = "Update `#@__member_pms` set writetime='0' WHERE id='{$row['id']}' AND folder='inbox' AND toid='{$cfg_ml->M_ID}' AND isadmin='1';";
				$dsql -> ExecuteNoneQuery($query);
			} else {
				$query = "DELETE FROM `#@__member_pms` WHERE id in($ids) AND toid='{$cfg_ml->M_ID}' AND folder LIKE 'inbox'";
			}
		}
	} else if ($folder == 'outbox') {
		$query = "Delete From `#@__member_pms` WHERE id in($ids) AND fromid='{$cfg_ml->M_ID}' AND folder LIKE 'outbox' ";
	} else {
		$query = "Delete From `#@__member_pms` WHERE id in($ids) AND fromid='{$cfg_ml->M_ID}' Or toid='{$cfg_ml->M_ID}' AND folder LIKE 'outbox' Or (folder LIKE 'inbox' AND hasview='0')";
	}
	$dsql -> ExecuteNoneQuery($query);
	sendResult(CODE_SUCCESS, "成功删除指定的消息!");
}
else if ($action == 'list') {

	$wsql = '';
	if ($folder == 'outbox') {
		$wsql = " `fromid`='{$cfg_ml->M_ID}' AND folder LIKE 'outbox' ";
		$tname = "发件箱";
	} elseif ($folder == 'inbox') {
		$query = "SELECT * FROM `#@__member_pms` WHERE folder LIKE 'outbox' AND isadmin='1'";
		$dsql -> SetQuery($query);
		$dsql -> Execute();
		while ($row = $dsql -> GetArray()) {
			$row2 = $dsql -> GetOne("SELECT * FROM `#@__member_pms` WHERE fromid = '$row[id]' AND toid='{$cfg_ml->M_ID}'");
			if (!is_array($row2)) {
				$row3 = "INSERT INTO
                `#@__member_pms` (`floginid`,`fromid`,`toid`,`tologinid`,`folder`,`subject`,`sendtime`,`writetime`,`hasview`,`isadmin`,`message`)
                VALUES ('admin','{$row['id']}','{$cfg_ml->M_ID}','{$cfg_ml->M_LoginID}','inbox','{$row['subject']}','{$row['sendtime']}','{$row['writetime']}','{$row['hasview']}','{$row['isadmin']}','{$row['message']}')";
				$dsql -> ExecuteNoneQuery($row3);
			}
		}
		if ($state == "1") {
			$wsql = " toid='{$cfg_ml->M_ID}' AND folder='inbox' AND writetime!='' and hasview=1";
			$tname = "收件箱";
		} else if ($state == "-1") {
			$wsql = "toid='{$cfg_ml->M_ID}' AND folder='inbox' AND writetime!='' and hasview=0";
			$tname = "收件箱";
		} else {
			$wsql = " toid='{$cfg_ml->M_ID}' AND folder='inbox' AND writetime!=''";
			$tname = "收件箱";
		}
	} else {
		$wsql = " (`fromid` ='{$cfg_ml->M_ID}' AND folder LIKE 'outbox') OR (`toid` = '{$cfg_ml->M_ID}' AND folder LIKE 'inbox') ";
	}

	if(!empty($msgid)){
		$wsql .= " AND id < '$msgid'";
	}
	
	//是否全部查询
	if(empty($all)){
		$litmit = " LIMIT 0,20 ";
	}
	
	$query = "SELECT * FROM `#@__member_pms` WHERE $wsql ORDER BY sendtime DESC ".$litmit;
	//查询
	$dsql -> SetQuery($query);
	$dsql -> Execute();
	$messageList = array();
	while ($row = $dsql -> GetArray()) {
		$message = new Message($row['id'], $row['floginid'], $row['fromid'], $row['toid'], $row['tologinid'], $row['folder'], $row['subject'], $row['sendtime'], $row['writetime'], $row['hasview'], $row['isadmin'], $row['message']);
		array_push($messageList, $message);
	}
	echo json_encode($messageList);
}
