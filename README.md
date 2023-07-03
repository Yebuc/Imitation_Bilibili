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



































