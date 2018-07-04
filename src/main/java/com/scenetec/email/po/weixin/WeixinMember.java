/**
  * Copyright 2018 bejson.com
  */
package com.scenetec.email.po.weixin;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2018-07-03 14:20:36
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class WeixinMember {

    private String userid;
    private String name;
    private String english_name;
    private String mobile;
    private List<Integer> department;
    private List<Integer> order;
    private String position;
    private String gender;
    private String email;
    private int isleader;
    private int enable = 1;
    private String avatar_mediaid;
    private String telephone;
    private Extattr extattr;
    private boolean to_invite;
    private External_profile external_profile;

    private boolean needDelete;
    private boolean needUpdate;

    public boolean isNeedDelete() {
        return needDelete;
    }

    public void setNeedDelete(boolean needDelete) {
        this.needDelete = needDelete;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public WeixinMember(){}

    public WeixinMember(String userid, String name, String mobile, String email, List<Integer> department ){
        this.userid = userid;
        this.name = name;
        this.department = department;
        this.mobile = mobile;
        this.email = email;
    }

    public void setUserid(String userid) {
         this.userid = userid;
     }
     public String getUserid() {
         return userid;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setEnglish_name(String english_name) {
         this.english_name = english_name;
     }
     public String getEnglish_name() {
         return english_name;
     }

    public void setMobile(String mobile) {
         this.mobile = mobile;
     }
     public String getMobile() {
         return mobile;
     }

    public void setOrder(List<Integer> order) {
         this.order = order;
     }
     public List<Integer> getOrder() {
         return order;
     }

    public void setPosition(String position) {
         this.position = position;
     }
     public String getPosition() {
         return position;
     }

    public void setGender(String gender) {
         this.gender = gender;
     }
     public String getGender() {
         return gender;
     }

    public void setEmail(String email) {
         this.email = email;
     }
     public String getEmail() {
         return email;
     }

    public void setIsleader(int isleader) {
         this.isleader = isleader;
     }
     public int getIsleader() {
         return isleader;
     }

    public void setEnable(int enable) {
         this.enable = enable;
     }
     public int getEnable() {
         return enable;
     }

    public void setAvatar_mediaid(String avatar_mediaid) {
         this.avatar_mediaid = avatar_mediaid;
     }
     public String getAvatar_mediaid() {
         return avatar_mediaid;
     }

    public void setTelephone(String telephone) {
         this.telephone = telephone;
     }
     public String getTelephone() {
         return telephone;
     }

    public void setExtattr(Extattr extattr) {
         this.extattr = extattr;
     }
     public Extattr getExtattr() {
         return extattr;
     }

    public void setTo_invite(boolean to_invite) {
         this.to_invite = to_invite;
     }
     public boolean getTo_invite() {
         return to_invite;
     }

    public void setExternal_profile(External_profile external_profile) {
         this.external_profile = external_profile;
     }
     public External_profile getExternal_profile() {
         return external_profile;
     }

    public List<Integer> getDepartment() {
        return department;
    }

    public void setDepartment(List<Integer> department) {
        this.department = department;
    }

    public boolean isTo_invite() {
        return to_invite;
    }
}