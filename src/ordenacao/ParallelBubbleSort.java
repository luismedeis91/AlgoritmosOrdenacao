package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** Bubble Sort paralelo: variante par-impar, cada fase dividida em tarefas Fork/Join. */
public class ParallelBubbleSort {

    static final int LIMITE = 16_384;

    public static void sort(int[] a, int threads) {
        int n = a.length;
        if (n < LIMITE) {
            BubbleSort.sort(a);
            return;
        }

        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            int semTroca = 0;
            // duas fases seguidas sem troca cobrem todos os pares vizinhos
            for (int fase = 0; fase < n && semTroca < 2; fase++) {
                int inicio = fase % 2;
                int pares = quantidadeDePares(n, inicio);
                if (pares <= 0) {
                    semTroca++;
                    continue;
                }
                boolean trocou = pool.invoke(new Tarefa(a, inicio, 0, pares));
                semTroca = trocou ? 0 : semTroca + 1;
            }
        } finally {
            pool.shutdown();
        }
    }

    /** Pares (j, j+1) da fase, com j = inicio, inicio+2, ... */
    private static int quantidadeDePares(int n, int inicio) {
        if (n - 2 < inicio) return 0;
        return (n - 2 - inicio) / 2 + 1;
    }

    private static class Tarefa extends RecursiveTask<Boolean> {
        private final int[] a;
        private final int inicio, k0, k1;

        Tarefa(int[] a, int inicio, int k0, int k1) {
            this.a = a;
            this.inicio = inicio;
            this.k0 = k0;
            this.k1 = k1;
        }

        @Override
        protected Boolean compute() {
            if ((k1 - k0) * 2 <= LIMITE) {
                return faseSequencial(a, inicio, k0, k1);
            }
            int meio = k0 + (k1 - k0) / 2;
            Tarefa esquerda = new Tarefa(a, inicio, k0, meio);
            Tarefa direita  = new Tarefa(a, inicio, meio, k1);
            invokeAll(esquerda, direita);   // pares disjuntos: sem corrida
            return esquerda.join() || direita.join();
        }
    }

    static boolean faseSequencial(int[] a, int inicio, int k0, int k1) {
        boolean trocou = false;
        for (int k = k0; k < k1; k++) {
            int j = inicio + 2 * k;
            if (a[j] > a[j + 1]) {
                swap(a, j, j + 1);
                trocou = true;
            }
        }
        return trocou;
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static boolean estaOrdenado(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        int threads = Runtime.getRuntime().availableProcessors();

        int[] exemplo = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Antes:  " + Arrays.toString(exemplo));
        sort(exemplo, threads);
        System.out.println("Depois: " + Arrays.toString(exemplo));

        int[][] bordas = { {}, {7}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}, {-3, 0, -1, 8, -10} };
        for (int[] caso : bordas) {
            int[] copia = caso.clone();
            sort(copia, threads);
            System.out.printf("%-20s -> %-20s %s%n",
                    Arrays.toString(caso), Arrays.toString(copia), estaOrdenado(copia) ? "OK" : "FALHOU");
        }

        // tamanhos acima de LIMITE pra passar pelo caminho paralelo
        Random rng = new Random(42);
        int testes = 20;
        for (int t = 0; t < testes; t++) {
            int[] v = rng.ints(LIMITE + rng.nextInt(LIMITE), -1000, 1000).toArray();
            int[] esperado = v.clone();
            Arrays.sort(esperado);
            sort(v, 1 + rng.nextInt(threads));
            if (!Arrays.equals(v, esperado)) {
                System.out.println("ERRO no teste " + t);
                return;
            }
        }
        System.out.println(testes + " testes aleatorios passaram.");

        int n = 50_000;
        int[] base = rng.ints(n, 0, 1_000_000).toArray();

        int[] v = base.clone();
        long inicio = System.nanoTime();
        BubbleSort.sort(v);
        long fim = System.nanoTime();
        System.out.printf("n = %d sequencial     em %.2f ms%n", n, (fim - inicio) / 1e6);

        for (int t = 1; t <= threads; t *= 2) {
            v = base.clone();
            inicio = System.nanoTime();
            sort(v, t);
            fim = System.nanoTime();
            System.out.printf("n = %d com %2d threads em %.2f ms%n", n, t, (fim - inicio) / 1e6);
        }
    }
}
