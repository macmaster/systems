/** mpi_matrix.cpp
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 4/12/17
 * 
 * concurrent matrix multiplier using mpi.
 */

#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

#include <vector>
#include <string>
#include <sstream>
#include <fstream>
#include <algorithm>

#define BUFFER_LENGTH 4096
using namespace std;

// function headers
void printVector(vector<int> &ivector);
void printMatrix(vector< vector<int> > &ivector);
void ReadInput(vector<int> &ivector, vector< vector<int> > &imatrix);

int main(int argc, char **argv){
	// initialize MPI.
	MPI_Init(NULL, NULL);
	int world_size, world_rank;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

	// processor name
	int name_length;
	char processor_name[MPI_MAX_PROCESSOR_NAME];
	MPI_Get_processor_name(processor_name, &name_length);

	// print greeting
	printf("hello from processor: %s\n", processor_name);
	printf("rank is %d out of %d processors.\n", world_rank, world_size);

	// read in the inputs
	vector<int> ivector;
	vector< vector<int> > imatrix;
	ReadInput(ivector, imatrix);

	// print inputs (DEBUG)
	printf("vector size: %d\n", ivector.size());
	printVector(ivector);
	printf("\n");
	printf("matrix size: %d x %d\n", imatrix.size(), imatrix.at(0).size());
	printMatrix(imatrix);
	printf("\n");

	MPI_Finalize();
}

void printVector(vector<int> &ivector){
	printf("{ ");
	for(int i = 0; i < ivector.size(); i++){
		if(i != 0) printf(", ");
		printf("%d", ivector[i]);
	} printf(" }\n");
}

void printMatrix(vector< vector<int> > &imatrix){
	for(int i = 0; i < imatrix.size(); i++){
		printVector(imatrix[i]);
	}
}

/**
 * Reads in the input vector and matrix from files.
 * Allocates memory and writes to the two parameter pointers.
 * postcondition: ivector and imatrix point to the two input structures.
 */
void ReadInput(vector<int> &ivector, vector< vector<int> > &imatrix){
	int rows = 0, cols = 0;
	string line; // buffer for lines
	istringstream lstream;
	ifstream vectorFile("vector.txt");
	ifstream matrixFile("matrix.txt");

	// parse vector line
	int value = 0;
	getline(vectorFile, line);
	lstream.str(line);
	while(lstream >> value)
		ivector.push_back(value);
	cols = ivector.size();
	lstream.clear();
	vectorFile.close();

	// read input matrix file.
	getline(matrixFile, line);
	lstream.str(line);
	lstream >> rows;
	printf("rows in input matrix: %d\n", rows);

	// parse file line by line
	for (int i = 0; i < rows; i++){
		getline(matrixFile, line);
		lstream.clear();
		lstream.str(line);
	
		// parse row.
		int value = 0;
		vector<int> row;
		while(lstream >> value)
			row.push_back(value);

		imatrix.push_back(row);
	}
	lstream.clear();
	matrixFile.close();
}

