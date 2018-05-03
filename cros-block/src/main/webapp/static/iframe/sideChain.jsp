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
<style type="text/css">
body{
	width:100vw;
	height:100vh;
	<%-- background-image: url("<%=path %>/static/img/publicChain.png");
	background-size:100vw 100vh;
	background-repeat: no-repeat; --%>
	}
	*{
		margin: 0;
		padding: 0;
	}
	a{
		text-decoration: none;
	}
	#a1{
		position: absolute;
		top:11%;
		left: 10%;
	}
	#a2{
		position: absolute;
		top:11%;
		left: 33%;
	}
	#a3{
		position: absolute;
		top:11%;
		left:55%;
	}
	#a4{
		position: absolute;
		top:11%;
		left:79%;
	}
	#a5{
		position: absolute;
		top:8%;
		left: 1%;
	}
	#a6{
		position: absolute;
		top:8%;
		left: 25%;
	}
	#a7{
		position: absolute;
		top:8%;
		left: 47%;
	}
	#a8{
		position: absolute;
		top:56%;
		left: 16%;
	}
	#a9{
		position: absolute;
		top:77%;
		left: 10%;
	}
	#a10{
		position: absolute;
		top:26%;
		left: 10%;
	}
	#a11{
		position: absolute;
		top:8%;
		left: 70%;
	}
	#a12{
		position: absolute;
		top:60%;
		left: 10%;
	}
	#title{
		position: relative;
		left: 940px;
		top:20px;
		width: 96px;
		height: 36px;
		border-radius: 3px;
		background-color: rgba(54, 130, 239, 1);
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
	.num{
		width: 180px;
		height: 100px;
	}
</style>
</head>
<body>
	<div id="title" style="vertical-align: middle;display: table-cell;">	
		<a href="javascript:void(0)" style="color: white;" >&nbsp;&nbsp;&nbsp;一键建链</a>
	</div>
	<div id="block"></div>
	<a id="a1" href="<%=path %>/static/iframe/buildSideChain.jsp?num=49" title="一键建链"><img class="num" src="<%=path %>/static/img/49.png"></a>
	<a id="a2" href="<%=path %>/static/iframe/buildSideChain.jsp?num=50" title="一键建链"><img class="num" src="<%=path %>/static/img/50.png"></a>
	<a id="a3" href="<%=path %>/static/iframe/buildSideChain.jsp?num=51" title="一键建链"><img class="num" src="<%=path %>/static/img/51.png"></a>
	<a id="a4" href="<%=path %>/static/iframe/buildSideChain.jsp?num=52" title="一键建链"><img class="num" src="<%=path %>/static/img/52.png"></a>
	<div id="a5" ><img src="<%=path %>/static/img/1049.png"></div>
	<div id="a11"><img src="<%=path %>/static/img/1050.png"></div>
	<div id="a6" ><img src="<%=path %>/static/img/1050.png"></div>
	<div id="a7" ><img src="<%=path %>/static/img/1050.png"></div>
<%-- 	<div id="a10"><img src="<%=path %>/static/img/shulian.png"></div>
	<div id="a12"><img src="<%=path %>/static/img/shulian.png"></div>
	<a id="a8" href="javascript:void(0)" title="清平乡扶贫链"><img class="num" src="<%=path %>/static/img/qingping.png"></a> --%>

</body>
</html>