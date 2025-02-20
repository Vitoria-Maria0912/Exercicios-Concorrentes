package estudos;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

class SharedQueue {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;
    private final Semaphore mutex = new Semaphore(1);

    public SharedQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(int value) throws InterruptedException {
        mutex.acquire();
        while (queue.size() == capacity) {
            wait(); // Aguarda até que haja espaço na fila
        }
        queue.add(value);
        System.out.println("Produzido: " + value);
        notify(); // Notifica um consumidor esperando
        mutex.release();
    }

    public synchronized int consume() throws InterruptedException {
        mutex.acquire();
        while (queue.isEmpty()) {
            wait(); // Aguarda até que haja um item na fila
        }
        int value = queue.poll();
        System.out.println("Consumido: " + value);
        notify(); // Notifica um produtor esperando
        mutex.release();
        return value;
    }
}

public class ProducerConsumerExample {
    public static void main(String[] args) {
        SharedQueue queue = new SharedQueue(5);

        // Thread do produtor
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

        // Thread do consumidor
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
