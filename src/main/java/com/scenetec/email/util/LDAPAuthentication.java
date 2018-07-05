package com.scenetec.email.util;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.scenetec.email.po.Department;
import com.scenetec.email.po.EmailInfo;

public class LDAPAuthentication {
    private final String URL = "ldap://132.232.16.89:389/";
    private final String BASEDN = "dc=scenetec,dc=com";  // 根据自己情况进行修改
    private final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private LdapContext ctx = null;
    private final Control[] connCtls = null;

    private void LDAP_connect() {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
        env.put(Context.PROVIDER_URL, URL + BASEDN);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        String root = "cn=admin,dc=scenetec,dc=com";  //根据自己情况修改
        env.put(Context.SECURITY_PRINCIPAL, root);   // 管理员
        env.put(Context.SECURITY_CREDENTIALS, "123456");  // 管理员密码

        try {
            ctx = new InitialLdapContext(env, connCtls);
            System.out.println( "连接成功" );

        } catch (javax.naming.AuthenticationException e) {
            System.out.println("连接失败：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("连接出错：");
            e.printStackTrace();
        }

    }
    private void closeContext(){
        if (ctx != null) {
            try {
                ctx.close();
            }
            catch (NamingException e) {
                e.printStackTrace();
            }

        }
    }
    private String getUserDN(String uid) {
        String userDN = "";
        LDAP_connect();
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> en = ctx.search("", "uid=" + uid, constraints);

            if (en == null || !en.hasMoreElements()) {
                System.out.println("未找到该用户");
            }
            // maybe more than one element
            while (en != null && en.hasMoreElements()) {
                Object obj = en.nextElement();
                if (obj instanceof SearchResult) {
                    SearchResult si = (SearchResult) obj;
                    userDN += si.getName();
                    userDN += "," + BASEDN;
                } else {
                    System.out.println(obj);
                }
            }
        } catch (Exception e) {
            System.out.println("查找用户时产生异常。");
            e.printStackTrace();
        }

        return userDN;
    }

    public List<EmailInfo> search() throws NamingException {
    	//定义list集合存储对象
    	List<EmailInfo> emailInfoList = new ArrayList<EmailInfo>();
    	LDAP_connect();
        SearchControls   constraints   =   new   SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration en = null;
        try {
            en = ctx.search("", "uid=*", constraints);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        while   (en   !=   null   &&   en.hasMoreElements())
        {
        	EmailInfo info = new EmailInfo();
            Object   obj   =   en.nextElement();
            if(obj   instanceof   SearchResult)
            {
                SearchResult   si   =   (SearchResult)   obj;

                System.out.println( "name:   "   +   si.getName());//人员机构   机构信息
                 //处理人员机构信息
                //departmentInfo(si.getName());
               info.setDepartmentMap(departmentInfo(si.getName()));
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("memberDepartment", memberDepartment(si.getName()));
                Attributes attrs   =   si.getAttributes();
                if   (attrs   ==   null)
                {
                    System.out.println( "No   attributes ");
                    info.setMap(map);
                }
                else
                {
                	
                    for   (NamingEnumeration   ae   =   attrs.getAll();   ae.hasMoreElements();)
                    { 
                        Attribute   attr   =   (Attribute)   ae.next();
                        String   attrId   =   attr.getID();//属性key值
                        for   (Enumeration vals = attr.getAll(); vals.hasMoreElements();)
                        {
                            System.out.print(attrId   +   ":   ");
                            Object   o   =   vals.nextElement();
                            if(o   instanceof   byte[]) {
                                System.out.println(new   String((byte[])o));
                                map.put(attrId, attrId);
                            }
                            else
                                System.out.println(o);//属性value值
                            map.put(attrId, o);
                            
                        }
                    }
                    info.setMap(map);
                }
            }
            else
            {
                System.out.println(obj);
            }
            System.out.println();
            emailInfoList.add(info);
        }
        ctx.close();
        return emailInfoList;
    }

    public boolean list() throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list("text1");

        while(list.hasMore()){
            NameClassPair ncp = list.next();
            String nou = ncp.getNameInNamespace();
            System.out.println(nou);
        }

        return false;
    }

    public boolean authenricate(String UID, String password) {
        boolean valide = false;
        String userDN = getUserDN(UID);

        try {
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            ctx.reconnect(connCtls);
            System.out.println(userDN + " 验证通过");
            valide = true;
        } catch (AuthenticationException e) {
            System.out.println(userDN + " 验证失败");
            System.out.println(e.toString());
            valide = false;
        } catch (NamingException e) {
            System.out.println(userDN + " 验证失败");
            valide = false;
        }
        closeContext();
        return valide;
    }
    private  boolean addUser(String usr, String pwd) {

        try {
            LDAP_connect();
            BasicAttributes attrsbu = new BasicAttributes();
            BasicAttribute objclassSet = new BasicAttribute("objectclass");
            objclassSet.add("inetOrgPerson");
            attrsbu.put(objclassSet);
            attrsbu.put("sn", usr);
            attrsbu.put("cn", usr);
            attrsbu.put("uid", usr);
            attrsbu.put("userPassword", pwd);
            ctx.createSubcontext("uid=yorker", attrsbu);

            return true;
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        closeContext();
        return false;
    }
    public static void main(String[] args) {
        LDAPAuthentication ldap = new LDAPAuthentication();

        ldap.LDAP_connect();

        try {
            ldap.search();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        if(ldap.authenricate("yorker", "secret") == true){

            System.out.println( "该用户认证成功" );

        }
        //ldap.addUser("yorker","secret");
    	//departmentInfo("uid=jjjddd,ou=第二季,ou=测试");

    }
    //处理人员机构信息
    public static List<Department> departmentInfo(String departmentStr) {
    	List<Department> list = new ArrayList<Department>();
    	//cn=111 222,ou=下一集,ou=测试
    	String[] departmentArray = departmentStr.split(",");
    	for (int i = departmentArray.length-1; i > 0; i--) {
    		Department department = new Department();
    		String[] departmentName = departmentArray[i].split("=");
    		department.setName(departmentName[1]);
    		if(i==departmentArray.length-1) {
    			department.setRoot(true);
    		}else {
    			department.setRoot(false);
    		}
    		//根节点父部门为空
    		if(i!=1) {
    			Department childDepartment = new Department();
    			String[] departmentParentName = departmentArray[i-1].split("=");
    			childDepartment.setName(departmentParentName[1]);
    			//department.setDepartment(childDepartment);
    		}
    		list.add(department);
		}
    	return list;
    }
    //获取人员所在机构名称
    public String memberDepartment(String departmentStr) {
    	if(departmentStr.contains(",")) {
    		String[] departmentArray = departmentStr.split(",");
        	String[] departmentName = departmentArray[1].split("=");
        	return departmentName[1];
    	}
    	return null;
    }
}

