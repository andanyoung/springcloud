package spring.cloud.chapter15.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.chapter15.service.XaService;
import spring.cloud.chapter15.utils.SnowFlakeWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/xa")
public class XaController {

    @Autowired
    private XaService xaService = null;

    @GetMapping("/transaction")
    public Map<String, Object> transaction() {
        // 使用SnowFlake算法
        SnowFlakeWorker worker = new SnowFlakeWorker(3L);
        Long id = worker.nextId();
        String content = "content" + id;
        Long id2 = worker.nextId();
        String content2 = "content" + id2;
        int count = xaService.inisertFoo(id, content, id2, content2);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }
}
