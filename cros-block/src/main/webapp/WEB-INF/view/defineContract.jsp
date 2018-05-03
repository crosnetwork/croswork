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
    <title>自定义智能合约</title>
    <link rel="stylesheet" href="css/reset.css" />
    <link rel="stylesheet" href="css/animate.css" />
	<link rel="stylesheet"type="text/css" href="static/iview/iview.css"> 
	<link rel="stylesheet" href="css/Global.css" />
	<script type="text/javascript"src="static/vue/vue.min.js"></script> 
	<script type="text/javascript"src="static/iview/iview.min.js"></script>
	<script type="text/javascript"src="js/drag.js"></script>		
	<style>
		#vue-object>div{
			background:#EFF3F6;
			width:100%;
			height:100vh;
		}
		.component-box{
			padding:10px;
		}
		.ivu-tabs-nav .ivu-tabs-tab .ivu-icon{
			margin-right:0;
		}
		.com-box-li{
			width:62px;
		}
		.unit-name{
			width:60px;
		}
		.modify-area{
			margin:10px auto;
			padding:10px;
			border:1px solid lightgrey;
			width:95%;
			height:calc(100vh - 60px);	
			overflow-y:scroll;
			overlow-x:hidden;
		}
		.office-header span{
			display:inline-block;
			width:33%;
			height:40px;
			text-align"center;
			float:left;
			vertical-align:top;
			padding:10px;
			box-sizing:border-box;
			border:1px dashed transparent;
			position:relative;
		}
		#span0,#span1,#span2{
			font-size:18px;
			line-height:20px;
		}
		.com-box-li P{
			padding:2px 5px;
			overflow:hidden;
    		text-overflow:ellipsis;
    		white-space:nowrap;
		}
		#span9{
			margin:10px auto 0;
			width:99%;
			min-height:120px;	
		}
		.office-area .activegrid{
			border-color:lightgrey;
			cursor:pointer;
		}
		#span9 p{
			text-align:left !important;
		}
		.grid-btn{
			margin:10px;
		}
		.link-left,.link-right{
			width:50%;
			padding:10px;
			box-sizing:border-box;
		}
		.link-left em{
			display:inline-block;
			width:220px;
			height:20px;
			border:1px dashed transparent;
			border-bottom:1px solid lightgrey;
		}
		.link-left i{
			width:60px;
			height:20px;
			line-height:25px;
			vertical-align: top;
    		display: inline-block;
		}
		.link-left>div{
			padding:10px 0;
		}
		.link-list>li{
			border:1px solid lightgrey;
			box-shadow:1px 1px 0 rgba(0,0,0,.4);
			margin:5px auto;			
		}
		.link-left em{
			text-align:center;
		}
		.link-right{
			padding:30px;font-size:14px;line-height:18px;
		}
		.modify-area .remove-office-unit{
			position:absolute;
			right:5px;
			top:5px;
			padding:0;
			border:none;
			display:none;
			color:red;
		}
		.end-link{
			margin-top:10px;
			margin-bottom:10px;
		}
		.end-link>div{
			width:50%;
			padding:15px 30px;
			box-sizing:border-box;
		}
		.end-link p{
			margin:0 auto;
			text-align:center;
			height:60px;
			line-height:60px;
			width:60px;
			border-radius:50%;
			border:1px solid lightgrey;
			color:white;
			background:#57a3f3;
		}
		.end-line{
			height:30px;
			width:1px;
			background:black;
			margin:3px auto;
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
				<div class="component-box">
					 <p>组件列表</p>
					 <tabs size='small' >
		                <tab-pane icon="ios-browsers-outline" label="布局">
		                	<ul>
		                		<li  class="com-box-li" v-for="(item,index) in layoutList">
					        		<span class="unit-name">{{item.name}}</span>
					        		<p style="text-align:center;" :id="index" ondragstart="drag(event)" draggable="true">{{item.textarea}}</p>
		                		</li>
		                	</ul>
		                </tab-pane>
		                <tab-pane icon="ios-compose-outline" label="输入">
		                	<ul>
		                		<li class="com-box-li" v-for="(item,index) in inputList">
					        		<span class="unit-name">{{item.name}}</span>
					        		<p :id="item.name+index" ondragstart="drag(event)" draggable="true">{{item.person||item.date||item.cash}}</p>
		                		</li>
		                	</ul>
		                </tab-pane>
		                <tab-pane icon="ios-game-controller-b-outline" label="功能">
		                	<ul>
		                		<li class="com-box-li" v-for="(item,index) in actionList">
		                			<div v-if="item.name=='金额组件'" @click="addLink">
		                				<Icon size='20' type="ios-compose"></Icon>
					        			<span class="unit-name">{{item.name}}</span>
		                			</div>
		                			<div v-else-if="item.name=='输出组件'" @click="showLinkNum">
		                				<Icon size='20' type="ios-compose"></Icon>
					        			<span class="unit-name">{{item.name}}</span>
		                			</div>
		                			<div v-else>
		                				<Icon size='20' type="ios-compose"></Icon>
					        			<span class="unit-name">{{item.name}}</span>
		                			</div>
		                		</li>
		                	</ul>
		                </tab-pane>
		            </tabs>
				</div>			
			</div>
			
			<div class="int-contract-r fl">
				<spin fix size="large" v-if="spinShow" style="background:white"></spin>
				<div class="clearfix int-contract-setting">
					<i-button @click="showGrid" class="grid-btn fr"  type="primary" icon="ios-grid-view-outline" size="small">网格</i-button>
					<i-button @click="saveTemplate" class="grid-btn fr"  type="primary" icon="clipboard" size="small">保存模版</i-button>
				</div>
				<div class="modify-area">
					<div class="office-area">
						<div class="office-header clearfix">
							<span 
							@mouseleave="hideDelete(index)"
							@mouseover="canDelete(index)" 
							:class="[isShowGrid?'activegrid':'']" 
							v-for="(item,index) in areaList" 
							:id="'span'+index" ondrop="drop(event)" 
							ondragover="allowDrop(event)">
								<i-button title="删除" type="ghost" icon="android-remove-circle"  @click="deleteNode(index)" class="remove-office-unit"></i-button>
							</span>
						</div>	
						<div>
							<ul class="link-list">
								<li class="clearfix animated bounceInDown" v-for="(item,index) in linkList">
									<div class="link-left fl">
										<div><i>分期数&nbsp;：</i><em :class="[isShowGrid?'activegrid':'']" ondrop="drop(event)" ondragover="allowDrop(event)"></em></div>
										<div><i>支付方&nbsp;：</i><em :class="[isShowGrid?'activegrid':'']" ondrop="drop(event)" ondragover="allowDrop(event)"></em></div>
										<div><i>收款方&nbsp;：</i><em :class="[isShowGrid?'activegrid':'']" ondrop="drop(event)" ondragover="allowDrop(event)"></em></div>	
										<div><i>金额总数：</i><em :class="[isShowGrid?'activegrid':'']" ondrop="drop(event)" ondragover="allowDrop(event)"></em></div>
										<div><i>执行时间：</i><em :class="[isShowGrid?'activegrid':'']" ondrop="drop(event)" ondragover="allowDrop(event)"></em></div>									
									</div>
									<div class="link-right fr">
										  <code style="color:#488EF3">
										  	address public owner;<br>
										  uint public last_completed_migration;<br>
										  <br>
										  modifier restricted() {<br>
										    if (<code style="color:#EBD158">msg.sender == owner</code>){<br>
										  }<br>
										  function Migrations() {<br>
										    owner = msg.sender;<br>
										  }<br>
										  </code>
									</div>
								</li>	
							</ul>
						</div>	
						<div class="animated bounceInDown" v-for="(item,index) in linkNumList" style="width:300px;margin:10px auto;">
							<i-input v-model="linkNum" type="number">
							 	<em slot="prepend">项目环节数</em>
								<i-button @click="showLinkEnd" slot="append" icon="android-arrow-down"></i-button>	
							</i-input>
						</div>
						
						<div class="clearfix end-link" v-show="isCanShow">
							<div class="fl">
								<p>START</p>
								<div class="end-line"></div>
								<div v-for="(item,index) in linkEnd" style="width:200px;margin:0 auto;">
									<i-select v-model="linkUser"  size="small" clearable placeholder="请选择参与人">
	       		 						<i-option v-for="item in userList" :value="item.value" :key="item.value">{{ item.name }}</i-option>
	       		 					</i-select>
	       		 					<div class="end-line"></div>
								</div>
								<p>END</p>							
							</div>
							
							<div class="fl">
								  <code style="color:#488EF3">
								  	address public owner;<br>
								  uint public last_completed_migration;<br>
								  <br>
								  modifier restricted() {<br>
								    if (<code style="color:#EBD158">msg.sender == owner</code>){<br>
								  }<br>
								  function Migrations() {<br>
								    owner = msg.sender;<br>
								  }<br>
								  </code>
							</div>	
						</div>	
					</div>					
				</div>		
			</div>
			
			<modal v-model="inputModal" title="保存模版" >
		     	<div class="modal-box">
		     		<i-form :model="inputForm" :label-width="60" inline>
				        <form-item label="合约名称">
				            <i-input v-model="inputForm.name" placeholder="Enter something..."></i-input>
				        </form-item>
				        <form-item label="环节数">
				            <i-input v-model="inputForm.number" disabled></i-input>
				        </form-item>
				        <br>
				        <form-item label="公布消耗">
				            <i-input v-model="inputForm.pay" placeholder="Enter something..."></i-input>
				        </form-item>	
				        <form-item label="合约分类">
				        	<i-select v-model="inputForm.kinds" placeholder="Select Industry">
				                <i-option value="1">农业</i-option>
				                <i-option value="2">金融业</i-option>
				                <i-option value="3">教育业</i-option>
				                <i-option value="4">房地产</i-option>
				                <i-option value="5">物流业</i-option>
				            </i-select>
				        </form-item><br>
				        
				        <form-item label="设置金额">
				        	<input-number :max="90000000" v-model="inputForm.cash" ></input-number>
				        </form-item><br>			
				        <form-item label="是否公开">
				            <radio-group v-model="inputForm.isOpen">
				                <radio label="open">公开</radio>
				                <radio label="close">私人</radio>
				            </radio-group>
				        </form-item>			        
				    </i-form>
		     	</div>   
		    </modal>			
		</div>		
	</div>
	
	<script>
		var vm = new Vue({
			el:"#vue-object",
			data:{
				spinShow:false,
				isShowGrid:false,
				inputModal:false,
				isCanShow:false,
				inputForm:{
					name:'',
					number:0,
					cash:'',
					isOpen:'',
					pay:'',
					kinds:''
					},
				userInfo:{
					userName:"Day",
					totalMoney:'900000'
				},
				layoutList:[
					{name:"标题一",type:1,radio:'',textarea:'测试的标题'},
					{name:"标题二",type:1,radio:'',textarea:'扶贫生产管理协议'},
					{name:"标题三",type:1,radio:'',textarea:'测试的标题2'},
					{name:"段落",type:2,radio:'',textarea:'小颖在目前负责的项目中,负责给同事提供所需组件,在这期间,我们家大颖姐姐让我 写个拖拽组件,一开始我是用click实现,先将你要拖拽的dom点一下,然后再点你要放的位置,这个dom再通过小颖写的方法,渲染在你要显示的地方,虽然功能实现了,可是没有实现拖拽,我那是点击,所以小颖今天就看了下html5的拖放,然后写了个小示例,希望对大家有所帮助'}
				],
				inputList:[
					{name:"数值组件",type:'cash',radio:'',cash:'100'},
					{name:"日期组件",type:'date',radio:'',date:'2018-5-1'},
					{name:"参与人组件",type:'person',radio:'',person:'甲方:_______________'},
					{name:"参与人组件",type:'person',radio:'',person:'乙方:_______________'},
				],
				actionList:[
					{name:"金额组件",type:'cash'},
					{name:"输出组件",type:'output'},
					{name:"接收组件",type:'getunit'},
				],
				areaList:[1,2,3,4,5,6,7,8,9,10],
				linkList:[],
				linkNumList:[],
				linkEnd:[],
				userList:[{name:"测试人员1",value:'1'},{name:"测试人员2",value:'2'}],
				linkNum:'',
				linkUser:'',
				
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
				showGrid:function(){
					var _self = this;
					_self.isShowGrid?_self.isShowGrid=false:_self.isShowGrid=true;
				},
				saveTemplate:function(){
					this.inputForm.number = this.linkList.length;
					if(this.inputForm.number==0){
						this.$Message.error('您还未构建完成完整的模版！');
						return false;
					}
					this.inputModal = true;
				},
				addLink:function(){
					var _index=1;
					this.linkList.push(_index);
				},
				deleteNode:function(index){
					var _id = 'span'+index;
					var oRemoveDom = document.getElementById(_id);
					if(oRemoveDom.getElementsByTagName('p').length>0){
						oRemoveDom.removeChild(oRemoveDom.getElementsByTagName('p')[0]);
					}
				},
				canDelete:function(index){
					var _id = 'span'+index;
					var oRemoveDom = document.getElementById(_id);
					var _len = oRemoveDom.getElementsByTagName('p').length;
					if(_len>0){
						oRemoveDom.getElementsByTagName('button')[0].style.display = "block";	
					}else{
						oRemoveDom.getElementsByTagName('button')[0].style.display = "none";	
					}
				},
				hideDelete:function(index){
					var _id = 'span'+index;
					var oRemoveDom = document.getElementById(_id);
					var _len = oRemoveDom.getElementsByTagName('p').length;
					if(_len>0){
						oRemoveDom.getElementsByTagName('button')[0].style.display = "none";
					}
				},
				showLinkNum:function(){
					var _self = this;
					if(_self.linkList.length==0){
						this.$Message.error('您还未创建金额组件！');
						return false;
					}
					if(_self.linkNumList.length>=1){
						_self.$Message.warning('您已经创建了接收组件了');
						return false;
					}
					_self.linkNumList.push(1);
				},
				showLinkEnd:function(){
					var _self = this;
					_self.linkEnd.length=0;
					if(Number(_self.linkNum)!='' && Number(_self.linkNum)>0){
						for(var i=0;i<Number(_self.linkNum);i++){
							_self.linkEnd.push(i);
						}
						_self.isCanShow = true;
					}else{
						_self.$Message.warning('请先输入环节数！');
						_self.isCanShow = false;
					}
					
				}
			}
		})	

	</script>
	
</body>
</html>