package org.pkaq.core.auth.openapi.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取的HttpServletRequest
 *
 * @author PKAQ
 */
@Slf4j
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /**
     * 缓存的Body数据
     */
    private final byte[] cachedBody;

    /**
     * 构造函数(立即读取并缓存Body)
     *
     * @param request 原始请求
     * @throws IOException 读取失败
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());

        log.trace("缓存请求体 - 长度: {}, 方法: {}, 路径: {}",
                cachedBody.length,
                request.getMethod(),
                request.getRequestURI());
    }

    /**
     * 获取缓存的Body(字节数组)
     *
     * @return Body字节数组
     */
    public byte[] getCachedBody() {
        return cachedBody;
    }

    /**
     * 获取缓存的Body(字符串)
     *
     * @return Body字符串
     */
    public String getCachedBodyAsString() {
        return new String(cachedBody, StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    /**
     * 自定义ServletInputStream
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream buffer;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.buffer = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("ReadListener not supported");
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }
    }
}
