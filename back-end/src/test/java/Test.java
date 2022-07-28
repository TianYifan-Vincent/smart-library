import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.lib.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;


public class Test {
/**
 * @ClassName Test
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/5 14:25
 * @Version 1.0
 **/


    @org.junit.Test
    public void test1(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        System.out.println(year);
        System.out.println(week);
        System.out.println(weekday);
    }



}
