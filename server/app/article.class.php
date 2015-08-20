<?php
class Article
{
	var $id;
	var $title;
	var $description;
	var $thumbnail;
	var $content;
	var $submitDate;
	var $type;
	var $url;
	
    //php5构造函数
    function __construct($id,$title,$description,$thumbnail,$content,$submitDate,$type,$url)
    {
    	$this->id = $id;
        $this->title = $title;
		$this->description = $description;
		$this->thumbnail = $thumbnail;
		$this->content = $content;
		$this->submitDate = $submitDate;
		$this->type = $type;
		$this->url = $url;
    }

    function Article($id,$title,$description,$thumbnail,$content,$submitDate,$type,$url)
    {
        $this->__construct($id,$title,$description,$thumbnail,$content,$submitDate,$type,$url);
    }

	

}