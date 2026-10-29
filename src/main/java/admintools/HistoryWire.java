package admintools;

import arc.util.serialization.Json;
import arc.util.serialization.JsonWriter;

/** Keep primitive v2 DTOs independent from global game JSON configuration. */
public final class HistoryWire {
    private static final Json JSON = new Json(JsonWriter.OutputType.json);
    static {
        JSON.setTypeName(null);
        JSON.setUsePrototypes(false);
        JSON.setIgnoreUnknownFields(false);
    }
    private HistoryWire() {}
    public static String write(Object value) {
        return JSON.toJson(value, value.getClass());
    }
    public static <T> T read(Class<T> type, String text) {
        return JSON.fromJson(type, text);
    }
}
