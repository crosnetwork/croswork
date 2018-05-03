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
    <link rel="stylesheet" href="css/reset.css" />
    <link rel="stylesheet" href="css/animate.css" />
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript" src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript" src="static/easyUi/jquery.easyui.min.js"></script>
	
	
	<script type="text/javascript">
		var toclick = 1 ;
		$(document).ready(function(){
				$("#body2Div").hide();
				$("#body3Div").hide();
				$('#cc').combobox({
					 url:'data/contrats.json',
					 valueField:'id',
					 textField:'text',
				    width:360,
				    height:44
				});
				$("#toNext").on("click",function(){
						var crowdName = $("[name='crowdName']").val();
						var crowdNum = $("[name='crowdNum']").val();
						var crowdAmt = $("[name='crowdAmt']").val();
						var key = 1 ;
						if(crowdName.length <=0){
								alert("请输入众包名称");
								key = 0 ;
							}
						if(crowdNum <=0){
							//需要加数字验证
							alert("请正确输入数字");
							$("[name='crowdNum']").val(1);
							key = 0 ;
						}
						if(crowdAmt <=0){
							//这里需要去后台查看其cors金额是否大于悬赏金额
							alert("请正确输入金额");
							$("[name='crowdAmt']").val("");
							key = 0 ;
						}
						if(key == 1){
							$("#bodyDiv").hide();
							$("#body2Div").empty();
							$("#body2Div").css("display","flex");
							$("#body2Div").css("flex-direction","column");
							$("#body2Div").append("<div class = 'to-text-align' style = 'margin-top: 40px;'><label class='define-lable'>"+crowdName+"</label></div>")
							$("#body2Div").append("<div class = 'to-text-align' style = 'margin-top: 20px;'><label class='define-lable'>需要流程环节数："+crowdNum+"</label> <label class='define-lable' style='margin-left:60px;'>悬赏金额："+crowdAmt+"</label><label class='define-lable' style='margin-left:60px;' id = 'cros-pay'>发布消耗：30cros</label></div>")
							
							for (var int = 0; int < crowdNum; int++) {
								$("#body2Div").append("<div class = 'big-div' id='big-div"+int+"'>"
	         											    +"<div class='left-div to-text-align define-lable' id='left"+int+"'>"+(int+1)+"</div>"
	         												+"<div class='center-div to-text-align define-lable' id='center"+int+"'>"
	         													+"<div class='midol-contrat-div' ><a onclick='toChooseContrat(\""+int+"\");' id = 'midol-contrat"+int+"' href='javascript:;'>请选择您的智能合约</a></div>"
	         													+"<div class='agent-div' >设置重复环节数：<input type='text' class= 'to-text-align' name = 'agentNum"+int+"' size = '1' value = '1'></div>"
	         													+"<div class='agent-div' >设置悬赏金额：<input type='text' class= 'to-text-align' name = 'agentAmt"+int+"' size = '5' value = '0'></div>"
	         												+"</div>"
	         												+"<div class='right-div to-text-align define-lable' id='right"+int+"'>"
	         													+"<div onclick = 'toGetSee(\""+int+"\")'>+</br>添加描述</div>"
	         												+"</div>"
	         												+"<div id='description"+int+"' hidden='true' style='width:90%;height:30%;float:left;padding-left: 20px;'></div>"
	         											+"</div>")
							}
							$("#body2Div").append("<div class = 'button-div2 to-text-align'><span id='toNext2'>下一步</span></div>");



							
							$("#body2Div").show();
								key = 1; 
							}


							$(".button-div2").on("click",function(){
								$(".midol-contrat-div").css("background","#F1F2F6");
								$(".midol-contrat-div a").css("color","#9D9D9D");
								$(".agent-div input[type='text']").attr("readonly","readonly");
								$(".right-div").empty();
								toclick = 2;
								$("#body3Div").show();
								$(".button-div2").hide();
								$("#body2Div").append("<div class = 'button-div3 to-text-align' ><span id='toNext3'>生成模板</span></div");
								
							})
						
					})
				
				$("#jianfa").on("click",function(){
					$("[name='crowdNum']").val($("[name='crowdNum']").val()-1 > 0?$("[name='crowdNum']").val()-1:1)
					})
				$("#jiafa").on("click",function(){
					$("[name='crowdNum']").val($("[name='crowdNum']").val()-(-1))
					})
					
			})
			function toChooseContrat(id){
			if(toclick == 1){
				$('#win').window({
				    width:540,
				    height:300,
				    modal:true,
				    //href:"crowd/defineCrowdNext"
					content:"<iframe src='crowd/defineCrowdNext?id="+id+"' frameborder='0'  style='width: 100%;height: 98%'/>"
				});
			}
				
					//	alert(id);
			}
			function toGetSee(id){
			if(toclick == 1){
				$('#win').window({
				    width:600,
				    height:400,
				    modal:true,
					content:"<iframe src='crowd/defineCrowdDescription?id="+id+"' frameborder='0'  style='width: 100%;height: 98%'/>"

				});
			}
			}
	</script>
	<style>
		*{
			padding: 0px;
			margin: 0px;
		}
		#vue-object>div{
			background:#EFF3F6;
			width:100%;
			height:100vh;
			
		}
		.int-contract-r{
			background:#EFF3F6;
			overflow-y: auto;
		}
		.int-page{
			width:95%;
			margin:20px auto;
			border:1px solid lightgrey;
			/* height:calc(100vh - 45px); */
			min-height:calc(100vh - 45px);
			background:white;
			overflow-x:hidden;
			overflow-y:auto;
		}
		.int-list-header{
			width:350px;
		}
		.int-list-header,.int-list-body{
			padding:10px;
		}
		.int-list-header>input{
			width:300px;
		}
		.int-list-body ul{
			overflow-x:hidden;
			overflow-y:scroll;
			height:calc(100vh - 120px);
			border-top:1px solid lightgrey;
		}
		.int-list-body li{
			width:30%;
			float:left;
			padding:8px;
			height:140px;
			border:1px solid lightgrey;
			margin:10px 0;
			box-sizing:border-box;
			border-radius:5px;
			transition:.3s;
			cursor:pointer;
		}
		.int-list-body li:hover{
			transform:translateY(-5px);
			box-shadow:3px 3px 0 rgba(0,0,0,.2);
		}
		.int-list-body li:nth-child(3n-1){
			margin:10px 4%;
		}
		.unit-header{
			padding-top:5px;
			height:30px;
		}
		.unit-body{
			margin-top:10px;
			font-size:20px;
			line-height:40px;
			height:40px;
		}
		.unit-foot{
			margin-top:10px;
			height:32px;
		}
		#bodyDiv{
			position: relative;
		}
		#body2Div{
			hidden : true;
		}
		.built-in{
			width:95%;
			margin:20px auto;
			height:40px;
		}
		.int-built-in{
			width:auto;
			padding: 10px 20px 0px 20px;
			height:40px;
			float:left;
			font-size: 15px;
			background-color: #E5EBEF ;
			
		}
		.int-built-center{
			width:auto;
			height:100%;
			float:left;
			background-color: #E5EBEF ;
			
		}
		.int-built-first{
			color: #3682EF ;
		}
		.first-div{
			position: relative;
			left: 15%;
			top: 60px;
		}
		.no-first-div{
			position: relative;
			left: 15%;
			top: 100px;
		}
		.define-lable{
			font-size: 20px;
		}
		input{
			border:1px solid lightgrey; 
			height: 44px;
			font-size: 20px;
		}
		.to-text-align{
		    text-align: center;
		 }	
		 .button-div{
		 	position: absolute;
			right:10%;
			top: 30px;
			background-color: #3682EF;
			width: 96px;
			height: 36px;
			font-size:16px; 
			color: #FFFFFF ;
		 }
		 
		 .button-div2{
		 	position: absolute;
			right:10%;
			top: 100px;
			background-color: #3682EF;
			width: 96px;
			height: 36px;
			font-size:16px; 
			color: #FFFFFF ;
		 }
		 
		  .button-div3{
		 	position: absolute;
			right:10%;
			top: 100px;
			background-color: #3682EF;
			width: 96px;
			height: 36px;
			font-size:16px; 
			color: #FFFFFF ;
		 }
		 
		 #toNext{
		 	position: relative;
		 	top: 20%;
		 }
		 #toNext2{
		 	position: relative;
		 	top: 20%;
		 }
		  #toNext3{
		 	position: relative;
		 	top: 20%;
		 }
		 .big-div{
		 	height: 80px;
		 	width: 900px;
		 	border: 1px solid #ADAEBD;
		 	margin-top:30px;
		 	align-self:center;
		 }
		 .left-div{
		 	height: 100%;
		 	width: 10%;
		 	border-width: 0px;
		 	float: left;
		 	background-color: #F1F2F6;
		 	line-height: 80px; /*设置line-height与父级元素的height相等*/
		 	overflow: hidden;/*防止内容超出容器或者产生自动换行*/
		 }
		  .center-div{
		 	height: 100%;
		 	width: 80%;
		 	border-width: 0px;
		 	float: left;
		 	/*line-height: 80px; 设置line-height与父级元素的height相等*/
		 	overflow: hidden;/*防止内容超出容器或者产生自动换行*/
		 	display:flex;
		 }
		  .right-div{
		 	height: 100%;
		 	width: 10%;
		 	border-width: 0px;
		 	float: left;
		 	background-color: #3682EF;
		 	display:flex;
		 }
		 
		 .right-div div{
		 	padding-left:25px;
		 	font-size: 10px;
		 	color: #FFFFFF;
		 	align-self:center;
		 }
		 
		.midol-contrat-div{
			background: linear-gradient(#435C79,#202F44);
			height: 30px;
			width: auto;
			align-self:center;
			margin-left: 20px;
		}
		
		.midol-contrat-div a{
			color: #FFFFFF;
			font-size: 17px;
			padding: 4px;
		}
		
		.agent-div{
			height: 30px;
			width: auto;
			align-self:center;
			margin-left: 20px;
		}
		
		.agent-div input{
			height : 30px;
		}
		
		#body3Div{
			display:flex;
			flex-direction:column;
		}
		
		 .big3-div{
		 	height: 80px;
		 	width: 900px;
		 	margin-top:30px;
		 	align-self:center;
		 	font-size: 17px;
		 	display:flex;
			flex-direction:column;
		 }
		 
		 .half{
		 	align-self:center;
		 	height: 40px;
		 	width: 900px;
		 }
	</style>
</head>
<body>
	<div id="vue-object">
		<div class="clearfix">
			<!-- 左侧栏 -->
			<div class="int-contract-l fl">
				<div class="l-header">
					<img src="static/img/Day.jpg" alt="头像" />
					<p class="user-name" >Yeran,你好！</p>
					<p class="user-wallet">cros:&nbsp;<strong >300000</strong></p>					
				</div>				
			</div>
			<div class="int-contract-r fl">
				<div class="built-in">
					<div class="int-built-in int-built-first">定义众包</div>
					<div class="int-built-center"><img alt="" src="static/img/built07.png" style="width:18px;"></div>
					<div class="int-built-in">生成模板</div>
					<div class="int-built-center"><img alt="" src="static/img/built06.png" style="width:17px;"></div>
				</div>
				<div class="int-page">
					
					<div class="int-list-body" id="bodyDiv">
						<div class = "first-div">
							<label  class="define-lable">众包名称：</label>
							<input type="text" name="crowdName" placeholder="请输入您得众包名称" size="40" />
						</div>
						<div class = "no-first-div">
							<label  class="define-lable">需要环节：</label>
							<a id="jianfa" href="javascript:;"><img style="padding-left:30px; " alt="" src="static/img/jianfa02.png"></a>
							<input class="to-text-align" name="crowdNum" type="text" value="1"  size="4"  />
							<a id="jiafa" href="javascript:;"><img alt="" src="static/img/jiafa02.png"></a>
						</div>
						<div class = "no-first-div" style="margin-top: 30px">
							<label  class="define-lable">发布悬赏：</label>
							<input type="text" name="crowdAmt" placeholder="请输入您的悬赏金额" size="40" /><label id="label-1" class="define-lable">cros</label>
						</div>
						<div class = "button-div to-text-align">
							<span id="toNext">下一步</span>
						</div>
					</div>
					
					<div class="int-list-body" id="body2Div" >
					</div>
					<div class="int-list-body" id="body3Div"  ><!--  -->
						<div class="big3-div">
							<div class="half"><img alt="" src="static/img/shuI01.png" style="padding-right:10px;">模板公开</div>
							<div class="half"><label><input  type="radio" name="isPublic">公开</label> <label><input type="radio" name="isPublic">私密</label></div>
						</div>
						<div class="big3-div">
							<div class="half"><img alt="" src="static/img/shuI01.png" style="padding-right:10px;">模板分类</div>
							<div class="half"><input id="cc"></div>
						</div>
						<div class="big3-div">
							<div class="half"><img alt="" src="static/img/shuI01.png" style="padding-right:10px;">设置金额</div>
							<div class="half"><input class="easyui-textbox" style="width:360px;height:44px;" data-options="prompt:'请设置使用金额'"  ></div>
						</div>
						<div class="big3-div">
							<div class="half"><img alt="" src="static/img/shuI01.png" style="padding-right:10px;">公开消耗</div>
							<div class="half"><input class="easyui-textbox" value="30" style="width:360px;height:44px;">cos</div>
						</div>
					
					</div>
				</div>
			</div>
		</div>		
	</div>
	<div id="win"></div>
	
</body>
</html>