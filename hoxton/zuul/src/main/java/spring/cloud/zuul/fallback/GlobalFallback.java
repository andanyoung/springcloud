package spring.cloud.zuul.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import spring.cloud.common.vo.ResultMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**** imports ****/
@Component
public class GlobalFallback implements FallbackProvider {
    @Override
    public String getRoute() { // ①
        return "*"; // 指定为所有微服务的降级服务
        // 如果需要指定为特定微服务的，可以返回具体的serviceId，如下
        // return "user"
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        return new ClientHttpResponse() { // ②
            // 获取响应码
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }

            // 获取响应码的编码
            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.INTERNAL_SERVER_ERROR.value();
            }

            // 获取响应码的描述信息
            @Override
            public String getStatusText() throws IOException {
                return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
            }

            @Override
            public void close() {
            }

            // 获取响应体
            @Override
            public InputStream getBody() throws IOException {
                // 转换为JSON字符串
                ObjectMapper mapper = new ObjectMapper();
                String message = cause.getCause().getMessage();
                // 包装为结果信息类
                ResultMessage result = new ResultMessage(false, message);
                String body = mapper.writeValueAsString(result);
                return new ByteArrayInputStream(body.getBytes());
            }

            // 获取响应头
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                // 设置请求头
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                return headers;
            }
        };
    }
}