# Imitation_Bilibili
参考哔哩哔哩，抖音等视频平台，从数据库、网站架构设计、从零开发并实现用户、权限、动态、视频、弹幕等核心功能





##### 发布订阅模式

```md
使用redis做缓存，实现发布订阅模式的时候，用户发布动态时，MQ将用户动态数据发送到redis中，而用户需要从redis中取到相应的数据。
```

##### 注意发布订阅模式与观察者模式的区别哦

````md
````





##### bug：

权限信息查不出来，显示sql语句错误，但是仔细排查暂时还没有排查出来---待修复
UserAuthApi.java中的getUserAuthorities方法里的细微错误
报错信息：

```md
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 10
### The error may exist in file [E:\APPDownload\Java\xiangmu\Imitation B station\Project\imitation-bilibili\imitation-bilibili-dao\target\classes\mapper\authRoleElementOperation.xml]
### The error may involve com.imooc.bilibili.dao.AuthRoleElementOperationDao.getRoleElementOperationsByRoleIds-Inline
### The error occurred while setting parameters
### SQL: select             areo.*,             aeo.elementName,             aeo.elementCode,             aeo.operationType         from             t_auth_role_element_operation areo             left join t_auth_element_operation aeo on areo.elementOperationId = aeo.id         where             areo.roleId in
### Cause: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 10; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 10]
```

已经修复----原因，没有增加错误处理，如果数据库里面查不出来数据，那么对应的接收对象会报错。得先增加一个错误处理，避免这个情况发生。 





##### 扩展：如果权限和用户原来越多的话，在使用aop进行操作的时候，会使注解越来越长，怎么解决这个问题？

答：可以新增权限组的概念，将相同角色但是权限相同的划分为一个权限组，这样就可以有效的避免代码过度冗余和复杂化

AOP的使用范例是UserMomentApi中的@PostMapping("/user-moments")//添加用户动态---也叫新增用户动态 ------->其它地方也用了



##### 注意！！

--->基本上每个接口都会调用userSupport.getCurrentUserId的方式来得到用户的唯一标识符，有两个原因--->第一：是因为userId作为用户唯一标识符直接明文传输太危险了，而登录是通过手机号或邮箱，密码一起实现登录的，所以在后端就可以直接自己查找到userId，而userId是封装加密到token中里去了，这就避免了userId在网络中的明文传输，减少了风险。第二：是因为每次在从Token中取userId的时候，系统首先需要验证一下当前token是否合法，即是否过了有效期或是非法token，这给系统多加了一层防护。



##### 为什么要使用双token的方式实现登录？

答：因为，如果用户在退出登录之后，其登录之前的token还未到期，那么其他人是还可以使用这个token去访问服务器资源的，所以这显然不是一个正确或安全的操作，需要做进一步升级。所以双token模式出现了。主要是这个原因--->**用户也可以依赖refresh token不用重复性的进行登录操作，提升用户体验**,，refresh token可以刷新access token保持登录状态。

但，在用户退出登录之后，会在refresh token表中吧对应userId的refresh token给删除掉。



##### 双token机制优势--->access token 与 refresh token

```md
可以对用户进行无感的有效时间的刷新
```



##### FastDFS中Tracker服务器已经拥有了负载均衡的功能，为什么还要使用Nginx呢？

```md
Nginx在系统中发挥的作用主要有两个：
```

- 实现反向代理的功能

- 实现负载均衡

```md
再加一层负载均衡的作用使得系统拥有两层的负载均衡，即减少了Tracker服务器的压力，又使系统变得更加灵活。减少服务器的性能损耗！
```



##### 注意！！！FastDFS访问的服务器端口号我设置为了8080，这个看情况可以改变



##### 关于后端错误提示语的小Tips

```md
报错提示语一般不要写的太详细，一个模模糊糊的说明就好了，可供前后端明白就可以。
原因是，如果有人在对应的接口上部署抓包工具的话，是可以把说明需要的一些参数类型给抓取到的，在攻击者进行暴力破解接口参数的时候，如果报错消息太过详细的话会有一定的危险性，所以模糊一点就够了！
```



##### 断点续传功能

```md
在大文件上传时，如果文件过大的话会导致服务器带宽变得紧张，请求的速度就会下降；若文件上传的过程中，遇到服务中断、网络中断或页面崩溃等等导致文件上传失败，重新上传全部文件会损耗服务器性能，也让用户体验更差。---->所以，断点续传功能的出现解决了这个问题。把大文件进行分片传输   其实是需要前端进行文件分片会好一点-->后端也可以实现
```



##### 秒传

```md
针对于同一个文件，如果已经上传过了，再来一个同样的文件上传时会告诉用户已经上传成功了。这需要将每个文件的内容进行MD5的加密获得一个唯一的文件标识字符串。
无论是给这个文件改名或者改变其的类型，其底层的二进制文件流是没有变化的，所以对内容进行md5加密可以得到该文件的唯一标识符。
一般对文件进行MD5内容加密是由前端完成的。更安全
```



##### 注意要设置spring boot最大文件大小上传限制

```md
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```



#### 瀑布流视频列表开发

**videoAPI的开发**

##### 瀑布流的概述与意义

```md
瀑布流是一种布局，多行的等宽元素进行排列，后面排不开的元素会依次的排到其他元素的后面去。在一个完整的页面当中,由于高度是有限制的，首先会加载一部分元素的内容，暂时加载不到的元素会放到后面进行加载，不会进入页面之后就全部加载。节省一些网络请求的资源，同时让页面的显示速度变得更快。---后端实现方式原理其实就是一个分页查询，分页的大小就为当前页面高度内所需要的数据大小。
```



##### 视频在线播放功能

```md
视频在线观看和下载本质上都是视频的下载，都是从云端或者相应的文件服务器获取到想要的内容。分片下载(推荐)---或者完整文件下载

展示的方式有两种：
1.直接通过http的方式访问云端存储的内容---一般返回的状态码为206而非200，指返回的是部分内容即分片获取视频内容(二进制内容)
如果我们直接通过http的方式将路径和文件类型拼接好，从浏览器里访问自己的FastDFS文件库的话，浏览器会自动帮我们进行分片的获取.
但是这种方式也有缺点;
	首先，文件路径没有隐藏，在浏览器上输入的是最终的文件路径(或者从前端页面上获取)，会暴露给所有人，不安全！不好进行权限控制。
	
2.通过后端的接口，“包一层”来进行传输----有点类似于适配器模式的原理。通过后端的服务器来进行文件的获取(推荐！！)
前端访问后端第一个接口得到想要访问的相对路径，接着带上这个相对路径再访问后端的第二个接口，由后端再对路径进行拼接成绝对路径去访问FastDFS服务器获取视频文件，再以流的形式传回给前端。后端拿到文件二进制流数据之后，可以在进行一个AES的数据流加密再传输给前端，前端再解密就可以了。
这样最多暴露的就是相对地址，而最重要的服务器ip地址和端口号是不暴露的，提升了安全性。

```



#### 视频社交属性方面的功能开发

##### 视频点赞功能

```md
注意用户在未登录的模式下也是可以看视频的   只是不能使用一些如点赞、收藏等社交属性的功能
```

##### 小tips

```md
当更新的数据量比较大的时候，如：视频投币场景下，如果像之前收藏视频接口一样更新使用先删除后添加的操作处理，会使数据量有一个成倍的增长。如果一个视频有二十万的投币，如果使用的是先删除后增加的操作，会使数据处理量达到四十万，这使数据量级增加了一倍，所以要直接使用update操作将数据量级控制在最小的范围里。

而使用先删除后增加的操作，是避免了一些细微字段的值修改而使用，方便一点。看情况改变实现方式！
```

![image-20230629225024559](C:\Users\Amber\AppData\Roaming\Typora\typora-user-images\image-20230629225024559.png)

##### 视频点赞



##### 视频收藏



##### 视频投币



##### 视频评论

```md
使用树形结构存储设计表----闭包表
DROP TABLE IF EXISTS `t_video_comment`;
CREATE TABLE `t_video_comment`  (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `videoId` bigint NOT NULL COMMENT '视频id',
  `userId` bigint NOT NULL COMMENT '用户id',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论',
  `replyUserId` bigint NULL DEFAULT NULL COMMENT '回复用户id',
  `rootId` bigint NULL DEFAULT NULL COMMENT '根节点评论id',
  `createTime` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频评论表' ROW_FORMAT = Dynamic;
```

接收视频评论的实体类VideoComment中，需要有三个冗余字段，以此来接收孩子评论(即回复评论)，已经要回复user的简介和用户本身的简介。

![image-20230630114155671](C:\Users\Amber\AppData\Roaming\Typora\typora-user-images\image-20230630114155671.png)

##### 记得动态SQL的拼接调节与使用

![image-20230703110438599](C:\Users\Amber\AppData\Roaming\Typora\typora-user-images\image-20230703110438599.png)





##### 流程

进入首页调用分页视频的接口-->进行瀑布流的模式加载-->找到我们想看的视频-->通过调用视频详情接口进行视频详情的查看-->在视频详情的页面当中会把视频详情、视频点赞、视频投币、视频评论等相关的视频社交属性进行加载和展示-->加载弹幕与展示弹幕



##### 注意service设计的小Tips

```md
不要在一个Service里过度的引入大量的其它Service，要保持一个树形的引入结构，但单个节点的分支不能太多，得保持一个相对健康的数量级。保持健康的可维护性和可读性

也不要在service里茫然的随便引入其它service，防止出现service循环引入的问题
```







#### 弹幕系统

##### 场景分析

```md
客户端针对某一视频创建了弹幕，发送后端进行处理，后端需要对所有正在观看该视频的用户推送该弹幕
```

##### 思考实现方式

```md
使用短连接进行通信
使用长连接进行通信
```

##### 短连接实现方案

```md
所有观看视频的客户端不断轮询后端，若有新的弹幕则拉取到前端进行展示
```

##### 缺点

```md
轮询的效率非常低，非常的浪费资源--->因为HTTP协议只能由客户端向服务端发起，故必须不停的连接后端，请求新的数据
```

**不推荐使用短连接方式实现**



##### 长连接实现方案

```md
在客户端和服务端之间构建一个可以长久连接的双边通信通道---->采用WebSocket协议进行前后端通信
```

##### WebSocket协议优点

**先分清楚单工、半双工、全双工的含义**

```md
单工： 数据传输只允许在一个方向上的传输，只能一方来发送数据，另一方来接收数据并发送。例如：对讲机

半双工：数据传输允许两个方向上的传输，但是同一时间内，只可以有一方发送或接受消息。例如：打电话

全双工：同时可进行双向传输。例如：websocket

```

**然后**

```md
HTTP协议的通信只能由客户端发起，做不到服务器主动向客户端推送消息。而WebSocket协议是基于TCP的一种新的网络通信协议，它实现了浏览器与服务器之间的全双工通信。
报文体积小（WebSocket没有请求头），支持长连接。

HTTP协议在1.1以下是属于单工通信的，1.1版本及以上通过keep alive可以实现半双工通信。在HTTP2.0基于TCP构建的情况下，允许服务端主动向客户端发送请求，实现了全双工通信。
```

##### WebSocket缺点

```md
没有请求头，相较于HTTP协议可扩展性差。
```

**要根据具体情况具体分析确定要使用的协议，因为各有优缺点**



##### 弹幕系统结构设计

![image-20230703144854951](C:\Users\Amber\AppData\Roaming\Typora\typora-user-images\image-20230703144854951.png)



MQ的特点是**异步、解耦和削峰**



**注意在要使用高并发的模块时，要先测试下服务器最大的并发数量是多大，以及在多少并发数量下我们可以达到最大的信息吞吐量--->每秒能处理的最大请求数量。通过这些参数，去设计并发请求的拆分，以及MQ信息保存的设计。**



```
统计当前长连接有多少人正在连接需要使用到AtomicInteger--->java给提供的一个原子性操作的一个类   保证线程安全
```



##### 多例模式下bean注入问题的解决

```md
springBoot当中，依赖注入默认使用的是单例模式。使用websocket时，当一个客户端来连接时就需要新生成一个新的websocketService，如此是一个多例模式。对于多例模式，需要通过一个ConcurrentHashMap类，将每个客户端唯一的key来存储该客户端对应生成的webSocketService。**所以注意哦**在多例模式注入Bean的情况下，使用@Autowired自动注入的话是会出问题的。因为默认是单例注入，如果第二个客户端请求过去去请求不到已经注入过的bean的，会为null。----->可以通过ApplicationContext来获取相关的实体类与bean，从某些角度上解决了springBoot单例注入的弊端
```



##### webSocket通信流程

比如服务端想推送消息给客户端

```md
首先通过客户端的唯一标识--->从ConcurrentHashMap中取到该和客户端相对应的WebSocketService--->而在每个WebSocketService中保存了对应与当次连接的一个Session--->再通过Session的一些相关方法得以与该客户端通信
```





#### 接入ElasticSearch、可视化Kibana

##### 优势

```md
在传统的行与列构建的关系型数据库当中，在存储内容、格式复杂的数据上是非常不灵活的(如json格式的数据，我们需要把json里的对象每行每列进行拆分，保存至多张表里面，这样开起来就稍许麻烦了)，如果我们要构建一个完整的数据样例往往需要多张数据库表的连接下进行多表查询，得到结果之后得再构建一个比较庞大的Object对象

如果我们可以将对象按照对象的方式来存储，这样我们就能更加专注于使用数据，重新利用对象的灵活性。

JSON是一种以人可读的、文本表示对象的方法。它已经编程NoSQL师姐交换数据的事实标准。


而在ElasticSearch当中，可以直接使用json的格式存储数据，通过ElasticSearch所提供的功能对json数据全文内容进行全文搜索，可以有效的提升搜索的效率以及搜索的维度与范围。
```



##### SpringBoot接入ES步骤

第一步引入依赖：

```xml
<dependency><!--springframework自带的一个操作ES的依赖-->
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
            <version>2.5.1</version>
</dependency>
```

第二步创建一个关于ElasticSearch的配置类，配置类中创建一个ESClient客户端。注意要和Service存在一个项目下。  也叫做连接类

```java
@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticsearch.url}")//在properties里配置号ES的url（要包括端口号）
    private String esUrl;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient(){//ES封装的一个通过Restfull格式的方法去访问ES相关API的客户端
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(esUrl)
                .build();
        return RestClients.create(clientConfiguration).rest();//返回一个可以直接调用ES相关API的对象
    }
}
```

第三步需要创建一个类似于在操作数据库的时候DAO角色的一个接口类，我暂且把它叫做repositoy，在DAO层，于dao在一个层面。

```java
public interface VideoRepository extends ElasticsearchRepository<Video, Long> {//需要继承ElasticsearchRepository接口

    Video findByTitleLike(String keyword);
}
```

第四步可以直接使用VideoRepository实例化videoRepository因为其继承了ElasticsearchRepository父类接口，拥有了一些普通方法，和mybitasplus作用一样。又或者使用RestHighLevelClient操作数据。

```java
 public void addVideo(Video video){
        videoRepository.save(video);
    }

 SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
```





##### 问题---java中自己定义的特殊类，比如有一些特殊的字段(userInfoList)加入的实体类该如何与ES相对应呢？

使用注解解决

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Document(indexName = "videos")//索引的名称   如果在ES中没有这个索引的话，ES会自动创建
public class Video {

    @Id
    private Long id;

    @Field(type = FieldType.Long)//type表示在ES当中应该以哪种格式存储
    private Long userId;//用户id

    private String url; //视频链接

    private String thumbnail;//封面

    @Field(type = FieldType.Text)//Text指，title是可以支持分词查询的
    private String title; //标题

    private String type;// 0自制 1转载

    private String duration;//时长

    private String area;//分区

    private List<VideoTag> videoTagList;//标签列表  可以进行个性化的推荐与定制

    @Field(type = FieldType.Text)
    private String description;//简介

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;
```



##### 全文搜索

既可以搜视频相关，也可以搜用户相关nickName等等

**高亮、分词等功能实现**









#### 内容推荐与观看记录

**通过记录观看历史，获取视频内容，通过推荐公式对用户进行个性化的内容推荐**

##### 观看记录

```md
规则--->针对同一个视频，同一个用户一天只能增加一次播放记录，游客也是一天一次(用户退出登录后观看也参与计算播放量)
```

##### 游客区分规则

```md
操作系统+浏览器+IP三个参数信息来区别---->这三个参数都可以在请求头里面获取到
```

##### 内容推荐

```md
使用协同过滤算法来实现内容推荐，实现选用Apache Mahout来具体实现。

Mahout是一个开源的分布式机器学习算法库，它是一个基于Java实现的可扩展、高效的推荐引擎。

Mahout常用于:基于用户的推荐、基于内容的推荐。
```

##### 推荐算法步骤

第一

```md
收集用户偏好数据，用户的偏好数据开源体现在多种操作行为上，如点赞、收藏、转发、是否购买等
```

第二

```md
数据降噪与归一化处理：不同偏好维度的数据需要进行数据降噪与归一化处理来形成统一的偏好得分

归一化处理的原因--->由于在不同维度上的评分机制或者偏好计算是不一样的，不好做统一的偏好计算处理，需要有一套归一化的处理机制将不同维度上的数据形成统一的偏好计算。
```

第三

```md
算出相似物品或用户：基于用户推荐、基于内容推荐
```



##### 基于用户的推荐

```md
核心思想：推荐和此用户相似的用户喜欢的内容

本质是基于用户对内容的偏好找到相邻的相似用户，然后将邻居用户喜欢的内容推荐给当前用户

例子：张三爱看恐怖电影、爱情电影，李四爱看戏剧电影、王五爱看爱情电影、恐怖电影、纪录片，那么张三和王五的相似度比较高，所以开源推荐张三看纪录片！
```



##### 基于物品的推荐

```md
核心思想：推荐和此用户喜欢内容的相似内容给该用户

本质的基于用户对内容的偏好找到相似的内容，然后依据用户的历史行为偏好，推荐相似的内容给用户

例子：张三以前买手机，则会把和收集相似度搞的其他商品推荐给张三
```



##### 偏好打分机制示例

```sql
CREATE TABLE `t_video_operation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `userId` BIGINT DEFAULT NULL COMMENT '用户id',
  `videoId` BIGINT DEFAULT NULL COMMENT '视频id',
  `operationType` VARCHAR(5) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '操作类型：0点赞、1收藏、2投币',
  `createTime` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb3 COMMENT='视频操作表';
```

```sql
select
	userId,
	videoId,
	sum(case operationType
       		when '0' then 6 #点赞
        	when '1' then 2 #收藏
        	when '2' then 2 #投币
        	else 0 and
       )as 'value'
       from
       	t_video_operation
       	group by userId,videoId
       	order by userId
```





#### 弹幕遮罩

**处理视频生成人像黑白剪影，实现遮挡弹幕的效果**



















