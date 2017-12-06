/**
 *
 *汇付天下有限公司
 * Copyright (c) 2006-2013 ChinaPnR,Inc.All Rights Reserved.
 */
package com.huifu.virgo.remote.model;

import java.io.Serializable;

/**
 * @author: alex.wang
 * @version: $Id: ReflectHelper.java v0.1 2013-8-13 下午6:06 alex.wang $
 * @date:2013-8-13 下午6:06
 */
public class Page implements Serializable {

    /**  */
    private static final long serialVersionUID  = 5448730297269867672L;

    /**  */

    public static final int   DEFAULT_PAGE_SIZE = 10;

    private int               pageSize;
    private int               currentPage;
    private int               prePage;
    private int               nextPage;
    private int               totalPage;
    private int               totalCount;

    private int               limit;

    public Page() {
        this.currentPage = 1;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }
    
    public int getStartRows(){
        if(currentPage == 0){
            return 0;
        }
        return pageSize*currentPage - pageSize;
    }
    
    public int getEndRows(){
        if(currentPage == 0){
            return pageSize;
        }
        return pageSize*currentPage;
    }

    /**
     * 
     * @param currentPage
     * @param pageSize
     */
    public Page(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
