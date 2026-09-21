package com.meiyuemall.logistics.track;

import java.util.List;

/**
 * 国内快递轨迹查询端口（I5 占位）。
 * 正式实现可接快递100/菜鸟等；无轨迹时允许人工校正。
 */
public interface ExpressTrackQueryPort {
    record TrackNode(String status, String description, String timeIso) {}
    record QueryResult(boolean found, List<TrackNode> nodes, String rawPreview) {}

    QueryResult query(String carrierCode, String trackingNo);
}
