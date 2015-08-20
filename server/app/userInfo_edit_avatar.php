<?php
/**
 * 上传头像
 */
require_once (dirname(__FILE__) . '/../include/common.inc.php');
require_once (dirname(__FILE__) . '/result.class.php');

function FileUploads($upname, $handname, $userMid = 0, $utype = 'image', $exname = '', $maxwidth = 0, $maxheight = 0, $water = false, $isadmin = false) {
	global $cfg_imgtype, $cfg_mb_addontype, $cfg_mediatype, $cfg_user_dir, $cfg_basedir, $cfg_dir_purview;

	//当为游客投稿的情况下，这个 id 为 0
	if (empty($userMid))
		$userMid = 0;
	if (!is_dir($cfg_basedir . $cfg_user_dir . "/$userMid")) {
		MkdirAll($cfg_basedir . $cfg_user_dir . "/$userMid", $cfg_dir_purview);
		CloseFtp();
	}
	//有上传文件
	$allAllowType = str_replace('||', '|', $cfg_imgtype . '|' . $cfg_mediatype . '|' . $cfg_mb_addontype);
	if (!empty($GLOBALS[$upname]) && is_uploaded_file($GLOBALS[$upname])) {
		$nowtme = time();

		$GLOBALS[$upname . '_name'] = trim(preg_replace("#[ \r\n\t\*\%\\\/\?><\|\":]{1,}#", '', $GLOBALS[$upname . '_name']));
		//源文件类型检查
		if ($utype == 'image') {
			if (!preg_match("/\.(" . $cfg_imgtype . ")$/", $GLOBALS[$upname . '_name'])) {
				sendResult(CODE_FAILD,"你所上传的图片类型不在许可列表，请上传{$cfg_imgtype}类型！");
			}
			$sparr = Array("image/pjpeg", "image/jpeg", "image/gif", "image/png", "image/xpng", "image/wbmp");
			$imgfile_type = strtolower(trim($GLOBALS[$upname . '_type']));
			if (!in_array($imgfile_type, $sparr)) {
				sendResult(CODE_FAILD,'上传的图片格式错误，请使用JPEG、GIF、PNG、WBMP格式的其中一种！');
			}
		} else if ($utype == 'flash' && !preg_match("/\.swf$/", $GLOBALS[$upname . '_name'])) {
			endResult(CODE_FAILD,'上传的文件必须为flash文件！');
		} else if ($utype == 'media' && !preg_match("/\.(" . $cfg_mediatype . ")$/", $GLOBALS[$upname . '_name'])) {
			endResult(CODE_FAILD,'你所上传的文件类型必须为：' . $cfg_mediatype);
		} else if (!preg_match("/\.(" . $allAllowType . ")$/", $GLOBALS[$upname . '_name'])) {
			endResult(CODE_FAILD,"你所上传的文件类型不被允许！");
		}
		//再次严格检测文件扩展名是否符合系统定义的类型
		$fs = explode('.', $GLOBALS[$upname . '_name']);
		$sname = $fs[count($fs) - 1];
		$alltypes = explode('|', $allAllowType);
		if (!in_array(strtolower($sname), $alltypes)) {
			endResult(CODE_FAILD,'你所上传的文件类型不被允许！');
		}
		//强制禁止的文件类型
		if (preg_match("/(asp|php|pl|cgi|shtm|js)$/", $sname)) {
			endResult(CODE_FAILD,'你上传的文件为系统禁止的类型！');
		}
		if ($exname == '') {
			$filename = $cfg_user_dir . "/$userMid/" . dd2char($nowtme . '-' . mt_rand(1000, 9999)) . '.' . $sname;
		} else {
			$filename = $cfg_user_dir . "/{$userMid}/{$exname}." . $sname;
		}
		move_uploaded_file($GLOBALS[$upname], $cfg_basedir . $filename) or die("上传文件到 {$filename} 失败！");
		@unlink($GLOBALS[$upname]);

		if (@filesize($cfg_basedir . $filename) > $GLOBALS['cfg_mb_upload_size'] * 1024) {
			@unlink($cfg_basedir . $filename);
			endResult(CODE_FAILD,'你上传的文件超出系统大小限制！');
			exit();
		}

		//加水印或缩小图片
		if ($utype == 'image') {
			include_once (DEDEINC . '/image.func.php');
			if ($maxwidth > 0 || $maxheight > 0) {
				ImageResize($cfg_basedir . $filename, $maxwidth, $maxheight);
			} else if ($water) {
				WaterImg($cfg_basedir . $filename);
			}
		}
		return $filename;
	}
	//没有上传文件
	else {
		//强制禁止的文件类型
		if ($handname == '') {
			return $handname;
		} else if (preg_match("/\.(asp|php|pl|cgi|shtm|js)$/", $handname)) {
			exit('Not allow filename for not safe!');
		} else if (!preg_match("/\.(" . $allAllowType . ")$/", $handname)) {
			exit('Not allow filename for filetype!');
		}
		// 2011-4-10 修复会员中心修改相册时候错误(by:jason123j)
		else if (!preg_match('#^http:#', $handname) && !preg_match('#^' . $cfg_user_dir . '/' . $userMid . "#", $handname) && !$isadmin) {
			exit('Not allow filename for not userdir!');
		}
		return $handname;
	}
}

///////start ..........

if (empty($userid)) {
	sendResult(CODE_FAILD, '没有指定userid');
}

//TODO 验证用户有效性 使用当前登录用户

global $db;
$sql = "SELECT mid FROM #@__member WHERE userid = '$userid'";
$row = $db -> GetOne($sql);
if (!is_array($row)) {
	sendResult(CODE_FAILD, '找不到这个用户');
} else {
	$mid = $row['mid'];
}

$userdir = $cfg_user_dir . '/' . $mid;

if ($action == 'save') {
	$maxlength = 1000 * 1024;
	//最大 不超过 1M
	if (!preg_match("#^" . $userdir . "#", $oldface)) {
		$oldface = '';
	}
	
	if (is_uploaded_file($face)) {
		if (@filesize($_FILES['face']['tmp_name']) > $maxlength) {
			sendResult(CODE_FAILD, "你上传的头像文件超过了系统限制大小：{$cfg_max_face} K！");
		}
		//删除旧图片（防止文件扩展名不同，如：原来的是gif，后来的是jpg）
		if (preg_match("#\.(jpg|gif|png)$#i", $oldface) && file_exists($cfg_basedir . $oldface)) {
			@unlink($cfg_basedir . $oldface);
		}
		//上传新工图片
		$face = FileUploads('face', $oldface, $mid, 'image', time(), 200, 200);
	} else {
		$face = $oldface;
	}
	$query = "UPDATE `#@__member` SET `face` = '$face' WHERE mid='{$mid}' ";
	$dsql -> ExecuteNoneQuery($query);
	// 清除缓存
	//$cfg_ml->DelCache($mid);
	$data['avatar'] = $cfg_basehost . $face;
	sendResult(CODE_SUCCESS, "上传成功", $data);
} else if ($action == 'delold') {
	if (empty($oldface)) {
		sendResult(CODE_FAILD, "没有头像可以删除");
	}

	if (!preg_match("#^" . $userdir . "#", $oldface) || preg_match('#\.\.#', $oldface)) {
		$oldface = '';
	}
	if (preg_match("#\.(jpg|gif|png)$#i", $oldface) && file_exists($cfg_basedir . $oldface)) {
		@unlink($cfg_basedir . $oldface);
	}

	$defaultAvatar = "/app/avatar.png";
	$query = "UPDATE `#@__member` SET `face` = '$defaultAvatar' WHERE mid='{$mid}' ";
	$dsql -> ExecuteNoneQuery($query);
	// 清除缓存
	//$cfg_ml->DelCache($mid);
	sendResult(CODE_SUCCESS, "已还原头像");
}
?>