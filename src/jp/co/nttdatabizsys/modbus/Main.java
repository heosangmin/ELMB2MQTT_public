package jp.co.nttdatabizsys.modbus;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends TimerTask {
    private TimerTask task;
    private Timer timer;
    private Sample sample;

    public static void main(String[] args) {
        try {
            Sample sample = new Sample();
            Main main = new Main();
            main.setTimerTask(sample);
            sample.subscribe();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTimerTask(Sample sample){
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
		timer.scheduleAtFixedRate(task, cal.getTime(), 60000);

    }

    @Override
    public void run() {
        try{
            sample.readAndPublishAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}