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
<title>Insert title here</title>
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
		top:380px;
		width: 200px;
		height: 40px;
		border-radius: 3px;
		background-color: rgba(54, 130, 239, 1);
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
	a{
		text-decoration: none;
	}
	table{
		margin-top: 10px;
	}
</style>
</head>
<body>
<center>
	<form action="">
		<table style="width: 400px;height: 300px;">
			<tr><td><b>Token名称</b></td></tr>
			<tr><td><input type="text" style="width: 400px;height: 35px;" border="2px" required="required"/></td></tr>
			<tr><td><b>发行数量</b></td></tr>
			<tr><td><input type="text" style="width: 400px;height: 35px;" border="2px" required="required"/></td></tr>
			<tr><td><b>最小位数</b></td></tr>
			<tr><td><input type="text" style="width: 400px;height: 35px;" border="2px" required="required"/></td></tr>
			<tr><td><b>发行消耗</b></td></tr>
			<tr bgcolor="#F0F8FF"><td style="width: 400px;height: 35px;">&nbsp;30CROS</td></tr>
			<div id="title" style="vertical-align: middle;display: table-cell;">	
				<a onclick="buildToken()" href="javascript:void(0)" style="color: white;" >&nbsp;&nbsp;发行Token</a>
			</div>
		</table>
	</form>	
</center>
</body>
<script type="text/javascript">
	function buildToken(){
		 $("input[type='text']").each(function () {
             if ($(this).val() != "") {
            	 $.messager.progress({msg:'正在发行中...'}); 
             		setTimeout(function(){
             		  $.messager.progress('close');$.messager.confirm('提示','Token发行成功',function(r){
             		  	  if (r){
               		  			history.back(-1);
             		    	}else{
             		    		history.back(-1);
                 		    	}
             	    	});}, 3300);

             }
         })
	}
</script>
</html>