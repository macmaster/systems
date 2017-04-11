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

void master(int world_size);
void slave(int rank);
int multiply(vector<int> &v1, vector<int> &v2);

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

	// workload begins.
	if(world_rank == 0){
		master(world_size);
	} else{
		slave(world_rank);
	}
	
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

/**
 * processing work for the master process.
 * read the input files.
 * partition out the workload over the network.
 * perform the extra chunk.
 * collect the results.
 * write the output file.
 */
void master(int procs){
	// read in the inputs
	vector<int> ivector;
	vector< vector<int> > imatrix;
	ReadInput(ivector, imatrix);
	int n = imatrix.size(), m = ivector.size(); // rows, cols
	
	// matrix multiplication result
	vector<int> result(n);

	// // print inputs (DEBUG)
	// printf("vector size: %d\n", ivector.size());
	// printVector(ivector);
	// printf("\n");
	// printf("matrix size: %d x %d\n", imatrix.size(), imatrix.at(0).size());
	// printMatrix(imatrix);
	// printf("\n");
	
	
	if(procs - 1 == 0){
		// solo it...
		printf("solo it...\n");
		// process every row
		for(int r = 0; r < n; r++){
			vector<int> &row = imatrix[r];
			result[r] = multiply(row, ivector);
		}
	} 
	
	else{ // send out assignments.
		int stride = (n / (procs - 1)), extra = (n % (procs - 1));
		for(int i = 1; i < procs; i++){
			int dim[2] = {stride, m}; // send dimensions
			MPI_Send(dim, 2, MPI_INT, i, 0, MPI_COMM_WORLD);

			// send the vector
			MPI_Send(&ivector[0], m, MPI_INT, i, 0, MPI_COMM_WORLD);

			// send matrix row
			int start = ((i - 1) * stride) + extra;
			for(int j = 0; j < stride; j++){
				// printf("rank 0: sending msg from %d with size %d, stride %d, extra %d\n", start + j, n, stride, extra);
				MPI_Send(&imatrix[start + j][0], m, MPI_INT, i, j + 1, MPI_COMM_WORLD);
			}
		}

		// process extra rows
		for(int r = 0; r < extra; r++){
			vector<int> &row = imatrix[r];
			result[r] = multiply(row, ivector);
		}

		// collect and reduce result vector
		for(int i = 1; i < procs; i++){
			vector<int> values(stride);
			MPI_Recv(&values[0], stride, MPI_INT, i, stride + 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			int start = ((i - 1) * stride) + extra;
			for(int r = 0; r < stride; r++){
				result[start + r] = values[r];
			}
		}

	}

	// write to file
	printf("Number of processes: %d\n", procs);
	printf("result vector: "); printVector(result);
	printf("\n");

}



/**
 * receive the partition from the master.
 * perform the vector operations.
 * send it back to the master.
 */
void slave(int rank){	
	int rows = 0, cols = 0, dim[2]; // dim = {rows, cols}
	MPI_Recv(dim, 2, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
	rows = dim[0]; cols = dim[1]; // set dimensions.

	int buffer[cols]; // receive the vector input.
	MPI_Recv(buffer, cols, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
	vector<int> ivector(cols);
	for(int i = 0; i < cols; i++){
		ivector[i] = buffer[i];
	}

	// receive matrix section input.
	vector< vector<int> > imatrix(rows, vector<int>(cols));
	for(int j = 0; j < rows; j++){
		MPI_Recv(buffer, cols, MPI_INT, 0, j + 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		for(int k = 0; k < cols; k++){
			imatrix[j][k] = buffer[k];
		}
	}

	// DEBUG routine:
	//printf("rank: %d. \t dim: %d x %d\n", rank, rows, cols);
	//printf("ivector:\n"); printVector(ivector);
	//printf("imatrix:\n"); printMatrix(imatrix);
	
	// vector result of the submatrix multiplication.
	vector<int> result(rows);
	for(int r = 0; r < rows; r++){
		vector<int> &row = imatrix[r];
		result[r] = multiply(row, ivector);
	}

	// report result vector to master.
	MPI_Send(&result[0], rows, MPI_INT, 0, rows + 1, MPI_COMM_WORLD);
}


/**
 * multiplies two vectors together.
 * the result is a scalar.
 * returns the scalar dot product of two vectors
 * precondition: vectors are of equal length.
 */
int multiply(vector<int> &v1, vector<int> &v2){
	int result = 0;
	for(int i = 0; i < v1.size(); i++){
		result += v1[i] * v2[i];
	}
	return result;
}
