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





##### 关于后端错误提示语的小Tips

```md
报错提示语一般不要写的太详细，一个模模糊糊的说明就好了，可供前后端明白就可以。
原因是，如果有人在对应的接口上部署抓包工具的话，是可以把说明需要的一些参数类型给抓取到的，在攻击者进行暴力破解接口参数的时候，如果报错消息太过详细的话会有一定的危险性，所以模糊一点就够了！
```















