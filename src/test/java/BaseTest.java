import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import com.example.util.ParamCheckUtil;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
public class BaseTest {
    private static final String FILE_NAME = "check_rule.json";

    @Test
    public void test() {
        Console.log(ParamCheckUtil.check(getCheckObject(), getCheckRulesStr()));
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

    private String getCheckRulesStr() {
        final File file = new File(Objects.requireNonNull(BaseTest.class.getResource(FILE_NAME)).getFile());
        return new String(FileUtil.readBytes(file), StandardCharsets.UTF_8);
    }

}
