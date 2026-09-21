package com.meiyuemall.boot.config;

import com.meiyuemall.catalog.domain.Category;
import com.meiyuemall.catalog.domain.CategoryStatus;
import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.repo.CategoryRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.decoration.domain.StorePage;
import com.meiyuemall.decoration.domain.StorePageStatus;
import com.meiyuemall.decoration.repo.StorePageRepository;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.repo.UserAccountRepository;
import com.meiyuemall.tenant.domain.MemberRole;
import com.meiyuemall.tenant.domain.SellerMember;
import com.meiyuemall.tenant.domain.Store;
import com.meiyuemall.tenant.domain.StoreStatus;
import com.meiyuemall.tenant.domain.Tenant;
import com.meiyuemall.tenant.domain.TenantStatus;
import com.meiyuemall.tenant.repo.SellerMemberRepository;
import com.meiyuemall.tenant.repo.StoreRepository;
import com.meiyuemall.tenant.repo.TenantRepository;
import com.meiyuemall.platform.domain.PlatformBanner;
import com.meiyuemall.platform.repo.PlatformBannerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

/**
 * I13 演示种子（可开关）+ I28 Banner 补种。
 * <p>
 * 启用条件：{@code meiyue.demo.enabled=true}（通常随 {@code spring.profiles.active=demo} 加载
 * {@code application-demo.yml}）。
 * </p>
 * <p>
 * 幂等键：店主用户名 {@code seller1}；若已存在则跳过账号/商品，仍会补种空 Banner。
 * 写入内容：
 * <ul>
 *   <li>买家 {@code buyer1 / buyer123}（角色 BUYER）</li>
 *   <li>店主 {@code seller1 / seller123}（角色 BUYER + SELLER_OWNER）</li>
 *   <li>租户 + 店铺 {@code demo-flower}（状态 OPEN）</li>
 *   <li>2 个已上架商品（挂 V2 预置类目「鲜花绿植」）</li>
 *   <li>已发布装修页（BANNER + PRODUCT_RECOMMEND，含商品 ID）</li>
 *   <li>平台首页运营 Banner（非直播）</li>
 * </ul>
 * 不写入任何支付/AI 密钥；不做真实分账。
 * </p>
 */
@Component
@Order(200)
@ConditionalOnProperty(prefix = "meiyue.demo", name = "enabled", havingValue = "true")
public class DemoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);

    /** 幂等：已有该用户则认为演示数据已种过 */
    public static final String SELLER_USERNAME = "seller1";
    public static final String BUYER_USERNAME = "buyer1";
    /** 店铺 slug，买家可通过公开接口按租户看装修/商品 */
    public static final String STORE_SLUG = "demo-flower";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final StoreRepository storeRepository;
    private final SellerMemberRepository sellerMemberRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StorePageRepository storePageRepository;
    private final PlatformBannerRepository platformBannerRepository;

    public DemoSeeder(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository,
            StoreRepository storeRepository,
            SellerMemberRepository sellerMemberRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            StorePageRepository storePageRepository,
            PlatformBannerRepository platformBannerRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.storeRepository = storeRepository;
        this.sellerMemberRepository = sellerMemberRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.storePageRepository = storePageRepository;
        this.platformBannerRepository = platformBannerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureBanners();

        if (userAccountRepository.existsByUsername(SELLER_USERNAME)) {
            log.info("演示种子已存在（{}），跳过账号商品；Banner 已校验", SELLER_USERNAME);
            return;
        }

        // 1) 买家账号（方便开箱登录买家端）
        UserAccount buyer = new UserAccount();
        buyer.setUsername(BUYER_USERNAME);
        buyer.setPasswordHash(passwordEncoder.encode("buyer123"));
        buyer.setDisplayName("演示买家");
        buyer.setStatus(UserStatus.ENABLED);
        buyer.setRoles(EnumSet.of(RoleCode.BUYER));
        userAccountRepository.save(buyer);

        // 2) 店主账号
        UserAccount seller = new UserAccount();
        seller.setUsername(SELLER_USERNAME);
        seller.setPasswordHash(passwordEncoder.encode("seller123"));
        seller.setDisplayName("演示店主");
        seller.setStatus(UserStatus.ENABLED);
        seller.setRoles(EnumSet.of(RoleCode.BUYER, RoleCode.SELLER_OWNER));
        userAccountRepository.save(seller);

        // 3) 租户 + 店铺
        Tenant tenant = new Tenant();
        tenant.setName("美月鲜花示范店");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);

        Store store = new Store();
        store.setTenantId(tenant.getId());
        store.setName("美月鲜花示范店");
        store.setSlug(STORE_SLUG);
        store.setStatus(StoreStatus.OPEN);
        // I31：示范店默认运费 8 元，满 199 包邮
        store.setFreightCents(800);
        store.setFreeShippingThresholdCents(19900L);
        storeRepository.save(store);

        SellerMember member = new SellerMember();
        member.setTenantId(tenant.getId());
        member.setUserId(seller.getId());
        member.setMemberRole(MemberRole.OWNER);
        sellerMemberRepository.save(member);

        // 4) 类目：优先挂「鲜花绿植」（Flyway V2 已插入）；没有则取第一个 ENABLED
        Long categoryId = categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ENABLED).stream()
                .filter(c -> "鲜花绿植".equals(c.getName()))
                .map(Category::getId)
                .findFirst()
                .orElseGet(() -> categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ENABLED).stream()
                        .map(Category::getId)
                        .findFirst()
                        .orElse(null));

        // 5) 示例商品（已上架）
        Product rose = createOnSaleProduct(
                tenant.getId(),
                categoryId,
                "红玫瑰花束 · 示范",
                "开箱演示用鲜花，MOCK 支付可走完闭环",
                "<p>演示详情：红玫瑰 11 枝，勿用于真实发货。</p>",
                "DEMO-ROSE-11",
                "11枝/束",
                9900,
                100
        );
        Product lily = createOnSaleProduct(
                tenant.getId(),
                categoryId,
                "白百合礼盒 · 示范",
                "第二件演示商品，用于装修推荐楼层",
                "<p>演示详情：白百合礼盒。</p>",
                "DEMO-LILY-BOX",
                "礼盒装",
                12900,
                50
        );
        productRepository.saveAll(List.of(rose, lily));

        // 6) 已发布装修页（楼层写入商品 ID，便于买家端展示）
        String floorsJson = """
                [
                  {"type":"BANNER","enabled":true,"title":"美月示范店欢迎你","imageUrl":"","sortOrder":0},
                  {"type":"PRODUCT_RECOMMEND","enabled":true,"title":"热卖推荐","productIds":[%d,%d],"sortOrder":1}
                ]
                """.formatted(rose.getId(), lily.getId()).trim();

        StorePage published = new StorePage();
        published.setTenantId(tenant.getId());
        published.setStoreId(store.getId());
        published.setTemplateCode("simple_banner");
        published.setThemeColor("#1a5f4a");
        published.setFloorsJson(floorsJson);
        published.setStatus(StorePageStatus.PUBLISHED);
        published.setPublishedAt(Instant.now());
        storePageRepository.save(published);

        log.info(
                "演示种子已写入：buyer1/buyer123 · seller1/seller123 · 店铺 slug={} · tenantId={} · 商品 {}/{}",
                STORE_SLUG,
                tenant.getId(),
                rose.getId(),
                lily.getId()
        );
    }

    /** I28：若无 Banner 则写入两条示意运营位（渐变占位图用 CSS/空图 URL） */
    private void ensureBanners() {
        if (platformBannerRepository.count() > 0) {
            return;
        }
        PlatformBanner a = new PlatformBanner();
        a.setTitle("月色精选 · 开箱即用");
        a.setImageUrl("https://picsum.photos/seed/meiyue-banner1/1200/400");
        a.setLinkUrl("/products");
        a.setSortOrder(20);
        a.setEnabled(true);
        PlatformBanner b = new PlatformBanner();
        b.setTitle("多店一单 · 安心履约");
        b.setImageUrl("https://picsum.photos/seed/meiyue-banner2/1200/400");
        b.setLinkUrl("/products");
        b.setSortOrder(10);
        b.setEnabled(true);
        platformBannerRepository.saveAll(List.of(a, b));
        log.info("演示 Banner 已写入 2 条");
    }

    /**
     * 组装已上架 SPU + 单个 SKU。
     *
     * @param tenantId   租户 ID
     * @param categoryId 类目 ID（可空）
     * @param title      商品标题
     * @param subtitle   副标题
     * @param detailHtml 详情 HTML
     * @param skuCode    SKU 编码（租户内唯一）
     * @param specText   规格文案
     * @param priceCents 价格（分）
     * @param stockQty   可售库存
     */
    private Product createOnSaleProduct(
            Long tenantId,
            Long categoryId,
            String title,
            String subtitle,
            String detailHtml,
            String skuCode,
            String specText,
            long priceCents,
            int stockQty
    ) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setCategoryId(categoryId);
        product.setTitle(title);
        product.setSubtitle(subtitle);
        product.setDetailHtml(detailHtml);
        product.setStatus(ProductStatus.ON_SALE);

        ProductSku sku = new ProductSku();
        sku.setTenantId(tenantId);
        sku.setProduct(product);
        sku.setSkuCode(skuCode);
        sku.setSpecText(specText);
        sku.setPriceCents(priceCents);
        sku.setStockQty(stockQty);
        product.getSkus().add(sku);
        return product;
    }
}
