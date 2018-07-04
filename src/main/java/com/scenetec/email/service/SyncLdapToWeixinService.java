package com.scenetec.email.service;

import com.scenetec.email.exception.SyncWeixinDepartmentException;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import com.scenetec.email.po.weixin.WeixinDepartment;
import com.scenetec.email.po.weixin.WeixinMember;
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
            // 同步增加部门
            syncAddDepartment(ldapDepartments, rootWeixinDepartment, weixinDepartmentMap, ldapDepartmentMap);
            // 删除增加部门
            syncDeleteDepartment(weixinDepartments, rootWeixinDepartment);

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
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

            List<WeixinMember> weixinMemberSearchResults = scenetecWeixinService.searchUser(root.getId());
            Map<String, WeixinDepartment> weixinDepartmentMap = weixinDepartments.stream().collect(Collectors.toMap(WeixinDepartment::getName, item->item));
            Map<String, WeixinMember> weixinMemberSearchResultMap = weixinMemberSearchResults.stream().collect(Collectors.toMap(WeixinMember::getUserid, item->item));

            for (LdapPerson person: ldapPeopleList) {
                String cn = person.getCn();
                WeixinMember weixinMember = weixinMemberSearchResultMap.get(cn);

                if (weixinMember == null) {
                    person.needAdd = true;
                }else {
                    weixinMember.setNeedDelete(false);
                    isNeedUpdate(person, weixinMember, weixinDepartmentMap);
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

                    WeixinMember weixinMemberAdd = new WeixinMember(person.getCn(), person.getSn(), person.getMobile(), person.getEmail(), departments);
                    weixinMemberAdd.setEnable(1); // 启用
                    try {
                        scenetecWeixinService.createUser(weixinMemberAdd);
                        logger.info("同步用户成功: " + person.toString());
                    }catch (Exception e){
                        logger.info("同步用户失败: " + person.toString());
                        e.printStackTrace();
                    }
                }
            }
            for (WeixinMember weixinMember: weixinMemberSearchResults) {
                if (weixinMember.isNeedUpdate()) {
                    try {
                        scenetecWeixinService.updateUser(weixinMember);
                        logger.info("更新用户成功: " + weixinMember.toString());
                    }catch (Exception e){
                        logger.info("更新用户失败: " + weixinMember.toString());
                        e.printStackTrace();
                    }

                }
                if (weixinMember.isNeedDelete()) {
                    try {
                        scenetecWeixinService.deleteUser(weixinMember.getUserid());
                        logger.info("删除用户成功: " + weixinMember.toString());
                    }catch (Exception e){
                        logger.info("删除用户失败: " + weixinMember.toString());
                        e.printStackTrace();
                    }

                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private void isNeedUpdate(LdapPerson person, WeixinMember weixinMember, Map<String, WeixinDepartment> weixinDepartmentMap) {

        String ldapPersonDepartmentName = person.getDepartmentName();
        WeixinDepartment ldapDepartment = weixinDepartmentMap.get(ldapPersonDepartmentName);

        if (ldapDepartment == null) {
            logger.warn("部门不同步");
            return;
        }

        if (person.getCn().equals(weixinMember.getUserid())
                && person.getMobile().equals(weixinMember.getMobile())
                && person.getSn().equals(weixinMember.getName())
                && person.getEmail().equals(weixinMember.getEmail())
                && ldapDepartment.getId().equals(weixinMember.getDepartment().get(0))) {
            weixinMember.setNeedUpdate(false);
        }else {
            weixinMember.setNeedUpdate(true);
            weixinMember.setName(person.getSn());
            weixinMember.setEmail(person.getEmail());
            weixinMember.setMobile(person.getMobile());
            List<Integer> depts = new ArrayList<>();
            depts.add(ldapDepartment.getId());
            weixinMember.setDepartment(depts);
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

    private void syncDeleteDepartment(List<WeixinDepartment> weixinDepartments, WeixinDepartment rootWeixinDepartment) {
        for (WeixinDepartment department: weixinDepartments) {
            if (department.isNeedDelete()) {
                logger.info("需要删除的部门: " + department.toString());
                try {
                    // 查询部门下用户
                    List<WeixinMember> srList = scenetecWeixinService.searchUser(department.getId());
                    deleteDepartmentFromUser(srList, department.getId(), rootWeixinDepartment.getId());
                    for (WeixinMember sr: srList) {
                        WeixinMember memberUpdate = new WeixinMember();
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
    }

    private void syncAddDepartment(List<LdapDepartment> ldapDepartments, WeixinDepartment rootWeixinDepartment, Map<String, WeixinDepartment> weixinDepartmentMap, Map<String, LdapDepartment> ldapDepartmentMap) {
        for (LdapDepartment ldapDepartment: ldapDepartments) {
            if (ldapDepartment.isNeedAdd()) {
                WeixinDepartment weixinDepartment = new WeixinDepartment();
                if (ldapDepartment.isRoot()) {
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
            }
        }
    }

    // 从用户的部门列表里删除该部门
    private void deleteDepartmentFromUser(List<WeixinMember> srList, Integer id, Integer rootId) {
        for (WeixinMember sr: srList) {
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
}
