#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <math.h>

int *buffer, N, M, pos = 0, total_primos = 0, producao_concluida = 0;
int *primos_por_thread;
sem_t mutex, cheio, vazio;

int ehPrimo(long long int n) {
    if (n <= 1) return 0;
    if (n == 2) return 1;
    if (n % 2 == 0) return 0;
    for (int i = 3; i <= sqrt(n); i += 2)
        if (n % i == 0) return 0;
    return 1;
}

void* produtor(void *arg) {
    int atual = 1;
    while (atual <= N) {
        sem_wait(&vazio);
        sem_wait(&mutex);

        int count = 0;
        for (int i = 0; i < M && atual <= N; i++) {
            buffer[i] = atual++;
            count++;
        }
        pos = count;
        printf("\nProdutor: inseriu %d números no buffer.\n", count);

        sem_post(&mutex);

        // libera um "cheio" por item inserido
        for (int i = 0; i < count; i++) sem_post(&cheio);
    }

    // sinaliza fim da produção
    sem_wait(&mutex);
    producao_concluida = 1;
    sem_post(&mutex);

    // desbloqueia consumidores restantes
    for (int i = 0; i < M * 2; i++) sem_post(&cheio);
    pthread_exit(NULL);
}

void* consumidor(void *arg) {
    int id = *(int*)arg;
    free(arg);

    while (1) {
        sem_wait(&cheio);
        sem_wait(&mutex);

        if (pos == 0 && producao_concluida) {
            sem_post(&mutex);
            break;
        }

        if (pos > 0) {
            int num = buffer[--pos];
            if (pos == 0 && !producao_concluida)
                sem_post(&vazio);
            sem_post(&mutex);

            if (ehPrimo(num)) {
                sem_wait(&mutex);
                primos_por_thread[id]++;
                total_primos++;
                sem_post(&mutex);
                printf("Consumidor %d processando %d -> primo\n", id, num);
            } else {
                printf("Consumidor %d processando %d -> não primo\n", id, num);
            }
        } else {
            sem_post(&mutex);
        }
    }
    pthread_exit(NULL);
}

int main() {
    int num_consumidores;
    printf("Digite N e M: ");
    scanf("%d %d", &N, &M);
    printf("Digite o número de consumidores: ");
    scanf("%d", &num_consumidores);

    buffer = malloc(M * sizeof(int));
    primos_por_thread = calloc(num_consumidores + 1, sizeof(int));
    pthread_t prod, cons[num_consumidores];

    sem_init(&mutex, 0, 1);
    sem_init(&cheio, 0, 0);
    sem_init(&vazio, 0, 1);

    pthread_create(&prod, NULL, produtor, NULL);
    for (int i = 0; i < num_consumidores; i++) {
        int *id = malloc(sizeof(int));
        *id = i + 1;
        pthread_create(&cons[i], NULL, consumidor, id);
    }

    pthread_join(prod, NULL);
    for (int i = 0; i < num_consumidores; i++) pthread_join(cons[i], NULL);

    int vencedora = 1;
    for (int i = 2; i <= num_consumidores; i++)
        if (primos_por_thread[i] > primos_por_thread[vencedora])
            vencedora = i;

    printf("\nTotal de primos: %d\n", total_primos);
    printf("Vencedora: %d (%d primos)\n", vencedora, primos_por_thread[vencedora]);

    free(buffer);
    free(primos_por_thread);
    sem_destroy(&mutex);
    sem_destroy(&cheio);
    sem_destroy(&vazio);
    return 0;
}
