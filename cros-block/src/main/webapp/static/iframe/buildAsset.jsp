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
		<a href="javascript:void(0)" style="color: white;" id="next">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;下一步</a>
	</div>
	<br>
	<br>
	<center>
		<table  style="width: 800px;height: 50px;">
			<tr><td class="leftTd"><b>用户地址</b></td></tr>
			<tr><td><input type="text" style="width: 800px;height: 25px" disabled="disabled" value="0xf0a36357c4f9835c4f9835c7050dfe811b9a67e85"/></td></tr>
		</table>
		<br>
	<form action="">
		<table id="addTable" style="width: 800px;height: 350px;">
		  	<tr><td style="height: 25px;"><b>&nbsp;资产详情</b></td></tr>
			<tr><td style="width: 500px;height: 25px;"><input type="text" name="assetName" style="width: 500px;height: 25px;" required="required" placeholder="请填写资产名称"></td>
				<td><input type="number" class="value" id="assetValue" style="width: 300px;height: 25px;" required="required" placeholder="请填写资产估价(单位：万元)"></td></tr>
			<tr><td style="height: 25px;"><b>&nbsp;资产详情</b></td></tr>
			<tr><td style="width: 500px;height: 25px;"><input type="text" name="assetName1" style="width: 500px;height: 25px;" required="required" placeholder="请填写资产名称"></td>
				<td><input type="number" class="value" id="assetValue1" style="width: 300px;height: 25px;" required="required" placeholder="请填写资产估价(单位：万元)"></td></tr>
			<tr><td style="height: 25px;"><b>&nbsp;资产详情</b></td></tr>
			<tr><td style="width: 500px;height: 25px;"><input type="text" name="assetName2" style="width: 500px;height: 25px;" required="required" placeholder="请填写资产名称"></td>
				<td><input type="number" class="value" id="assetValue2" style="width: 300px;height: 25px;" required="required" placeholder="请填写资产估价(单位：万元)"></td></tr>
			<tr><td style="height: 25px;"><b>&nbsp;资产详情</b></td></tr>
			<tr><td style="width: 500px;height: 25px;"><input type="text" name="assetName3" style="width: 500px;height: 25px;" required="required" placeholder="请填写资产名称"></td>
				<td><input type="number" class="value" id="assetValue3" style="width: 300px;height: 25px;" required="required" placeholder="请填写资产估价(单位：万元)"></td></tr>
			<tr><td style="height: 25px;"><b>&nbsp;资产详情</b></td></tr>
			<tr><td style="width: 500px;height: 25px;"><input type="text" name="assetName3" style="width: 500px;height: 25px;" required="required" placeholder="请填写资产名称"></td>
				<td><input type="number" class="value" id="assetValue4" style="width: 300px;height: 25px;" required="required" placeholder="请填写资产估价(单位：万元)"></td></tr>
			<tr><td class='leftTd'><b>资产总值：<input id="amountAsset" type="text" style="height:40px;font-size:larger; border: 0px;outline:none" value=0 disabled="disabled"/>万人民币</b></td></tr>
		</table>
	</form>
	</center>
	<div id="win"></div>
</body>
<script type="text/javascript">
	$(".value").change(function(){
			var assetValue = parseFloat($(this).val());
			var amount = parseFloat($("#amountAsset").val()); 
		  $("#amountAsset").val(amount+=assetValue);
	});
	$(function () {
	    $("#next").click(function () {
	    	    var assetName = $("#assetName").val();
	    	    var assetValue = $("#assetValue").val();
	            if (assetName == ""||assetValue == "") {
	                $.messager.alert('提示','请填写有效的资产信息');
	            }else {
	            	$.messager.progress({msg:'正在认证中'}); 
	            	setTimeout(function(){
	            		  $.messager.progress('close');$.messager.confirm('提示','认证成功,去发行Token？',function(r){
	            		  	  if (r){
	            		  		$('#win').window({
	            		  		    width:600,
	            		  		    height:500,
	            		  		  	minimizable:false,
	            		  		    modal:true,
	            		  		    title:'发行Token',
	            		  		    inline:false,
	            		  		    content: "<iframe scrolling='no' frameborder='0' border='0' height='100%' width='100%' src='<%=basePath %>static/iframe/buildToken.jsp'></iframe>"
	            		  		});
	          		    	}else{
	          		    		history.back(-1);
		          		    	}
	          	    	});}, 3300);
				}
	    })
	})


	function sure(){
		
		if($(input)=="" || description==""){
			$.messager.alert('提示','请填写有效信息!','warning');
		}else{
			$.messager.confirm('提示','提交将消耗30CROS,确认提交吗?',function(r){
			    if (r){
					
			    }
			});
		}
	   
	}
	
	function GetRequest() {   
	   var url = location.search; //获取url中"?"符后的字串   
	   var theRequest = new Object();   
	   if (url.indexOf("?") != -1) {   
	      var str = url.substr(1);   
	      strs = str.split("&");   
	      for(var i = 0; i < strs.length; i ++) {   
	    	  url = theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);   
	      }   
	   }   
	   return url;   
	}   
/* 	$(document).ready(function() {
		var tb = $("#addTable");
		$.ajax({
				url: "data/blockHash.json",
				type: 'GET',
				dataType: 'json',
				success: function (data) {
					for (var int = 0; int < data.rows.length; int++) {
						if(GetRequest()==data.rows[int].id){
							tb.html("<tr><td class='leftTd'><b>创世纪区块Hash值</b></td><td>&nbsp;"+data.rows[int].parentHash+"</td></tr>"+
									"<tr><td class='leftTd'><b>主链高度</b></td><td>&nbsp;"+ data.rows[int].blockHight+"</td></tr>"+
									"<tr><td class='leftTd'><b>主链区块Hash值</b></td><td>&nbsp;"+data.rows[int].blockHash+"</td></tr>"+
									"<tr><td class='leftTd'><b>建链销耗</b></td><td>&nbsp;30CROS</td></tr>");
						}
					}
				},
				error: function () {
				}
			}); 
	})*/
</script>
</html>