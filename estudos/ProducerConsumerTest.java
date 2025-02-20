package estudos;

public class ProducerConsumerTest {
    public static void main(String[] args) {
        SharedQueue queue = new SharedQueue(5); // Capacidade máxima do buffer = 5

        // Criando produtores
        Runnable producerTask = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.produce(i);
                    Thread.sleep(1); // Simula tempo de produção
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // Criando consumidores
        Runnable consumerTask = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.consume();
                    Thread.sleep(1); // Simula tempo de consumo
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // Criando e iniciando múltiplas threads
        Thread producer1 = new Thread(producerTask, "Produtor-1");
        Thread producer2 = new Thread(producerTask, "Produtor-2");
        Thread consumer1 = new Thread(consumerTask, "Consumidor-1");
        Thread consumer2 = new Thread(consumerTask, "Consumidor-2");

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        // Aguarda a finalização das threads
        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Teste finalizado.");
    }
}
