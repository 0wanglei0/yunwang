package com.rave.yunwang.bean;

import java.io.Serializable;

/**
 * 实体基类
 * @author zgx
 *
 */
public abstract class Entity implements Comparable, Serializable {
	@Override
	public int compareTo(Object object) {
		// TODO Auto-generated method stub
		if (this==object){
			return 0; 
		}
		return -1;
	}

}
