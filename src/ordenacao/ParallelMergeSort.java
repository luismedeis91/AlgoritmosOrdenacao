package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Merge Sort paralelo (Fork/Join)
 *
 * Ideia: as duas metades do merge sort sao independentes, entao cada uma
 * vira uma tarefa que pode rodar em outra thread do ForkJoinPool.
 *   1. Divide o vetor ao meio.
 *   2. Ordena as duas metades EM PARALELO (invokeAll).
 *   3. Intercala as metades (merge sequencial, igual ao MergeSort).
 *
 * Detalhes desta implementacao:
 *   - Abaixo de LIMITE elementos usa o MergeSort sequencial: criar tarefa
 *     pra pedaco pequeno custa mais do que ordenar direto.
 *   - O vetor auxiliar e compartilhado: cada tarefa so mexe em aux[lo..hi],
 *     e as faixas das tarefas irmas nao se sobrepoem (sem condicao de corrida).
 *   - O numero de threads e o paralelismo do ForkJoinPool.
 *
 * Complexidade:
 *   - Trabalho total: O(n log n) (o mesmo do sequencial)
 *   - Tempo com p threads: ~O(n log n / p + n), o merge do topo e sequencial
 *   - Espaco: O(n) do vetor auxiliar
 *   - Estavel: sim
 */
public class ParallelMergeSort {

    static final int LIMITE = 10_000;

    public static void sort(int[] a, int threads) {
        int[] aux = new int[a.length];
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new Tarefa(a, aux, 0, a.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] a, aux;
        private final int lo, hi;

        Tarefa(int[] a, int[] aux, int lo, int hi) {
            this.a = a;
            this.aux = aux;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            if (hi - lo < LIMITE) {
                MergeSort.mergeSort(a, aux, lo, hi);   // pedaco pequeno: sequencial
                return;
            }
            int mid = lo + (hi - lo) / 2;
            invokeAll(new Tarefa(a, aux, lo, mid),
                      new Tarefa(a, aux, mid + 1, hi)); // espera as duas terminarem
            if (a[mid] <= a[mid + 1]) return;          // ja esta em ordem
            MergeSort.merge(a, aux, lo, mid, hi);
        }
    }

    private static boolean estaOrdenado(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        int threads = Runtime.getRuntime().availableProcessors();

        // 1) exemplo simples
        int[] exemplo = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("Antes:  " + Arrays.toString(exemplo));
        sort(exemplo, threads);
        System.out.println("Depois: " + Arrays.toString(exemplo));

        // 2) casos de borda
        int[][] bordas = { {}, {7}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}, {-3, 0, -1, 8, -10} };
        for (int[] caso : bordas) {
            int[] copia = caso.clone();
            sort(copia, threads);
            System.out.printf("%-20s -> %-20s %s%n",
                    Arrays.toString(caso), Arrays.toString(copia), estaOrdenado(copia) ? "OK" : "FALHOU");
        }

        // 3) teste aleatorio: compara com Arrays.sort
        //    tamanhos ate 5 * LIMITE pra passar de fato pelo caminho paralelo
        Random rng = new Random(42);
        int testes = 200;
        for (int t = 0; t < testes; t++) {
            int[] v = rng.ints(rng.nextInt(5 * LIMITE), -1000, 1000).toArray();
            int[] esperado = v.clone();
            Arrays.sort(esperado);
            sort(v, 1 + rng.nextInt(threads));
            if (!Arrays.equals(v, esperado)) {
                System.out.println("ERRO no teste " + t);
                return;
            }
        }
        System.out.println(testes + " testes aleatorios passaram.");

        // 4) tempo: sequencial x paralelo com varias quantidades de threads
        int n = 1_000_000;
        int[] base = rng.ints(n, 0, 1_000_000).toArray();

        int[] v = base.clone();
        long inicio = System.nanoTime();
        MergeSort.sort(v);
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
