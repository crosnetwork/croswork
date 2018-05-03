//Daylv-122821315@qq.com

/*
 * const and function for common --2018.4.25
 */

//获取url参数
var getQueryString = function(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if(r != null) return unescape(r[2]);
	return null;
}
// api调用出现错误回调函数
var invoke_api_error = function() {
	alert("error,接口出现异常。")
};

//api调用中loading画面
var invoke_loading = function() {
	
};

// 调用完毕
var invoke_complete = function() {
	
};

//var hrp_server = 'http://localhost:8080/cros-block';


/**
 * ajax
 * 调用快捷方式 
 * @param {Object} api
 * @param {Object} params
 * @param {Object} callback
 * @param {Object} useMask
 */
function invokeApiSimple(api, params, callback, useMask) {

	var successFun = function(ret) {
		if(ret.code == "200") {
			if(callback) {
				callback(ret);
			}
		} else {
			msg(ret.data);
		}
	};

	if(useMask) {
		invokeHrpApi(api.url, api.method, params, successFun, invoke_api_error, invoke_loading, invoke_complete);
	} else {
		invokeHrpApi(api.url, api.method, params, successFun, invoke_api_error);
	}
}

/**
 * 
 * @param {Object} url
 * @param {Object} method
 * @param {Object} data
 * @param {Object} successFun
 * @param {Object} errorFun
 * @param {Object} beforeSendFun
 * @param {Object} completeFun
 */
function invokeHrpApi(url, method, data1, successFun, errorFun, beforeSendFun, completeFun) {

	$.ajax({
		type: method,
		url: url,
		async: true,
		dataType: 'json',
		data: data1,
		success: function(rdata) {

			if(successFun) {
				successFun(rdata);
			}

		},
		error: function(xhr, textStatus) {

			if(errorFun) {
				errorFun();

			} else {

				if(invoke_api_error) {
					invoke_api_error();
				} else {
					alert("接口调用出现异常");
				}
			}

		},
		beforeSend: function(xhr) {

			if(beforeSendFun) {
				beforeSendFun();
			}
		},

		complete: function(xhr, msg) {

			if(completeFun) {
				completeFun();
			}
		}
	});

}

/**
 * 同步调用
 * @param {Object} url
 * @param {Object} data
 * @param {Object} successFun
 * @param {Object} errorFun
 * @param {Object} beforeSendFun
 * @param {Object} completeFun
 */
function invokeHrpApiSync(url, method, data, successFun, errorFun, beforeSendFun, completeFun) {

	$.ajax({
		type: method,
		url: url,
		async: false,
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		data: JSON.stringify(data),
		success: function(rdata) {

			if(successFun) {
				successFun(rdata);
			}

		},
		error: function(xhr, textStatus) {

			if(errorFun) {
				errorFun();

			} else {

				alert("接口调用出现异常");
			}

		},
		beforeSend: function(xhr) {

			if(beforeSendFun) {
				beforeSendFun();
			}
		},

		complete: function(xhr, msg) {

			if(completeFun) {
				completeFun();
			}
		}
	});

}