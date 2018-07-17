package com.scenetec.email.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.bean.ParamBean;
import com.scenetec.email.exception.WeixinRequestException;
import com.scenetec.email.po.Department;
import com.scenetec.email.po.EmailInfo;
import com.scenetec.email.po.Person;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import com.scenetec.email.util.HttpClientUtil;

@Component
public class ScenetecMailService {
	
	private static Logger logger = LoggerFactory.getLogger(ScenetecMailService.class);

	@Autowired
	private ParamBean paramBean;
	@Autowired
	private LdapManager ldap;
	
	private long lastGetTokenTimestamp=0;
    private String token;
    private Map<String, Department> departM = new HashMap<String, Department>();
    private List<Person> emailUserList = null;
    
    @Scheduled(fixedRate = 3600000,initialDelay=40000)
	public void getLadpData() {
    	logger.info("---调用同步企业邮箱服务开始---");
		// LdapManager ldap = new LdapManager();
			// ladp人员信息
			List<LdapPerson> ldapPersonList = ldap.search();
			// 机构信息
			List<LdapDepartment> ldapDepartmentList = ldap.getDepartmentTreeFromPerson(ldapPersonList);
			// 获取企业邮箱机构信息
			Map<String, Department> departmentMap = deparementAll();
			// 调用机构比较方法
			List<LdapDepartment> addList = operDate(ldapDepartmentList, departmentMap);
			// 新增机构
			scenetecMailDepartment(addList);
			//梳理人员机构信息，防止同一个人存在多个部门的请求
			//邮箱中所有用户
			getEmailAllUser();
			// 人员信息
			scenetecMailPerson(ldapPersonList);
			// 获取待删除人员列表
			List<String> deleteUserList = getDeleteUserList(ldapPersonList);
			//删除人员
			deleteUser(deleteUserList);
			//获取待删除部门列表
			List<Department> deleteDepartmentList = getDeleteDepartment(ldapDepartmentList,departmentMap);
			//删除部门
			deleteDepartment(deleteDepartmentList);
			logger.info("---调用同步企业邮箱服务结束---");	
	}

	// 同步机构
	public void scenetecMailDepartment(List<LdapDepartment> addList) {
		Map<String, String> departmentIdMap = new HashMap<>();
		//新建的部门列表
		if (addList != null && addList.size() > 0) {
			while(true) {
				if(addList == null || addList.size() == 0) {
					return;
				}
				for (int i=0;i< addList.size();i++) {
					LdapDepartment ldapDepartment = addList.get(i);
					String parentId = ldapDepartment.getParentId();
					//为空说明父节点不存在
					if (!StringUtils.isEmpty(parentId)) {
								// 调用创建部门服务
								String createDepartmentUrl = paramBean.getDepartmentCreate() + "?access_token=" + getToken();
								JSONObject param = new JSONObject();
								param.put("name", ldapDepartment.getName());
								param.put("parentid", Long.valueOf(parentId));
								String createDepRes = HttpClientUtil.sendEmailPost(param.toJSONString(), createDepartmentUrl);
								checkException(createDepRes);
								JSONObject object = JSONObject.parseObject(createDepRes);
								String errorCode = String.valueOf(object.get("errcode"));
								if("0".equals(errorCode)) {
								   // departM.put(key, value)
								    Department depart = new Department();
								    depart.setId(String.valueOf(object.get("id")));
								    depart.setName(ldapDepartment.getName());
								    departM.put(ldapDepartment.getName(), depart);
								}
								logger.info("创建邮箱部门入参："+param.toJSONString()+",结果："+createDepRes);
								addList.remove(i);
								JSONObject json = JSONObject.parseObject(createDepRes);
								departmentIdMap.put(ldapDepartment.getName(), json.getString("id"));
							}else {
								//在新增
								if(departmentIdMap.containsKey(ldapDepartment.getParentName())){
									ldapDepartment.setParentId(departmentIdMap.get(ldapDepartment.getParentName()));
									addList.remove(i);
									addList.add(ldapDepartment);
								}
							}
						}
			}
			
				}
	}

	// 新增成员
	public void addUser(LdapPerson ldapPerson) {
		// 获取所有机构
		//Map<String, Department> departmentMap = deparementAll();
	    Map<String, Department> departmentMap = departM;
		// 创建人员
		JSONObject param = new JSONObject();
		String userId = ldapPerson.getCn();
		param.put("userid", userId);
		param.put("name", ldapPerson.getSn());
		// 人员部门信息
		List<String> dppartments = ldapPerson.getDepartments();
		if (dppartments != null && dppartments.size() > 0) {
			Department departmet = departmentMap.get(dppartments.get(0));
			param.put("department", new long[] { Long.valueOf(String.valueOf(departmet.getId())) });
		}
		param.put("mobile", ldapPerson.getMobile());
		param.put("password", paramBean.getDefaultPwd());
		String createUserUrl = paramBean.getUserCreate() + "?access_token=" + getToken();
		String createUserRes = HttpClientUtil.sendEmailPost(param.toJSONString(), createUserUrl);
		checkException(createUserRes);
		logger.info("创建人员入参："+param.toJSONString()+",创建结果:"+createUserRes);
	}

	// 更新成员信息
	public void updateUser(LdapPerson ldapPerson) {
	    Map<String, Department> departmentMap = departM;
        String userId = ldapPerson.getCn();
        // 调用查询成员服务
        Map<String, Person> emailPersons = emailUserList.stream().collect(Collectors.toMap(Person::getUserId, person->person));
	    if(equealObj(ldapPerson,emailPersons.get(userId))) {
	        return;
	    }
		//Map<String, Department> departmentMap = deparementAll();
	   
		JSONObject updateParam = new JSONObject();
		updateParam.put("userid", userId);
		updateParam.put("name", ldapPerson.getSn());
		List<String> dppartments = ldapPerson.getDepartments();
		if (dppartments != null && dppartments.size() > 0) {
			Department departmet = departmentMap.get(dppartments.get(0));
			updateParam.put("department", new long[] { Long.valueOf(String.valueOf(departmet.getId())) });
		}
		updateParam.put("mobile", ldapPerson.getMobile());
		String updateUserUrl = paramBean.getUserUpdate() + "?access_token=" + getToken();
		String userUpadtRes = HttpClientUtil.sendEmailPost(updateParam.toJSONString(), updateUserUrl);
		checkException(userUpadtRes);
		logger.info("更新邮箱人员信息入参："+updateParam.toJSONString()+",更新结果："+userUpadtRes);
	}

	// 同步成员信息
	public void scenetecMailPerson(List<LdapPerson> ldapPersonList) {

		for (LdapPerson ldapPerson : ldapPersonList) {
			//String userId = ldapPerson.getCn() + "@scenetec.com";
			if (isExitUser(ldapPerson)) {
				// 更新成员信息
				updateUser(ldapPerson);
			} else {
				// 新增成员信息
				addUser(ldapPerson);
			}
		}

	}
	// 获取企业邮箱中的所有部门
	public Map<String, Department> deparementAll() {
		
				// 获取服务列表
				String departmentListUrl = paramBean.getDepartmentList() + "?access_token=" + getToken();
				String departmentListRes = HttpClientUtil.sendEmailGet(null, departmentListUrl);
				checkException(departmentListRes);
				if (!StringUtils.isEmpty(departmentListRes)) {
					JSONObject departmentListResJson = JSONObject.parseObject(departmentListRes);
					String errorCode = String.valueOf(departmentListResJson.get("errcode"));
					if ("0".equals(errorCode)) {
						JSONArray jsonArray = JSONArray
								.parseArray(String.valueOf(departmentListResJson.get("department")));
						if (jsonArray != null && jsonArray.size() > 0) {
							for (Object object : jsonArray) {
								JSONObject jsonObject = JSONObject.parseObject(String.valueOf(object));
								Department depart = new Department();
								depart.setId(String.valueOf(jsonObject.get("id")));
								depart.setName(String.valueOf(jsonObject.get("name")));
								depart.setParentId(String.valueOf(jsonObject.get("parentid")));
								departM.put(String.valueOf(jsonObject.get("name")), depart);
							}
						}
					}
				}
		return departM;
	}

	// 获取企业邮箱中的所有成员
	public List<Person> getEmailAllUser() {
		// 获取根部门及以下子部门的所有成员信息
		String userSimpleUrl = paramBean.getUserSimpleList() + "?access_token=" + getToken()
				+ "&department_id=1&fetch_child=1";
		String userSimpleRes = HttpClientUtil.sendEmailGet(null, userSimpleUrl);
		checkException(userSimpleRes);
		if (!StringUtils.isEmpty(userSimpleRes)) {
			JSONObject userSimpleResJson = JSONObject.parseObject(userSimpleRes);
			String userSimpleErrCode = String.valueOf(userSimpleResJson.get("errcode"));
			// 等于0表示成功
			if ("0".equals(userSimpleErrCode)) {
				JSONArray jsonArray = JSONArray.parseArray(String.valueOf(userSimpleResJson.get("userlist")));// 成员列表
				if (jsonArray != null && jsonArray.size() > 0) {
					// 获取的企业邮箱中的成员userId列表
					emailUserList = new ArrayList<Person>();
					for (Object object : jsonArray) {
						JSONObject userJsonObject = JSONObject.parseObject(String.valueOf(object));
						String userId = String.valueOf(userJsonObject.get("userid"));
						Person person = new Person();
						person.setUserId(userId);
						person.setName(String.valueOf(userJsonObject.get("name")));
						person.setMobile(String.valueOf(userJsonObject.get("mobile")));
						List<String> list = new ArrayList<String>();
						JSONArray array = (JSONArray)userJsonObject.get("department");
						for (Object object2 : array) {
                            list.add(String.valueOf(object2));
                        }
						person.setDepartment(list);
						person.setEnable(String.valueOf(userJsonObject.get("enable")));
						//person.setDepartment(JSONArray.parse(String.valueOf(userJsonObject.get("department"))).);
						emailUserList.add(person);
					}
					return emailUserList;
				}
			} else {
				// 失败
				logger.error("更新成员信息失败："+userSimpleRes);
			}
		}
		return null;
	}

	// 待删除用户
	public List<String> getDeleteUserList(List<LdapPerson> ldapPersonList) {
		List<String> emailUserIdList = emailUserList.stream().map(Person::getUserId).collect(Collectors.toList());
		List<String> ldapUserList = new ArrayList<String>();
		for (LdapPerson ldapPerson : ldapPersonList) {
			ldapUserList.add(ldapPerson.getCn());
		}
		//
		emailUserIdList.removeAll(ldapUserList);
		
		// 系统邮箱
		List<String> systemEmailList = paramBean.getSystemEmail();
		emailUserIdList.removeAll(systemEmailList);
		return emailUserIdList;
	}

	// 比较
	// 比较机构数据中的各种操作
	public List<LdapDepartment> operDate(List<LdapDepartment> ldapDepartmentList,
			Map<String, Department> departmentMap) {
		List<LdapDepartment> list = new ArrayList<LdapDepartment>();
		// 判断ldap中的机构是否在企业邮箱中存在
		for (LdapDepartment ldapDepartment : ldapDepartmentList) {
			String name = ldapDepartment.getName();
			String parentName = ldapDepartment.getParentName();
			if (departmentMap.containsKey(name)) {
				continue;
			}
			if (!StringUtils.isEmpty(parentName)) {
				// 判断父节点是否存在
				if (departmentMap.containsKey(parentName)) {
					Department department = departmentMap.get(parentName);
					ldapDepartment.setParentId(department.getId());
				}
			} else {
				ldapDepartment.setParentId("1");
			}

			list.add(ldapDepartment);
		}
		return list;
	}

	//待删除的部门
	public List<Department> getDeleteDepartment(List<LdapDepartment> ldapDepartmentList,Map<String, Department> departmentMap){
		List<String> ldapDepartmentNameList = new ArrayList<String>();
		List<Department> deleteDepartmentList = new ArrayList<Department>();
		for (LdapDepartment ldapDepartment : ldapDepartmentList) {
			ldapDepartmentNameList.add(ldapDepartment.getName());
		}
		for (Map.Entry<String, Department> entry : departmentMap.entrySet()) {
			//判断当前机构是否是要删除的部门
			if(!ldapDepartmentNameList.contains(entry.getKey())) {
				//删除当前部门
				deleteDepartmentList.add(entry.getValue());
				
			}
		}
		return deleteDepartmentList;
	}
	//获取token
	public String getToken() {
	   
	    if(lastGetTokenTimestamp==0) {
	        getEmailToken();
	    }else {
	        //判断token是否实效
	        if(tokenExpired()) {
	            getEmailToken(); 
	        }
	    }
		
		return token;
	}
 //获取token
	public void getEmailToken() {
	    Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("corpid", paramBean.getCorpid());
        parameter.put("corpsecret", paramBean.getCorpsecret());
        String getTokenRes = HttpClientUtil.sendEmailGet(parameter, paramBean.getGetTokenUrl());
        checkException(getTokenRes);
        if (!StringUtils.isEmpty(getTokenRes)) {
            JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
            String errCode = String.valueOf(getTokenResJson.get("errcode"));
            String errmsg = String.valueOf(getTokenResJson.get("errmsg"));
            if ("0".equals(errCode)) {
                token = String.valueOf(getTokenResJson.get("access_token"));
                lastGetTokenTimestamp = System.currentTimeMillis();
            }
        }
	}
    /***
     * @return token是否已经失效
     */
    public boolean tokenExpired() {
        return System.currentTimeMillis() - lastGetTokenTimestamp > 7200*1000;
    }
	// 删除成员服务
	public void deleteUser(List<String> deleteUserList) {
		if (deleteUserList != null && deleteUserList.size() > 0) {
			for (String userId : deleteUserList) {
			    if(isEnable(userId)) {
			        return;
			    }
			       JSONObject updateParam = new JSONObject();
			        updateParam.put("userid", userId);
			        updateParam.put("enable", 0);//禁用
			        String updateUserUrl = paramBean.getUserUpdate() + "?access_token=" + getToken();
			        String userUpadtRes = HttpClientUtil.sendEmailPost(updateParam.toJSONString(), updateUserUrl);
			        checkException(userUpadtRes);
			        logger.info("禁用邮箱人员信息入参："+updateParam.toJSONString()+",禁用结果："+userUpadtRes);
			}
		}
	}

	//判断用户状态是否是禁用，如果不是则禁用
	public boolean isEnable(String userId) {
	       Map<String, Person> emailPersons = emailUserList.stream().collect(Collectors.toMap(Person::getUserId, person->person));
           Person person = emailPersons.get(userId);
           String enable = person.getEnable();
           if("0".equals(enable)) {
               return true;
           }
	    return false;
	}
	//删除部门服务
	public void deleteDepartment(List<Department> deleteDepartmentList) {
	  if(deleteDepartmentList!=null&&deleteDepartmentList.size()>0) {
		for (Department department : deleteDepartmentList) {
			if(!"0".equals(department.getParentId())) {
			String deleteDepartmentUrl = paramBean.getDepartmentDelete()+"?access_token=" + getToken() + "&id=" + department.getId();
			String deleteDepartmentRes = HttpClientUtil.sendEmailGet(null, deleteDepartmentUrl);
			checkException(deleteDepartmentRes);
           // System.out.println("deleteDepartmentRes:"+deleteDepartmentRes);
            logger.info("删除部门入参为：userid="+department.getId()+",结果为："+deleteDepartmentRes);
			}
		 }
    	}	
	}
	// 获取成员是否存在
	public boolean isExitUser(LdapPerson ldapPerson) {
	    String userId = ldapPerson.getCn() + "@scenetec.com";
	    ldapPerson.setCn(userId);
		// 调用查询成员服务
	    Map<String, Person> emailPersons = emailUserList.stream().collect(Collectors.toMap(Person::getUserId, person->person));
		if(emailPersons.containsKey(userId)) {
		    return true;
		}else {
		    return false;
		}

	}
	
	//比较对象是否相等
	public boolean equealObj(LdapPerson ldapPerson,Person person) {
	    if(ldapPerson.getCn().equals(person.getUserId())&&ldapPerson.getSn().equals(person.getName())&&
	        ldapPerson.getMobile().equals(person.getMobile())&&departM.get(ldapPerson.getDepartments().get(0)).getId().equals(person.getDepartment().get(0))
	        ) {
	        return true;
	    }else {
	        return false;
	    }
	}
	//检查调用腾讯邮箱api异常
    private void checkException (String resultJson) {
        if (StringUtils.isEmpty(resultJson)) {
            throw new WeixinRequestException("无返回");
        }

        String errcode = JSONObject.parseObject(resultJson).getString("errcode");
        String errmsg = JSONObject.parseObject(resultJson).getString("errmsg");
        if (!errcode.equals("0")) {
            errmsg = "错误码： " + errcode + "，错误信息: "+ errmsg;
            logger.error(errmsg);
            throw new WeixinRequestException(errmsg);
        }

    }
}