package org.pkaq;

import org.pkaq.security.jwt.JwtConstant;
import org.pkaq.security.jwt.JwtUtil;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/27 8:22
 */
public class JwtTest {
    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.build(JwtConstant.JWT_TTL, "9199482d76b443ef9f13fefddcf0046c");
        System.out.println(token);

        boolean b = jwtUtil.valid(token);
        System.out.println(b);

        String uid = jwtUtil.getUid(token);
        System.out.println(uid);
    }
}
