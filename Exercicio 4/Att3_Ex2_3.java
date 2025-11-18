/* Disciplina: Programacao Concorrente */
/* Prof.: Silvana Rossetto */
/* Laboratório: 11 */
/* Codigo: Exemplo de uso de futures */
/* -------------------------------------------------------------------*/

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

//classe para verificar números primos (Exercício 2)
class VerificadorPrimo implements Callable<Long> {
    private final long numero;
    
    //construtor
    public VerificadorPrimo(long n) {
        this.numero = n;
    }
    
    //funcao para determinar se um numero eh primo
    private boolean ehPrimo(long n) {
        if(n <= 1) return false;
        if(n == 2) return true;
        if(n % 2 == 0) return false;
        for(long i = 3; i <= Math.sqrt(n) + 1; i += 2) {
            if(n % i == 0) return false;
        }
        return true;
    }

    //método para execução - retorna 1 se for primo, 0 se não for
    public Long call() throws Exception {
        return ehPrimo(numero) ? 1L : 0L;
    }
}

//classe para contar primos em um intervalo (Exercício 3)
class ContadorPrimosIntervalo implements Callable<Long> {
    private final long inicio;
    private final long fim;
    
    //construtor
    public ContadorPrimosIntervalo(long inicio, long fim) {
        this.inicio = inicio;
        this.fim = fim;
    }
    
    //funcao para determinar se um numero eh primo
    private boolean ehPrimo(long n) {
        if(n <= 1) return false;
        if(n == 2) return true;
        if(n % 2 == 0) return false;
        for(long i = 3; i <= Math.sqrt(n) + 1; i += 2) {
            if(n % i == 0) return false;
        }
        return true;
    }

    //método para execução - conta primos no intervalo
    public Long call() throws Exception {
        long count = 0;
        for(long i = inicio; i <= fim; i++) {
            if(ehPrimo(i)) {
                count++;
            }
        }
        return count;
    }
}

//classe runnable original (mantida para referência)
class MyCallable implements Callable<Long> {
  //construtor
  MyCallable() {}
 
  //método para execução
  public Long call() throws Exception {
    long s = 0;
    for (long i=1; i<=100; i++) {
      s++;
    }
    return s;
  }
}

//classe do método main
public class FuturePool  {
  private static final int NTHREADS = 10;
  
  // Exercício 2: teste simples com números individuais
  public static void testePrimosIndividuais() {
      System.out.println("=== EXERCÍCIO 2: Verificação de Primos Individuais ===");
      
      ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
      List<Future<Long>> list = new ArrayList<Future<Long>>();

      // Testa alguns números individuais
      long[] numerosTeste = {2, 3, 4, 5, 17, 18, 19, 20, 23, 29, 30, 31, 37, 41, 997};
      
      for (long numero : numerosTeste) {
          Callable<Long> worker = new VerificadorPrimo(numero);
          Future<Long> submit = executor.submit(worker);
          list.add(submit);
      }

      System.out.println("Números testados: " + list.size());
      
      // Recupera e exibe os resultados
      for (int i = 0; i < numerosTeste.length; i++) {
          try {
              long resultado = list.get(i).get();
              System.out.println("Número " + numerosTeste[i] + (resultado == 1 ? " é primo" : " não é primo"));
          } catch (InterruptedException e) {
              e.printStackTrace();
          } catch (ExecutionException e) {
              e.printStackTrace();
          }
      }
      executor.shutdown();
  }
  
  // Exercício 3: contar primos em intervalo grande
  public static void contarPrimosIntervalo(long N, int numParticoes) {
      System.out.println("\n=== EXERCÍCIO 3: Contagem de Primos de 1 a " + N + " ===");
      
      ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
      List<Future<Long>> list = new ArrayList<Future<Long>>();

      // Divide o intervalo em partes para processamento paralelo
      long tamanhoParticao = N / numParticoes;
      
      for (int i = 0; i < numParticoes; i++) {
          long inicio = i * tamanhoParticao + 1;
          long fim = (i == numParticoes - 1) ? N : (i + 1) * tamanhoParticao;
          
          Callable<Long> worker = new ContadorPrimosIntervalo(inicio, fim);
          Future<Long> submit = executor.submit(worker);
          list.add(submit);
          
          System.out.println("Partição " + i + ": [" + inicio + " - " + fim + "]");
      }

      System.out.println("Número de partições: " + list.size());
      
      // Soma os resultados de todas as partições
      long totalPrimos = 0;
      for (Future<Long> future : list) {
          try {
              totalPrimos += future.get();
          } catch (InterruptedException e) {
              e.printStackTrace();
          } catch (ExecutionException e) {
              e.printStackTrace();
          }
      }
      
      System.out.println("Total de números primos de 1 a " + N + ": " + totalPrimos);
      executor.shutdown();
  }

  public static void main(String[] args) {
      // Executa o exercício 2
      testePrimosIndividuais();
      
      // Executa o exercício 3 com diferentes valores de N
      contarPrimosIntervalo(1000, 10);        // N pequeno
      contarPrimosIntervalo(100000, 20);      // N médio  
      contarPrimosIntervalo(1000000, 50);     // N grande
  }
}