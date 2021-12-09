package jp.co.nttdatabizsys.modbus;

import java.sql.Timestamp;

public class Logger {
    private static Logger logger = null;
    private boolean onoff = true;
    
    public static Logger getLogger(boolean onoff) {
        if (logger == null) {
            logger = new Logger(onoff);
        }
        return logger;
    }

    private Logger(boolean onoff) {
        this.onoff = onoff;
    }

    public void log(String text) {
        if (onoff) {
            System.out.println(text);
        }
    }

    public void log(String format, String text) {
        if (onoff) {
            System.out.println(String.format(format, text));
        }
    }

    public void logWithTimestamp(String text) {
        if (onoff) {
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + text);
        }
    }

}
