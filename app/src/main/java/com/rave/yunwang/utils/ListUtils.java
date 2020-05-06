package com.rave.yunwang.utils;

import com.rave.yunwang.bean.Entity;

import java.util.List;


/**
 * List相关类
 * @author zhanggx
 *
 */
public class ListUtils {
	/**
	 * 检查Entity的list是否相等
	 * @param list
	 * @param list2
	 * @return
	 */
	public static boolean checkEntityListEqual(List<Entity> list, List<Entity> list2){
		if (list==null||list2==null){
			return false;
		}
		//判断历史记录是否和原来的一样
		if (list.size()!=list2.size()){
			return false;
		}

		boolean same=true;
		for (int i=0;i<list.size();i++){
			if (!list.get(i).equals(list2.get(i))){
				same=false;
				break;
			}
		}
		return same;
	}
	/**
	 * 检查Entity是否在list中
	 * @param list
	 * @return
	 */
	public static boolean checkEntityInList(List<Entity> list, Entity entity){
		if (list==null||list.size()==0){
			return false;
		}
		for (int i=0;i<list.size();i++){
			if (list.get(i).equals(entity)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查list是否为空
	 *
	 * @param list
	 * @return
	 */
	public static boolean isListEmpty(List list){
		if (list==null||list.size()==0){
			return true;
		}
		return false;
	}
}
