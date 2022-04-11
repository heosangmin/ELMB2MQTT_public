package jp.co.nttdatabizsys.elmb2mqtt.util;

import org.kohsuke.args4j.Option;

public class ArgBean {
    @Option(name = "-c", aliases = {"--config"}, usage = "--config /etc/elmb2mqtt.conf")
    public String configfile = "etc/elmb2mqtt_el.conf";

    @Option(name = "-1", aliases = {"--once"}, usage="1回のみ実行し終了しますう。")
    public boolean once = false;

    @Option(name = "-i", aliases = {"--interval"}, usage="ミリ秒単位の繰り返し実行間隔を指定します。")
    public int interval = 60000;
}
