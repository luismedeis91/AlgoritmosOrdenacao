package ordenacao;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

/** Mede os 4 algoritmos, serial e paralelo, variando n, entrada e threads; grava resultados/tempos.csv. */
public class Benchmark {

    static final String[] ALGORITMOS = {"Bubble", "Selection", "Quick", "Merge"};
    static final int[] TAMANHOS = {2_500, 5_000, 10_000, 20_000};
    static final String[] ENTRADAS = {"aleatorio", "ordenado", "invertido", "quase_ordenado", "repeticoes"};
    static final int AMOSTRAS = 5;

    public static void main(String[] args) throws IOException {
        int nucleos = Runtime.getRuntime().availableProcessors();
        Files.createDirectories(Path.of("resultados"));
        Path saida = Path.of("resultados", "tempos.csv");

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(saida))) {
            csv.println("algoritmo,versao,threads,entrada,n,amostra,tempo_ms");

            for (String alg : ALGORITMOS) {
                aquecer(alg);
                for (int n : TAMANHOS) {
                    for (String entrada : ENTRADAS) {
                        int[] base = gerar(n, entrada);

                        amostrar(csv, alg, "serial", 1, entrada, n, base);
                        for (int t = 1; t <= nucleos; t *= 2) {
                            amostrar(csv, alg, "paralelo", t, entrada, n, base);
                        }
                        // o laco acima dobra (1, 2, 4, 8...) e pula o total quando
                        // nao e potencia de 2; mede tambem o caso "todos os nucleos"
                        if ((nucleos & (nucleos - 1)) != 0) {
                            amostrar(csv, alg, "paralelo", nucleos, entrada, n, base);
                        }
                        System.out.printf("%-10s n=%-7d %s%n", alg, n, entrada);
                    }
                }
            }
        }
        System.out.println("nucleos disponiveis: " + nucleos);
        System.out.println("CSV em " + saida.toAbsolutePath());
    }

    /** Grava AMOSTRAS medicoes de uma configuracao. */
    static void amostrar(PrintWriter csv, String alg, String versao, int threads,
                         String entrada, int n, int[] base) {
        for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
            double ms = medir(alg, base, versao.equals("serial") ? 0 : threads);
            csv.printf("%s,%s,%d,%s,%d,%d,%.4f%n", alg, versao, threads, entrada, n, amostra, ms);
        }
    }

    /** threads = 0 roda a versao serial. Confere o resultado e devolve o tempo em ms. */
    static double medir(String alg, int[] base, int threads) {
        int[] a = base.clone();
        long inicio = System.nanoTime();
        ordenar(alg, a, threads);
        double ms = (System.nanoTime() - inicio) / 1e6;
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) throw new IllegalStateException("nao ordenou: " + alg);
        }
        return ms;
    }

    static void ordenar(String alg, int[] a, int threads) {
        switch (alg) {
            case "Bubble"    -> { if (threads == 0) BubbleSort.sort(a);    else ParallelBubbleSort.sort(a, threads); }
            case "Selection" -> { if (threads == 0) SelectionSort.sort(a); else ParallelSelectionSort.sort(a, threads); }
            case "Quick"     -> { if (threads == 0) QuickSort.sort(a);     else ParallelQuickSort.sort(a, threads); }
            case "Merge"     -> { if (threads == 0) MergeSort.sort(a);     else ParallelMergeSort.sort(a, threads); }
            default -> throw new IllegalArgumentException(alg);
        }
    }

    /** Roda antes de medir, senao as primeiras amostras saem infladas pela compilacao JIT. */
    static void aquecer(String alg) {
        int[] v = gerar(5_000, "aleatorio");
        for (int i = 0; i < 3; i++) {
            ordenar(alg, v.clone(), 0);
            ordenar(alg, v.clone(), 2);
        }
    }

    /** Vetor de n elementos com a natureza pedida; semente fixa pra ser reproduzivel. */
    static int[] gerar(int n, String entrada) {
        Random rng = new Random(42 + n);
        int[] a = rng.ints(n, 0, 1_000_000).toArray();
        switch (entrada) {
            case "aleatorio" -> { }
            case "ordenado" -> Arrays.sort(a);
            case "invertido" -> {
                Arrays.sort(a);
                for (int i = 0, j = n - 1; i < j; i++, j--) {
                    int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                }
            }
            case "quase_ordenado" -> {
                Arrays.sort(a);
                for (int k = 0; k < n / 100; k++) {   // desordena 1% das posicoes
                    int i = rng.nextInt(n), j = rng.nextInt(n);
                    int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                }
            }
            case "repeticoes" -> {
                for (int i = 0; i < n; i++) a[i] = rng.nextInt(10);
            }
            default -> throw new IllegalArgumentException(entrada);
        }
        return a;
    }
}
