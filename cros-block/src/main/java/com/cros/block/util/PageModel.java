package com.cros.block.util;

import java.util.List;

public class PageModel<T>{  
	    //结果集   
	   private List<T> datas;   
	   //查询记录数   
	   private int rowCount;   
	   //每页多少条数据   
	   private int pageSize=20;  
	   //第几页   
	   private int pageNo=1;  
	   //跳过几条数  
	   private int skip=0;   
	   /** 
	     * 总页数 
	     * @return 
	     */   
	   public int getTotalPages(){   
	        return(rowCount+pageSize-1)/pageSize;   
	   }  
	     
	   public List<T> getDatas() {  
	      return datas;  
	   }  
	   public void setDatas(List<T>datas) {  
	      this.datas = datas;  
	   }  
	   public int getRowCount() {  
	      return rowCount;  
	   }  
	   public void setRowCount(int rowCount) {  
	      this.rowCount = rowCount;  
	   }  
	   public int getPageSize() {  
	      return pageSize;  
	   }  
	   public void setPageSize(int pageSize) {  
	      this.pageSize = pageSize;  
	   }  
	   public int getSkip() {  
	      skip=(pageNo-1)*pageSize;  
	      return skip;  
	   }  
	   public void setSkip(int skip) {  
	      this.skip = skip;  
	   }  
	   
	   public int getPageNo() {  
	      return pageNo;  
	   }  
	   
	   public void setPageNo(int pageNo) {  
	      this.pageNo = pageNo;  
	   }   
	    
	     
	     
	}  
