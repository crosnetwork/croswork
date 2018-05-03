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
	h3{
		position: relative;
		top:30px;
		right: 400px;
	}
	
	#title{
		position: relative;
		left:400px;
		width: 100px;
		height: 30px;
		border-radius: 3px;
		background-color: green;
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
	a{
		text-decoration: none;
	}
	table{
		margin-top: 10px;
	}
	hr{
		width: 800px;
	}
	h4{
	  position: relative;
	  right: 300px;
	  top: 10px;
	}
	span{
	  position: relative;
	  bottom: 15px;
	  text-align: left;
	}
</style>
</head>
<body>
<center>
<h3>Token交易</h3>
<div id="title" style="vertical-align: middle;display: table-cell;">
	<a href="javascript:void(0)" style="color: white;font-size: small;">&nbsp;关闭详情</a>
</div>
<br>

<hr style="width: 900px">
	<h4>txHash:</h4><span>1Pyk9mqKugHvkfpYt2563uDaZbD5T6Ux5o</span>
	<hr>
	<h4>事件：</h4><span>Token交易</span>
	<hr>
	<h4>状态：</h4><span>成功</span>
	<hr>
	<h4>时间：</h4><span>2018-04-30</span>
	<hr>
	<h4>CROS消耗：</h4><span>30</span>
	<hr>
	<h4>Token合约地址：</h4><span>1Pyk9mqKugHvkfpYt2563uDaZbD5T6Ux5o</span>
	<hr>
	<h4>Token名称：</h4><span>KO</span>
	<hr>
	<h4>TokenFrom：</h4><span>1Dg7gmb8pbkmMBxFStKKDVBFcp9asSfC4E</span>
	<hr>
	<h4>TokenTo：</h4><span>1JvywFVsjP5M9djqYi8dJxEKBdghshnciR</span>
	<hr>
	<h4>交易总金额：</h4><span>30000KO</span>
	<hr>
	<h4>交易数量：</h4><span>300000</span>
	<hr>
	<h4>交易单价：</h4><span>0.1</span>
	<hr>
</center>
</body>
<script type="text/javascript">
	$("#title").click(function(){
			history.go(-1);
		})
</script>
</html>