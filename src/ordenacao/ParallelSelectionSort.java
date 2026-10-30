package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** Selection Sort paralelo: a busca do minimo vira uma reducao Fork/Join. */
public class ParallelSelectionSort {

    static final int LIMITE = 16_384;

    public static void sort(int[] a, int threads) {
        int n = a.length;
        if (n < LIMITE) {
            SelectionSort.sort(a);
            return;
        }

        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            for (int i = 0; i < n - 1; i++) {
                int min = (n - i <= LIMITE)
                        ? minimoSequencial(a, i, n - 1)
                        : pool.invoke(new Tarefa(a, i, n - 1));
                if (min != i) swap(a, i, min);
            }
        } finally {
            pool.shutdown();
        }
    }

    private static class Tarefa extends RecursiveTask<Integer> {
        private final int[] a;
        private final int lo, hi;

        Tarefa(int[] a, int lo, int hi) {
            this.a = a;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected Integer compute() {
            if (hi - lo < LIMITE) {
                return minimoSequencial(a, lo, hi);
            }
            int mid = lo + (hi - lo) / 2;
            Tarefa esquerda = new Tarefa(a, lo, mid);
            Tarefa direita  = new Tarefa(a, mid + 1, hi);
            invokeAll(esquerda, direita);   // tarefas so leem o vetor: sem corrida
            int e = esquerda.join();
            int d = direita.join();
            return a[e] <= a[d] ? e : d;
        }
    }

    static int minimoSequencial(int[] a, int lo, int hi) {
        int min = lo;
        for (int j = lo + 1; j <= hi; j++) {
            if (a[j] < a[min]) {
                min = j;
            }
        }
        return min;
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

        int[] exemplo = {29, 10, 14, 37, 13, 5, 88};
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
        SelectionSort.sort(v);
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
