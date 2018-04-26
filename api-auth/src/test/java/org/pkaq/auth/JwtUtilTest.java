package org.pkaq.auth;

import org.pkaq.security.jwt.JwtConstant;
import org.pkaq.security.jwt.JwtUtil;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/26 20:58
 */
public class JwtUtilTest {
    public void buildTest(){
        JwtUtil jwt  = new JwtUtil();
        String token = jwt.build(JwtConstant.JWT_TTL,"9199482d76b443ef9f13fefddcf0046c");
        System.out.println(token);
    }

    public static void main(String[] args) {
        JwtUtil jwt  = new JwtUtil();
        String token = jwt.build(JwtConstant.JWT_TTL,"9199482d76b443ef9f13fefddcf0046c");
        System.out.println(token);

        boolean b = jwt.valid(token);
        System.out.println(b);
    }

}
