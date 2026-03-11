package com.su.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    /**
     * 鐢熸垚jwt
     * 浣跨敤Hs256绠楁硶, 绉佸寵浣跨敤鍥哄畾绉橀挜
     *
     * @param secretKey jwt绉橀挜
     * @param ttlMillis jwt杩囨湡鏃堕棿(姣)
     * @param claims    璁剧疆鐨勪俊鎭?
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 鎸囧畾绛惧悕鐨勬椂鍊欎娇鐢ㄧ殑绛惧悕绠楁硶锛屼篃灏辨槸header閭ｉ儴鍒?
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 鐢熸垚JWT鐨勬椂闂?
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 纭繚瀵嗛挜闀垮害绗﹀悎HS256绠楁硶瑕佹眰锛堣嚦灏?56浣嶏級
        byte[] keyBytes = getSecretKeyBytes(secretKey);

        // 璁剧疆jwt鐨刡ody
        JwtBuilder builder = Jwts.builder()
                // 濡傛灉鏈夌鏈夊０鏄庯紝涓€瀹氳鍏堣缃繖涓嚜宸卞垱寤虹殑绉佹湁鐨勫０鏄庯紝杩欎釜鏄粰builder鐨刢laim璧嬪€硷紝涓€鏃﹀啓鍦ㄦ爣鍑嗙殑澹版槑璧嬪€间箣鍚庯紝灏辨槸瑕嗙洊浜嗛偅浜涙爣鍑嗙殑澹版槑鐨?
                .setClaims(claims)
                // 璁剧疆绛惧悕浣跨敤鐨勭鍚嶇畻娉曞拰绛惧悕浣跨敤鐨勭閽?
                .signWith(signatureAlgorithm, keyBytes)
                // 璁剧疆杩囨湡鏃堕棿
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token瑙ｅ瘑
     *
     * @param secretKey jwt绉橀挜 姝ょ閽ヤ竴瀹氳淇濈暀濂藉湪鏈嶅姟绔? 涓嶈兘鏆撮湶鍑哄幓, 鍚﹀垯sign灏卞彲浠ヨ浼€? 濡傛灉瀵规帴澶氫釜瀹㈡埛绔缓璁敼閫犳垚澶氫釜
     * @param token     鍔犲瘑鍚庣殑token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {

        // 纭繚瀵嗛挜闀垮害绗﹀悎HS256绠楁硶瑕佹眰锛堣嚦灏?56浣嶏級
        byte[] keyBytes = getSecretKeyBytes(secretKey);
        
        // 寰楀埌DefaultJwtParser
        Claims claims = Jwts.parser()
                // 璁剧疆绛惧悕鐨勭閽?
                .setSigningKey(keyBytes)
                // 璁剧疆闇€瑕佽В鏋愮殑jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }
    
    /**
     * 鑾峰彇绗﹀悎瀹夊叏瑕佹眰鐨勫瘑閽ュ瓧鑺傛暟缁?
     * @param secretKey 鍘熷瀵嗛挜瀛楃涓?
     * @return 绗﹀悎HS256绠楁硶瑕佹眰鐨勫瘑閽ュ瓧鑺傛暟缁?
     */
    private static byte[] getSecretKeyBytes(String secretKey) {
        // 濡傛灉鍘熷瀵嗛挜闀垮害瓒冲锛屽垯鐩存帴浣跨敤锛涘惁鍒欎娇鐢↘eys宸ュ叿绫荤敓鎴?
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length >= 32) {
            // 瀵嗛挜闀垮害瓒冲锛岀洿鎺ヤ娇鐢?
            return keyBytes;
        } else {
            // 瀵嗛挜闀垮害涓嶈冻锛岀敓鎴愮鍚堣姹傜殑瀵嗛挜
            // 涓轰簡淇濇寔鍚戝悗鍏煎鎬э紝鎴戜滑鍩轰簬鍘熷瀵嗛挜鐢熸垚涓€涓‘瀹氭€х殑瀹夊叏瀵嗛挜
            byte[] secureBytes = new byte[32];
            System.arraycopy(keyBytes, 0, secureBytes, 0, Math.min(keyBytes.length, 32));
            for (int i = keyBytes.length; i < 32; i++) {
                secureBytes[i] = (byte) (keyBytes[i % keyBytes.length] ^ i);
            }
            return secureBytes;
        }
    }
}
