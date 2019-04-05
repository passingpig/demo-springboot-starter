package demo.pay;

import demo.lib.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoPayApplication.class)
public class UserTests {

    @Autowired
    private UserService userService;

    @Test
    public void testUserService() {
        userService.getByToken("abc");
    }

}
