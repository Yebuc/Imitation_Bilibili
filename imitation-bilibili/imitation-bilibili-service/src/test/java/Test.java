import com.imooc.bilibili.service.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Amber
 * @create 2023-07-04 22:07
 */
public class Test {


    public static void main(String[] args){
        ApplicationContext applicationContext = WebSocketService.getApplicationContext();
        Object corsConfig = applicationContext.getBean("CorsConfig");
        System.out.println(corsConfig);
    }
}
