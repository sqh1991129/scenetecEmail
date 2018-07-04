package com.scenetec.email.service;

import com.scenetec.email.exception.SyncWeixinDepartmentException;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import com.scenetec.email.po.weixin.WeixinDepartment;
import com.scenetec.email.po.weixin.WeixinMemberAdd;
import com.scenetec.email.po.weixin.WeixinMemberSearchResult;
import com.scenetec.email.po.weixin.WeixinMemberUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncLdapToWeixinService {

    private Logger logger = LoggerFactory.getLogger(SyncLdapToWeixinService.class);
    
    @Resource
    private LdapManager ldapManager;

    @Resource
    private ScenetecWeixinService scenetecWeixinService;

    public void syncDepartment() {
        try {
            List<LdapPerson> ldapPeopleList = ldapManager.search();
            if (ldapPeopleList == null) {
                return;
            }
            List<LdapDepartment> ldapDepartments = ldapManager.getDepartmentTreeFromPerson(ldapPeopleList);
            List<WeixinDepartment> weixinDepartments = scenetecWeixinService.getDepartments(null);

            WeixinDepartment rootWeixinDepartment = getWeixinDepartmentRoot(weixinDepartments);

            Map<String, WeixinDepartment> weixinDepartmentMap = weixinDepartments.stream().collect(Collectors.toMap(WeixinDepartment::getName, item->item));
            Map<String, LdapDepartment> ldapDepartmentMap = ldapDepartments.stream().collect(Collectors.toMap(LdapDepartment::getName, item->item));

            for (LdapDepartment ldapDepartment: ldapDepartments) {
                String ldapDepartmentName = ldapDepartment.getName();
                WeixinDepartment weixinDepartment = weixinDepartmentMap.get(ldapDepartmentName);
                if (weixinDepartment == null) {
                    // 需要新增
                    ldapDepartment.setNeedAdd(true);
                }else {
                    weixinDepartment.setNeedDelete(false);
                }
            }

            for (LdapDepartment ldapDepartment: ldapDepartments) {
                if (ldapDepartment.isNeedAdd()) {
                    WeixinDepartment weixinDepartment = new WeixinDepartment();
                   /* if (ldapDepartment.isRoot()) {
                        weixinDepartment.setName(ldapDepartment.getName());
                        weixinDepartment.setParentid(rootWeixinDepartment.getId());
                        try {
                            scenetecWeixinService.createDepartment(weixinDepartment);
                            logger.info("同步部门成功: " +  ldapDepartment.toString());
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        createDepartment(ldapDepartmentMap, weixinDepartmentMap, ldapDepartment, rootWeixinDepartment);
                    }
                    */
                }
            }

            for (WeixinDepartment department: weixinDepartments) {
                if (department.isNeedDelete()) {
                    logger.info("需要删除的部门: " + department.toString());
                    try {
                        // 查询部门下用户
                        List<WeixinMemberSearchResult> srList = scenetecWeixinService.searchUser(department.getId());
                        deleteDepartmentFromUser(srList, department.getId(), rootWeixinDepartment.getId());
                        for (WeixinMemberSearchResult sr: srList) {
                            WeixinMemberUpdate memberUpdate = new WeixinMemberUpdate();
                            memberUpdate.setDepartment(sr.getDepartment());
                            memberUpdate.setUserid(sr.getUserid());
                            scenetecWeixinService.updateUser(memberUpdate);
                        }
                        scenetecWeixinService.deleteDepartment(department.getId()+"");
                        logger.info("删除部门成功: ");
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    // 从用户的部门列表里删除该部门
    private void deleteDepartmentFromUser(List<WeixinMemberSearchResult> srList, Integer id, Integer rootId) {
        for (WeixinMemberSearchResult sr: srList) {
            List<Integer> departmentList = sr.getDepartment();
            int index = departmentList.indexOf(id);
            if (index >= 0) {
                departmentList.remove(index);
            }
            if (departmentList.size() == 0) {
                departmentList.add(rootId);
            }
        }
    }

    //    @Scheduled(fixedRate = 30000)
    /**
     * 同步用户
     */
    public void syncMember() {

        try {
            List<LdapPerson> ldapPeopleList = ldapManager.search();
            if (ldapPeopleList == null) {
                return;
            }
            List<WeixinDepartment> weixinDepartments = scenetecWeixinService.getDepartments(null);
            WeixinDepartment root = getWeixinDepartmentRoot(weixinDepartments);

            List<WeixinMemberSearchResult> weixinMemberSearchResults = scenetecWeixinService.searchUser(root.getId());
            Map<String, WeixinDepartment> weixinDepartmentMap = weixinDepartments.stream().collect(Collectors.toMap(WeixinDepartment::getName, item->item));
            Map<String, WeixinMemberSearchResult> weixinMemberSearchResultMap = weixinMemberSearchResults.stream().collect(Collectors.toMap(WeixinMemberSearchResult::getUserid, item->item));

            for (LdapPerson person: ldapPeopleList) {
                String cn = person.getCn();
                WeixinMemberSearchResult sr = weixinMemberSearchResultMap.get(cn);

                if (sr == null) {
                    person.needAdd = true;
                }else {
                    sr.setNeedDelete(false);
                    String ldapPersonDepartmentName = person.getDepartmentName();
                    Integer departmentId = sr.getDepartment().get(0);
                    WeixinDepartment srDepartment = weixinDepartmentMap.get(departmentId);
                    if (srDepartment != null ){
                        if(!ldapPersonDepartmentName.equals(srDepartment.getName())) {
                            sr.setNeedUpdate(true);
                            List<Integer> departments = new ArrayList<>();
                            departments.add(departmentId);
                            sr.setDepartment(departments);
                        }
                    }
                }

            }

            for (LdapPerson person: ldapPeopleList) {
                if (person.needAdd) {
                    String departmentName = person.getDepartmentName();
                    WeixinDepartment weixinDepartment = weixinDepartmentMap.get(departmentName);
                    if (weixinDepartment == null) {
                        logger.info("-----------请检查，部门不同步--------");
                        logger.info(person.toString());
                        continue;
                    }
                    Integer departmentId = weixinDepartment.getId();
                    List<Integer> departments = new ArrayList<>();
                    departments.add(departmentId);
                    if (StringUtils.isEmpty(person.getCn()) || StringUtils.isEmpty(person.getSn()) || StringUtils.isEmpty(person.getMobile()) || (departments==null || departments.size() == 0)) {
                        logger.info("-----------校验失败--------");
                        logger.info(person.toString());
                        continue;
                    }

                    WeixinMemberAdd weixinMemberAdd = new WeixinMemberAdd(person.getCn(), person.getSn(), person.getMobile(), person.getEmail(), departments);
                    try {
                        scenetecWeixinService.createUser(weixinMemberAdd);
                        logger.info("同步用户成功: " + person.toString());
                    }catch (Exception e){
                        logger.info("同步用户失败: " + person.toString());
                        e.printStackTrace();
                    }
                }
            }
            for (WeixinMemberSearchResult sr: weixinMemberSearchResults) {
                if (sr.isNeedUpdate()) {
                    try {
//                        scenetecWeixinService.updateUser(sr);
                        logger.info("更新用户成功: " + sr.toString());
                    }catch (Exception e){
                        logger.info("更新用户失败: " + sr.toString());
                        e.printStackTrace();
                    }

                }
                if (sr.isNeedDelete()) {
                    try {
//                        scenetecWeixinService.deleteUser(sr.getUserid());
                        logger.info("删除用户成功: " + sr.toString());
                    }catch (Exception e){
                        logger.info("删除用户失败: " + sr.toString());
                        e.printStackTrace();
                    }

                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    private WeixinDepartment getWeixinDepartmentRoot(List<WeixinDepartment> weixinDepartments) {

        for (WeixinDepartment department: weixinDepartments) {
            if (department.getParentid() == 0) {
                return department;
            }
        }
        return null;
    }

    private void createDepartment(Map<String, LdapDepartment> ldapDepartmentMap, Map<String, WeixinDepartment> weixinDepartmentMap, LdapDepartment ldapDepartment, WeixinDepartment rootWeixinDepartment) {
        LdapDepartment parentLdapDepartment = ldapDepartmentMap.get(ldapDepartment.getParentName());

        if (parentLdapDepartment!=null && parentLdapDepartment.isNeedAdd()) {
            createDepartment(ldapDepartmentMap, weixinDepartmentMap, parentLdapDepartment, rootWeixinDepartment);
        }else {
            WeixinDepartment weixinDepartment = new WeixinDepartment();
            weixinDepartment.setName(ldapDepartment.getName());

            if (parentLdapDepartment == null) {
                weixinDepartment.setParentid(rootWeixinDepartment.getId());
            }else {
                WeixinDepartment parentWeixinDepartment = weixinDepartmentMap.get(parentLdapDepartment.getName());
                if (parentWeixinDepartment == null) {
                    throw new SyncWeixinDepartmentException("无法获取父节点");
                }
                weixinDepartment.setParentid(parentWeixinDepartment.getId());
            }

            ldapDepartment.setNeedAdd(false);
            try {
                scenetecWeixinService.createDepartment(weixinDepartment);
                logger.info("同步部门成功: " +  ldapDepartment.toString());
            }catch (Exception e) {
                logger.info("同步部门失败: " +  ldapDepartment.toString());
                e.printStackTrace();
            }
        }
    }

}
