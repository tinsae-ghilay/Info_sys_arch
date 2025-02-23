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
# define DIMENSION          24
// below is original code
// Makro, welches einen 2-dimensionalen Index in einem 1-dimensionalen Index umwandelt
// x .. x-Index (Anzahl der Zeilen)
// y .. y-Index (Anzahl der Spalten)
// s .. Größe der y-Dimension (Max. Anzahl der Spalten)
#define MATINDEX(x,y)		((x)*(DIMENSION)+(y))

// for testing purposes only
#define TEST_COUNT 500

// Die Matrizen A, B und C
// Diese als globale Variablen zu definieren, macht es für die Threads einfacher, darauf zuzugreifen
int *A, *B, *C;

// gets a time stamp in microseconds
uint64_t get_timestamp() {
    struct timespec ts;
    clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &ts);
    return (uint64_t)(ts.tv_sec * 1000000 + ts.tv_nsec / 1000);
}

// Funktion, welche von den Threads ausgeführt wird
int calculate_cell(int x, int y) {
	// Caste übergebene Daten zum richtigen Datentyp
	// Bereche Zeile * Spalte
	int result = 0;
	for (int i = 0; i < DIMENSION; i++) {
		result += A[MATINDEX(x,i)] * B[MATINDEX(i,y)];
	}
	// Speichere Ergebnis in die richtige Zelle der C Matrix
	//C[MATINDEX(data->x,data->y)] = result;
	return result;
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
    // testing cases
    uint64_t total_time = 0;
    for(int t = 0; t < TEST_COUNT; t++){
        uint64_t start = get_timestamp();
        // calculate the product matrix in a blocking
        for (int x = 0; x < DIMENSION; x++) {
            for (int y = 0; y < DIMENSION; y++) {
                C[MATINDEX(x,y)] = calculate_cell(x,y);

            }
        }
        uint64_t end = get_timestamp();
        total_time+= (end - start);
        printf("total time = %ld\n", total_time);
    }
    uint64_t average = total_time / TEST_COUNT;
    printf("average time for 4 cores = %ld\n",average);
	// Gebe Ergebnis aus
	//printMatrix(C);

	return 0;
}

