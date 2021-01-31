#include <stdio.h>
#include <time.h>
#include <mpi.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#define PI 3.1415
#define simulationTimeProportion 0.00001

struct region
    {
        int xCoordinateStart;
        int yCoordinateStart;
        int xCoordinateEnd;
        int yCoordinateEnd;
        struct region *next;

    };
typedef struct message
{
    float xCoordinate;
    float yCoordinate;
} message;


struct region* createRegions(int areaWidth, int areaLength, int regionWidth, int regionLength){

    struct region *firstRegion = NULL;
    float xRegion = areaLength / regionLength;
    float yRegion = areaWidth / regionWidth;
    for (int i = 0; i < yRegion; i++)
    {
        for (int j = 0; j < xRegion; j++)
        {
            struct region *newRegion;
            newRegion = malloc(sizeof(struct region));
            newRegion->xCoordinateStart = j * regionLength;
            newRegion->xCoordinateEnd = (j+1) * regionLength;
            newRegion->yCoordinateStart = i * regionWidth;
            newRegion->yCoordinateEnd = (i+1) * regionWidth;
            newRegion->next = firstRegion;
            firstRegion = newRegion;
        } 
    }
    
}
void mainTimer(float simulationTime){
    float trigger = simulationTime * simulationTimeProportion;
    float msec;
    clock_t before = clock();
    do{
        clock_t difference = clock() - before;
            msec = difference * 1000 / CLOCKS_PER_SEC;
    }while(msec < trigger);
}

int main(int argc, char** argv){
    MPI_Init(&argc, &argv);

    float individuals = atof(argv[1]);
    float infected = atof(argv[2]);
    float areaWidth = atof(argv[3]);
    float areaLength = atof(argv[4]);
    float regionWidth = atof(argv[5]);
    float regionLength = atof(argv[6]);
    float velocity = atof(argv[7]);
    float spreadingDistance = atof(argv[8]);
    float simulationSeconds = atof(argv[9]);

    printf("individuals: %f\ninfected: %f\n", individuals, infected);

    int rank, size;
    float initialxCoordinate = (areaWidth / 2);  
    float initialyCoordinate = (areaLength / 2);
    float xCoordinate = initialxCoordinate;
    float yCoordinate = initialyCoordinate;
    float directionAngle;
    char *state;
    struct region * firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);


    //Create the message type
    message msg;
    MPI_Datatype mpi_message;
    int struct_len = 2;
    int block_lens[struct_len];
    MPI_Datatype types[struct_len];
    MPI_Aint displacements[struct_len];
    MPI_Aint current_displacement = 0;
    block_lens[0] = 1;
    types[0] = MPI_FLOAT;
    displacements[0] = (size_t) &(msg.xCoordinate) - (size_t) &msg;
    block_lens[1] = 1;
    types[1] = MPI_FLOAT;
    displacements[1] = (size_t) &(msg.yCoordinate) - (size_t) &msg;
    MPI_Type_create_struct(struct_len, block_lens, displacements, types, &mpi_message);
    MPI_Type_commit(&mpi_message);

    directionAngle = 2 * PI * rank / size;

    if(rank >= 0.0 && rank < infected){
        state = "infected";
    }else
    {
        state = "susceptible";
    } 
    printf("Process %d has started working!\nActual position: %f, %f\nDirection: %f\nState: %s", rank, xCoordinate, yCoordinate, directionAngle, state);

    double xVelocity = velocity * cos(directionAngle);
    double yVelocity = velocity * sin(directionAngle);
    while (1)
    {   
        mainTimer(simulationSeconds);
        xCoordinate = initialxCoordinate + xVelocity * simulationSeconds;
        yCoordinate = initialyCoordinate + yVelocity * simulationSeconds;
        message *sendMessage =  malloc(sizeof(message));
        message *receiveMessage =  malloc(sizeof(message));
        sendMessage->xCoordinate = xCoordinate;
        sendMessage->yCoordinate = yCoordinate;
        MPI_Request request;
        MPI_Status status;
        for (int i = 0; i < size; i++)
        {
            if(i != rank){
                MPI_Isend(&sendMessage, 1, mpi_message, i, 0, MPI_COMM_WORLD, &request);
                MPI_Recv(&receiveMessage, 1, mpi_message, i, 0, MPI_COMM_WORLD, &status);
            }
        }
        
    }
    
    MPI_Type_free(&mpi_message);
    MPI_Finalize();
}