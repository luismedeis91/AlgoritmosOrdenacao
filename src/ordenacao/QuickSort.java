package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Quick Sort: particao de 3 vias com pivo aleatorio. */
public class QuickSort {

    public static void sort(int[] a) {
        quickSort(a, 0, a.length - 1);
    }

    static void quickSort(int[] a, int lo, int hi) {
        while (lo < hi) {
            long faixa = partition(a, lo, hi);
            int lt = inicioDosIguais(faixa);
            int gt = fimDosIguais(faixa);
            if (lt - lo < hi - gt) {   // recursao no lado menor, laco no maior
                quickSort(a, lo, lt - 1);
                lo = gt + 1;
            } else {
                quickSort(a, gt + 1, hi);
                hi = lt - 1;
            }
        }
    }

    /**
     * Deixa a[lo..lt-1] < pivo, a[lt..gt] == pivo, a[gt+1..hi] > pivo e devolve
     * lt e gt empacotados num long. Tirar os iguais da recursao e o que evita o
     * O(n^2) em entradas com poucos valores distintos.
     */
    static long partition(int[] a, int lo, int hi) {
        // ThreadLocalRandom: a versao paralela nao disputa o mesmo gerador
        int r = lo + ThreadLocalRandom.current().nextInt(hi - lo + 1);
        swap(a, lo, r);
        int pivo = a[lo];

        int lt = lo, gt = hi, i = lo + 1;
        while (i <= gt) {
            if (a[i] < pivo)      swap(a, lt++, i++);
            else if (a[i] > pivo) swap(a, i, gt--);
            else                  i++;
        }
        return ((long) lt << 32) | (gt & 0xffffffffL);
    }

    static int inicioDosIguais(long faixa) {
        return (int) (faixa >>> 32);
    }

    static int fimDosIguais(long faixa) {
        return (int) faixa;
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
        int[] exemplo = {10, 80, 30, 90, 40, 50, 70};
        System.out.println("Antes:  " + Arrays.toString(exemplo));
        sort(exemplo);
        System.out.println("Depois: " + Arrays.toString(exemplo));

        int[][] bordas = { {}, {7}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}, {-3, 0, -1, 8, -10} };
        for (int[] caso : bordas) {
            int[] copia = caso.clone();
            sort(copia);
            System.out.printf("%-20s -> %-20s %s%n",
                    Arrays.toString(caso), Arrays.toString(copia), estaOrdenado(copia) ? "OK" : "FALHOU");
        }

        Random rng = new Random(42);
        int testes = 1000;
        for (int t = 0; t < testes; t++) {
            int[] v = rng.ints(rng.nextInt(50), -100, 100).toArray();
            int[] esperado = v.clone();
            Arrays.sort(esperado);
            sort(v);
            if (!Arrays.equals(v, esperado)) {
                System.out.println("ERRO no teste " + t);
                return;
            }
        }
        System.out.println(testes + " testes aleatorios passaram.");

        int n = 1_000_000;
        int[] grande = rng.ints(n, 0, 1_000_000).toArray();
        long inicio = System.nanoTime();
        sort(grande);
        long fim = System.nanoTime();
        System.out.printf("n = %d ordenado em %.2f ms%n", n, (fim - inicio) / 1e6);

        inicio = System.nanoTime();
        sort(grande);
        fim = System.nanoTime();
        System.out.printf("n = %d (ja ordenado) em %.2f ms%n", n, (fim - inicio) / 1e6);
    }
}
