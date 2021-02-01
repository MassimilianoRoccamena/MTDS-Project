#include <stdio.h>
#include <time.h>
#include <mpi.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <stdbool.h>

#define PI 3.1415
#define simulationTimeProportion 0.00001
#define secondsInTenMinutes 600

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
    char processState;
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
    return firstRegion;
    
}
void mainTimer(float simulationTime){

    float trigger = simulationTime * simulationTimeProportion * 1000;
    float msec;
    clock_t before = clock();
    do{
        clock_t difference = clock() - before;
            msec = difference * 1000 / CLOCKS_PER_SEC;
    }while(msec < trigger);
    //printf("The timer has been triggered at %f\n", msec);
}
float calculateDistance(float firstx, float firsty, float secondx, float secondy){
    return sqrt(pow(firstx-secondx,2) + pow(firsty - secondy, 2));
}
bool checkOutOfBound(float x, float y, float xMax, float yMax){
    bool oob = false;
    if(x <= -xMax || x >= xMax || y <= -yMax || y >= yMax){
        oob = true;
    }
    return oob;
}
float changeDirection(float x, float y, float xMax, float yMax, float actualAngle){
    float startAngle;
    float finishAngle;
    int random = rand() % 10 + 1;
    if(x >= xMax){
        startAngle = PI / 2;
        finishAngle = PI * 3 / 2;
        if(y >= yMax){
            startAngle += PI / 2;
        }
        if(y <= -yMax){
            finishAngle -= PI/2;
        }
    }else if(x <= -xMax){
        startAngle = - PI / 2 ;
        finishAngle = PI / 2;
        if(y >= yMax){
            finishAngle -= PI / 2;
        }
        if(y <= yMax){
            startAngle += PI/2;
        }
    }else if(y >= yMax){
        startAngle = PI;
        finishAngle = PI * 2;
    }else if(y <= -yMax){
        startAngle = 0;
        finishAngle = PI;
    }else
    {
        return actualAngle;
    }
    
    return(startAngle + (finishAngle - startAngle) / 10 * random);
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

    int rank, size;
    float initialxCoordinate = (areaWidth / 2);  
    float initialyCoordinate = (areaLength / 2);
    float actualxCoordinate = initialxCoordinate;
    float actualyCoordinate = initialyCoordinate;
    float directionAngle;
    char state[20];
    struct region * firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);


    //Create the message type
    message msg;
    MPI_Datatype mpi_message;
    int struct_len = 3;
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
    block_lens[2] = 1;
    types[2] = MPI_CHAR;
    displacements[2] = (size_t) &(msg.processState) - (size_t) &msg;
    MPI_Type_create_struct(struct_len, block_lens, displacements, types, &mpi_message);
    MPI_Type_commit(&mpi_message);

    directionAngle = 2 * PI * rank / size;

    if(rank >= 0.0 && rank < infected){
        strcpy(state, "infected");
    }else
    {
        strcpy(state, "susceptible");
    } 
    //printf("Process %d has started working!\nActual position: %f, %f\nDirection: %f\nState: %c\n", rank, actualxCoordinate, actualyCoordinate, directionAngle, state[0]);

    int count = 0;
    int day = 0;
    double xVelocity = velocity * cos(directionAngle);
    double yVelocity = velocity * sin(directionAngle);
    unsigned long secondsInDay = 86400;
    unsigned long secondsInThreeMonths = 13089600;
    int contactWithInfect = 0;
    bool possibleInfected = false;
    int simulatedDay = secondsInDay / simulationSeconds;
    int simulatedTenMinutes = secondsInTenMinutes / simulationSeconds;
    int simulatedThreeMonths = secondsInThreeMonths / simulationSeconds;
    while (1)
    {   
        mainTimer(simulationSeconds);
        count++;
        if(contactWithInfect != 0 && contactWithInfect % simulatedTenMinutes == 0){
            strcpy(state, "infected");
        }
        if(count % simulatedDay == 0){
            day++;
            printf("[PROCESS %d] DAY %d Position: %f, %f State: %s\n", rank, day, actualxCoordinate, actualyCoordinate, state);
        }
        if (checkOutOfBound(actualxCoordinate, actualyCoordinate, areaLength / 2, areaWidth / 2))
        {
            directionAngle = changeDirection(actualxCoordinate, actualyCoordinate, areaLength/2, areaWidth/2, directionAngle);
            initialxCoordinate = actualxCoordinate;
            initialyCoordinate = actualyCoordinate;
            xVelocity = velocity * cos(directionAngle);
            yVelocity = velocity * sin(directionAngle); 
        }
        actualxCoordinate = initialxCoordinate + xVelocity * simulationSeconds * count;
        actualyCoordinate = initialyCoordinate + yVelocity * simulationSeconds * count;
        message *sendMessage =  malloc(sizeof(message));
        message *receiveMessage =  malloc(sizeof(message));
        sendMessage->xCoordinate = actualxCoordinate;
        sendMessage->yCoordinate = actualyCoordinate;
        sendMessage->processState = state[0];
        MPI_Request request;
        MPI_Status status;
        for (int i = 0; i < size; i++)
        {
            if(i != rank){
                //printf("[PROCESS %d] Sending message: %f, %f, %c\n", rank, sendMessage->xCoordinate, sendMessage->yCoordinate, sendMessage->processState);
                MPI_Isend(sendMessage, 1, mpi_message, i, 0, MPI_COMM_WORLD, &request);
                MPI_Recv(receiveMessage, 1, mpi_message, i, 0, MPI_COMM_WORLD, &status);
                printf("[PROCESS %d] Message received from %d XCoordinate: %f YCoordinate: %f state: %c\n",rank, i, receiveMessage->xCoordinate,receiveMessage->yCoordinate, receiveMessage->processState);
                if(receiveMessage->processState == 'i' && calculateDistance(actualxCoordinate, actualyCoordinate, receiveMessage->xCoordinate, receiveMessage->yCoordinate) <= spreadingDistance){
                    possibleInfected = true;
                    contactWithInfect++;
                    break;
                }
            }
            possibleInfected = false;
        }
        free(sendMessage);
        free(receiveMessage);
        if(!possibleInfected){
            contactWithInfect = 0;
        } 
    }
    
    MPI_Type_free(&mpi_message);
    MPI_Finalize();
}