

package com.cros.block.util;

import java.util.List;

/**
 * ClassName:Page <br/>
 * Function: TODO 封装分页Page类
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年4月18日 下午3:32:42 <br/>
 * 
 * @author youhongkun
 * @version
 * @since JDK 1.6
 * @see
 */
public class Page extends BasePage {
	private static final long serialVersionUID = -970177928709322222L;

	public final static ThreadLocal<Page> threadLocal = new ThreadLocal<Page>();

	private List<?> data;

	public Page() {
	}

	public Page(int currentPage, int pageSize) {
		super(currentPage, pageSize);
	}
	
	public Page(int currentPage, int pageSize, int totalCount) {
		super(currentPage, pageSize, totalCount);
	}

	public Page(int currentPage, int pageSize, int totalCount, List<?> data) {
		super(currentPage, pageSize, totalCount);
		this.data = data;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<?> data) {
		this.data = data;
	}
}
