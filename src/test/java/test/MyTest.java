package test;

import cn.hutool.core.lang.Console;
import com.example.vo.CheckRule;
import org.junit.Test;

/**
 * @Author jerrypro
 * @Date 2021/7/15
 * @Description
 */
public class MyTest {
    @Test
    public void test() {
        CheckRule checkRule = new CheckRule();
        checkRule.setMsg("aaa");
        Console.log(checkRule.getMsg());
    }
}
