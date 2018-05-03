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
					wd.$("#midol-contrat"+id).text($("#cc").combobox('getText'));
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
    		top:20%;
    	}
    	.to-search-contrats{
    		position: absolute;
    		left: 20%;
    		top: 40%;
    	}
    	
    	.toSend{
    		background-color: #3682EF;
    		font-size: 16px;
    		height: 36px;
    		width: 96px;
    		color: #FFFFFF;
    		position: absolute;
    		left: 40%;
    		top: 70%;
    	}
    </style>
</head>
<body>
		<div id="bodyDiv">
			<label class="label-style">选择智能合约</label>
			<div class="to-search-contrats"><input id="cc"></div>
			<button class="toSend" >确认提交</button>
		</div>
</body>
</html>