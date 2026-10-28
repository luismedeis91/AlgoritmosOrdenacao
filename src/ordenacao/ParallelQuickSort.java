package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Quick Sort paralelo (Fork/Join)
 *
 * Ideia: depois da particao, os lados esquerdo e direito do pivo sao
 * independentes, entao cada um vira uma tarefa no ForkJoinPool.
 *   1. Particiona (sequencial, mesma particao do QuickSort).
 *   2. Ordena os dois lados EM PARALELO (invokeAll).
 *
 * Detalhes desta implementacao:
 *   - Abaixo de LIMITE elementos usa o QuickSort sequencial.
 *   - Nao precisa de merge: quando os dois lados terminam, o vetor ja esta ordenado.
 *   - Particao do topo e sequencial (O(n)), entao o ganho cresce conforme a
 *     recursao abre mais tarefas.
 *   - Pivo aleatorio com ThreadLocalRandom (cada thread tem seu gerador).
 *
 * Complexidade:
 *   - Trabalho total esperado: O(n log n)
 *   - Tempo com p threads: ~O(n log n / p + n)
 *   - Pior caso: O(n^2) (muito improvavel com pivo aleatorio)
 *   - Espaco: O(log n) de pilha por tarefa
 *   - Estavel: nao
 */
public class ParallelQuickSort {

    static final int LIMITE = 10_000;

    public static void sort(int[] a, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new Tarefa(a, 0, a.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] a;
        private final int lo, hi;

        Tarefa(int[] a, int lo, int hi) {
            this.a = a;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            if (hi - lo < LIMITE) {
                QuickSort.quickSort(a, lo, hi);        // pedaco pequeno: sequencial
                return;
            }
            int p = QuickSort.partition(a, lo, hi);
            invokeAll(new Tarefa(a, lo, p - 1),
                      new Tarefa(a, p + 1, hi));       // lados disjuntos: sem corrida
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
        int[] exemplo = {10, 80, 30, 90, 40, 50, 70};
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
        QuickSort.sort(v);
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
