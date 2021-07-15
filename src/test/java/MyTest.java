import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Author jerrypro
 * @Date 2021/7/15
 * @Description
 */
public class MyTest {
    private static final String FILE_NAME = "check_rule.json";

    @Test
    public void test() {
        final URL resource = MyTest.class.getResource(FILE_NAME);
        final byte[] bytes =
                FileUtil.readBytes(new File(Objects.requireNonNull(resource).getFile()));
        Console.log(new String(bytes, StandardCharsets.UTF_8));
    }
}
