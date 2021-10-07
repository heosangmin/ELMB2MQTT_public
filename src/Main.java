
public class Main {
    public static void main(String[] args) {
        Sample sample;
        try {
            sample = new Sample();
            sample.readAndPublishAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}