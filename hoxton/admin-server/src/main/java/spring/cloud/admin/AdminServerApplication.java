package spring.cloud.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 驱动Admin服务端启动
@EnableAdminServer
public class AdminServerApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AdminServerApplication.class,args);
    }
}
