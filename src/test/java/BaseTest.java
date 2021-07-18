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
    public void testParamCheckUtil() {
        Console.log(ParamCheckUtil.check(getCheckObject(), getCheckRulesStr()));
    }

    @Test
    public void getResourcePath() {
        Console.log(BaseTest.class.getResource(FILE_NAME));
    }

    private CheckObject getCheckObject() {
        CheckObject checkObject = new CheckObject();
        checkObject.setName("sss");
        checkObject.setOrderNo("");
        checkObject.setPhone("phone");
        checkObject.setIdCard("32098119960707397");
        return checkObject;
    }

    private String getCheckRulesStr() {
        final File file = new File(Objects.requireNonNull(BaseTest.class.getResource(FILE_NAME)).getFile());
        return new String(FileUtil.readBytes(file), StandardCharsets.UTF_8);
    }

}
