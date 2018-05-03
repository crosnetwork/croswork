<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String flag = "";
String name = "";
String password = "";
String isflag = "";
String rePassword="";
String id="";
try{ 
	rePassword=(String)request.getAttribute("rePassword");
	id=(String)request.getAttribute("id");
	isflag=(String)request.getAttribute("isflag");
	if(isflag!=null && !isflag.isEmpty()){
		flag=(String)request.getAttribute("flag");
		name=(String)request.getAttribute("username");
		password=(String)request.getAttribute("password");
	}else{
    Cookie[] cookies=request.getCookies(); 
    if(cookies!=null){ 
    for(int i=0;i<cookies.length;i++){
        if(cookies[i].getName().equals("cookie_user")){ 
        String value =  cookies[i].getValue();
        if(value!=null&&!"".equals(value)){
        	flag=cookies[i].getValue().split("-")[0];
            name=cookies[i].getValue().split("-")[1]; 
            if(cookies[i].getValue().split("-")[2]!=null && !cookies[i].getValue().split("-")[2].equals("null")){
                password=cookies[i].getValue().split("-")[2]; 
            }
           }
           } 
	}
   } 
   } 
}catch(Exception e){ 
   e.printStackTrace(); 
} 
%> 
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html >
<html lang="en">
<head>
	<base href="<%=basePath %>">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" charset="UTF-8"></meta>
    <title>登录</title>
    <link rel="stylesheet" href="css/reset.css" />
	<link rel="stylesheet"type="text/css" href="static/bootstrap/css/bootstrap.min.css"> 
	<link rel="stylesheet" href="css/animate.css" />
	<link rel="stylesheet" href="css/login.css" />
	<script type="text/javascript"src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript"src="js/block-const.js"></script>  
	<script type="text/javascript"src="static/easyUi/jquery.easyui.min.js"></script>	
	
</head>

<body>
 	<div class="login-container">
		<div>
			<div class="login-header">
				<a href="" >&lt;&nbsp;返回首页</a>
			</div>
			
			<div class="login-body">
				<div class="login-img-window">
					<img style="background: white;border-radius:50%" src="<%=request.getContextPath()%>/static/img/logo.png" alt="logo" />
					<p>cros平台</p>
				</div>
				<div class="login-body-container animated">
					<form id="login-form" method="post">
						<div class="input-group" style="margin:10px 0;width:100%;">
						    <span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-name" id="basic-addon1"></span>
						    <input id="user-name" type="text" style='background:#F1F2F6;text-indent:20px;' class="form-control" placeholder="请输入账号" aria-describedby="basic-addon1">
						</div>
						<div class="input-group" style="margin:10px 0;width:100%;">
							<span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-pass" id="basic-addon2"></span>
							<input id="user-pass" style='background:#F1F2F6;text-indent:20px;' placeholder="请输入密码" class="form-control" type="password" name="password" aria-describedby="basic-addon2"></input>
						</div>
						<div class="input-group login-code-input">
							<span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-code" id="basic-addon3"></span>
							<input id="user-code" style='background:#F1F2F6;text-indent:20px;' type="text" class="form-control" placeholder="请输入验证码" aria-describedby="basic-addon3">
							<div class="code-img">
								<span>
									<img onClick="flushCode();" src="<%=request.getContextPath()%>/auth/code" width="60px" height="35px" id="code"/>
								</span>
							</div>
						</div>
						
			            <span><input style="vertical-align: middle;margin:0;" type="checkbox" name="flag" id="flag" value="1">记住密码</span>
						
						<button id="log-btn" type="button" class="btn btn-default login-submit">立即登录</button>
					</form>
					<div>
						<p>没有账号？<a href="javascript:;" onClick="toRegister()">点击生成账号</a></p>
					</div>
				</div>
				
				<!-- 注册 -->
				<div class="register-body animated">
					<form id="register-form" method="post" class="animated">
						<div class="input-group" style="margin:10px 0;width:100%;">
						    <span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-name" id="basic-addon4"></span>
						    <input id="reg-name" type="text" style='background:#F1F2F6;text-indent:20px;' class="form-control" placeholder="请输入账号" aria-describedby="basic-addon4">
						</div>
						<div class="input-group" style="margin:10px 0;width:100%;">
							<span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-pass" id="basic-addon5"></span>
							<input id="reg-pass" style='background:#F1F2F6;text-indent:20px;' placeholder="请输入密码" class="form-control" type="password" name="password" aria-describedby="basic-addon5"></input>
						</div>
						<div class="input-group" style="margin:10px 0;width:100%;">
							<span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-pass" id="basic-addon6"></span>
							<input style='background:#F1F2F6;text-indent:20px;' placeholder="请确认密码" class="form-control" type="password" name="password" aria-describedby="basic-addon5"></input>
						</div>
						<div class="input-group login-code-input">
							<span style="width:24px;height:24px;z-index:99;position: absolute;left:3px;top:5px" class="icon-code" id="basic-addon7"></span>
							<input id="reg-code" style='background:#F1F2F6;text-indent:20px;' type="text" class="form-control" placeholder="请输入验证码" aria-describedby="basic-addon3">
							<div class="code-img">
								<span>
									<img onClick="flushCode();" src="<%=request.getContextPath()%>/auth/code" width="60px" height="35px" id="code"/>
								</span>
							</div>
						</div>
						<button id="reg-btn" type="button" class="btn btn-default login-submit">立即注册</button>
					</form>
					<div class="type-area animated">
						<button onclick="chooseType()" id="type-btn" type="button" class="btn btn-default login-submit animated" >生成共链账号</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<script>
		var block_api = {
			url:"auth/personLogin",
			method:"POST"
		}
		var block_register = {
			url:"auth/register",
			method:"POST"
		}
		
		function flushCode(){
			$('#code').attr('src','<%=request.getContextPath()%>/auth/code?time='+new Date().getTime());
		}

		function toRegister(){
			$('.login-body-container').addClass('zoomOutLeft');
			$('.register-body').addClass('zoomInRight');
			$('.register-body').css({
				display:'block'
			})
		}
		function chooseType(){
			$('.type-area').addClass('zoomOutLeft');
			$('#register-form').addClass('zoomInRight');
			$('#register-form').css({
				display:'block'
			})
		}
	
		$('.form-control').focus(function(){
			$('.form-control').css({
				backgroundColor:'#F1F2F6'
			})
			$(this).css({
				backgroundColor:'white'
			})
			
			if($(this).prev().hasClass('icon-name')){
				$(this).prev().addClass('active-user')
			}else if($(this).prev().hasClass('icon-pass')){
				$(this).prev().addClass('active-pass')
			}else{
				$(this).prev().addClass('active-code')
			}	
			
		})
		$('.form-control').blur(function(){
			$('.form-control').css({
				backgroundColor:'#F1F2F6'
			})
			$('.form-control').prev().removeClass('active-user');
			$('.form-control').prev().removeClass('active-pass');
			$('.form-control').prev().removeClass('active-code')
			
		})
		$("#reg-btn").click(function(){
			
			var data1 = {
				userName:$("#reg-name").val(),
				passWord:$("#reg-pass").val(),
				checkCode:$("#reg-code").val(),
			}
			console.log(data1)
			var successFun = function(ret) {
				if(ret.success == true) {
					alert("注册成功！")
				} else {
					alert("error")
				}
			};
			invokeHrpApi(block_register.url, block_register.method, data1, successFun, invoke_api_error);
		})
		$("#log-btn").click(function(){
			var data1 = {
				userName:$("#user-name").val(),
				passWord:$("#user-pass").val(),
				checkCode:$("#user-code").val(),
				flag:0,
			}
			var successFun = function(ret) {
				if(ret.success == true) {
					alert("登录成功！")
				} else {
					alert("error")
				}
			};
			invokeHrpApi(block_api.url, block_api.method, data1, successFun, invoke_api_error);
		})
	</script>
</body>
</html>