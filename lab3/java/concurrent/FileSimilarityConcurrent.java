package lab3.java.concurrent;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FileSimilarityConcurrent {

    // Mapa compartilhado para armazenar a fingerprint de cada arquivo.
    private static final Map<String, List<Long>> fileFingerprints = new HashMap<>();

    // Lock e condition para sincronizar a contagem de tarefas concluídas.
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition allDone = lock.newCondition();
    private static int completedTasks = 0;
    private static int totalTasks = 0;

    // Semaphore para limitar o número de threads lendo arquivos simultaneamente (opcional)
    private static final Semaphore semaphore = new Semaphore(5); // por exemplo, 5 ao mesmo tempo

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java FileSimilarityConcurrent filepath1 filepath2 ... filepathN");
            System.exit(1);
        }

        totalTasks = args.length;

        // Para cada arquivo, cria uma thread que calcula sua fingerprint
        for (String path : args) {
            new Thread(new FileSumTask(path)).start();
        }

        // Aguarda que todas as threads terminem (usando lock e condition)
        lock.lock();
        try {
            while (completedTasks < totalTasks) {
                allDone.await();
            }
        } finally {
            lock.unlock();
        }

        // Com todas as fingerprints calculadas, realiza as comparações entre os arquivos
        List<String> files = new ArrayList<>(fileFingerprints.keySet());
        for (int i = 0; i < files.size(); i++) {
            for (int j = i + 1; j < files.size(); j++) {
                String file1 = files.get(i);
                String file2 = files.get(j);
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                float similarityScore = similarity(fingerprint1, fingerprint2);
                System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
            }
        }
    }

    // Tarefa que processa um arquivo: calcula sua fingerprint e armazena no mapa compartilhado
    static class FileSumTask implements Runnable {
        private final String filePath;

        public FileSumTask(String filePath) {
            this.filePath = filePath;
        }

        public void run() {
            try {
                // Usa o semaphore para limitar a concorrência (opcional)
                semaphore.acquire();
                List<Long> fingerprint = fileSum(filePath);
                semaphore.release();

                // Armazena a fingerprint no mapa compartilhado de forma segura
                lock.lock();
                try {
                    fileFingerprints.put(filePath, fingerprint);
                    completedTasks++;
                    // Se todas as tarefas estiverem concluídas, sinaliza a thread principal
                    if (completedTasks == totalTasks) {
                        allDone.signal();
                    }
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Calcula a "impressão digital" (fingerprint) de um arquivo lendo-o em blocos
    private static List<Long> fileSum(String filePath) throws IOException {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
            }
        }
        return chunks;
    }

    // Soma os valores dos bytes de um buffer (convertendo para inteiro sem sinal)
    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    // Calcula a similaridade entre duas fingerprints
    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }
        return (float) counter / base.size();
    }
}
