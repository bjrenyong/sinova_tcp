package sinova.tcp.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * Created by haoxiaodong on 2016-1-7.<br/>
 * <p/>
 * AES加密解密处理类
 */
public class MD5Util {
    private static final Logger logger = LoggerFactory.getLogger(MD5Util.class);

    public static String encrypt(String content) {
        String outStr = "";
        if (content != null && (!content.equals(""))) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(content.getBytes());
                byte b[] = md.digest();
                StringBuffer buf = new StringBuffer();
                for (int i = 1; i < b.length; i++) {
                    int c = b[i] >>> 4 & 0xf;
                    buf.append(Integer.toHexString(c));
                    c = b[i] & 0xf;
                    buf.append(Integer.toHexString(c));
                }
                outStr = buf.toString();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return outStr;
    }

}
