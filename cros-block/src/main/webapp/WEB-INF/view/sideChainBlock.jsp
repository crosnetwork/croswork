<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html >
<html lang="en">
<head>
	<base href="<%=basePath %>">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" charset="UTF-8"></meta>
    <title>个人中心</title>
    <link rel="stylesheet" href="css/reset.css" />
    <link rel="stylesheet" href="css/animate.css" />
    <link rel="stylesheet" href="css/mainChain.css" />
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript"src="static/vue/vue.min.js"></script> 
	<script type="text/javascript"src="static/iview/iview.min.js"></script>	
<style>
		#vue-object>div{
			background:#EFF3F6;
			width:100%;
			height:100vh;
		}
		.int-contract-l{
			width:240px;
			height:100vh;
			background:white;
		}
		.l-header{
			padding-top:30px;
		}
		.l-header img{
			display:block;
			width:100px;
			height:100px;
			border-radius:50%;
			margin:0 auto;
		}
		.l-header p{
			width:100px;
			margin:10px auto;
			font-family:"微软雅黑"
		}
		.user-name{
			font-size:18px;
			font-weight:bold;
			line-height:26px;
		}
		.user-wallet{
			
		}
	</style>
</head>
<body>
	<div id="vue-object">
		<div class="clearfix">
			<div class="int-contract-l fl">
				<div class="l-header">
					<img src="<%=request.getContextPath()%>/static/img/Day.jpg" alt="头像" />
					<p class="user-name">Day,你好！</p>
					<p class="user-wallet">cros:&nbsp;<strong>900000</strong></p>
				</div>
			</div>
			<div id="content">
				<iframe src="<%=request.getContextPath()%>/static/iframe/sideChain.jsp" frameborder="0"  style="width: 100%;height: 100%"/>
			</div>
		</div>		
	</div>
	
	<script>

		

	</script>
	
</body>
</html>