package ordenacao;

import java.util.Arrays;
import java.util.Random;

/**
 * Merge Sort
 *
 * Ideia (dividir e conquistar):
 *   1. Divide o vetor ao meio.
 *   2. Ordena cada metade recursivamente.
 *   3. Intercala (merge) as duas metades ordenadas.
 *
 * Recorrencia: T(n) = 2T(n/2) + O(n)  =>  T(n) = O(n log n)  (Teorema Mestre)
 *
 * Detalhes desta implementacao:
 *   - O vetor auxiliar e alocado UMA vez so e reaproveitado em todo merge.
 *   - Se a[mid] <= a[mid+1], as metades ja estao em ordem e o merge e pulado.
 *
 * Complexidade:
 *   - Todos os casos: O(n log n)
 *   - Espaco: O(n) do vetor auxiliar
 *   - Estavel: sim (usa <= no merge)
 */
public class MergeSort {

    public static void sort(int[] a) {
        int[] aux = new int[a.length];
        mergeSort(a, aux, 0, a.length - 1);
    }

    static void mergeSort(int[] a, int[] aux, int lo, int hi) {
        if (lo >= hi) return;                 // 0 ou 1 elemento: ja ordenado
        int mid = lo + (hi - lo) / 2;         // evita overflow de (lo + hi) / 2
        mergeSort(a, aux, lo, mid);
        mergeSort(a, aux, mid + 1, hi);
        if (a[mid] <= a[mid + 1]) return;     // ja esta em ordem
        merge(a, aux, lo, mid, hi);
    }

    /** Intercala a[lo..mid] e a[mid+1..hi], que ja estao ordenados. */
    static void merge(int[] a, int[] aux, int lo, int mid, int hi) {
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }

        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid)               a[k] = aux[j++];  // esquerda acabou
            else if (j > hi)           a[k] = aux[i++];  // direita acabou
            else if (aux[i] <= aux[j]) a[k] = aux[i++];  // <= mantem estabilidade
            else                       a[k] = aux[j++];
        }
    }

    private static boolean estaOrdenado(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // 1) exemplo simples
        int[] exemplo = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("Antes:  " + Arrays.toString(exemplo));
        sort(exemplo);
        System.out.println("Depois: " + Arrays.toString(exemplo));

        // 2) casos de borda
        int[][] bordas = { {}, {7}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}, {-3, 0, -1, 8, -10} };
        for (int[] caso : bordas) {
            int[] copia = caso.clone();
            sort(copia);
            System.out.printf("%-20s -> %-20s %s%n",
                    Arrays.toString(caso), Arrays.toString(copia), estaOrdenado(copia) ? "OK" : "FALHOU");
        }

        // 3) teste aleatorio: compara com Arrays.sort
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

        // 4) tempo: O(n log n) garantido
        int n = 1_000_000;
        int[] grande = rng.ints(n, 0, 1_000_000).toArray();
        long inicio = System.nanoTime();
        sort(grande);
        long fim = System.nanoTime();
        System.out.printf("n = %d ordenado em %.2f ms%n", n, (fim - inicio) / 1e6);
    }
}
