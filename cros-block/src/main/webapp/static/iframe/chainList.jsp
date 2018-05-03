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
<title>chain list</title>
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
 #title{
		position: relative;
		left: 600px;
		top:20px;
		width: 96px;
		height: 36px;
		border-radius: 3px;
		background-color: rgba(54, 130, 239, 1);
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
#input{
	 position:relative;
	 top:20px;
	 left:100px;
	 float:left;
}
a{
	text-decoration: none;
}
#tb{
	height: 70px;
	background-color: #E5F3FF;
}
</style>
</head>
<body>
<center>
	<table id="dg" style="width:100%;"></table>
	<div id="tb">
		<div id="input">
			<input type="text" id="searnum" name="name" style="width:166px;height:35px;border-radius: 15px;background-color:#E5F3FF" placeholder="&nbsp;&nbsp;搜索区块"/>
		</div>
	 	<div id="title" style="vertical-align: middle;display: table-cell;">	
			<a href="javascript:void(0)" style="color: white;" onclick="toToken()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;发行Token</a>
		</div>
	</div>
</center>
</body>
<script type="text/javascript">

	function toToken(){
		window.top.$("#token").click();
	}

$('#dg').datagrid({
	url:"data/chainlist.json",
/* 	pagination:true, */
	rownumbers:false,
    singleSelect:false,
    autoRowHeight:false,
    fitColumns:true,
    striped:false,
    checkOnSelect:true,
    selectOnCheck:true,
    pageSize:10,
    toolbar:'#tb',
    rowStyler: function() {
    	return 'height: 35px';
    	},
	columns:[[
		{field:'id',title:'chainId',hidden:true},
		{field:'blockHight',title:'高度',align:'center',width:200,styler : function(value, row, index) {  
            return 'border:0;vertical-align:middle;background-color:#fffff;';  
        }  },
		{field:'time',title:'区块时间',align:'center',width:200,styler : function(value, row, index) {  
            return 'border:0;vertical-align:middle;background-color:#fffff;';  
        }  },
		{field:'blockNum',title:'区块记录',align:'center',width:200,styler : function(value, row, index) {  
            return 'border:0;vertical-align:middle;background-color:#fffff;';  
        }  },
		{field:'cros',title:'区块消耗',align:'center',width:200,styler : function(value, row, index) {  
            return 'border:0;vertical-align:middle;background-color:#fffff;';  
        }  },
		{field:'operations',align:'center',formatter: function(value,rowData,index){
			return "<a href='javascript:void(0)' id='update' onclick='update("+rowData.id+")'><font size='5'>></font></a>"
			},styler : function(value, row, index) {  
                return 'border:0;vertical-align:middle;background-color:#fffff;';  
            }  },
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