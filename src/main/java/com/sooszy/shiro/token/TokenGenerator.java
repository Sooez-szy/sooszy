package com.sooszy.shiro.token;

import com.sooszy.util.esapi.EncryptException;
import com.sooszy.util.esapi.IYCPESAPI;
import org.owasp.esapi.ESAPI;


/**
 * token生成器
 *
 */
public class TokenGenerator {

    /**
     * 生成用户登陆token
     *
     * @param uname
     * @param ts
     * @param seed
     * @return
     * @throws EncryptException
     */
    public static String genToken(String uname, long ts, String seed) throws EncryptException {
        return IYCPESAPI.encryptor().hash(uname + ts, seed);
    }

    /**
     * 生成系统的加密seed
     *
     * @return
     * @throws EncryptException
     */
    public static String genSeed() throws EncryptException {
        return IYCPESAPI.encryptor().hash(new String(ESAPI.securityConfiguration().getMasterKey()), new String(ESAPI.securityConfiguration().getMasterSalt()));
    }
}
