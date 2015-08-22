<?php
require_once(dirname(__FILE__)."/config.php");
require_once (dirname(__FILE__) . '/result.class.php');
require_once (dirname(__FILE__) . '/userInfo.class.php');
require_once (DEDEINC . '/userlogin.class.php');
if(empty($action))$action = '';

if ($action == 'get') {
	global $cfg_basehost;
	if(empty($userid)){
		sendResult(CODE_FAILD,'没有指定userid');
	}
	global $db;
    $sql = "SELECT #@__member.userid,#@__member.sex,#@__member.email,#@__member.face,#@__member.sex,
    			#@__member_person.birthday,#@__member_person.lovemsg,#@__member_person.place
    			FROM #@__member INNER JOIN #@__member_person 
    			ON (#@__member_person.mid = #@__member.mid) 
    			WHERE #@__member.userid = '$userid'";
    $row = $db->GetOne($sql);
	
	//getisfriend
	$friendquery = "SELECT id FROM #@__member_friends WHERE mid='{$cfg_ml->M_ID}' AND floginid = '$userid'";
	$friends = $db->GetOne($friendquery);
	if(is_array($friends)){
		$row['isFriend'] = 1;
	}else{
		$row['isFriend'] = 0;
	}
	
    if(!is_array($row)){
    	sendResult(CODE_FAILD,'找不到这个用户');
	}else{
		$userinfo = getUserInfo($row);
			
		sendResult(CODE_SUCCESS,'',$userinfo);
	}
}
//保存
else if ($action == 'save') {
	//检查登陆
	if(!$cfg_ml->IsLogin()){
		sendResult(CODE_FAILD, "你还没登陆");
	}
	
	$mid = $cfg_ml->M_ID;
	$updateMemberSql = "update #@__member set ";
	$updateMemberPersonSql = "update #@__member_person set ";;
	
	//开始检查字段
	if(!empty($birthday)){
		$updateMemberPersonSql.="#@__member_person.birthday = '$birthday',";
	}
	
	if(!empty($uname)){
		$updateMemberSql.="#@__member.uname = '$uname',";
		$updateMemberPersonSql.="#@__member_person.uname = '$uname',";
	}
	
	if(!empty($sex)){
		switch($sex){
			case 0:
				$sex ='男';
				break;
			case 1:
				$sex ='女';
				break;
			case -1:
				$sex ='保密';
				break;
		}
		$updateMemberSql.="#@__member.sex = '$sex',";
		$updateMemberPersonSql.="#@__member_person.sex = '$sex',";
	}
	
	if(!empty($place)){
		$updateMemberPersonSql.="#@__member_person.place = '$place',";
	}
	
	if(!empty($description)){
		$updateMemberPersonSql.="#@__member_person.lovemsg = '$description',";
	}
	
	$where = " where mid = '$mid'";
	$updateMemberSql = substr($updateMemberSql,0,-1).$where;
	$updateMemberPersonSql = substr($updateMemberPersonSql,0,-1).$where;
	
	$dsql -> ExecuteNoneQuery($updateMemberSql);
	$dsql -> ExecuteNoneQuery($updateMemberPersonSql);
	
	sendResult(CODE_SUCCESS, "修改成功");
}
?>
