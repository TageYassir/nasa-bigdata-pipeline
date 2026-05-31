package nasa;

import java.util.regex.*;
public class NasaLogParser {
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[(.*?)\\] \"(\\S+) ([^ ]+)[^\\\"]*\" (\\d{3}) (\\S+)"
    );
    public static ParsedLog parse(String logLine) {
        Matcher m = LOG_PATTERN.matcher(logLine);
        if (!m.find()) return null;
        try {
            return new ParsedLog(
                    m.group(1),
                    m.group(2),
                    m.group(3),
                    m.group(4),
                    Integer.parseInt(m.group(5)),
                    m.group(6)
            );
        } catch (Exception e) {
            return null;
        }
    }
    public static class ParsedLog {
        public final String host, timestamp, method, url, status, bytes;
        public ParsedLog(String h, String ts, String method, String url, int status, String bytes) {
            this.host = h; this.timestamp = ts; this.method = method; this.url = url; this.status = String.valueOf(status); this.bytes = bytes;
        }
    }
}