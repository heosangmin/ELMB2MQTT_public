package jp.co.nttdatabizsys.elmb2mqtt.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ログをコンソール出力するためのユティリティクラス
 */
public class Logger {
    /**Logger本体 */
    private static Logger logger = null;
    /**出力On/Off */
    private boolean onoff = true;
    
    /**
     * Loggerクラスを取得する。
     * @return Logger本体
     */
    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    private Logger() {}

    /**
     * 出力をOn/Offする。
     * @param onoff on/offフラグ
     */
    public void setOnoff(boolean onoff) {
        this.onoff = onoff;
    }

    /**
     * コンソールにログを出力する。
     * @param text ログテキスト
     */
    public void log(String text) {
        if (onoff) {
            System.out.println(text);
        }
    }

    /**
     * コンソールにログをフォーマットに合わせて出力する。
     * @param format 文字列フォーマット（String.format()の１番目のパラメータ）
     * @param text ログテキスト
     */
    public void log(String format, String text) {
        if (onoff) {
            System.out.println(String.format(format, text));
        }
    }

    /**
     * コンソールに時刻とログを出力する。
     * @param text ログテキスト
     * @param timestamp (boolean)ログの頭に時刻を出力するかを決めるフラグ
     */
    public void log(String text, boolean timestamp) {
        if (onoff) {
            if (timestamp) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
                OffsetDateTime odt = OffsetDateTime.now();
                System.out.println("[" + odt.format(formatter) + "]" + text);
            } else {
                System.out.println(text);
            }
        }
    }
}
