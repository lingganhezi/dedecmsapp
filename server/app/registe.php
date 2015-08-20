<?php
require_once (dirname(__FILE__) . '/../include/common.inc.php');
require_once (dirname(__FILE__) . '/result.class.php');
require_once DEDEINC . '/membermodel.cls.php';

$result = new Result();

if ($cfg_mb_allowreg == 'N') {
	sendResult(CODE_FAILD, "服务器后台关闭了注册功能");
}

if(strlen($email)>36){
	sendResult(CODE_FAILD, "你的注册邮箱名字长度超过限制，最长为36位字符");
}

$username = $email;
$username = trim($username);
$password = trim($password);
$userid = $username;

if (!CheckEmail($email)) {
	sendResult(CODE_FAILD, "Email格式不正确");
}

//检测用户名是否存在
$row = $dsql -> GetOne("SELECT mid FROM `#@__member` WHERE userid LIKE '$userid' ");
if (is_array($row)) {
	sendResult(CODE_FAILD, "此用户已经注册过");
}

//会员的默认金币
$dfscores = 0;
$dfmoney = 0;
$dfrank = $dsql -> GetOne("SELECT money,scores FROM `#@__arcrank` WHERE rank='10' ");
if (is_array($dfrank)) {
	$dfmoney = $dfrank['money'];
	$dfscores = $dfrank['scores'];
}
$jointime = time();
$logintime = time();
$joinip = GetIP();
$loginip = GetIP();
$pwd = md5($password);
$mtype = '个人';
$safeanswer = HtmlReplace($safeanswer);
$safequestion = HtmlReplace($safequestion);
$sex = '男';
$spaceSta = ($cfg_mb_spacesta < 0 ? $cfg_mb_spacesta : 0);
//头像
$face = "/app/avatar.png";

$inQuery = "INSERT INTO `#@__member` (`mtype` ,`userid` ,`pwd` ,`uname` ,`sex` ,`rank` ,`money` ,`email` ,`scores` ,
        `matt`, `spacesta` ,`face`,`safequestion`,`safeanswer` ,`jointime` ,`joinip` ,`logintime` ,`loginip` )
       VALUES ('$mtype','$userid','$pwd','$username','$sex','10','$dfmoney','$email','$dfscores',
       '0','$spaceSta','$face','$safequestion','$safeanswer','$jointime','$joinip','$logintime','$loginip'); ";
if ($dsql -> ExecuteNoneQuery($inQuery)) {
	$mid = $dsql -> GetLastID();

	//写入默认会员详细资料
	if ($mtype == '个人') {
		$space = 'person';
	} else if ($mtype == '企业') {
		$space = 'company';
	} else {
		$space = 'person';
	}

	//写入默认统计数据
	$membertjquery = "INSERT INTO `#@__member_tj` (`mid`,`article`,`album`,`archives`,`homecount`,`pagecount`,`feedback`,`friend`,`stow`)
                   VALUES ('$mid','0','0','0','0','0','0','0','0'); ";
	$dsql -> ExecuteNoneQuery($membertjquery);

	//写入默认空间配置数据
	$spacequery = "INSERT INTO `#@__member_space`(`mid` ,`pagesize` ,`matt` ,`spacename` ,`spacelogo` ,`spacestyle`, `sign` ,`spacenews`)
                    VALUES('{$mid}','10','0','{$uname}的空间','','$space','',''); ";
	$dsql -> ExecuteNoneQuery($spacequery);

	//写入其它默认数据
	$dsql -> ExecuteNoneQuery("INSERT INTO `#@__member_flink`(mid,title,url) VALUES('$mid','',''); ");

	$membermodel = new membermodel($mtype);
	$modid = $membermodel -> modid;
	$modid = empty($modid) ? 0 : intval(preg_replace("/[^\d]/", '', $modid));
	$modelform = $dsql -> getOne("SELECT * FROM #@__member_model WHERE id='$modid' ");

	if (!is_array($modelform)) {
		sendResult(CODE_FAILD, "注册失败，服务器后台 模型表单不存在");
	} else {
		$dsql -> ExecuteNoneQuery("INSERT INTO `{$membermodel->table}` (`mid`) VALUES ('{$mid}');");
	}

} else {
	sendResult(CODE_FAILD, "注册失败");
}

sendResult(CODE_SUCCESS, "注册成功");
?>