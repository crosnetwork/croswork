<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<base href="<%=basePath %>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CROS平台</title>
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="static/easyUi/themes/icon.css">
	<script type="text/javascript" src="static/easyUi/jquery.min.js"></script>
	<script type="text/javascript" src="static/easyUi/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="static/easyUi/locale/easyui-lang-zh_CN.js"></script>
<style type="text/css">
	*{
		margin: 0px;
		padding: 0px;
	}
	a{text-decoration: none}
 	#head.li{
		display: inline-block;
		<!-- background-color: #7190E0; -->
	} 
	#orgName{
		position:absolute;
		left:42%;
		top:10px;
		font-size: 25px;
		font-weight: bolder;
		font-family: serif;
	}
	#adminName{
		position:absolute;
		right:5%;
		top:18px;
		font-size: 15px;
		font-weight: bolder;
		font-family: serif;
		
	}
	#adminPhoto{
		position:absolute;
		right:9%;
		top:5px;
	}
	#logout{
		position:absolute;
		right:2%;
		top:10px;
	}
</style>
</head>
<body>
	<div class="easyui-layout" style="width:100%;height:636px;">
		<div id="head" data-options="region:'north'" style="height:60px">
		<div class="nav">
            <ul>
            <li class="meun in1"><a href="#">首页</a>
                <ul id="sub_01" class="sub" >
                    <li><a href="#" src="a.aspx" class="cs-navi-tab">aaaaaaaaaaaa</a></li>
                    <li><a href="#" src="b.aspx" class="cs-navi-tab">bbbbbbbbbbb</a></li>
                    <li><a href="#" src="c.aspx" class="cs-navi-tab">cccccccccccccc</a></li>
                    <li><a href="#" src="d.aspx" class="cs-navi-tab">ddddddddddd</a></li>
                    <li><a href="#" src="e.aspx" class="cs-navi-tab">eeeeeeeeeeeee</a></li>
                </ul>
            </li>
            <li class="meun in2"><a href="#">关于我们</a>
                <ul id="sub_02" class="sub" >
                    <li><a href="#">公司介绍</a></li>
                    <li><a href="#">企业文化</a></li>
                    <li><a href="#">人才招聘</a></li>
                    <li><a href="#">公司大事记</a></li>
                </ul>
            </li>
            <li class="meun in3"><a href="#">新闻动态</a>
                <ul id="sub_03" class="sub" >
                    <li><a href="#">新闻动态11111111新闻动态</a></li>
                    <li><a href="#">新闻动态222222222新闻动态2</a></li>
                    <li><a href="#">新闻动态33333333新闻动态</a></li>
                    <li><a href="#">新闻动态4444444444444444新闻动态</a></li>
                </ul>
            </li>
            </ul>
    </div>
		</div>
		<div data-options="region:'west',split:true" style="width:200px;">
			
		</div>
		<div data-options="region:'center',iconCls:'icon-ok'">
			<!-- <div id="tt" class="easyui-tabs" data-options="tools:'#tab-tools'" style="width:100%;height:100%">
			
			</div> -->
		</div>
	</div>
	<div id="win"></div>
	
</body>
<script type="text/javascript">
		function addmenu() {
		    var header = $('.layout-expand .layout-button-down').parent().parent();
		    var menu = $('<div style="position:fixed;left:0;top:0;background:#fafafa;"></div>').appendTo(header);
		    var btn = $('<a href="#">test</a>').appendTo(menu);
		    btn.menubutton({
		        menu: '#mymenu'
		    });
		}
		$(function () {
		    $('.meun').hover(function () { $(this).find('ul').stop(true, true).slideDown(); },
		function () {
		    $(this).find('ul').stop(true, true).slideUp('fast');
		})
		}) 
		
		function addTab(title, url) {
		    if ($('#tabs').tabs('exists', title)) {
		        $('#tabs').tabs('select', title); 
		        var currTab = $('#tabs').tabs('getSelected');
		        var url = $(currTab.panel('options').content).attr('src');
		        if (url != undefined && currTab.panel('options').title != 'Home') {
		            $('#tabs').tabs('update', {
		                tab: currTab,
		                options: {
		                    content: createFrame(url)
		                }
		            })
		        }
		    } else {
		        var content = createFrame(url);
		        $('#tabs').tabs('add', {
		            title: title,
		            content: content,
		            closable: true
		        });
		    }
		    tabClose();
		}
		function createFrame(url) {
		    var s = '<iframe scrolling="auto" frameborder="0"  src="' + url + '" style="width:100%;height:100%;"></iframe>';
		    return s;
		}

	//easyUI 选项卡
	function addPanel(title,url,funcType){
		//判断标签页是否存在 存在 不要在打开
		var flag = $('#tt').tabs('exists',title);
		if(!flag){
			var show = false;
			if (funcType==0) {
				show = true;
			}
				$('#tt').tabs('add',{
				    title:title,
				    content:'<iframe src="'+url+'" width="100%" height="100%" scrolling=auto frameborder=0></iframe>',
					closable:show,
					onClose:funcType
				});
			}else{
				//存在 就打开现有的
				$('#tt').tabs('select',title);	
		}
	}
 	 $('#tt').tabs({
		  onBeforeClose: function(title){
			  var target = this;
				$.messager.confirm('提示信息','确定关闭 '+title +' 页面么？',function(r){
					if (r){
						var opts = $(target).tabs('options');
						var bc = opts.onBeforeClose;
						opts.onBeforeClose = function(){};  // allowed to close now
						$(target).tabs('close',title);
					}
				});
				return false;	// prevent from closing
		  }
		}); 
		//给返回按钮提供返回到菜单的方法
 		function tabsClose(){  
 		    var tab=$('#tt').tabs('getSelected');//获取当前选中tabs  
 		    var index = $('#tt').tabs('getTabIndex',tab);//获取当前选中tabs的index  
 		    $('#tt').tabs('close',index);//关闭对应index的tabs  
 		}  

	//打开一个窗口
	var openWindow = function(options){
		 options = !options ? {} :options;
         options.width = !options.width ? 500 : options.width;
         options.height = !options.height ? 400 : options.height;
         options.url = !options.url ? "" : options.url;
         options.title = !options.title ? "未知窗口" : options.title;
         options.close = !options.close ? function(){} : options.close;
		
		$('#win').window({
			title:options.title,
		    width:options.width,
		    height:options.height,
		    inline:false,
		    modal:true,
		    content:'<iframe src="'+options.url+'" width="100%" height="100%" scrolling=auto frameborder=0></iframe>',
			onClose:options.close
			});
	}

	var closeWindow = function(){
		$('#win').window('close')
	}

	var logout = function(){
		$.messager.confirm('确认消息窗口', '你确定退出系统么???', function(r){
			if (r){
				$.ajax({
					url:"logout.do",
					type:"GET",
					async:true,
					data:{},
					dataType:"json",
					success:function(data,status){
						window.location.href="login.jsp";
					   },
					})
			}
		});
	}
</script>
</html>