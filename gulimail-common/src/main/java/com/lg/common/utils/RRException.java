/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.lg.common.utils;

import com.lg.common.exception.BizCodeEnum;

/**
 * 自定义异常
 *
 * @author Mark sunlightcs@gmail.com
 */
public class RRException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    private String msg;
    private int code = 500;
    
    public RRException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public RRException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}
	
	public RRException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}
	
	public RRException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

	public RRException(BizCodeEnum bizCodeEnum) {
		super(bizCodeEnum.getMsg());
		this.msg = bizCodeEnum.getMsg();
		this.code = bizCodeEnum.getCode();
	}

	public RRException(BizCodeEnum bizCodeEnum, Throwable e) {
		super(bizCodeEnum.getMsg(), e);
		this.msg = bizCodeEnum.getMsg();
		this.code = bizCodeEnum.getCode();
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	
}
