package com.scenetec.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.bean.ParamBean;
import com.scenetec.email.po.Department;
import com.scenetec.email.po.EmailInfo;
import com.scenetec.email.util.HttpClientUtil;
import com.scenetec.email.util.LDAPAuthentication;

import javafx.print.JobSettings;

@Component
public class ScenetecMailService {

	@Autowired
	private ParamBean paramBean;
	
	public List<EmailInfo> getLadpData(){
		List<EmailInfo> info = new ArrayList<EmailInfo>();
		LDAPAuthentication ldap = new LDAPAuthentication();
	/*		List<EmailInfo> emailInfoList = ldap.search();
			List<String> ladpUserList = new ArrayList<String>();
			for (EmailInfo emailInfo : emailInfoList) {
				//机构信息
				List<Department> department = emailInfo.getDepartmentMap();
				//先处理部门数据
				scenetecMailDepartment(department);
				//处理成员信息
				scenetecMailPerson(emailInfo);
				//删除成员信息
				Map<String, Object> map = emailInfo.getMap();
				ladpUserList.add(String.valueOf(map.get("cn"))+"@scenetec.com");
				//删除机构信息
			}
			//删除成员信息
			deleteUserInfo(ladpUserList);*/
			deleteDepartment();
	
		return info;
	}
	//同步机构
	public String scenetecMailDepartment(List<Department> department) {
		if(department!=null&&department.size()>0) {
			for (Department department2 : department) {
				//获取token
				Map<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("corpid", paramBean.getCorpid());
				parameter.put("corpsecret", paramBean.getCorpsecret());
				String getTokenRes = HttpClientUtil.sendGet(parameter, paramBean.getGetTokenUrl());
				if(!StringUtils.isEmpty(getTokenRes)) {
					JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
					System.out.println("getTokenResJson:"+getTokenResJson);
					String errCode = String.valueOf(getTokenResJson.get("errcode"));
					String errmsg = String.valueOf(getTokenResJson.get("errmsg"));
					if("0".equals(errCode)) {
						String token = String.valueOf(getTokenResJson.get("access_token"));
						//查找部门是否存在
						JSONObject findDep = new JSONObject();
						findDep.put("name", department2.getName());
						findDep.put("fuzzy", 0);//精确查找
						String searchDepartmentUrl = paramBean.getDepartmentSearch()+"?access_token="+token;
						String searchDepartmentRes = HttpClientUtil.sendPost(findDep.toJSONString(), searchDepartmentUrl);
						JSONObject searchDepartmentResJson = JSONObject.parseObject(searchDepartmentRes);
						System.out.println("searchDepartmentResJson:"+searchDepartmentResJson);
						String searchDepartmentCode = String.valueOf(searchDepartmentResJson.get("errcode"));
						//返回结果
                        if("0".equals(searchDepartmentCode)) {
                        	JSONArray departmentArray = JSONArray.parseArray(String.valueOf(searchDepartmentResJson.get("department")));
                        	if(departmentArray==null||departmentArray.size()==0) {
                        		//调用创建部门服务
        						String createDepartmentUrl = paramBean.getDepartmentCreate()+"?access_token="+token;
        						JSONObject param = new JSONObject();
        						param.put("name", department2.getName());
        						//如果是根部门，设置根部门parentid为1
        						if(department2.isRoot()) {
        							param.put("parentid", 1);
        						}
        						String createDepRes =  HttpClientUtil.sendPost(param.toJSONString(), createDepartmentUrl);
        						System.out.println("createDepRes:"+createDepRes);
        						if(!StringUtils.isEmpty(createDepRes)) {
        							JSONObject createDepResJson = JSONObject.parseObject(createDepRes);	
        						String createDep = String.valueOf(createDepResJson.get("errcode"));
        						if("0".equals(createDep)) {
        							
        							//获取创建的父部门id
        							String partentId = String.valueOf(createDepResJson.get("id"));
        							//创建子部门
        							/*if(department2.getDepartment()!=null) {
        								JSONObject childParam = new JSONObject();
            							childParam.put("name", department2.getDepartment().getName());
                						//如果是根部门，设置根部门parentid为1
            							childParam.put("parentid", Long.valueOf(partentId));
                						String createChildDepRes =  HttpClientUtil.sendPost(childParam.toJSONString(), createDepartmentUrl);
                						System.out.println("createChildDepRes:"+createChildDepRes);
        							}*/
        							
        						}
        							return createDepRes;
        						}
                        	}
                        	
                        }
						
					}else {
						System.out.println("errorCode:"+errCode+",errorMsg:"+errmsg);
						return getTokenRes;
					}
				}
			}
			
		}
		 
 		return null;
	}
	//同步成员信息
	public String scenetecMailPerson(EmailInfo emailInfo) {
		 //获取token
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("corpid", paramBean.getCorpid());
		parameter.put("corpsecret", paramBean.getCorpsecret());
		String getTokenRes = HttpClientUtil.sendGet(parameter, paramBean.getGetTokenUrl());
		if(!StringUtils.isEmpty(getTokenRes)) {
			JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
			String errCode = String.valueOf(getTokenResJson.get("errcode"));
			String errmsg = String.valueOf(getTokenResJson.get("errmsg"));
			if("0".equals(errCode)) {
				String token = String.valueOf(getTokenResJson.get("access_token"));
				//调用创建部门服务
				String createUserUrl = paramBean.getUserCreate()+"?access_token="+token;
				
				//Person person = emailInfo.getPerson();
				Map<String, Object> map = emailInfo.getMap();
				//暂时测试写死param.put("userid", map.get("email"));
				String departMentName = String.valueOf(map.get("memberDepartment"));//成员所在机构名称
				//查询当前机构的机构号
				//查找部门是否存在
				JSONObject findDep = new JSONObject();
				findDep.put("name", departMentName);
				findDep.put("fuzzy", 0);//精确查找
				String searchDepartmentUrl = paramBean.getDepartmentSearch()+"?access_token="+token;
				String searchDepartmentRes = HttpClientUtil.sendPost(findDep.toJSONString(), searchDepartmentUrl);
				JSONObject searchDepartmentResJson = JSONObject.parseObject(searchDepartmentRes);
				System.out.println("searchDepartmentResJson:"+searchDepartmentResJson);
				String searchDepartmentCode = String.valueOf(searchDepartmentResJson.get("errcode"));
				if("0".equals(searchDepartmentCode)) {
					JSONArray departmentArray = JSONArray.parseArray(String.valueOf(searchDepartmentResJson.get("department")));
					//获取部门
					if(departmentArray!=null&&departmentArray.size()>0) {
						JSONObject jsonObject = JSONObject.parseObject(String.valueOf(departmentArray.get(0)));
						//查询成员是否存在，没有的话创建，有的话更新
						String userId = map.get("cn")+"@scenetec.com";
						//调用查询成员服务
						Map<String, Object> userGetParameter = new HashMap<String, Object>();
						userGetParameter.put("userid", userId);
						String userGetUrl = paramBean.getUserGet()+"?access_token="+token;
						String userGetRes = HttpClientUtil.sendGet(userGetParameter, userGetUrl);
						if(!StringUtils.isEmpty(userGetRes)) {
							//获取返回码
							JSONObject userGetResJson = JSONObject.parseObject(userGetRes);
							String userGetCode = String.valueOf(userGetResJson.get("errcode"));
							if("60111".equals(userGetCode)) {
								//用户不存在，新增用户
								JSONObject param = new JSONObject();
								param.put("userid", userId);
								param.put("name", map.get("sn"));
								param.put("department", new long[] {Long.valueOf(String.valueOf(jsonObject.get("id")))});
								param.put("position", map.get("position"));
								param.put("mobile", map.get("mobile"));
								param.put("tel", map.get("tel"));
								param.put("extid", map.get("extid"));
								param.put("gender", map.get("gender"));
								param.put("slaves", map.get("slaves"));
								//param.put("password", map.get("password"));
								param.put("password", "123456Az@");
								param.put("cpwd_login", map.get("cpwd_login"));
								String createUserRes =  HttpClientUtil.sendPost(param.toJSONString(), createUserUrl);
								System.out.println("createUserRes:"+createUserRes);
							}if("0".equals(userGetCode)){
								//存在,更新用户信息
								JSONObject updateParam = new JSONObject();
								updateParam.put("userid", userId);
								updateParam.put("name", map.get("sn"));
								updateParam.put("department", new long[] {Long.valueOf(String.valueOf(jsonObject.get("id")))});
								updateParam.put("position", map.get("position"));
								updateParam.put("mobile", map.get("mobile"));
								updateParam.put("tel", map.get("tel"));
								updateParam.put("extid", map.get("extid"));
								updateParam.put("gender", map.get("gender"));
								updateParam.put("slaves", map.get("slaves"));
								//param.put("password", map.get("password"));
								updateParam.put("cpwd_login", map.get("cpwd_login"));
								String updateUserUrl = paramBean.getUserUpdate()+"?access_token="+token;
								String userUpadtRes = HttpClientUtil.sendPost(updateParam.toJSONString(), updateUserUrl);
								System.out.println("userUpadtRes:"+userUpadtRes);
							}else {
								//出现其他异常
								System.out.println("查询用户异常："+userGetRes);
							}
						}
						
/*						if(!StringUtils.isEmpty(createUserRes)) {
							return createUserRes;
						}*/
					}
					
				}
			}else {
				System.out.println("errorCode:"+errCode+",errorMsg:"+errmsg);
				return getTokenRes;
			}
	}
		return null;}
	//删除成员信息
	public void deleteUserInfo(List<String> ladpUserList) {
		 //获取token
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("corpid", paramBean.getCorpid());
		parameter.put("corpsecret", paramBean.getCorpsecret());
		String getTokenRes = HttpClientUtil.sendGet(parameter, paramBean.getGetTokenUrl());
		if(!StringUtils.isEmpty(getTokenRes)) {
			JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
			String errCode = String.valueOf(getTokenResJson.get("errcode"));
			String errmsg = String.valueOf(getTokenResJson.get("errmsg"));
			if("0".equals(errCode)) {
				String token = String.valueOf(getTokenResJson.get("access_token"));
				//获取根部门及以下子部门的所有成员信息
				Map<String, Object> userSimpleListparameter = new HashMap<String, Object>();
				userSimpleListparameter.put("department_id", 1);//根目录
				userSimpleListparameter.put("fetch_child", 1);//递归查询
				String userSimpleUrl = paramBean.getUserSimpleList()+"?access_token="+token;
				String userSimpleRes = HttpClientUtil.sendGet(userSimpleListparameter, userSimpleUrl);
				System.out.println("userSimpleRes:"+userSimpleRes);
				if(!StringUtils.isEmpty(userSimpleRes)) {
					JSONObject userSimpleResJson = JSONObject.parseObject(userSimpleRes);
					String userSimpleErrCode = String.valueOf(userSimpleResJson.get("errcode"));
					//等于0表示成功
					if("0".equals(userSimpleErrCode)) {
						JSONArray jsonArray = JSONArray.parseArray(String.valueOf(userSimpleResJson.get("userlist")));//成员列表
						if(jsonArray!=null&&jsonArray.size()>0) {
							//获取的企业邮箱中的成员userId列表
							List<String> emailUserList = new ArrayList<String>();
							for (Object object : jsonArray) {
								JSONObject userJsonObject = JSONObject.parseObject(String.valueOf(object));
								String userId = String.valueOf(userJsonObject.get("userid"));
								emailUserList.add(userId);
							}
							//移除ladp中存在的用户
							emailUserList.removeAll(ladpUserList);
							//移除企业邮箱中的系统用户
							emailUserList.remove(paramBean.getSystemEmail());
							//emailUserList中剩余的用户为待删除用户
							for (String string : emailUserList) {
								//调用删除成员服务
								Map<String, Object> userDeleteparameter = new HashMap<String, Object>();
								userDeleteparameter.put("userid", string);
								String userDeleteUrl = paramBean.getUserDelete()+"?access_token="+token;
								String userDeleteRes = HttpClientUtil.sendGet(userDeleteparameter, userDeleteUrl);
								System.out.println("userDeleteRes:"+userDeleteRes);
							}
						}
					}else {
						//失败
						System.out.println("查询成员信息失败："+userSimpleRes);
					}
				}
			}
		}
		
		
	}
	//删除部门信息
	public Map<String, Object> deleteDepartment() {
		//查询企业邮箱中的所有部门
		// 获取token
				Map<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("corpid", paramBean.getCorpid());
				parameter.put("corpsecret", paramBean.getCorpsecret());
				String getTokenRes = HttpClientUtil.sendGet(parameter, paramBean.getGetTokenUrl());
				if (!StringUtils.isEmpty(getTokenRes)) {
					JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
					String errCode = String.valueOf(getTokenResJson.get("errcode"));
					if ("0".equals(errCode)) {
						String token = String.valueOf(getTokenResJson.get("access_token"));
						String departmentListUrl = paramBean.getDepartmentList()+"?access_token="+token;
						String departmentListRes = HttpClientUtil.sendGet(null, departmentListUrl);
						if(!StringUtils.isEmpty(departmentListRes)) {
							JSONObject departmentListResJson = JSONObject.parseObject(departmentListRes);
							String errorCode = String.valueOf(departmentListResJson.get("errcode"));
							if("0".equals(errorCode)) {
								//所有机构列表
								JSONArray jsonArray = JSONArray.parseArray(String.valueOf(departmentListResJson.get("department")));
								if(jsonArray!=null&&jsonArray.size()>0) {
									for (Object object : jsonArray) {
										Map<String, Department> map = new HashMap<String, Department>();
										JSONObject jsonObject = JSONObject.parseObject(String.valueOf(object));
										//parameter.put(String.valueOf(jsonObject.get("name")), jsonObject.get("id"));
										String parentId = String.valueOf(jsonObject.getString("parentid"));
										if("0".equals(parentId)) {
											Department department = new Department();
											department.setName(String.valueOf(jsonObject.getString("name")));
											String id = (String.valueOf(jsonObject.getString("id")));
											map.put(id, department);
										}else {
										//	Department department = getDeparement(jsonObject, map);
											//System.out.println(department);
										}
									}
								}
							}
						}
					}
				}
		return null;
	}
	
	//组装部门数据
	public static List<Department> getDeparement( Map<String, Department> map,Department dep) {

		for (Map.Entry<String, Department> entry : map.entrySet()) {
			String key = entry.getKey();
			Department department = entry.getValue();
			if(key.equals("0")) {
				//department
			}else {
				List<Department> list = getDeparement(map,department);
				department.setChildrenDepartment(list);
			}
		}
		
		return null;
	}
	
	
	//获取企业邮箱中的所有部门
	public Map<String, Object> deparementAll(String token) {
		//获取服务列表
		Map<String, Object> parameter = new HashMap<String, Object>();
		//parameter.put("id", id);
		String departmentListUrl = paramBean.getDepartmentList()+"?access_token="+token;
		String departmentListRes = HttpClientUtil.sendGet(parameter, departmentListUrl);
		if(!StringUtils.isEmpty(departmentListRes)) {
			JSONObject departmentListResJson = JSONObject.parseObject(departmentListRes);
			String errorCode = String.valueOf(departmentListResJson.get("errcode"));
			if("0".equals(errorCode)) {
				JSONArray jsonArray = JSONArray.parseArray(String.valueOf("department"));
				if(jsonArray!=null&&jsonArray.size()>0) {
					for (Object object : jsonArray) {
						JSONObject jsonObject = JSONObject.parseObject(String.valueOf(object));
						parameter.put(String.valueOf(jsonObject.get("name")), jsonObject.get("id"));
					}
				}
			}
		}
		return parameter;
	}
	
	public static void main(String[] args) {
		String str = "[{\"name\":\"厦门信钛科技有限公司\",\"id\":5755537439999919806,\"parentid\":0,\"order\":1340016318},{\"name\":\"厦门帝网信息科技有限公司\",\"id\":5755537439999919845,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"测试部门\",\"id\":5755537439999919858,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"测试新建部门\",\"id\":5755537439999919862,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"text1\",\"id\":5755537439999919867,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"text2\",\"id\":5755537439999919868,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"测试\",\"id\":5755537439999919869,\"parentid\":5755537439999919806,\"order\":0},{\"name\":\"产品研发部\",\"id\":5755537439999919846,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"董事会办公室\",\"id\":5755537439999919847,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"经营管理层\",\"id\":5755537439999919848,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"人力资源部\",\"id\":5755537439999919849,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"财务部\",\"id\":5755537439999919850,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"综合管理部\",\"id\":5755537439999919851,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"渠道部\",\"id\":5755537439999919852,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"运营部\",\"id\":5755537439999919853,\"parentid\":5755537439999919845,\"order\":0},{\"name\":\"下一集\",\"id\":5755537439999919870,\"parentid\":5755537439999919869,\"order\":0}]";
        JSONArray jsonArray = JSONArray.parseArray(str);
        List<Map<String, Department>> list = new ArrayList<Map<String, Department>>();
        Map<String, Department> map = new HashMap<String, Department>();
        for (Object object : jsonArray) {
			JSONObject jsonObject = JSONObject.parseObject(String.valueOf(object));
			String parentId = (String.valueOf(jsonObject.getString("parentid")));
			Department department = new Department();
			department.setId(String.valueOf(jsonObject.getString("id")));
			department.setName(String.valueOf(jsonObject.getString("name")));
			department.setParentId(parentId);
			map.put(parentId, department);
			//list.add(map);
		}
        System.out.println(map);
	}
}
