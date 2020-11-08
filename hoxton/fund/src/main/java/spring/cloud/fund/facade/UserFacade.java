package spring.cloud.fund.facade;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.cloud.common.pojo.UserInfo;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.fund.facade.interceptor.UserInterceptor;
import spring.cloud.fund.fallback.UserFallbackFactory;

import java.util.List;

// 声明为OpenFeign的客户端
@FeignClient(value = "user",
        /* fallback = UserFallback.class s  */ // ①
        //换成 配置降级工厂
        fallback = UserFallbackFactory.class // ①
        /*configuration = UserFacade.UserFeignConfig.class  */ // ②
)
public interface UserFacade {

    class UserFeignConfig {

        // 注入Spring MVC消息转换器工厂
        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters = null;

        /**
         * 此处需要注意， Bean的名称要和默认装配的保持一致*
         *
         * @return 编码器
         */
        @Bean(name = "feignDecoder") // 设置为"prototype"，代表只对当前客户端使用
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Decoder clientDecoder() {
            return new SpringDecoder(messageConverters);
        }

        /**
         * 创建拦截器，非自动装配的组件会通过类型查找
         *
         * @return 拦截器
         */
        @Bean // 设置为"prototype"，代表只对当前客户端使用
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public RequestInterceptor userInterceptor() {
            return new UserInterceptor();
        }

        /**
         * 日志级别，非自动装配的组件会通过类型查找
         *
         * @return 日志级别
         */
        @Bean // 设置为"prototype"，代表只对当前客户端使用
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        Logger.Level loggerLevel() {
            return Logger.Level.FULL;
        }

        /**
         * 创建客户端构建器
         *
         * @return 客户端构建器
         */
        // 注意名称需保持一致
        @Bean(name = "feignBuilder")
        // 设置为"prototype"，代表只对当前客户端使用
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Feign.Builder clientBuilder() {
            return Feign.builder();// ③
        }
    }


    /**
     * 获取用户信息
     *
     * @param id -- 用户编号
     * @return 用户信息
     */
    @GetMapping("/user/info/{id}")
// 注意方法和注解的对应选择
    public UserInfo getUser(@PathVariable("id") Long id);

    /**
     * 修改用户信息
     *
     * @param userInfo -- 用户
     * @return 用户信息
     */
    @PutMapping("/user/info") // 注意方法和注解的对应选择
    public UserInfo putUser(@RequestBody UserInfo userInfo);

    /**
     * 根据id数组获取用户列表
     *
     * @param ids -- 数组
     * @return 用户列表
     */
    @GetMapping("/user/infoes2")
    public ResponseEntity<List<UserInfo>> findUsers2(
// @RequestParam代表请求参数
            @RequestParam("ids") Long[] ids);

    /**
     * 删除用户信息，使用请求头传参
     *
     * @param id -- 用户编号
     * @return 成败结果
     */
    @DeleteMapping("/user/info")
    public ResultMessage deleteUser(
// @RequestHeader代表请求头传参
            @RequestHeader("id") Long id);

    /**
     * 传递文件流
     *
     * @param file -- 文件流
     * @return 成败结果
     */
    @RequestMapping(value = "/user/upload",
            // MediaType.MULTIPART_FORM_DATA_VALUE
            // 说明提交一个"multipart/form-data"类型的表单
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultMessage uploadFile(
// @RequestPart代表传递文件流
            @RequestPart("file") MultipartFile file);


}
