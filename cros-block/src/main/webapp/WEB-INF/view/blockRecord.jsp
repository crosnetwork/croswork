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
			<div class='user-act-table' >
				<p>区块记录 </p>
				<i-table :loading="loading" :columns="columnsTable" :data="tableList"></i-table>
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
				userInfo:{
					name:'Day',
					crosNum:'108',
					passWord:'',
					textarea:'',
				},
				columnsTable: [
                    {
                        title: 'TxHash',
                        key: 'Token_address'
                    },
                    {
                        title: '事件',
                        key: 'Event'
                    },
                    {
                        title: '状态',
                        key: 'eventStatus'
                    },
                    {
                        title: '时间',
                        key: 'time'
                    },
                    {
                        title: 'CROS消耗',
                        key: 'pay_cros'
                    },
                    {
                        title: '查看详情',
                        key: 'look'
                    }
               
                ],
               
				tableList:[
					{
				     "Token_address":"1Pyk9mqKugHvkfpYt2563uDaZbD5T6Ux5o",
					 "Event":"发行Token",
					 "eventStatus":"成功",
					 "time":"2018-04-30",
					 "pay_cros":"30CROS",
					}
				],
			},
			
			created:function(){
				var _self = this;
				setTimeout(function(){
					_self.loading = false;
				},1000)
			},
			methods:{
				
			}
		})


	</script>
</body>
</html>