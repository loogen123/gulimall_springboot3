/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */
package com.lg.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lg.common.exception.BizCodeEnum;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public R() {
		put("code", 0);
		put("msg", "success");
	}
	
	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	
	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R error(BizCodeEnum bizCodeEnum) {
		return error(bizCodeEnum.getCode(), bizCodeEnum.getMsg());
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R();
	}

	public static R ok(BizCodeEnum bizCodeEnum) {
		R r = new R();
		r.put("code", bizCodeEnum.getCode());
		r.put("msg", bizCodeEnum.getMsg());
		return r;
	}

	@Override
    public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode() {
		return (Integer) this.get("code");
	}

	public <T> T getData(TypeReference<T> typeReference) {
		Object data = get("data");
		String jsonString = JSON.toJSONString(data);
		T t = JSON.parseObject(jsonString, typeReference);
		return t; // 确保返回的是 T，而不是 Object
	}

	public <T> T getData(String key, TypeReference<T> typeReference) {
		Object data = get(key);
		// 将对象转为 JSON 字符串，再反序列化回目标 VO 对象
		String jsonString = JSON.toJSONString(data);
		return JSON.parseObject(jsonString, typeReference);
	}
	public R setData(Object data) {
		put("data", data);
		return this;
	}
}
