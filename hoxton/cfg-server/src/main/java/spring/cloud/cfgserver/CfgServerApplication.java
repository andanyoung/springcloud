package spring.cloud.cfgserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
// 驱动该微服务为Config服务端
@EnableConfigServer
public class CfgServerApplication extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(CfgServerApplication.class, args);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception { // ②
        // 密码编码器
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 可通过passwordEncoder.encode("a123456")得到加密后的密码
        String pwd
                = "$2a$10$KRDqyu/oZqGmTN5DSHxWjenCiro0PG07IzC0zM.TX2uobnYO2N8DO";
        // 使用内存存储
        auth.inMemoryAuthentication()
                // 设置密码编码器
                .passwordEncoder(passwordEncoder)
                // 注册用户：admin，密码：a123456,并赋予USER和ADMIN的角色权限
                .withUser("admin")
                // 设置用户密码
                .password(pwd)
                // 赋予角色权限
                .roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception { // ③
        http.authorizeRequests()
                // 限定ANT风格的路径
                .antMatchers("/**")
                // 限定可以访问的角色权限
                .hasRole("ADMIN")
                // 请求关闭页面需要ROLE_ADMIN橘色
                .and().formLogin().and()
                // 启动HTTP基础验证
                .httpBasic();
    }
}
