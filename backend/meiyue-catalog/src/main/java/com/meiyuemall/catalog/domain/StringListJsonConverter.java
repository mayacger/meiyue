package com.meiyuemall.catalog.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * List&lt;String&gt; ↔ JSON 文本（I34 图集）。
 * 优先解析 JSON 数组；失败则按逗号/换行拆分。
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : attribute) {
            if (s == null || s.isBlank()) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(s.trim().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        sb.append(']');
        return first ? null : sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        String raw = dbData.trim();
        List<String> out = new ArrayList<>();
        if (raw.startsWith("[") && raw.endsWith("]")) {
            String inner = raw.substring(1, raw.length() - 1).trim();
            if (inner.isEmpty()) {
                return out;
            }
            StringBuilder cur = new StringBuilder();
            boolean inStr = false;
            boolean esc = false;
            for (int i = 0; i < inner.length(); i++) {
                char c = inner.charAt(i);
                if (esc) {
                    cur.append(c);
                    esc = false;
                    continue;
                }
                if (c == '\\' && inStr) {
                    esc = true;
                    continue;
                }
                if (c == '"') {
                    inStr = !inStr;
                    continue;
                }
                if (c == ',' && !inStr) {
                    String v = cur.toString().trim();
                    if (!v.isEmpty()) {
                        out.add(v);
                    }
                    cur.setLength(0);
                    continue;
                }
                if (inStr) {
                    cur.append(c);
                }
            }
            String last = cur.toString().trim();
            if (!last.isEmpty()) {
                out.add(last);
            }
            return out;
        }
        for (String part : raw.split("[,\\n]")) {
            String v = part.trim();
            if (!v.isEmpty()) {
                out.add(v);
            }
        }
        return out;
    }
}
