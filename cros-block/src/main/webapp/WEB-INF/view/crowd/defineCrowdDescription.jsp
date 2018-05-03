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
	<title>定义众包</title>
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/icon.css">
	<link rel="stylesheet" type="text/css" href="static/easyUi/demo/demo.css">
	<script type="text/javascript" src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript" src="static/easyUi/jquery.easyui.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function(){
				$('#cc').combobox({
					 url:'data/contrats.json',
					 valueField:'id',
					 textField:'text',
				    required:true,
				    width:360,
				    height:44
				});

				$(".toSend").on("click",function(){
					var wd = parent.window;
					var id = getUrlParam("id");
					wd.$("#center"+id).css("height","70%");
					wd.$("#right"+id).css("height","70%");
					wd.$("#right"+id).css("background-color","#FFFFFF");
					wd.$("#right"+id +"  div").css("padding-left","0px");
					wd.$("#right"+id +"  div").empty();
					wd.$("#right"+id +"  div").append("<img src='static/img/upDs01.png' /><span style='color:#232325;magin-right:30px;'>修改描述</span>");
					//wd.$("#right"+id +"  div").css("color","#232325");
					wd.$("#description"+id).html("<div id='description"+id+"' style='width:90%;height:30%;float:left;padding-left: 20px;'>"+$(".to-description").text()+"<div>")
					wd.$("#description"+id).removeAttr("hidden");	
					//wd.$("#midol-contrat"+id).text($(".to-description").text());
					wd.$("#win").window("close");
				})
			})
		 function getUrlParam(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
            var r = window.location.search.substr(1).match(reg);  //匹配目标参数
            if (r != null) return unescape(r[2]); return null; //返回参数值
        }
	</script>
    <style>
    	*{
    		margin: 0px;
    		padding: 0px;
    	}
    	#bodyDiv{
    		width:100%;
    		height:100%;
    		position:absolute;
    		top: 0px;
    		left: 0px;
    	}
    	.label-style{
    		font-size: 20px;
    		color: #232325;
    		position: absolute;
    		left: 20%;
    		top:10%;
    	}
    	.to-description{
    		position: absolute;
    		left: 20%;
    		top: 20%;
    		border: 1px solid #ADAEBD;
    		width:300px;
    		height:200px;
    	}
    	
    	.toSend{
    		background-color: #3682EF;
    		font-size: 16px;
    		height: 36px;
    		width: 96px;
    		color: #FFFFFF;
    		position: absolute;
    		left: 40%;
    		top: 80%;
    	}
    </style>
</head>
<body>
		<div id="bodyDiv">
			<label class="label-style">添加描述</label>
			<div class="to-description" contenteditable="true" ></div>
			<button class="toSend" >确认提交</button>
		</div>
</body>
</html>