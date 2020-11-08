package spring.cloud.user.controller;

import UserInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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


    @GetMapping("/infoes/{ids}")
    @ResponseBody
    public ResponseEntity<List<UserInfo>> findUsers(
            @PathVariable("ids") Long[] ids) {
        List<UserInfo> userList = new ArrayList<>();
        for (Long id : ids) {
            UserInfo userInfo
                    = new UserInfo(id, "user_name_" + id, "note_" + id);
            userList.add(userInfo);
        }
        ResponseEntity<List<UserInfo>> response // 将结果封装为响应实体
                = new ResponseEntity<>(userList, HttpStatus.OK);
        return response;
    }

}

