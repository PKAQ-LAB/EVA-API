package io.nerv;

import io.nerv.core.exception.OathException;
import io.nerv.properties.Jwt;
import io.nerv.core.token.jwt.JwtUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/27 8:22
 */
public class JwtTest extends BaseTest{
    @Autowired
    private Jwt jwtConfig;
    @Test
    public void bcryptTest() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String md5 = "21232f297a57a5a743894a0e4a801fc3";
        String pwd = encoder.encode("21232f297a57a5a743894a0e4a801fc3");
        System.out.println(pwd);
        if (encoder.matches(md5, pwd)){
            System.out.println("match");
        } else {
            System.out.println("not match");
        }

    }
    @Test
    public void tokenTest() throws OathException {
        System.out.println("sign user is : " + jwtConfig.getSign());
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.build(500, "9199482d76b443ef9f13fefddcf0046c");
        System.out.println(token);

        boolean b = jwtUtil.valid(token);
        System.out.println(b);

        String uid = jwtUtil.getUid(token);
        System.out.println(uid);
    }
}
