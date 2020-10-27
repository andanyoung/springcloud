package spring.cloud.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.cloud.common.vo.UserInfo;

@Controller
@RequestMapping("/user")
public class UserInfoController {
    /**
     * 模拟获取用户信息
     *
     * @param id -- 用户编号
     * @return 用户信息
     */
    @GetMapping("/info/{id}")
    @ResponseBody
    public UserInfo getUser(@PathVariable("id") Long id) {
        UserInfo userInfo = new UserInfo(1L, "user_name_" + id, "note_" + id);
        return userInfo;
    }

    /**
     * 模拟更新用户信息
     *
     * @param id       -- 用户编号
     * @param userName -- 用户名称
     * @param note     -- 备注
     * @return 用户信息
     */
    @PutMapping("/info")
    @ResponseBody
    public UserInfo putUser(@RequestBody UserInfo userInfo) {
        return userInfo;
    }
}

