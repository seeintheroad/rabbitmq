package demo.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * direct消费者
 */
@Component
public class DirectConsumer {
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "directs",type="direct"),//绑定的交换机
                    key={"info","error","warn"}//路由的key
            )
    })
    public void receive1(String message){
        System.out.println("info,error,warn===="+message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "directs",type="direct"),//绑定的交换机
                    key={"error"}//路由的key
            )
    })
    public void receive2(String message){
        System.out.println("error===="+message);
    }

}
