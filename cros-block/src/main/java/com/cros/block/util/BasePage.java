package com.cros.block.util;

import java.io.Serializable;

/**
 * ClassName:BasePage <br/>
 * Function: TODO 封装分页Page类
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年4月18日 下午2:52:26 <br/>
 * 
 * @author youhongkun
 * @version
 * @since JDK 1.6
 * @see
 */
public class BasePage implements Serializable {
	private static final long serialVersionUID = -3623448612757722222L;
	public final static int DEFAULT_PAGE_SIZE = 10;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private int currentResult;
	private int totalPage;
	private int currentPage = 1;
	private int totalCount = -1;

	public BasePage(int currentPage, int pageSize, int totalCount) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
	}
	
	public BasePage(int currentPage, int pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}

	public int getTotalCount() {
		return this.totalCount;
	}

	public void setTotalCount(int totalCount) {
		if (totalCount < 0) {
			this.totalCount = 0;
			return;
		}
		this.totalCount = totalCount;
	}

	public BasePage() {
	}

	public int getFirstResult() {
		return (this.currentPage - 1) * this.pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize < 0) {
			this.pageSize = DEFAULT_PAGE_SIZE;
			return;
		}
		this.pageSize = pageSize;
	}

	public int getTotalPage() {
		if (this.totalPage <= 0) {
			this.totalPage = (this.totalCount / this.pageSize);
			if ((this.totalPage == 0) || (this.totalCount % this.pageSize != 0)) {
				this.totalPage += 1;
			}
		}
		return this.totalPage;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageNo(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageNo() {
		return this.currentPage;
	}

	public boolean isFirstPage() {
		return this.currentPage <= 1;
	}

	public boolean isLastPage() {
		return this.currentPage >= getTotalPage();
	}

	public int getNextPage() {
		if (isLastPage()) {
			return this.currentPage;
		}
		return this.currentPage + 1;
	}

	public int getCurrentResult() {
		this.currentResult = ((getPageNo() - 1) * getPageSize());
		if (this.currentResult < 0) {
			this.currentResult = 0;
		}
		return this.currentResult;
	}

	public int getPrePage() {
		if (isFirstPage()) {
			return this.currentPage;
		}
		return this.currentPage - 1;
	}
}
