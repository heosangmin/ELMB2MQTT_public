
public class Main {
    public static void main(String[] args) {
        Worker worker;
        try {
            worker = new Worker();
            worker.readAndPublishAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}