package estudos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SharedResource {
    private int count = 0;
    private final Lock mutex = new ReentrantLock();
    
    public synchronized void waitForThreshold(int threshold) throws InterruptedException {
        while (count < threshold) {
            wait(); // Espera até que count >= threshold
        }
        System.out.println(Thread.currentThread().getName() + " liberada!");
    }

    public synchronized void incrementAndNotify() {
        mutex.lock();
        count++;
        System.out.println("Contador atualizado para: " + count);
        mutex.unlock();
        notifyAll(); // Acorda todas as threads esperando
    }
}

public class NotifyAllExample {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();
        
        // Criando múltiplas threads que esperam pela condição
        for (int i = 0; i < 3; i++) {
            new Thread(
                () -> {
                    try {
                        resource.waitForThreshold(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "Thread-" + i
            ).start();
        }

        // Simulando eventos que incrementam o contador
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    resource.incrementAndNotify();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
