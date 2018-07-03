package com.scenetec.email.po;

import java.util.List;
import java.util.Map;

public class EmailInfo {
	
	private Person person;
	private Department department;
	private Map<String, Object> map;
	private List<Department> departmentMap;
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	public Map<String, Object> getMap() {
		return map;
	}
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	public List<Department> getDepartmentMap() {
		return departmentMap;
	}
	public void setDepartmentMap(List<Department> departmentMap) {
		this.departmentMap = departmentMap;
	}

}
