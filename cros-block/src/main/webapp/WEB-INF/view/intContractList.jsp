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
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript"src="static/vue/vue.min.js"></script> 
	<script type="text/javascript"src="static/iview/iview.min.js"></script>	
	<style>
		#vue-object>div{
			background:#EFF3F6;
			width:100%;
			height:100vh;
		}
		.int-contract-r{
			background:#EFF3F6;
		}
		.int-page{
			width:95%;
			margin:20px auto;
			border:1px solid lightgrey;
			height:calc(100vh - 45px);
			background:white;
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
	</style>
</head>
<body>
	<div id="vue-object">
		<div class="clearfix">
			<div class="int-contract-l fl">
				<div class="l-header">
					<img src="<%=request.getContextPath()%>/static/img/Day.jpg" alt="头像" />
					<p class="user-name" v-html="userInfo.userName+',你好'">！</p>
					<p class="user-wallet">cros:&nbsp;<strong v-html="userInfo.totalMoney"></strong></p>					
				</div>				
			</div>
			<div class="int-contract-r fl">
				<spin fix size="large" v-if="spinShow" style="background:white"></spin>
				<div class="int-page">
					<div class="int-list-header">
						<i-input v-model="searchValue" placeholder="Enter something..." style="width: 200px">
							<i-button  slot="append" stype="ghost"	 icon="ios-search-strong"></i-button>
						</i-input>
					</div>
					
					<div class="int-list-body">
						<ul class="clearfix">
							<li v-for="(item,index) in intList">
								<div>
									<div class="unit-header">
										<Tag color="green" v-html="item.type"></Tag>
    									<Tag color="yellow" v-html="item.industry"></Tag>
									</div>
									<p class="unit-body" v-html="item.title"></p>
									<div class="clearfix unit-foot">
										<div class="fl" style="font-size: 18px;"><Icon size="18" type="eye" v-html="item.view"></Icon>&nbsp;</div>
										<div class="fr">
											<poptip confirm title="确定删除吗？" @on-ok="deleteUnit(index)" placement="right">
										       <i-button stype="ghost" shape="circle" icon="ios-trash-outline"></i-button>
										    </poptip>
											<i-button type="ghost" shape="circle" icon="ios-person-outline"></i-button>
										</div>
									</div>
								</div>
							</li>
						</ul>
					
					</div>
					
				</div>
			</div>
		</div>		
	</div>
	
	<script>
		var vm = new Vue({
			el:'#vue-object',
			data:{
				userInfo:{
					userName:"Day",
					totalMoney:'900000'
				},
				spinShow:false,
				searchValue:'',
				intList:[
					{
						type:'私人',
						industry:'房地产',
						title:'安置房审批手续',
						view:'2'
					},{
						type:'公开',
						industry:'农业',
						title:'扶贫生产管理协议',
						view:'2135'
					},{
						type:'公开',
						industry:'农业',
						title:'扶贫生产管理协议',
						view:'2135'
					},
					{
						type:'公开',
						industry:'金融',
						title:'中信银行年利率调整条例',
						view:'3890'
					},{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					{
						type:'公开',
						industry:'教育',
						title:'九年义务教育增加协议',
						view:'45690'
					},
					
				]
			},
			created:function(){
				this.mockRequest();
			},
			methods:{
				mockRequest:function(callback){
					var _self = this;
					_self.spinShow = true;
					setTimeout(function(){
						_self.spinShow = false;
						},1000)
				},
				deleteUnit:function(index){
					var _self = this;
					_self.spinShow = true;
					setTimeout(function(){
						_self.spinShow = false;
						_self.intList.splice(index,1);
						_self.$Message.success('删除成功！')
					},1000)
					
				}
			}
			
	
		});
		

		

	</script>
	
</body>
</html>