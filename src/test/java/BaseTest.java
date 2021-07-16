import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import com.example.util.JsonRuleChecker;
import com.example.util.ParamCheckUtil;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(fileContent, StandardCharsets.UTF_8);

    }

    @Test
    public void test() {
        CheckObject checkObject = getCheckObject();
        JsonRuleChecker.check(checkObject, getCheckRulesStr());
    }

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
        checkObject.setName("");
        checkObject.setOrderNo("orderNo");
        checkObject.setPhone("phone");
        return checkObject;
    }

    private String getCheckRulesStr() {
        final File file = new File(Objects.requireNonNull(BaseTest.class.getResource(FILE_NAME)).getFile());
        return new String(FileUtil.readBytes(file), StandardCharsets.UTF_8);
    }

}
