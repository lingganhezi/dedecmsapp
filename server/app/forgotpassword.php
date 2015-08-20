<?php
/**
 * 密码重设
 */
require_once(dirname(__FILE__)."/config.php");
require_once (dirname(__FILE__) . "/inc_pwd_functions.php");
require_once (dirname(__FILE__) . '/result.class.php');
$result = new Result();
$userid = $email;
//验证邮箱，用户名
if (!preg_match("#(.*)@(.*)\.(.*)#", $email)) {
	$result -> setResult(CODE_FAILD, "Email格式不正确");
	echo json_encode($result);
	exit();
}

$member = member($email, $userid);

//判断系统邮件服务是否开启
if ($cfg_sendmail_bysmtp == "Y") {
	//发送新邮件；
    sn($member['mid'], $userid, $member['email']);
} else {
	$result -> setResult(CODE_FAILD, "服务器 邮件服务没有启动");
	echo json_encode($result);
	exit();
}


?>