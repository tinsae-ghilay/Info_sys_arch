#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
// for timer
#include <time.h>
#include <stdint.h> 

// Die Dimensionen sind zwar fix am Übungszettel vorgegeben,
// aber prinzipiell sollte man sie trotzdem im Programm nicht hart-coden.
// Wenn man sie als Konstanten definiert, kann man sie später leichter ändern.

// since we working with square matricis. we only need one dimension
// size of matrix is the dimension squared
# define DIMENSION          36
// task per thread is as its name suggests
// if we set this to 1, each thread will calculate single cell in the result matrix
// if we set it to 0, I plan to make it a blocking task(no threads created)
# define TASKS_PER_THREAD   162
// for testing purposes only
#define TEST_COUNT 100
// below is original code
// Makro, welches einen 2-dimensionalen Index in einem 1-dimensionalen Index umwandelt
// x .. x-Index (Anzahl der Zeilen)
// y .. y-Index (Anzahl der Spalten)
// s .. Größe der y-Dimension (Max. Anzahl der Spalten)
#define MATINDEX(x,y)		((x)*(DIMENSION)+(y))

// Die Matrizen A, B und C
// Diese als globale Variablen zu definieren, macht es für die Threads einfacher, darauf zuzugreifen
int *A, *B, *C;

// Struktur für die Datenübergabe an die Threads
// x .. Zeile der A Matrix
// y .. Spalte der B Matrix
typedef struct {
	int x;
	int y;
} thread_data_t;

// gets a time stamp in microseconds
uint64_t get_timestamp() {
    struct timespec ts;
    clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &ts);
    return (uint64_t)(ts.tv_sec * 1000000 + ts.tv_nsec / 1000);
}
    /*
double get_timestamp() {
	struct timespec time;
	clock_gettime(CLOCK_REALTIME, &time);
	return (double)time.tv_sec + (double)time.tv_nsec/1000000000.0;
}*/

// Funktion, welche von den Threads ausgeführt wird
void* thread_func(void* arg) {
	for(int i = 0; i < 100; i++){
        // Caste übergebene Daten zum richtigen Datentyp
        thread_data_t* data = (thread_data_t*)arg;
        // Bereche Zeile * Spalte
        int result = 0;
        for (int i = 0; i < DIMENSION; i++) {
            result += A[MATINDEX(data->x,i)] * B[MATINDEX(i,data->y)];
        }
        // Speichere Ergebnis in die richtige Zelle der C Matrix
        C[MATINDEX(data->x,data->y)] = result;
    }
	return NULL;
}
// calculates number of threads to create 
// depending on size of matrix and and tasks per thread
int getThreadCount(int size, int tasks){
    if(tasks == 0){
        return 0;
    }else if(tasks > size){
        return 1;
    }else{
        int div = size / tasks;
        // if task doesnt divide size perfectly, 
        // we have to add one extra thread that does the remining cells
        return (size % tasks) == 0? div : div +1;
    }
}
// calculates which thread does a task
// this depends on TASKS_PER_THREAD, and cell coordinates
int getThreadIndex(int x, int y, int tasks, int size){
    if(tasks != 0){
        return (x * size + y) / tasks;
    }else{
        return -1;
    }
}

// Gebe die übergebene Matrix aus
// mat .. Matrix,
// x .. Anzahl der Zeilen
// y .. Anzahl der Spalten
void printMatrix(int* mat) {
	int min = INT_MAX, max = INT_MIN, sum = 0;
	// Schleife über alle Zeilen
	for (int i = 0; i < DIMENSION; i++) {
		int firstElement = 1;
		// Schleife über alle Spalten
		for (int j = 0; j < DIMENSION; j++) {
			// Für das erste Element müssen wir davor kein '\t' ausgeben
			if (firstElement) {
				firstElement = 0;
			} else {
				printf("\t");
			}
			int e = mat[MATINDEX(i,j)];
			// Gebe Element aus
			printf("%d", e);
			// Berechne min
			if (e < min) {
				min = e;
			}
			// Berechne max
			if (e > max) {
				max = e;
			}
			// Berechne Summe
			sum += e;
		}
		printf("\n");
	}
	// Gebe min, max und summe aus
	printf("Minimaler Wert: %d; Maximaler Wert: %d; Summe aller Werte: %d\n", min, max, sum);
}

// frees memory allocated for matrices
void clean_up_matrices(){
    free(A);
    free(B);
    free(C);
}


int main() {
    // DIMENSION shouldnt be 0.
    if(DIMENSION <= 0){
        perror("A matrix can only have dimensions greater than 0");
        return 1;
    }
    // size of matrices, 
    // since we are working with quadratic matrices, One size fits all
    int size = DIMENSION * DIMENSION;

    // number of threads, depending on tasks per thread
    int thread_count = getThreadCount(size, TASKS_PER_THREAD);
    if(!thread_count){
        perror("There are no threads to do a task. Exiting");
        return 1;
    }
	// Alloziere Speicher für die Matrizen
	A = malloc(sizeof(int)* size);
	B = malloc(sizeof(int)* size);
	C = malloc(sizeof(int)* size);

    // make sure there are no surprises here too
    if(!A || !B || !C){
        clean_up_matrices();
        perror("Error allocating memory to one of the matrices");
        return 1;

    }

	// Fülle die Matrizen A und B mit Zufallswerten (zwischen 0 und 10)
	for (int i = 0; i < size; i++) {
		A[i] = rand() % 10;
	} 
	for (int i = 0; i < size; i++) {
		B[i] = rand() % 10;
	}

	// Definiere Thread-Handles und Thread-Eingabedaten
	thread_data_t threadData[size];
	pthread_t threads[thread_count];
    // testing cases
    uint64_t total_time = 0;
    for(int t = 0; t < TEST_COUNT; t++){
        uint64_t start = get_timestamp();
        // Erzeuge einen Thread für jedes Zeilen-Spalten-Paar
        for (int x = 0; x < DIMENSION; x++) {
            for (int y = 0; y < DIMENSION; y++) {
                // Fülle Thread-Datenstruktur
                thread_data_t* data = &threadData[MATINDEX(x,y)];
                data->x = x;
                data->y = y;
                // Erzeuge Thread
                // and make sure there are no surprises
                if(pthread_create(&threads[getThreadIndex(x,y, TASKS_PER_THREAD, DIMENSION)/*THREAD_INDEX(x,y)*/], NULL, thread_func, data)!= 0){
                    fprintf(stderr,"Error creating thread number for coordinated (%d, %d)", x,y);
                    clean_up_matrices();
                    return 1;
                }
            }
        }
        // Warte bis alle Threads fertig sind
        for (int i = 0; i < thread_count; i++) {
            // and make sure there are no surprises
            if(pthread_join(threads[i], NULL) != 0){
                fprintf(stderr,"Error joining thread number %d", i);
                clean_up_matrices();
                return 1;
            }
        }

        uint64_t end = get_timestamp();
        total_time+= (end - start);
    }
    uint64_t average = (total_time / TEST_COUNT);

    printf("average time for %d cores = %ld\n",thread_count,average);
    

	// Gebe Ergebnis aus
	//printMatrix(C);

	return 0;
}
