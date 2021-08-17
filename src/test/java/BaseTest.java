import cn.hutool.core.lang.Console;
import com.example.util.FileUtil;
import com.example.util.ParamCheckUtil;
import mt.CheckObject;
import org.junit.Test;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
public class BaseTest {
    private static final String FILE_NAME = "check_rule.json";

    @Test
    public void testParamCheckUtil() {
        Console.log(ParamCheckUtil.check(getCheckObject(), FileUtil.readFile(FILE_NAME)));
    }

    private CheckObject getCheckObject() {
        CheckObject checkObject = new CheckObject();
        checkObject.setName("名字");
        checkObject.setPhone("13012345978");
        checkObject.setOrderNo("1234567890");
        // 身份证号码
        checkObject.setIdCard("1234567890");
        // 类型范围 1, 2, 3
        checkObject.setType("4");
        return checkObject;
    }


}
