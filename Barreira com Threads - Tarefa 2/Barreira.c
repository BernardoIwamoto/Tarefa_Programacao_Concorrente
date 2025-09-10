#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

pthread_mutex_t mutex;
pthread_cond_t cond;

long int soma = 0;
int impresso = 0;

void *ExecutaTarefa(void *arg) {
    int id = *(int*) arg;
    printf("Thread %d esta executando...\n", id);

    for (int i = 0; i < 100000; i++) {
        pthread_mutex_lock(&mutex);

        while (impresso) {
            pthread_cond_wait(&cond, &mutex);
        }

        soma++;
        if (soma % 1000 == 0) {
            impresso = 1;
            pthread_cond_signal(&cond);

            while (impresso) {
                pthread_cond_wait(&cond, &mutex);
            }
        }

        pthread_mutex_unlock(&mutex);
    }

    printf("Thread %d terminou!\n", id);
    pthread_exit(NULL);
}

void *extra(void *args) {
    int nthreads = *(int *) args;

    while (1) {
        pthread_mutex_lock(&mutex);

        while ((soma < (nthreads * 100000)) && !impresso) {
            pthread_cond_wait(&cond, &mutex);
        }

        if (impresso) {
            printf("soma = %ld\n", soma);
            impresso = 0;
            pthread_cond_broadcast(&cond);
        }

        if (soma >= (nthreads * 100000)) {
            pthread_mutex_unlock(&mutex);
            break;
        }

        pthread_mutex_unlock(&mutex);
    }
    pthread_exit(NULL);
}

int main(int argc, char *argv[]) {
    pthread_t *tid_sistema;
    int nthreads;

    if (argc < 2) {
        printf("Digite: %s <numero de threads>\n", argv[0]);
        return 1;
    }
    nthreads = atoi(argv[1]);

    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&cond, NULL);

    tid_sistema = (pthread_t *) malloc(sizeof(pthread_t) * (nthreads + 1));
    if (tid_sistema == NULL) return 2;

    int *ids = malloc(nthreads * sizeof(int));
    for (int i = 0; i < nthreads; i++) {
        ids[i] = i;
        pthread_create(&tid_sistema[i], NULL, ExecutaTarefa, (void *) &ids[i]);
    }

    pthread_create(&tid_sistema[nthreads], NULL, extra, (void *)&nthreads);

    for (int i = 0; i < nthreads + 1; i++) {
        pthread_join(tid_sistema[i], NULL);
    }

    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
    free(tid_sistema);
    free(ids);

    printf("Valor final de soma = %ld\n", soma);
    return 0;
}
