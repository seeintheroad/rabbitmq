package demo.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * topic消费者
 */
@Component
/**
 *   *代表一个
 *   #代表一个或者多个
 */
public class TopicConsumer {
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "topics",type="topic"),//绑定的交换机
                    key={"usr.save","user.*"}//路由的key
            )
    })
    public void receive1(String message){
        System.out.println("usr.save,user.*===="+message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "topics",type="topic"),//绑定的交换机
                    key={"user.#","user.*"}//路由的key
            )
    })
    public void receive2(String message){
        System.out.println("user.#,user.*===="+message);
    }
}
