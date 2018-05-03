<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html >
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge" charset="UTF-8"></meta>
    <title>个人中心</title>
    <link rel="stylesheet" href="css/reset.css" />
    <link rel="stylesheet" href="css/animate.css" />
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/userCenter.css" />
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript"src="static/easyUi/jquery.min.js"></script> 
	<script type="text/javascript"src="static/vue/vue.min.js"></script> 
	<script type="text/javascript"src="static/iview/iview.min.js"></script>	
</head>
<body>
	<div id="vue-object">
		<div class="user-container">
			<div class="user-center-header clearfix">
				<div class="fl">
					<img src="<%=request.getContextPath()%>/static/img/logo-header.png" alt="logo" />
				</div>
				<div class="fl header-nav">
					<i-menu mode="horizontal" theme="primary"  active-name="1" @on-select="changePage">
				        <menu-item name="page/indexPage">首页</menu-item>
				        <submenu name="2">
				            <template slot="title">区块链</template>
				            <menu-item name="page/mainChain">主链</menu-item>
			                <menu-item name="page/sideChain">侧链</menu-item>
				        </submenu>
		        		<menu-item id="token" name="page/tokenInfo">Token</menu-item>
				        <submenu name="4">
				            <template slot="title">智能合约</template>
				            <menu-item name="page/intContract">自定义组件</menu-item>
			                <menu-item name="page/defineContract">自定义智能合约</menu-item>
			                <menu-item name="page/intContractList">自定义合约模版管理</menu-item>
				        </submenu>
		        		<submenu name="5">
				            <template slot="title">工作流</template>
				            <menu-item name="crowd/defineCrowd">自定义组件</menu-item>
				            <!-- <menu-item name="page/intContract">工作流模板管理</menu-item>
			                <menu-item name="page/defineContract">众包平台</menu-item>
			                <menu-item name="page/intContractList">我的工作路</menu-item> -->
				        </submenu>
				    </i-menu>
				</div>
				<div class="fr header-user-settings">
					<ul class="clearfix">
						<li>
							<badge count="9">
								<Icon color="white" size="22" type="ios-bell-outline"></Icon>
						    </badge>
						</li>
						<li>
							<badge count="3">
						        <Icon color="white" size="22" type="ios-chatboxes-outline"></Icon>
						    </badge>
						</li>
						<li>
							<dropdown @on-click="userAction">
								<Icon color="white" size="22" type="ios-gear-outline"></Icon>
						        <dropdown-menu slot="list">
						            <dropdown-item name="userManager">个人中心</dropdown-item>
						            <dropdown-item name="2">修改密码</dropdown-item>
						            <dropdown-item name="3">退出</dropdown-item>
						        </dropdown-menu>
						    </dropdown>
						</li>
					</ul>
				</div>
			</div>
			<div class="box-window">
				<iframe :src="tagHtml" class="J_iframe" name="" width="100%" height="100%" frameborder="0">
				</iframe>
			</div>
		</div>

	</div>
	
	<script>
		var vm = new Vue({
			el:'#vue-object',
			data:{
				tagHtml:'',
			},
			created:function(){
				this.tagHtml = "page/indexPage"
			},
			methods:{
				userAction:function(val){
					this.tagHtml = val
				},
				changePage:function(name){
					console.log(name)
					this.tagHtml = name
				}
				
			}

		})

	</script>
	
</body>
</html>