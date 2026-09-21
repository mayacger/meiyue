package com.meiyuemall.identity.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.identity.dto.CaptchaChallengeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录图形验证码占位（I35）。
 * <p>可开关；关闭时登录不校验。答案存内存，TTL 5 分钟；非分布式生产可用 Redis 替换。</p>
 */
@Service
public class CaptchaService {

    private static final int TTL_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final boolean enabled;
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public CaptchaService(
            @Value("${meiyue.security.captcha-enabled:false}") boolean enabled
    ) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** 签发挑战；关闭时仅返回 enabled=false */
    public CaptchaChallengeResponse issue() {
        purgeExpired();
        if (!enabled) {
            return new CaptchaChallengeResponse(false, null, null);
        }
        String code = String.format("%04d", RANDOM.nextInt(10000));
        String id = UUID.randomUUID().toString().replace("-", "");
        store.put(id, new Entry(code, Instant.now().plusSeconds(TTL_SECONDS)));
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="120" height="40">
                  <rect width="120" height="40" fill="#f0f4f3"/>
                  <text x="18" y="28" font-size="22" font-family="monospace" fill="#0F3D38">%s</text>
                </svg>
                """.formatted(code);
        String dataUri = "data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return new CaptchaChallengeResponse(true, id, dataUri);
    }

    /** 登录前校验；未启用则直接通过 */
    public void verifyOrThrow(String captchaId, String captchaCode) {
        if (!enabled) {
            return;
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写图形验证码");
        }
        Entry entry = store.remove(captchaId.trim());
        if (entry == null || entry.expireAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已失效，请刷新");
        }
        if (!entry.code().equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Entry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().expireAt().isBefore(now)) {
                it.remove();
            }
        }
    }

    private record Entry(String code, Instant expireAt) {}
}
