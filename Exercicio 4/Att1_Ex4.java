import java.util.LinkedList;

//-------------------------------------------------------------------------------
class FilaTarefas {
    private final int nThreads;
    private final MyPoolThreads[] threads;
    private final LinkedList<Runnable> queue;
    private boolean shutdown;

    public FilaTarefas(int nThreads) {
        this.shutdown = false;
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new MyPoolThreads[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new MyPoolThreads();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            if (this.shutdown) return;
            queue.addLast(r);
            queue.notify();
        }
    }

    public void shutdown() {
        synchronized(queue) {
            this.shutdown = true;
            queue.notifyAll();
        }
        for (int i = 0; i < nThreads; i++) {
            try { threads[i].join(); }
            catch (InterruptedException e) { return; }
        }
    }

    private class MyPoolThreads extends Thread {
        public void run() {
            Runnable r;
            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty() && (!shutdown)) {
                        try { queue.wait(); }
                        catch (InterruptedException ignored) {}
                    }
                    if (queue.isEmpty()) return;
                    r = queue.removeFirst();
                }
                try { r.run(); }
                catch (RuntimeException e) {}
            }
        }
    }
}
//-------------------------------------------------------------------------------

class Primo implements Runnable {
   private final int n;
   public boolean resultado;

   public Primo(int n) {
      this.n = n;
      this.resultado = false;
   }

   private boolean ehPrimo(int n) {
      if (n <= 1) return false;
      if (n == 2) return true;
      if (n % 2 == 0) return false;

      int limite = (int)Math.sqrt(n) + 1;
      for (int i = 3; i < limite; i += 2) {
         if (n % i == 0) return false;
      }
      return true;
   }

   public void run() {
      resultado = ehPrimo(n);
   }
}

class MyPool1 {
    public static int contarPrimosSequencial(int N) {
        int cont = 0;
        for (int i = 1; i <= N; i++) {
            if (ehPrimoSeq(i)) cont++;
        }
        return cont;
    }

    private static boolean ehPrimoSeq(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;

        int limite = (int)Math.sqrt(n) + 1;
        for (int i = 3; i < limite; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static void main (String[] args) {
      if (args.length < 2) {
          System.out.println("Uso: java MyPool1 <N> <NTHREADS>");
          return;
      }

      int N = Integer.parseInt(args[0]);
      int NTHREADS = Integer.parseInt(args[1]);

      FilaTarefas pool = new FilaTarefas(NTHREADS);
      Primo[] tarefas = new Primo[N + 1];

      for (int i = 1; i <= N; i++) {
        Primo p = new Primo(i);
        tarefas[i] = p;
        pool.execute(p);
      }

      pool.shutdown();

      int totalPrimosParalelo = 0;
      for (int i = 1; i <= N; i++) {
        if (tarefas[i].resultado) totalPrimosParalelo++;
      }

      System.out.println("Resultado paralelizado: " + totalPrimosParalelo);

      int totalPrimosSequencial = contarPrimosSequencial(N);
      System.out.println("Resultado sequencial: " + totalPrimosSequencial);

      if (totalPrimosParalelo == totalPrimosSequencial) {
          System.out.println("CORRETO ✔");
      } else {
          System.out.println("ERRO ❌ — resultados diferentes!");
      }

      System.out.println("Terminou");
    }
}