package spring.cloud.cfgclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
// 驱动该微服务为Config服务端
@RestController
public class CfgClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CfgClientApplication.class, args);
    }

    // 读取配置文件的信息
    @Value("${version.message}") // ①
    private String versionMsg;

    // 读取配置文件的信息 test
    @Value("${version.test}")
    private String test;

    // 展示配置文件信息
    @GetMapping("/version/message")
    public String versionMessage() {
        return versionMsg;
    }

    // 展示配置文件信息
    @GetMapping("/version/test")
    public String versionTest() {
        return test;
    }
}
