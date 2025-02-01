package com.example.his.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.common.R;
import com.example.his.api.db.pojo.UserEntity;
import com.example.his.api.mis.controller.form.*;
import com.example.his.api.mis.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public R login(@RequestBody @Valid LoginForm form) {
        Map param = BeanUtil.beanToMap(form);

        Integer userId = userService.login(param);
        if (userId != null) {
            StpUtil.logout(userId,"Web");

            StpUtil.login(userId,"Web");
            String token = StpUtil.getTokenValueByLoginId(userId, "Web");
            List<String> permissions = StpUtil.getPermissionList();
            return R.ok().put("result", true).put("token", token).put("permissions", permissions);
        }
        return R.ok().put("result", false);
    }

    @GetMapping("/logout")
    @SaCheckLogin
    public R logout() {
        int userId = StpUtil.getLoginIdAsInt();
        StpUtil.logout(userId, "Web");
        return R.ok();
    }

    @PostMapping("/updatePassword")
    @SaCheckLogin
    public R updatePassword(@Valid @RequestBody UpdatePasswordForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        HashMap param = new HashMap() {{
            put("userId", userId);
            put("password", form.getPassword());
            put("newPassword", form.getNewPassword());
        }};
        int rows = userService.updatePassword(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchByPage")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchByPage(@Valid @RequestBody SearchUserByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        Map param = BeanUtil.beanToMap(form);
        param.put("start", start);
        PageUtils pageUtils = userService.searchByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @SaCheckPermission(value = {"ROOT", "USER:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertUserForm form) {
        UserEntity user = BeanUtil.toBean(form,UserEntity.class);
        user.setStatus(1);
        user.setRole(JSONUtil.parseArray(form.getRole()).toString());
        int rows = userService.insert(user);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchById")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchUserByIdForm form) {
        HashMap map = userService.searchById(form.getUserId());
        return R.ok().put("result", map);
    }

    @PostMapping("/update")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateUserForm form) {
        Map param = BeanUtil.beanToMap(form);
        param.replace("role", JSONUtil.parseArray(form.getRole()).toString());
        int rows = userService.update(param);
        if (rows == 1) {
            StpUtil.logout(form.getUserId());
        }
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteByIds")
    @SaCheckPermission(value = {"ROOT", "USER:DELETE"}, mode = SaMode.OR)
    public R deleteByIds(@Valid @RequestBody DeleteUserByIdsForm form) {
        Integer userId = StpUtil.getLoginIdAsInt();
        if (ArrayUtil.contains(form.getIds(), userId)) {
            return R.error("Cannot delete self account");
        }
        int rows = userService.deleteByIds(form.getIds());
        if (rows > 0) {
            for (Integer id : form.getIds()) {
                StpUtil.logout(id);
            }
        }
        return R.ok().put("rows", rows);
    }

    @PostMapping("/dismiss")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    public R dismiss(@RequestBody @Valid DismissForm form) {
        int rows = userService.dismiss(form.getUserId());
        if (rows > 0) {
            StpUtil.logout(form.getUserId().intValue());
        }
        return R.ok().put("rows", rows);
    }

    @GetMapping("/searchDoctorById")
    @SaCheckLogin
    public R searchDoctorById() {
        Integer userId = StpUtil.getLoginIdAsInt();
        HashMap map = userService.searchDoctorById(userId);
        return R.ok().put("result", map);
    }
}
