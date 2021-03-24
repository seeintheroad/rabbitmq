package demo.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class RabbitmqApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("测试hello word")
    public void test(){
        //参数1:队列名称 参数2:消息实体
        rabbitTemplate.convertAndSend("hello","hello world");
    }

    @Test
    @DisplayName("测试任务队列")
    public void testWork(){
        for (int i=0;i<10;i++){
            rabbitTemplate.convertAndSend("work","work模型"+i);
        }
    }

    @Test
    @DisplayName("测试广播形式fanout")
    public void testFanout(){
        //参数1: 交换机,参数2:路由key 参数3:消息实体
        rabbitTemplate.convertAndSend("logs","","Fanout模型");
    }
    @Test
    @DisplayName("测试路由形式direct")
    public void testDirect(){
        //参数1: 交换机,参数2:路由key 参数3:消息实体
        rabbitTemplate.convertAndSend("directs","info","direct模型发送的info类型的信息");
    }
    @Test
    @DisplayName("测试路由形式topic")
    public void testTopics(){
        //参数1: 交换机,参数2:路由key 参数3:消息实体
        rabbitTemplate.convertAndSend("topics","user.save","topic模型发送的user.save的信息");
    }

}
