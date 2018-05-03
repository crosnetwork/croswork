package com.cros.block.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 与版权家登记查询功能接口,该接口由我方提供,对方来调用
 * @creator     yeran
 * @create-time 2018-04-26 15:15:00
 * @version 1.0
 */
@Path("/cros")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ICrosMsgApi {
	/**
	 * 提交版权登记信息
	 * @param params
	 * @return
	 */
	@POST
	@Path("/newtx")
	public String crosRegisterData(String params);

	/**
	 * 查询版权信息
	 * @param params
	 * @return
	 */
	@POST
	@Path("/gettx")
	public String crosQueryData(String params);
}
