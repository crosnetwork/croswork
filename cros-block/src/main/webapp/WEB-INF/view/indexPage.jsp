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
	<script type="text/javascript"src="js/echarts.common.min.js"></script>	
	<style>
		#vue-object>div{
			background:#F9FAFD;
			width:100%;
			padding-top:20px;
			border:1px solid transparent;
			box-sizing:border-box;
		}
		.cw{
			width:1120px;
			margin:0 auto;
		}
		.chart-ul li{
			margin:10px 0;
			width:540px;
			height:320px;
			border:1px solid lightgrey;
			box-sizing:border-box;
			border-radius:10px;
			overflow:hidden;
			box-shadow: 2px 6px 10px 0px rgba(49, 123, 247, 0.16);
			cursor:pointer;
		}
		.chart
		
	</style>
</head>
<body>
	<div id="vue-object">
		<div>
			<div class="index-header cw">
				<carousel v-model="startIndex" loop>
			        <carousel-item>
			            <div class="demo-carousel">
			            	<img src="<%=request.getContextPath()%>/static/img/banner-index-1.png" alt="介绍1" />
			            </div>
			        </carousel-item>
			        <carousel-item>
			            <div class="demo-carousel">
			            	<img src="<%=request.getContextPath()%>/static/img/banner-index-2.png" alt="介绍2" />
			            </div>
			        </carousel-item>
			    </carousel>
			    <div>
			    	<ul class="chart-ul clearfix">
			    		<li class="fl" id="chart1"></li>
			    		<li class="fr" id="chart2"></li>
			    		<li class="fl" id="chart3"></li>
			    		<li class="fr" id="chart4"></li>
			    	</ul>
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
				startIndex:0,
				
			},
			created:function(){
				this.mockRequest()
			},
			mounted:function(){
			    this.initChartsOne();
			    this.initChartsTwo();
			    this.initChartsThree();
			    this.initChartsFour();
			},
			methods:{
				initChartsOne:function(){
					var myChart = echarts.init(document.getElementById('chart1'));					
				    var option = {
				    	backgroundColor:"rgba(39, 53, 75)",
				        title: {
				            text: '当月Token创建总量',
				            textStyle:{color:"white"}
				        },
				        tooltip: {},
				        xAxis: {
				        	axisLabel : {		                           
	                            textStyle: {color: '#fff'}
	                        },
				            data: ["2018/5/1","2018/5/2","2018/5/3","2018/5/4","2018/5/5","2018/5/6"]
				        },
				        yAxis: [
		                    {
		                        axisLabel : {textStyle: {color: '#fff'}
		                        }
		                    }
		        		],
				        series: [{
				            name: '销量',
				            type: 'line',
				            data: [232, 467, 1236, 854, 201, 478],
				            itemStyle : {  
                                normal : {  
                                    color:"white",
                                    lineStyle:{color:'#39D8FF'}  
                                }  
                            },  
				        }]
				    };  
			    	myChart.setOption(option);
				},
				initChartsTwo:function(){
					var myChart = echarts.init(document.getElementById('chart2'));					
				    var option = {
						    title:{
							    left:'center',
						    	text:"前十侧链Token总价值在所有Token总值中的占比"
							},
				    		tooltip: {
				    	        trigger: 'item',
				    	        formatter: "{a} <br/>{b}: {c} ({d}%)"
				    	    },
				    	    legend: {
				    	        orient: 'vertical',
				    	        x: 'left',
				    	        data:['侧链1','侧链2','侧链3','侧链4','侧链5']
				    	    },
				    	    series: [
				    	        {
				    	            name:'访问来源',
				    	            type:'pie',
				    	            radius: ['50%', '70%'],
				    	            avoidLabelOverlap: false,
				    	            label: {
				    	                normal: {
				    	                    show: false,
				    	                    position: 'center'
				    	                },
				    	                emphasis: {
				    	                    show: true,
				    	                    textStyle: {
				    	                        fontSize: '30',
				    	                        fontWeight: 'bold'
				    	                    }
				    	                }
				    	            },
				    	            labelLine: {
				    	                normal: {show: false}
				    	            },
				    	            data:[
				    	                {value:335, name:'侧链1'},
				    	                {value:310, name:'侧链2'},
				    	                {value:234, name:'侧链3'},
				    	                {value:135, name:'侧链4'},
				    	                {value:1548, name:'侧链5'}
				    	            ]
				    	        }
				    	    ]
				    };  
			    	myChart.setOption(option);
				},
				initChartsThree:function(){
					var myChart = echarts.init(document.getElementById('chart3'));	
					var option = {
						    tooltip: {
						        trigger: 'axis',
						        axisPointer: {
						            type: 'cross',
						            crossStyle: {color: '#999'}
						        }
						    },
						    legend: {data:['智能合约采用量','智能合约总量']},
						    xAxis: [
						        {
						            type: 'category',
						            data: ["2018/5/1","2018/5/2","2018/5/3","2018/5/4","2018/5/5","2018/5/6","2018/5/7"],
						            axisPointer: {type: 'shadow'},
						        }
						    ],
						    yAxis: [
						        {
						            type: 'value',
						            min: 0,
						            interval: 500,
						        }
						    ],	
						    series: [
						        {
						            name:'智能合约采用量',
						            type:'bar',
						            data:[123, 345, 452, 788, 1290, 1680,2397],
						            itemStyle:{  
					                     normal:{color:'#3682EF'}  
					                },  
						       		
						        },
						        {
						            name:'智能合约总量',
						            type:'line',
						            data:[300, 500, 1000, 1200, 1500,1800,2500],
						            itemStyle:{  
					                     normal:{color:'#FF9704'}  
					                },  
						        },
						        
						    ]
						};
					myChart.setOption(option);
				},
				initChartsFour:function(){
					var myChart = echarts.init(document.getElementById('chart4'));	
					var option = {
							backgroundColor:"rgba(39, 53, 75)",
							title:{
								 textStyle:{
										color:"white"
							        }
							},
						    tooltip: {
						        trigger: 'axis',
						        axisPointer: {
						            type: 'cross',
						            crossStyle: {
						                color: '#999'
						            }
						        }
						    },
						    legend: {data:['当月众包工作流发布量','当月众包工作流完成数'],textStyle: {color: '#fff'}},
						    xAxis: [
						        {	
						            type: 'category',
						            data: ["2018/5/1","2018/5/2","2018/5/3","2018/5/4","2018/5/5","2018/5/6","2018/5/7"],
						            axisPointer: {type: 'shadow'},
						            axisLabel : {		                           
		                           		textStyle: {color: '#fff'}
		                        	},
						        }
						    ],
						    yAxis: [
						        {
						            type: 'value',
						            min: 0,
						            interval: 500,
						            axisLabel : {		                           
		                           		textStyle: {color: '#fff'}
		                        	},
						        }
						    ],	
						    series: [
						        {
						            name:'当月众包工作流完成数',
						            type:'bar',
						            data:[123, 345, 452, 788, 1290, 1680,2397],
						            itemStyle:{  
					                     normal:{  
					                       color:'#3682EF',  
					                       }  
					                },  
						       		
						        },
						        {
						            name:'当月众包工作流发布量',
						            type:'line',
						            data:[300, 500, 1000, 1200, 1500,1800,2500],
						            itemStyle:{  
					                     normal:{  
					                       color:'#39D8FF',  
					                       }  
					                },  
						        },
						        
						    ]
						};
					myChart.setOption(option);
				},
				mockRequest:function(){
					var _self = this;
					_self.spinShow = true;
					setTimeout(function(){
						_self.spinShow = false;
					},1000)
				},
			}
	
		});

	</script>
	
</body>
</html>