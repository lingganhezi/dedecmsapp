<?php
require_once (dirname(__FILE__) . '/../include/common.inc.php');
require_once (dirname(__FILE__) . '/result.class.php');
require_once (dirname(__FILE__) . '/article.class.php');
require_once(DEDEINC.'/arc.archives.class.php');
if(empty($action))$action = '';

if($action == 'getList'){
	global $cfg_basehost;

	if(empty($aid)){
		$where = "";
	}else{
		$where = " AND id < '$aid'";
	}
	//arcrank = 0 审核过的
    $sql = "SELECT id,title,description, litpic FROM #@__archives where arcrank = '0' $where ORDER BY id desc limit 0,20";
	$dsql ->SetQuery($sql);
	$dsql ->Execute();
    $articles = array();
	while($row = $dsql -> GetArray())
    {
    	$articleData = new Archives($row['id']);
		$content;
		$article = new Article($articleData->ArcID,
			$articleData->Fields['title'],
			$articleData->Fields['description'],
			$cfg_basehost.$articleData->Fields['litpic'],
			$content,
			$articleData->Fields['pubdate'],
			$articleData->Fields['channel'],//这里的type 是 channel属性
			$cfg_basehost.$articleData-> GetTrueUrl());
        array_push($articles,$article);
    }
    echo json_encode($articles);
	exit();
}
echo json_encode(array());
?>
