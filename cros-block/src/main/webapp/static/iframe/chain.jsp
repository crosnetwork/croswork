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
		top:7%;
		left: 10%;
	}
	#a2{
		position: absolute;
		top:7%;
		left: 30%;
	}
	#a3{
		position: absolute;
		top:7%;
		left:50%;
	}
	#a4{
		position: absolute;
		top:7%;
		left: 70%;
	}
	#a5{
		position: absolute;
		top:42%;
		left: 70%;
	}
	#a6{
		position: absolute;
		top:42%;
		left: 50%;
	}
	#a7{
		position: absolute;
		top:42%;
		left: 30%;
	}
	#a8{
		position: absolute;
		top:42%;
		left: 10%;
	}
	#a9{
		position: absolute;
		top:77%;
		left: 10%;
	}
	#a10{
		position: absolute;
		top:77%;
		left: 30%;
	}
	#a11{
		position: absolute;
		top:77%;
		left: 50%;
	}
	#a12{
		position: absolute;
		top:77%;
		left: 70%;
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
		<a href="<%=path %>/static/iframe/sideChain.jsp" style="color: white;" >&nbsp;&nbsp;&nbsp;一键建链</a>
	</div>
	<div id="block"></div>
    <img src="<%=path %>/static/img/publicChain.png" width="85%">
	<a id="a1" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/49.png"></a>
	<a id="a2" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/50.png"></a>
	<a id="a3" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/51.png"></a>
	<a id="a4" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/52.png"></a>
	<a id="a5" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/53.png"></a>
	<a id="a6" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/54.png"></a>
	<a id="a7" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/55.png"></a>
	<a id="a8" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/56.png"></a>
	<a id="a9" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/57.png"></a>
	<a id="a10" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/58.png"></a>
	<a id="a11" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/59.png"></a>
	<a id="a12" href="<%=path %>/static/iframe/chainList.jsp" title="区块列表"><img class="num" src="<%=path %>/static/img/60.png"></a>
</body>
</html>