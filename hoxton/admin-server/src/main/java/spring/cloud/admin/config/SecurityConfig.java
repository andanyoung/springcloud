package spring.cloud.admin.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // 请求前缀路径
    private final String adminContextPath;

    // 构造方法
    public SecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    // 权限配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        // 已保存身份安全请求处理器
        SavedRequestAwareAuthenticationSuccessHandler successHandler
                = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        http.authorizeRequests() // 已经认证的路径
                // 配置签名后放行路径
                .antMatchers(adminContextPath + "/assets/**").permitAll()
                .antMatchers(adminContextPath + "/login").permitAll()
                .anyRequest().authenticated()
                .and()
                // 定义登录页
                .formLogin().loginPage(adminContextPath + "/login")
                // 请求成功处理器
                .successHandler(successHandler).and()
                // 登出路径
                .logout().logoutUrl(adminContextPath + "/logout").and()
                // 支持HTTP基本验证
                .httpBasic().and()
                // 禁止CSRF验证机制
                .csrf().disable();
        // @formatter:on
    }
}
