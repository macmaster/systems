CC = mpicxx
CXX = mpicxx
CFLAGS = -g # -Wall -pedantic
SRC = mpi_matrix.cpp
BACKUP = $(SRC:.cpp=.cpp~)
OBJ = $(SRC:.cpp=.o)
MAIN = main

all: $(MAIN)
	@echo compiled the mpi program...

$(MAIN): $(OBJ)
	$(CC) $(CFLAGS) -o $(MAIN) $(OBJ)

# TESTS AND USER PROGRAMS

test: $(MAIN) 
	./$(MAIN)

format: 
	indent $(SRC) -nbad -bap -nbc -bbo -hnl -br -brs -brf -c33 -cd33 -ncdb -ce -ci4 \
	-cli1 -d0 -di1 -nfc1 -i8 -ip0 -l120 -lp -npcs -nprs -npsl -sai \
	-saf -saw -ncs -nsc -sob -nfca -cp33 -ss -ts8 -il1
	$(RM) $(BACKUP)

clean:
	$(RM) $(MAIN) $(OBJ)




