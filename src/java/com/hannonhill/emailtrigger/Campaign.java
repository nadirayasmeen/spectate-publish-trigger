/*
 * Created on Jan 16, 2015 by nadirayasmeen
 * 
 * Copyright(c) 2000-2010 Hannon Hill Corporation.  All rights reserved.
 */
package com.hannonhill.emailtrigger;

import java.util.ArrayList;
import java.util.List;

public class Campaign {
	String name;
	String id;
	List<String> recepients = new ArrayList<String>();
	
	public Campaign(){
		
	}
	public Campaign(String name, String id, List<String> recepients){
		this.name = name;
		this.id = id;
		this.recepients = recepients;
	}
}
