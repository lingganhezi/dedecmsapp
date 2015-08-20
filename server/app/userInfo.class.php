<?php

class UserInfo
{
    var $id = 0;
    var $name = '';
	var $email;
	var $protrait;
	var $birthday;
	var $city;
	var $description;
	var $sex;
	
    //php5构造函数
    function __construct($id,$name,$email,$protrait,$birthday,$city,$description,$sex)
    {
    	$this->id = $id;
        $this->name = $name;
		$this->email = $email;
		$this->protrait = $protrait;
		$this->birthday = $birthday;
		$this->city = $city;
		$this->description = $description;
		$this->sex = $sex;
    }

    function UserInfo($id,$name,$email,$protrait,$birthday,$city,$description,$sex)
    {
        $this->__construct($id,$name,$email,$protrait,$birthday,$city,$description,$sex);
    }
}