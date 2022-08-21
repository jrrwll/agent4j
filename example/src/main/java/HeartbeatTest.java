import org.dreamcat.common.util.DateUtil;

/**
 * @author Jerry Will
 * @version 2022-08-19
 */
public class HeartbeatTest {

    public static void main(String[] args) throws Exception {
        HeartbeatTest obj = new HeartbeatTest();

        StaticInner si = new StaticInner();

        Inner i = obj.new Inner();

        class MethodInner {
            void call() {
                System.out.println("call method inner");
            }
        }
        MethodInner mi = new MethodInner();

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("call interface");
            }
        };

        Runnable r2 = () -> {
            System.out.println("call lambda inner");
        };

        for (;;) {
            r2.run();
            r1.run();
            mi.call();
            i.call();
            si.call();
            obj.call();
        }
    }

    void call() throws InterruptedException {
        System.out.println("[*] beat on " + DateUtil.formatNow());
        Thread.sleep(7000);
    }

    private static class StaticInner {

        void call() {
            System.out.println("call static inner");
        }
    }

    private class Inner {

        void call() {
            System.out.println("call no-static inner");
        }
    }
}
