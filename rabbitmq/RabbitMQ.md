

# RabbitMQ

### 1.安装RabbitMQ

![1607325826852](.\RabbitMQ.assets\1607325826852.png)

````
rpm -Uvh *.rpm --nodeps --force
````

### 2.启动RabbitMQ

```bash
#任意路径执行
service rabbitmq-server start #启动mq
#启动成功 Starting rabbitmq-server (via systemctl):                  [  OK  ]
service rabbitmq-server stop #停止mq
service rabbitmq-server restart #重新启动mq
systemctl status rabbitmq-server#查看mq状态
```

### 3.启动可视化界面

```bash
#开启管理页面
rabbitmq-plugins enable rabbitmq_management
#出现Applying plugin configuration to rabbit@www... started 6 plugins.即启动成功
#修改默认配置信息
cp /usr/share/doc/rabbitmq-server-3.6.5/rabbitmq.config.example /etc/rabbitmq/rabbitmq.config

vi /etc/rabbitmq/rabbitmq.config
#将注释放开,并去掉后面的逗号
```

![1608791051413](.\RabbitMQ.assets\1608791051413.png)

然后重启rabbitmq关闭防火墙,在页面输入ip和端口号(默认15672)即可进入登录页面,账号密码guest

(15672是管理页面的端口,mq的端口号是5672)

![1607326922146](.\RabbitMQ.assets\1607326922146.png)

![1609299998861](C:\Users\lirongsheng\Desktop\RabbitMQ.assets\1609299998861.png)

### 4.模型

#### 1.第一种模型(一对一)

==生产者--->队列--->消费者==

```bash
#生产者发送消息
创建连接mq的连接工厂对象
设置连接rabbitmq主机 192.168.112.132
设置连接rabbitmq主机端口号 5672
设置连接rabbitmq的虚拟主机 /test
设置连接虚拟主机的用户名和密码 test 123
获取连接对象
获取连接中的通道
通道绑定对应消息队列 
	参数1:队列名称(自动创建)
    参数2:用于定义队列特性是否持久化(重启mq队列不丢失,消息丢失) fasle 不持久化(重启mq会丢失队列和消息)
	参数3:是否独占队列 false 当前队列不止当前通道可以使用 
	参数4:是否在消费完成后自动删除队列 false不自动删除
	参数5:附加参数 null
发布消息 虚拟机(直连没有虚拟机""),队列名称,发布消息额外设置(可以设置队列中的消息重启mq消息也不会消失),具体内容.geBytes()
```

```java
 //获取连接对象
        Connection connection = connectionFactory.newConnection();
        //获取连接中的管道
        Channel channel = connection.createChannel();
        /*
        参数1:队列名称(自动创建)
        参数2:用于定义队列特性是否持久化(重启mq队列不丢失,消息丢失) fasle 不持久化(重启mq会丢失队列和消息)
        参数3:是否独占队列 false 当前队列不止当前通道可以使用
        参数4:是否在消费完成后自动删除队列 false不自动删除
        参数5:附加参数 null
        */
        channel.queueDeclare("hello",false,false,false,null);
        //发布消息
        Map<String,String> map = new HashMap<>();
        map.put("name","张三");
        map.put("age","15");
        String message = JSON.toJSONString(map);
        /*
        参数1:交换机(直连没有交换机"")
        参数2:队列名称
        参数3:发布消息额外设置(可以设置队列中的消息重启mq消息也不会消失)
        MessageProperties.PERSISTENT_TEXT_PLAIN代表持久化
        参数4:具体内容.geBytes()
        * */ 			     channel.basicPublish("","hello",MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        channel.close();
        connection.close();
```

```bash
#消费者消息
创建连接mq的连接工厂对象
设置连接rabbitmq主机 192.168.112.132
设置连接rabbitmq主机端口号 5672
设置连接rabbitmq的虚拟主机 /test
设置连接虚拟主机的用户名和密码 test 123
消费消息 队列名称,开启消息自动确认机制,消费时回调的接口
```

```java
		ConnectionFactory connectionFactory =ConnectionUtil.getConnection();
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        //每次消费只能消费一个消息
        channel.basicQos(1);
        channel.queueDeclare("hello",false,false,false,null);
        //消费信息
        /*
        参数1:消费那个队列的名称
        参数2:开启消息自动确认机制
        参数3:消费时的回调函数
        * */
        channel.basicConsume("hello",false,new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("==============="+new String(body));
                 /**
                 * 参数1:确认队列中的具体的哪个消息
                 * 参数2:是否开启多个消息同时确认
                 */
                channel.basicAck(envelope.getDeliveryTag(),false);
                 System.out.println("===============消费完毕");
            }
        });

```

#### 2.任务队列（一对多 轮询）

**当消费处理比较耗时时,生产消息的速度大于消费的速度,造成消息越来越多,无法处理，让多个消费者同时消费一个队列**

​     		     ==--->消费者==

==生产者--->队列 --->消费者==

> 生产者不变 消费者代码复制两遍

##### 消息确认机制

**问题**：在消费消息时,有一个参数设置为开启消息自动确认机制,当消息队列中有5条消息时,自动确认机制会将这5个消息全给一个消费者,然后**删除队列中的数据**,但当消费者**宕机后**还没有消费完的消息将会**丢失**

解决：将自动确认改为false，设置每次消费一个消息，并在消费后手动确认**实现能者多劳**

```java
 //每次消费只能消费一个消息
 channel.basicQos(1);
 //消费自动确认机制为false
channel.basicConsume("hello",false,new DefaultConsumer(channel) {
    ...
    System.out.println("==============="+new String(body));
     /**
      * 参数1:确认队列中的具体的哪个消息
      * 参数2:是否开启多个消息同时确认
      */
    channel.basicAck(envelope.getDeliveryTag(),false);
    System.out.println("===============消费完毕");
}
```

#### 3.发布(广播)fanout

​                             ==--->(临时)队列--->消费者==

==生产者--->交换机--->(临时)队列--->消费者==

应用场景：比如一个人注册之后会进行邮箱验证逻辑、给这个人加积分逻辑等等操作

> 生产者把消息给交换机，生产者无法决定发给哪个队列，交换机会把一个消息给多个队列，实现一条消息多个消费者消费实现不同的逻辑

> 生产者：创建管道，连接交换机，将消息给交换机

```java
ConnectionFactory connectionFactory = ConnectionUtil.getConnection();
 //获取连接对象
 Connection connection = connectionFactory.newConnection();
 //获取连接中的管道
 Channel channel = connection.createChannel();
 /**
 * 管道声明交换机
 * 参数1：交换机名称
 * 参数2：交换机类型 fanout：广播类型
 */
 channel.exchangeDeclare("register","direct");
 //发布消息
 Map<String,String> map = new HashMap<>();
 map.put("name","张三");
 map.put("age","15");
 map.put("routingkey","error")
 String message = JSON.toJSONString(map);
 channel.basicPublish("register","","error",message.getBytes());
 channel.close();
 connection.close();
```

> 消费者：创建管道，绑定交换机，创建临时队列，队列绑定交换机，消费消息

```java
 ConnectionFactory connectionFactory =ConnectionUtil.getConnection();
 Connection connection = connectionFactory.newConnection();
 Channel channel = connection.createChannel();
 //声明交换机
 channel.exchangeDeclare("register","fanout");
 //临时队列
 String queue = channel.queueDeclare().getQueue();
 //绑定交换机和队列
 channel.queueBind(queue,"register","error");
 //消费信息
 /*
 参数1:消费那个队列的名称
 参数2:开启消息自动确认机制
 参数3:消费时的回调函数
 * */
 channel.basicConsume(queue,true,new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("==============="+new String(body));
            }
	});

```

#### 4.路由

##### 1.路由之订阅模型-direct(直连)

​                             ==--^key^->(临时)队列--->消费者1==

==生产者--->交换机--^key^->(临时)队列--->消费者2==

> 在广播模式中，一条消息被所有订阅的都消费，我们希望不同的消息被不同的队列消费

> 生产者：交换机改为直连，向交换机发送消息时指定routingkey

```java
channel.exchangeDeclare("register","direct");
channel.basicPublish("register","","error",message.getBytes());
```

> 消费者1：在交换机和队列绑定时需指定一个routingkey，接受error信息
>
> 消费者2：在交换机和队列绑定时需指定一个routingkey，接受warning，info信息

```java
channel.exchangeDeclare("register","direct");
//绑定交换机和队列
 channel.queueBind(queue,"register","error");
```

```java
channel.exchangeDeclare("register","direct");
//绑定交换机和队列
channel.queueBind(queue,"register","error");
channel.queueBind(queue,"register","info");
channel.queueBind(queue,"register","waring");
```

##### 2.路由之订阅模型-topic

> 对于直连来说，topic对于routingkey可以使用通配符
>
> *代表一个 
>
> #代表一个或者多个

### 5.springboot整合

#### 1.搭建环境

##### 	1.1引入依赖

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-amqp</artifactId>
 </dependency>
```

##### 	2.配置配置文件

````properties
spring:
  rabbitmq:
    host: 192.168.112.132
    username: guest
    password: guest
    virtual-host: /test
    port: 5672
````

==RabbitTemplate==用于简化操作,使用时直接在项目中注入即可

#### 2.实战

##### 1.第一种模型

```java
/**
* 生产者生产消息,此时不会直接创建队列,有消费者后才会创建
*/
@Slf4j
@SpringBootTest
public class TestRabbitMQ {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("测试hello word")
    public void test(){
        //参数1:队列名称 参数2:消息实体
        rabbitTemplate.convertAndSend("hello","hello world");
    }
}

```

```java
/**
* 消费者消费消息
*/
@Component
//默认是持久化,非独占,不是自动删除的
@RabbitListener(queuesToDeclare = @Queue(value = "hello",durable = "true",autoDelete = "false"))
public class HelloConsumer {
    @RabbitHandler
    public void receive(String message){
        System.out.println("message===="+message);
    }
}
```

##### 2.任务队列轮询

```java
//生产者
@Test
@DisplayName("测试任务队列")
public void testWork(){
	for (int i=0;i<10;i++){
		rabbitTemplate.convertAndSend("work","work模型"+i);
	}
}
```

```java
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
/**
*消费者
*/
@Component
public class WorkConsumer {
    @RabbitListener(queuesToDeclare = @Queue(value = "work"))
    public void receive1(String message){
        System.out.println("message1===="+message);
    }

    @RabbitListener(queuesToDeclare = @Queue(value = "work"))
    public void receive2(String message){
        System.out.println("message2===="+message);
    }

}
```

> 这种默认轮询机制,要实现能者多劳要另外配置

##### 3.发布广播

```java
//生产者
 @Test
    @DisplayName("测试广播形式fanout")
    public void testFanout(){
        //参数1: 交换机,参数2:路由key 参数3:消息实体
        rabbitTemplate.convertAndSend("logs","","Fanout模型");
    }

```

```java
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
/**
*消费者
*/
@Component
public class FountConsumer {
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "logs",type="fanout")//绑定的交换机
            )
    })
    public void receive1(String message){
        System.out.println("message1===="+message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "logs",type="fanout")//绑定的交换机
            )
    })
    public void receive2(String message){
        System.out.println("message2===="+message);
    }
}
```

##### 4.路由

###### 	4.1 直连

```java
//生产者
@Test
@DisplayName("测试路由形式direct")
public void testDirect(){
	//参数1: 交换机,参数2:路由key 参数3:消息实体
	rabbitTemplate.convertAndSend("directs","info","direct模型发送的info类型的信息");
}
```

```java
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
        System.out.println("message1===="+message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "directs",type="direct"),//绑定的交换机
                    key={"error"}//路由的key
            )
    })
    public void receive2(String message){
        System.out.println("message2===="+message);
    }

}
```

###### 	4.2topic

```java
//生产者
 @Test
    @DisplayName("测试路由形式topic")
    public void testTopics(){
        //参数1: 交换机,参数2:路由key 参数3:消息实体
        rabbitTemplate.convertAndSend("topics","user.save","topic模型发送的user.save的信息");
    }
```

```java
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * topic消费者
 */
@Component
public class TopicConsumer {
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "topics",type="topic"),//绑定的交换机
                    key={"usr.save","user.*"}//路由的key
            )
    })
    public void receive1(String message){
        System.out.println("message1===="+message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange =@Exchange(value = "topics",type="topic"),//绑定的交换机
                    key={"order.#","user.*"}//路由的key
            )
    })
    public void receive2(String message){
        System.out.println("message2===="+message);
    }
}
```

