package com.meiyuemall.payment.job;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentReconcileBatch;
import com.meiyuemall.payment.domain.PaymentReconcileDiff;
import com.meiyuemall.payment.domain.PaymentStatus;
import com.meiyuemall.payment.repo.PaymentReconcileBatchRepository;
import com.meiyuemall.payment.repo.PaymentReconcileDiffRepository;
import com.meiyuemall.payment.repo.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 日对账任务占位：汇总本地成功支付；通道侧账单拉取待 I4 SDK 接入后补齐。
 * 当前：生成本地批次 + 若通道账单为空则记 CHANNEL_ONLY=0 的说明差异占位。
 */
@Component
public class PaymentReconcileJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileJob.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final PaymentRepository paymentRepository;
    private final PaymentReconcileBatchRepository batchRepository;
    private final PaymentReconcileDiffRepository diffRepository;

    public PaymentReconcileJob(
            PaymentRepository paymentRepository,
            PaymentReconcileBatchRepository batchRepository,
            PaymentReconcileDiffRepository diffRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.batchRepository = batchRepository;
        this.diffRepository = diffRepository;
    }

    /** 每天 02:15（服务器默认时区；生产请用 Asia/Shanghai） */
    @Scheduled(cron = "0 15 2 * * *")
    @Transactional
    public void reconcileYesterday() {
        LocalDate bizDate = LocalDate.now(ZONE).minusDays(1);
        for (PaymentChannel channel : List.of(PaymentChannel.WECHAT, PaymentChannel.ALIPAY, PaymentChannel.MOCK)) {
            runOne(bizDate, channel);
        }
    }

    /** 供运维手动触发 */
    @Transactional
    public PaymentReconcileBatch runOne(LocalDate bizDate, PaymentChannel channel) {
        return batchRepository.findByBizDateAndChannel(bizDate, channel.name())
                .orElseGet(() -> createBatch(bizDate, channel));
    }

    private PaymentReconcileBatch createBatch(LocalDate bizDate, PaymentChannel channel) {
        Instant from = bizDate.atStartOfDay(ZONE).toInstant();
        Instant to = bizDate.plusDays(1).atStartOfDay(ZONE).toInstant();
        List<Payment> local = paymentRepository.findByStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to)
                .stream()
                .filter(p -> p.getChannel() == channel)
                .toList();

        PaymentReconcileBatch batch = new PaymentReconcileBatch();
        batch.setBizDate(bizDate);
        batch.setChannel(channel.name());
        batch.setStatus("RUNNING");
        batch.setStartedAt(Instant.now());
        batch.setLocalCount(local.size());
        // 通道账单未接入：channel_count=0，记一条说明性差异
        batch.setChannelCount(0);
        batchRepository.save(batch);

        if (channel != PaymentChannel.MOCK && !local.isEmpty()) {
            PaymentReconcileDiff diff = new PaymentReconcileDiff();
            diff.setBatchId(batch.getId());
            diff.setDiffType("CHANNEL_ONLY");
            diff.setDetail("通道日账单拉取尚未实现（I4 骨架占位），本地成功笔数=" + local.size());
            diff.setStatus("OPEN");
            diffRepository.save(diff);
            batch.setDiffCount(1);
            batch.setNote("待接入官方对账文件/接口");
        } else {
            batch.setDiffCount(0);
            batch.setNote(channel == PaymentChannel.MOCK ? "MOCK 通道仅本地对账" : "无本地成功单");
        }
        batch.setStatus("DONE");
        batch.setFinishedAt(Instant.now());
        batchRepository.save(batch);
        log.info("日对账完成 date={} channel={} local={}", bizDate, channel, local.size());
        return batch;
    }
}
