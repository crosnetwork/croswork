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
		.com-box{
			width:100%;
			height:100%;
			box-sizing:border-box;
			padding:20px;
			
		}
		.com-box>div{
			border:1px solid lightgrey;
			min-height:400px;
			position:relative;
		}
		.com-box>div p{
			border-bottom:1px solid lightgrey;
			margin:10px;
			padding-bottom:5px;
		}
		
		.component-add-btn{
			position:absolute;
			right:10px;
			bottom:10px;
		}
		
	</style>
</head>
<body>
	<div id="vue-object">
		<div class="clearfix">
			<div class="int-contract-l fl">
				<div class="l-header">
					<img src="<%=request.getContextPath()%>/static/img/Day.jpg" alt="头像" />
					<p class="user-name">{{userInfo.userName}},你好！</p>
					<p class="user-wallet">cros:&nbsp;<strong>{{userInfo.totalMoney}}</strong></p>					
				</div>				
			</div>
			<div class="int-contract-r fl">
				<spin fix size="large" v-if="spinShow"></spin>
				<div>
					<tabs>
				        <tab-pane label="Layout component" icon="social-apple">
				        	<div class="com-box">
				        		<div>
				        			<p>布局组件</p>
				        			<div>
				        				<ul class="clearfix">
				        					<li @click="modifyCom(item)" v-for="(item,index) in layoutList" class="com-box-li">
				        						<poptip placement="bottom" trigger="hover"  :content="item.name">
					        						<Icon size='20' type="android-image"></Icon>
					        						<span class="unit-name">{{item.name}}</span>
				        						</poptip>
				        					</li>				        					
				        				</ul>
				        			</div>
				        		</div>
				        	</div>	
				        </tab-pane>
				        <tab-pane label="Input component" icon="social-windows">
				        	<div class="com-box">
				        		<div>
				        			<p>输入组件</p>
				        			<div>
				        				<ul class="clearfix">
				        					<li @click="modifyInput(item)" v-for="(item,index) in inputList" class="com-box-li">
				        						<poptip placement="bottom" trigger="hover"  :content="item.name">
					        						<Icon size='20' type="ios-compose"></Icon>
					        						<span class="unit-name">{{item.name}}</span>	
				        						</poptip>
				        					</li>				        					
				        				</ul>
				        			</div>
				        		</div>
				        	</div>
				        </tab-pane>
				        <tab-pane label="Functional component" icon="social-tux">
				        	<div class="com-box">
				        		<div>
				        			<p>功能组件</p>
				        			<div>
				        				<ul class="clearfix">
				        					<li v-for="(item,index) in actionList" class="com-box-li">
				        						<poptip placement="bottom" trigger="hover"  :content="item.name">
					        						<Icon size='20' type="ios-compose"></Icon>
					        						<span class="unit-name">{{item.name}}</span>	
				        						</poptip>
				        					</li>				        					
				        				</ul>
				        			</div>
				        		</div>
				        	</div>
				        </tab-pane>
				    </tabs>
				</div>
				
				<modal v-model="layoutModal" title="布局组件" @on-ok="saveLayOut">
			     	<div class="modal-box">
			     		<i-form :model="layoutForm" :label-width="60">
					        <form-item label="组件名称">
					            <i-input v-model="layoutForm.name" placeholder="Enter something..."></i-input>
					        </form-item>
					        
					        <form-item label="组件位置">
					            <radio-group v-model="layoutForm.radio">
					                <radio label="toLeft">居左</radio>
					                <radio label="toCenter">居中</radio>
					                <radio label="toRight">居右</radio>
					            </radio-group>
					        </form-item>
					        
					        <form-item label="组件内容">
					            <i-input v-model="layoutForm.textarea" type="textarea" :autosize="{minRows: 2,maxRows: 5}" placeholder="Enter something..."></i-input>
					        </form-item>
					    </i-form>
			     	</div>   
			    </modal>
				
				<modal v-model="inputModal" title="输入组件" @on-ok="saveInput">
			     	<div class="modal-box">
			     		<i-form :model="inputForm" :label-width="60">
					        <form-item label="组件名称">
					            <i-input v-model="inputForm.name" placeholder="Enter something..."></i-input>
					        </form-item>
					        
					        <form-item label="组件位置">
					            <radio-group v-model="inputForm.radio">
					                <radio label="toLeft">居左</radio>
					                <radio label="toCenter">居中</radio>
					                <radio label="toRight">居右</radio>
					            </radio-group>
					        </form-item>
					        <form-item label="金额" v-if="inputForm.type=='cash'">
					        	<input-number :max="99999999" :min="-999999999" v-model="inputForm.number"></input-number>
					        </form-item>
					        
					        <form-item label="日期" v-if="inputForm.type=='date'">
					        	<date-picker type="date" placeholder="Select date" v-model="inputForm.date"></date-picker>
					        </form-item>					        
					    </i-form>
			     	</div>   
			    </modal>
				
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
				isLoading:false,
				layoutModal:false,
				inputModal:false,
				tabs:[],
				formList:[],
				layoutForm:{name:'',radio:'',textarea:''},
				inputForm:{name:'',radio:'',date:'',cash:'',person:''},
				layoutList:[
					{name:"标题一",type:1,radio:'',textarea:''},
					{name:"标题二",type:1,radio:'',textarea:''},
					{name:"标题三",type:1,radio:'',textarea:''},
					{name:"段落",type:2,radio:'',textarea:''}
				],
				inputList:[
					{name:"数值组件",type:'cash',radio:'',cash:''},
					{name:"日期组件",type:'date',radio:'',date:''},
					{name:"参与人组件",type:'person',radio:'',person:''},
				],
				actionList:[
					{name:"金额组件",type:'cash'},
					{name:"输出组件",type:'output'},
					{name:"接收组件",type:'getunit'},
				]
			},
			created:function(){
				this.mockRequest()
			},
			methods:{
				mockRequest:function(){
					var _self = this;
					_self.spinShow = true;
					setTimeout(function(){
						_self.spinShow = false;
						},1000)
				},
				modifyCom:function(item){
					var _self = this;
					_self.layoutModal = true;
					_self.layoutForm.name = item.name;
					_self.layoutForm.radio = item.radio;
					_self.layoutForm.textarea = item.textarea;
					_self.layoutForm.type = item.type;
				},
				saveLayOut:function(){
					var _self = this;
					var _tmp = {};
					_self.mockRequest();
					deepCopy(_self.layoutForm,_tmp);
					_self.layoutList.push(_tmp);
					_self.layoutForm.name = '';
					_self.layoutForm.radio = '';
					_self.layoutForm.textarea = '';
				},
				modifyInput:function(item){
					var _self = this;
					_self.inputModal = true;
					_self.inputForm.name = item.name;
					_self.inputForm.radio = item.radio;
					_self.inputForm.date = item.date;
					_self.inputForm.type = item.type;
					_self.inputForm.date = item.cash;
					_self.inputForm.date = item.person;
					
				},
				saveInput:function(){
					var _self = this;
					var _tmp = {};
					_self.mockRequest();
					deepCopy(_self.inputForm,_tmp);
					_self.inputList.push(_tmp);
					_self.inputForm.name = '';
					_self.inputForm.radio = '';
					_self.inputForm.date = '';
					_self.inputForm.type = '';
					_self.inputForm.person = '';
					_self.inputForm.cash = '';
				},
				
			}
	
		});
		function deepCopy(p, c) {
			var c = c || {};
			for(var i in p) {
				if(typeof p[i] === 'object') {
					c[i] = (p[i].constructor === Array) ? [] : {};　　　　　　　　
					deepCopy(p[i], c[i]);　　　　　　
				} else {　　　　　　　　　
					c[i] = p[i];　　　　　　
				}　　　　
			}　　　　
			return c;　　
		}

		

	</script>
	
</body>
</html>