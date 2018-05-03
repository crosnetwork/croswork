<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<base href="<%=basePath %>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>public key</title>
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/icon.css">
	<script type="text/javascript" src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript" src="static/easyUi/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="static/easyUi/locale/easyui-lang-zh_CN.js"></script>
<style type="text/css">
 *{
 	margin: 0;
 	padding: 0;
 }
</style>
</head>
<body>
<center>
	<table id="dg" style="width:100%;"></table>
	<div id="tb">
	 	<center>
	 		<input type="text" id="searnum" name="name" style="width:166px;height:20px;line-height:35px;" placeholder="搜索侧链"/>
	 	</center>
	</div>
</center>
</body>
<script type="text/javascript">

$('#dg').datagrid({
	url:"data/chain.json",
/* 	pagination:true, */
	rownumbers:true,
    singleSelect:false,
    autoRowHeight:true,
    fitColumns:true,
    striped:true,
    checkOnSelect:true,
    selectOnCheck:true,
    pageSize:10,
    toolbar:'#tb',
	columns:[[
		{field:'id',title:'chainId',hidden:true},
		{field:'parentHash',title:'公链Hash',width:200},
		{field:'blockHash',title:'侧链Hash',hidden:true},
		{field:'blockName',title:'侧链名称',width:200},
		{field:'operations',align:'center',formatter: function(value,rowData,index){
			return "<button id='update' onclick='update("+rowData.id+")'>生成账号</button>"
			}},
    ]]
})
//模糊查询
var	getSearch = function(){
		var name=$("#searnum").val();
		$('#dg').datagrid('load', {
			"name":name,
		});
	}
</script>
</html>