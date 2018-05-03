package com.cros.block.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 与版权家登记查询功能接口,该接口由我方提供,对方来调用
 * @creator     youhongkun
 * @create-time Mar 11, 2016   10:26:22 PM
 * @version 1.0
 */
@Path("/weibei")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IWeibeiMsgApi {
	/**
	 * 提交版权登记信息
	 * @param params
	 * @return
	 */
	@POST
	@Path("/newtx")
	public String coprRegisterData(String params);

	/**
	 * 查询版权信息
	 * @param params
	 * @return
	 */
	@POST
	@Path("/gettx")
	public String coprQueryData(String params);
}
