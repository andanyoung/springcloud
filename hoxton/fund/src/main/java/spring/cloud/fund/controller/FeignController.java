package spring.cloud.fund.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.cloud.common.pojo.UserInfo;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.fund.facade.UserFacade;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/feign")
public class FeignController {

    @Autowired
    private UserFacade userFacade = null;

    @GetMapping("/user/{id}")
    public UserInfo getUser(@PathVariable("id") Long id) {
        UserInfo user = userFacade.getUser(id);
        return user;
    }

    @GetMapping("/user/{id}/{userName}/{note}")
    public UserInfo updateUser(@PathVariable("id") Long id,
                               @PathVariable("userName") String userName,
                               @PathVariable("note") String note) {
        UserInfo user = new UserInfo(id, userName, note);
        return userFacade.putUser(user);
    }

    /**
     * 以url?ids=xxx的形式传递参数
     *
     * @param ids -- 参数列表
     * @return 用户信息列表
     */
    @GetMapping("/infoes2")
    @ResponseBody
    public ResponseEntity<List<UserInfo>> findUsers2(
            @RequestParam("ids") Long[] ids) {
        List<UserInfo> userList = new ArrayList<>();
        for (Long id : ids) {
            UserInfo userInfo
                    = new UserInfo(id, "user_name_" + id, "note_" + id);
            userList.add(userInfo);
        }
        ResponseEntity<List<UserInfo>> response
                = new ResponseEntity<>(userList, HttpStatus.OK);
        return response;
    }

    /**
     * 删除用户
     *
     * @param id -- 使用请求头传递参数
     * @return 结果
     */
    @DeleteMapping("/info")
    @ResponseBody
    public ResultMessage deleteUser(@RequestHeader("id") Long id) {
        boolean success = id != null;
        String msg = success ? "传递成功" : "传递失败";
        return new ResultMessage(success, msg);
    }

    /**
     * 传递文件
     *
     * @param file -- 文件
     * @return 成败结果
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public ResultMessage uploadFile(@RequestPart("file") MultipartFile file) {
        boolean success = file != null && file.getSize() > 0;
        String message = success ? "文件传递成功" : "文件传递失败";
        return new ResultMessage(success, message);
    }

}
