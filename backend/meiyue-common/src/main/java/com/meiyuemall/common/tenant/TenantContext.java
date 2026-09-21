package com.meiyuemall.common.tenant;

/**
 * 请求级租户上下文（ThreadLocal）。
 * <p>
 * 对应规划 C6：共享库 + {@code tenant_id} 行级隔离。
 * 鉴权完成后由过滤器/拦截器写入；仓储层 / Hibernate Filter 读取后强制附加条件。
 * </p>
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code tenantId} — 当前商家租户 ID；买家跨店场景可为 null，平台治理也可为 null</li>
 *   <li>{@code actorType} — 参与者类型，见 {@link ActorType}</li>
 *   <li>{@code actorId} — 当前用户主键（买家/卖家成员/平台账号），未登录为 null</li>
 *   <li>{@code storeId} — MVP 与 tenant 1:1，预留给后续查询便利；可与 tenantId 相同语义</li>
 * </ul>
 * </p>
 * <p><b>安全约束：</b>客户端传入的 tenant_id 不可信；必须以鉴权结果为准写入本上下文。</p>
 */
public final class TenantContext {

    /**
     * 单次 HTTP 请求内有效的上下文载荷。
     *
     * @param tenantId  商家租户 ID，可为 null（买家/平台/匿名）
     * @param actorType 参与者类型，不可为 null
     * @param actorId   用户 ID，未登录可为 null
     * @param storeId   店铺 ID（MVP 与租户 1:1），可为 null
     */
    public record Holder(
            Long tenantId,
            ActorType actorType,
            Long actorId,
            Long storeId
    ) {
        public Holder {
            if (actorType == null) {
                actorType = ActorType.ANONYMOUS;
            }
        }
    }

    private static final ThreadLocal<Holder> CONTEXT = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 写入当前线程上下文（通常在过滤器入口调用）。
     *
     * @param holder 上下文载荷，不可为 null
     */
    public static void set(Holder holder) {
        CONTEXT.set(holder);
    }

    /**
     * 读取当前线程上下文；未设置时返回匿名占位，避免 NPE。
     */
    public static Holder get() {
        Holder holder = CONTEXT.get();
        if (holder == null) {
            return new Holder(null, ActorType.ANONYMOUS, null, null);
        }
        return holder;
    }

    /**
     * 当前商家租户 ID；买家/平台场景可能为 null。
     */
    public static Long getTenantId() {
        return get().tenantId();
    }

    /**
     * 必须在请求结束（过滤器 finally）调用，防止线程复用导致串租。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
