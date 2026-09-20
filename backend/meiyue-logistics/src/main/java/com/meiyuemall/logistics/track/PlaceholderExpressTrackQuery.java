package com.meiyuemall.logistics.track;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

/** 轨迹查询占位：返回模拟「已揽收」节点，便于联调状态机 */
@Component
public class PlaceholderExpressTrackQuery implements ExpressTrackQueryPort {
    @Override
    public QueryResult query(String carrierCode, String trackingNo) {
        if (trackingNo == null || trackingNo.isBlank()) {
            return new QueryResult(false, List.of(), "EMPTY_TRACKING");
        }
        return new QueryResult(true, List.of(
                new TrackNode("PICKED_UP", "【占位】承运商已揽收 " + carrierCode, Instant.now().toString()),
                new TrackNode("IN_TRANSIT", "【占位】运输中", Instant.now().toString())
        ), "PLACEHOLDER_QUERY");
    }
}
