package jp.co.nttdatabizsys.elmb2mqtt;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.kohsuke.args4j.CmdLineParser;

import jp.co.nttdatabizsys.elmb2mqtt.util.ArgBean;

/**
 * Modbus-MQTT間データ転送プログラム
 * 毎分（60秒間隔）Modbusデバイスから指定したアドレスのデータを取得しMQTTブロッカーへPublishする。
 */
public class Main extends TimerTask {
    private TimerTask task;
    private Timer timer;
    private Sample sample;

    /**
     * プログラムメイン
     * @param args １番目のパラメータにtestを指定しGet/Publish処理を１回のみ実行する。
     */
    public static void main(String[] args) {
        
        ArgBean bean = new ArgBean();
        CmdLineParser parser = new CmdLineParser(bean);

        try {
            parser.parseArgument(args);

            String configFilePath = bean.configfile;
            int interval = bean.interval;

            Sample sample = new Sample(configFilePath);

            if (bean.once) {
                sample.getAndPublish();
                System.exit(0);
            } 

            Main main = new Main();
            main.setTimerTask(sample, interval);
            sample.subscribe();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * プログラムスケジューリング設定
     * @param sample プログラム本体
     */
    public void setTimerTask(Sample sample, int interval){
        this.sample = sample;

		// 現在時刻取得
		Calendar cal = Calendar.getInstance();
		
		// 次回起動時刻設定(毎分5秒)
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		cal.add(Calendar.MINUTE, 1);
		
		// 設定
		timer = new Timer();
		task = this;
		
		// 実行
		timer.scheduleAtFixedRate(task, cal.getTime(), interval);

    }

    @Override
    public void run() {
        try{
            sample.getAndPublish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}