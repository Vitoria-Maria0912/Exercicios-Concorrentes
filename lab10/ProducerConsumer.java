import java.util.concurrent.*;

public class ProducerConsumerExample {
    private static final int CAPACIDADE_MAX = 5;

    public static void main(String[] args) {
        // Criar uma fila segura para múltiplas threads (BlockingQueue)
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(CAPACIDADE_MAX);

        // Criar um pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Criar o Producer (Callable para usar Future)
        Callable<Void> producer = () -> {
            for (int i = 1; i <= 10; i++) {
                Thread.sleep(1000); // Simula um tempo de produção
                queue.put(i);  // Adiciona na fila (bloqueia se cheia)
                System.out.println("🛠️ Produziu: " + i);
            }
            return null;
        };

        // Criar o Consumer (Runnable)
        Runnable consumer = () -> {
            try {
                while (true) {
                    Integer item = queue.take(); // Remove da fila (bloqueia se vazia)
                    System.out.println("✅ Consumiu: " + item);
                    Thread.sleep(1500); // Simula um tempo de processamento
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Submeter as tarefas
        Future<Void> producerFuture = executor.submit(producer); // Callable retorna Future
        executor.execute(consumer); // Runnable não retorna resultado

        try {
            // Aguarda a finalização do produtor
            producerFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Encerrar o executor após o produtor terminar
        executor.shutdown();
    }
}
