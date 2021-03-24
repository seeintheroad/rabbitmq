package demo.rabbitmq.provider;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 测试hello word
     *
     * @return
     */
    @RequestMapping("/helloWord")
    public Boolean test( String queueName, String msg) {
        //参数1:队列名称 参数2:消息实体
        rabbitTemplate.convertAndSend(queueName, msg);
        return true;
    }

    /**
     * 测试任务队列
     *
     * @return
     */
    @RequestMapping("/work")
    public Boolean testWork(String queueName,String msg) {
        for (int i = 0; i < 10; i++) {
//            rabbitTemplate.convertAndSend("work", "测试work工作模型轮询" + i);
            rabbitTemplate.convertAndSend(queueName, msg + i);
        }
        return true;
    }

    /**
     * 测试广播形式fanout
     *
     * @return
     */
    @RequestMapping("/fanout")
    public Boolean testFanout(String exchangeName,String msg) {
        //参数1: 交换机,参数2:路由key 参数3:消息实体
//        rabbitTemplate.convertAndSend("logs", "", "测试广播模式,是否多个接受");
        rabbitTemplate.convertAndSend(exchangeName, "", msg);
        return true;
    }

    /**
     * 测试路由形式direct
     *
     * @return
     */
    @RequestMapping("/direct")
    public Boolean testDirect(String exchangeName,String routerKey,String msg) {
        //参数1: 交换机,参数2:路由key 参数3:消息实体
//        rabbitTemplate.convertAndSend("directs", "info", "info信息显示");
//        rabbitTemplate.convertAndSend("directs", "error", "error信息显示");
        rabbitTemplate.convertAndSend(exchangeName, routerKey, msg);
        return true;
    }

    /**
     * 测试路由形式topic
     *
     * @return
     */
    @RequestMapping("/topics")
    public Boolean testTopics(String exchangeName,String routerKey,String msg) {
        //参数1: 交换机,参数2:路由key 参数3:消息实体
//        rabbitTemplate.convertAndSend("topics", "user.save", "topic模型发送的user.save的信息");
        rabbitTemplate.convertAndSend(exchangeName, routerKey, msg);
        return true;
    }
}
