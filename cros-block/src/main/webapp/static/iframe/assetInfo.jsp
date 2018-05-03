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
	table{
		margin-top: 30px;
	}
	#title{
		position: relative;
		top:20px;
		width: 96px;
		height: 36px;
		border-radius: 3px;
		background-color: rgba(54, 130, 239, 1);
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
</style>
</head>
<body>
<center>
	<br>
	<h4>资产详情</h4>
	<br>
	<hr>
	<form action="">
		<table style="width: 600px;height: 300px" >
			<tr><td>资产名称：</td><td><input type="text" value="北京西郊别墅"></td></tr>
			<tr><td>资产估值：</td><td><input type="text" value="6000">万人民币</td></tr>
			<tr><td>资产凭证：</td><td><img style="width: 150px;height: 75px" alt="凭证" src="static/img/bieshu.png">
									<img style="width: 150px;height: 75px" alt="凭证" src="static/img/bieshu.png">
									<img style="width: 150px;height: 75px" alt="凭证" src="static/img/bieshu.png"></td></tr>
			<tr><td></td><td><input id="upLoad" type="file" hidden=true /></td></tr>
		</table>
		<input id="title" type="submit" value="提交修改">
	</form>
</center>
</body>
<script type="text/javascript">
	$("img").click(function (){
		$("#upLoad").click();
		$("#upLoad").show();
	})
</script>
</html>