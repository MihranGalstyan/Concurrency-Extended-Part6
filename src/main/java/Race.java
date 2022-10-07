import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Mihran Galstyan
 * All rights reserved
 */
public class Race {
    public static void main(final String[] args) {
        List<String> result = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startPosition = new CountDownLatch(10);
        CountDownLatch endPosition = new CountDownLatch(10);

        Semaphore tunnel = new Semaphore(3);

        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String name = Thread.currentThread().getName();
                    long readyTime = (long) (Math.random() * 5000 + 1000);
                    long firstPart = (long) (Math.random() * 3000 + 1000);
                    long secondPart = (long) (Math.random() * 2000 + 1000);
                    long thirdPart = (long) (Math.random() * 3000 + 1000);
                    long totalTime = firstPart + secondPart + thirdPart;

                    System.out.println(name + " is getting ready to start");
                    try {
                        Thread.sleep(readyTime);
                        startPosition.countDown();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(name + " is ready in " + readyTime);
                    try {
                        startPosition.await();
                        System.out.println(" STARTED");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(firstPart);
                        System.out.println(name + " reached tunnel in " + firstPart);
                    } catch (InterruptedException e) {

                        throw new RuntimeException(e);
                    }
                    try {
                        tunnel.acquire();
                        tunnel();
                        tunnel.release();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(secondPart);
                        System.out.println(name + " came out from tunnel in " + secondPart);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(thirdPart);
                        result.add(name + " " + totalTime);
                        System.out.println(name + " finished in " + (firstPart + secondPart + thirdPart));
                        endPosition.countDown();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            endPosition.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nThe WINNER IS " + result.get(0));
    }

    public static void tunnel() {
        String name = Thread.currentThread().getName();

        System.out.println(name + " car is entered tunnel");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(name + " car came out from tunnel");
    }
}
