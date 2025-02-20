package estudos;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SharedQueueLock {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;
    private final Lock mutex = new ReentrantLock();
    private final Condition producer = mutex.newCondition();
    private final Condition consumer = mutex.newCondition();

    public SharedQueueLock(int capacity) {
        this.capacity = capacity;
    }

    public void produce(int value) throws InterruptedException {
        mutex.lock();
        try {
            while (queue.size() == capacity) {
                producer.await(); // Aguarda até que haja espaço na fila
            }
            queue.add(value);
            System.out.println("Produzido: " + value);
            consumer.signal(); // Notifica um consumidor
        } finally {
            mutex.unlock();
        }
    }

    public int consume() throws InterruptedException {
        mutex.lock();
        try {
            while (queue.isEmpty()) {
                consumer.await(); // Aguarda até que haja um item na fila
            }
            int value = queue.poll();
            System.out.println("Consumido: " + value);
            producer.signal(); // Notifica um produtor
            return value;
        } finally {
            mutex.unlock();
        }
    }
}

public class ProducerConsumerLockExample {
    public static void main(String[] args) {
        SharedQueueLock queue = new SharedQueueLock(5);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.produce(i);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.consume();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}
