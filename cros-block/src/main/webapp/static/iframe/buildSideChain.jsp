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
		left: 940px;
		top:20px;
		width: 96px;
		height: 36px;
		border-radius: 3px;
		background-color: rgba(54, 130, 239, 1);
		border: 1px solid rgba(0, 0, 0, 0.1);
	}
	table{
		border-collapse:collapse;
	}
	.leftTd{
		width: 200px;
		padding-left: 5px;
	}
	a{
		text-decoration: none;
	}
</style>
</head>
<body>
	<div id="title" style="vertical-align: middle;display: table-cell;">	
		<a href="javascript:void(0)" style="color: white;" id="tijiao" onclick="sure()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;提交</a>
		<a href="javascript:void(0)" style="color: white;" id="queding" hidden=true>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;确定</a>
	</div>
	<br>
	<br>
	<center>
		<table id="addTable" style="width: 800px;height: 150px;" border="1px">
			<tr><td class="leftTd"><b>创世纪区块Hash值</b></td><td>&nbsp;0000000000000000000000000000000000000000000000000000000000000000</td></tr>
			<tr><td class="leftTd"><b>主链区块Hash值</b></td><td></td></tr>
			<tr><td class="leftTd"><b>建链销耗</b></td><td>&nbsp;30CROS</td></tr>
		</table>
		<br>
		<table id="footTable" style="width: 800px;height: 250px;">
		  	<tr><td style="height: 35px;"><b>&nbsp;链接头信息</b></td></tr>
			<tr><td style="width: 800px;height: 35px;"><input type="text" id="headerInfo" style="width: 800px;height: 35px;"></td></tr>
			<tr><td style="height: 35px"><b>&nbsp;描述信息</b></td></tr>
			<tr><td><textarea style="width: 100%;height: 100%" id="description"></textarea></td></tr>
		</table>
	</center>
</body>
<script type="text/javascript">
	$("#queding").click(function(){
			location.href="<%=basePath %>static/iframe/sideChain.jsp";
		})


	function sure(){
		var headerInfo = $("#headerInfo").val();
		var description = $("#description").val();
		var block_name = $("#headerInfo").val();
		var block_description = $("#description").val();
		if(headerInfo=="" || description==""){
			$.messager.alert('提示','请填写有效信息!','warning');
		}else{
			$.messager.confirm('提示','提交将消耗30CROS,确认提交吗?',function(r){
			    if (r){
			    	var tb = $("#addTable");
			    	var ftb = $("#footTable");
			    	var block_name = $("#headerInfo").val();
			    	var block_description = $("#description").val();
					$.ajax({
							url: "build/getHash",
							type: 'POST',
							dataType: 'json',
							data:{"block_name":block_name,"block_description":block_description},
							success: function (data) {
										tb.html("<tr><td class='leftTd'><b>创世纪区块Hash值</b></td><td>&nbsp;&nbsp;0000000000000000000000000000000000000000000000000000000000000000</td></tr>"+
												"<tr><td class='leftTd'><b>主链区块Hash值</b></td><td>&nbsp;"+data.data.txHash+"</td></tr>"+
												"<tr><td class='leftTd'><b>建链销耗</b></td><td>&nbsp;30CROS</td></tr>");
										ftb.html("<tr><td style='height: 35px;'><b>&nbsp;链接头信息</b></td></tr>"+
												 "<tr><td style='width: 800px;height: 35px;'><input type='text' style='width: 800px;height: 35px;' disabled='disabled' value='"+data.data.block_name+"'></td></tr>"+
												 "<tr><td style='height: 35px'><b>&nbsp;描述信息</b></td></tr>"+
												 "<tr><td><textarea style='width: 100%;height: 100%' disabled='disabled' >"+data.data.block_description+"</textarea></td></tr>");
										$("#tijiao").hide();
										$("#queding").show();	
							},
							error: function () {
							}
						});
			    }
			});
		}
	   
	}
	
</script>
</html>