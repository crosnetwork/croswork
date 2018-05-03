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
    <link rel="stylesheet" type="text/css" href="static/easyUi/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/icon.css">
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript"src="static/vue/vue.min.js"></script> 
	<script type="text/javascript"src="static/iview/iview.min.js"></script>	
	<script type="text/javascript" src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript" src="static/easyUi/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="static/easyUi/locale/easyui-lang-zh_CN.js"></script>
	<style>
		.user-act-header{
			width:100%;
			height:176px;
			background:url('../static/img/user-bj.jpg') no-repeat center/cover;
		}	
		.user-act-body>div,.user-act-table{
			width:981px;
			margin:0 auto;
		}
		.user-act-body>div{
			position:relative;
		}
		.user-avatar{
			width:450px;	
			margin:0 auto;;
			text-align:center;
			position:relative;
		}
		.user-avatar-1{
			width:120px;
			height:120px;
			margin-left:-60px;
			position:absolute;
			top:-60px;
			left:50%;
			border-radius:50%;
			background:white;	
			overflow:hidden;		
		}
		.user-avatar-1 img{
			margin-top:5px;
			padding:5px;
			width:110px;
			height:110px;
			border-radius:50%;
		}
		.user-avatar-2{
			padding-top:60px;
		}
		.user-name{
			font-size:20px;
			line-height:24px;
			margin-bottom:10px;
		}
		.user-cros{
			background:url('static/img/user-cros.png') no-repeat 175px center;
		}
		.user-avatar-2>div{
			margin:10px;
		}
		.user-avatar-2>div p{
			margin:0 auto;
			width:380px;
			background:#F1F2F6;
		}
		.user-avatar-page{
			text-align:left;
		}
		.user-avatar-page i{
			padding:0 5px 5px 10px;
			transform: rotate(45deg);
		}
		.code-span{
			cursor:pointer;
			display:inline-block;
			width:24px;
			height:24px;
			background:url('../static/img/QRcode.png') no-repeat center;
		}
		.import-key,.sure-btn{
			border:none;
			border-radius:0;
			color:white;
			background: linear-gradient(to right,#4F6681,#27364A);
			background:-webkit-linear-gradient(to right,#4F6681,#27364A);  
		    background:-o-linear-gradient(to right,#4F6681,#27364A);  
		    background:-moz-linear-gradient(to right,#4F6681,#27364A);  
		}
		.import-key{
			position:absolute;
			top:10px;
			right:10px;
		}
		.user-act-table p{
			text-indent:20px;
			font-size:12px;
			line-height:30px;
			height:30px;
			color:white;
			background: linear-gradient(to left,#4F6681,#27364A);
			background:-webkit-linear-gradient(to left,#4F6681,#27364A);  
		    background:-o-linear-gradient(to left,#4F6681,#27364A);  
		    background:-moz-linear-gradient(to left,#4F6681,#27364A);  
		}
		.qr-code-modal{
			width:260px;
			height:260px;
		}
		.code-area{
			height:40px;
			opacity: 0;
		}
		.watchCode{
			animation: seeCode 1s .5s;
			animation-fill-mode: forwards;
		}
		@keyframes seeCode{
			from{
				opacity: 0;
				transform: translateY(0);
			}
			to{
				opacity: 1;
				transform: translateY(-30px);
			}
		}
	</style>
</head>
<body>
	<div id="vue-object">
		<div>
			<div class='user-act-header'></div>
			<div class='user-act-body'>
				<div>
					<div class="user-avatar">
						<div class="user-avatar-1">
							<img src="<%=request.getContextPath()%>/static/img/Day.jpg" alt="头像" />
						</div>
						<div class="user-avatar-2">
							<p class="user-name">{{userInfo.name}}</p>
							<p class="user-cros">cros&nbsp;{{userInfo.crosNum}}</p>
							<div class="clearfix">
								<p class="user-avatar-page fl">
									<Icon size="18" type="paperclip"></Icon>
									<span>&nbsp;&nbsp;地址&nbsp;&nbsp;0xf0a36357c4f9835c4f9835c7050dfe811b9a67e85</span>
								</p>
								<span @click="showQrCode" class="code-span fr"></span>
							</div>
						</div>	
					</div>
					<i-button @click="exportKey"  id="createToken" class="import-key" size="small">发行Token</i-button>
				</div>
				<modal width="280" v-model="modalQrCode">
			        <div class="qr-code-modal">
			        		二维码占位1
			        </div>
			    </modal>
			</div>
			<div>
				<div class='user-act-table' v-if="onOff	">
					<p>我发行的Token  <a href="javascript:void(0)" v-on:click="toggle()" style="padding-left: 800px">资产映射</a></p>
					<i-table :loading="loading" :columns="columnsTable" :data="tableList"></i-table>
				</div>
				<div class='user-act-table' v-else>
					<p>资产映射  <a href="asset" style="padding-left: 830px">添加资产</a></p>
					<i-table :loading="loading" :columns="columnsTable2" :data="tableList2"></i-table>
				</div>
			</div>
			<br>
			<div class='user-act-table'  v-if="onOff">
				<i-table :loading="loading" :columns="columnsTable3" :data="tableList3"></i-table>
			</div>
			
			 <modal v-model="importKey" @on-cancel=cancelCode width="360">
		        <p slot="header" style="color:#f60;text-align:center">
		            <icon type="information-circled"></icon>
		            <span>发行Token需要先进行资产认证哦~</span>
		        </p>
		        <div slot="footer" style="text-align:center;">
		            <i-button v-if="!showCode" @click="fetchCode" style="width:90px" class="sure-btn" size="small">去认证</i-button>
		        </div>
		    </modal>			
		</div>
	</div>
	
	<script>
		var vm = new Vue({
			el:'#vue-object',
			data:{
				modalQrCode:false,
				loading:true,
				importKey:false,
				showCode:false,
				onOff:true,	
				userInfo:{
					name:'Day',
					crosNum:'108',
					passWord:'',
					textarea:'',
				},
				columnsTable: [
                    {
                        title: 'Token名称',
                        key: 'Token_name'
                    },
                    {
                        title: '发行总量',
                        key: 'Total_num'
                    },
                    {
                        title: '库存量',
                        key: 'amount'
                    }
               
                ],
                columnsTable2: [
	                {
	                    title: '资产列表',
	                    key: 'asset_list'
	                },
	                {
	                    title: '认证时间',
	                    key: 'build_time'
	                },
	                {
	                    title: '资产估值(¥/万)',
	                    key: 'asset_amount'
	                }
	           
	            ],
	            columnsTable3: [
	                {
	                    title: '高度',
	                    key: 'block_height'
	                },
	                {
	                    title: '区块时间',
	                    key: 'block_time'
	                },
	                {
	                    title: '区块记录',
	                    key: 'block_record'
	                },
	                {
	                    title: '区块消耗',
	                    key: 'block_expend'
	                }
		           
		        ],
				tableList:[
					
				],
				
				tableList2:[
					{"asset_list":"北京西郊别墅","build_time":"2018-04-30","asset_amount":"6000","block_record":"1",}
				],
			 	
				tableList3:[
					{"block_height":"1049","block_time":"2018-04-30",}
				],
			},
			
			created:function(){
				var _self = this;
				setTimeout(function(){
					_self.loading = false;
				},1000)
			},
			methods:{
				toggle:function(){
					this.onOff = false;
		            this.isShow = !this.isShow;
		        },
				showQrCode:function(){
					this.modalQrCode = true;
				},
				exportKey:function(){
					this.importKey = true;
				},
				fetchCode:function(){
					window.location.href="asset";
				},
				cancelCode:function(){
					this.showCode = false;
				},
				copyCode:function(){
				
				}
			}

		})


	</script>
</body>
</html>