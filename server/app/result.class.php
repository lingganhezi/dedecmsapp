<?php

define('CODE_FAILD',0);
define('CODE_SUCCESS',1);

function sendResult($code,$msg,$data){
	echo json_encode(new Result($code,$msg,$data));
	exit();
}

class Result
{
    var $stateCode = CODE_FAILD;
    var $message = '';
	var $data;
	
    //php5构造函数
    function __construct($code = 0,$msg='',$data)
    {
    	$this->stateCode = $code;
        $this->message = $msg;
		$this->data = $data;
    }

    function Result($code,$msg,$data)
    {
        $this->__construct($code,$msg,$data);
    }

	function setResult($code,$msg,$data){
		$this->stateCode = $code;
        $this->message = $msg;
		$this->data = $data;
	}

}