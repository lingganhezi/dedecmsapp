<?php

class Message {
	var $floginid;
	var $fromid;
	var $toid;
	var $tologinid;
	var $folder;
	var $subject;
	var $sendtime;
	var $writetime;
	var $hasview;
	var $isadmin;
	var $message;
	var $msgid;

	//php5构造函数
	function __construct($id, $floginid, $fromid, $toid, $tologinid, $folder, $subject, $sendtime, $writetime, $hasview, $isadmin, $message) {
		$this -> floginid = $floginid;
		$this -> fromid = $fromid;
		$this -> toid = $toid;
		$this -> tologinid = $tologinid;
		$this -> folder = $folder;
		$this -> subject = $subject;
		$this -> sendtime = $sendtime;
		$this -> writetime = $writetime;
		$this -> hasview = $hasview;
		$this -> isadmin = $isadmin;
		$this -> message = $message;
		$this -> msgid = $id;
	}

	function Message($id, $floginid, $fromid, $toid, $tologinid, $folder, $subject, $sendtime, $writetime, $hasview, $isadmin, $message) {
		$this -> __construct($id, $floginid, $fromid, $toid, $tologinid, $folder, $subject, $sendtime, $writetime, $hasview, $isadmin, $message);
	}

}
