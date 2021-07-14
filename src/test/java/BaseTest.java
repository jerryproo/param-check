import cn.hutool.core.lang.Console;
import com.example.util.JsonRuleChecker;
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

    public static String readToString(File file) {

        long fileLength = file.length();
        byte[] fileContent = new byte[(int) fileLength];
        return new String(fileContent, StandardCharsets.UTF_8);

    }

    @Test
    public void test() {
        CheckObject checkObject = new CheckObject();
        checkObject.setName("test-name");
        checkObject.setOrderNo("orderNo");
        checkObject.setPhone("phone");
        JsonRuleChecker.check(checkObject,
                readToString(new File(Objects.requireNonNull(BaseTest.class.getResource(FILE_NAME)).getFile())));
    }

    @Test
    public void getResourcePath() {
        Console.log(BaseTest.class.getResource(FILE_NAME));
    }
}
