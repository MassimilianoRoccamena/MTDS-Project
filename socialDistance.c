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
        float xCoordinateStart;
        float yCoordinateStart;
        float xCoordinateEnd;
        float yCoordinateEnd;
        int infected;
        int immune;
        int susceptible;
        int regionNumber;
        struct region *next;
    };
typedef struct message
{
    float xCoordinate;
    float yCoordinate;
    char processState;
} message;

MPI_Datatype createMessageType(){
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
    return mpi_message;
}
struct region* createRegions(float areaWidth, float areaLength, float regionWidth, float regionLength){

    struct region *firstRegion = NULL;
    float xRegion = areaLength / regionLength;
    float yRegion = areaWidth / regionWidth;
    float startX = -areaLength/2;
    float startY = -areaWidth/2;
    int count = 0;
    for (int i = 0; i < yRegion; i++)
    {
        for (int j = 0; j < xRegion; j++)
        {   
            count++;
            struct region *newRegion;
            newRegion = malloc(sizeof(struct region));
            newRegion->xCoordinateStart = startX + j * regionLength;
            newRegion->xCoordinateEnd = startX + (j+1) * regionLength;
            newRegion->yCoordinateStart = startY + i * regionWidth;
            newRegion->yCoordinateEnd = startY + (i+1) * regionWidth;
            newRegion->infected = 0;
            newRegion->susceptible = 0;
            newRegion->immune = 0;
            newRegion->regionNumber = count;
            newRegion->next = firstRegion;
            firstRegion = newRegion;
            //printf("REGION %d: from: %f, %f to: %f, %f\n", newRegion->regionNumber, newRegion->xCoordinateStart, newRegion->yCoordinateStart, newRegion->xCoordinateEnd, newRegion->yCoordinateEnd);
        } 
    }
    return firstRegion;
    
}
void mainTimer(float simulationTime){

    float trigger = simulationTime * simulationTimeProportion * 1000;
    printf("trigger: %f\n", trigger);
    float msec = 0;
    clock_t before = clock();
    do{
        clock_t difference = clock() - before;
        msec = difference * 1000 / CLOCKS_PER_SEC;
    }while(msec < trigger);
    printf("The timer has been triggered at %f\n", msec);
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
void printInfected(struct region* first){
    struct region*p;
    for(p = first; p!=NULL; p = p->next){
        printf("REGION %d: %d infected, %d susceptible, %d immune\n", p->regionNumber, p->infected, p->susceptible, p->immune);
    }
}
void resetRegions(struct region* first, float x, float y, char state){
    struct region*p;
    for(p = first; p!=NULL; p = p->next){
        if(p->xCoordinateStart <= x && p->xCoordinateEnd > x && p->yCoordinateStart <= y && p->yCoordinateEnd > y){
            //printf("x: %f y: %f state: %c. Inside region %d\n", x, y, state, p->regionNumber);
            switch (state)
            {
            case 'i':
                p->infected = 1;
                break;
            case 's':
                p->susceptible = 1;
                break;
            case 'c':
                p->immune = 1;
                break;
            default:
                break;
            }
        }else{
            p->infected = 0;
            p->immune = 0;
            p->susceptible = 0;
        }
    }
    //printInfected(first);
}
void updateRegions(struct region* first, float x, float y, char state){
    struct region*p;
     for(p = first; p!=NULL; p = p->next){
        if(p->xCoordinateStart <= x && p->xCoordinateEnd > x && p->yCoordinateStart <= y && p->yCoordinateEnd > y){
            //printf("x: %f y: %f state: %c. Inside region %d\n", x, y, state, p->regionNumber);
            switch (state)
            {
            case 'i':
                p->infected++;
                break;
            case 's':
                p->susceptible++;
                break;
            case 'c':
                p->immune++;
                break;
            default:
                break;
            }
        }
    }
    //printInfected(first);
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
    float initialxCoordinate = 0;  
    float initialyCoordinate = 0;
    float actualxCoordinate = initialxCoordinate;
    float actualyCoordinate = initialyCoordinate;
    float directionAngle;
    char state[20];
    struct region * firstRegion;

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Datatype mpi_message = createMessageType();
    
    directionAngle = 2 * PI * rank / size;

    if(rank == 0){
        firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength);
    }

    if(rank >= 0.0 && rank < infected){
        strcpy(state, "infected");
    }else
    {
        strcpy(state, "susceptible");
    } 
    //printf("Process %d has started working!\nActual position: %f, %f\nDirection: %f\nState: %c\n", rank, actualxCoordinate, actualyCoordinate, directionAngle, state[0]);

    int iterations = 0;
    int movement = 0;
    int day = 0;
    int timeAsInfect = 0;
    int timeAsImmune = 0;
    double xVelocity = velocity * cos(directionAngle);
    double yVelocity = velocity * sin(directionAngle);
    unsigned long secondsInDay = 86400;
    unsigned long secondsInThreeMonths = 13089600;
    unsigned long secondsinTenDays = 864000;
    int contactWithInfect = 0;
    bool possibleInfected = false;
    int simulatedDay = secondsInDay / simulationSeconds;
    int simulatedTenDays = secondsinTenDays / simulationSeconds;
    int simulatedTenMinutes = secondsInTenMinutes / simulationSeconds;
    int simulatedThreeMonths = secondsInThreeMonths / simulationSeconds;
    while (1)
    {   
        //mainTimer(simulationSeconds);
        iterations++;
        movement++;
        if(state[0] == 'i'){
            timeAsInfect++;
        }
        if(state[0] == 'c'){
            timeAsImmune ++;
        }
        if(timeAsInfect != 0 && timeAsInfect % simulatedTenDays == 0){
            strcpy(state, "cured");
            timeAsInfect = 0;
        }
        if(timeAsImmune != 0 && timeAsImmune % simulatedThreeMonths == 0){
            strcpy(state, "susceptible");
            timeAsImmune = 0;
        }
        if(contactWithInfect != 0 && contactWithInfect % simulatedTenMinutes == 0){
            strcpy(state, "infected");
        }
         if(iterations % simulatedDay == 0 && rank == 0){
            day++;
            printf("DAY %d\n", day);
            printInfected(firstRegion);
        }
        if(rank == 0){
            resetRegions(firstRegion, actualxCoordinate, actualyCoordinate,state[0]);
        }
        float nextxCoordinate = initialxCoordinate + xVelocity * simulationSeconds * movement;
        float nextyCoordinate = initialyCoordinate + yVelocity * simulationSeconds * movement;
        if (checkOutOfBound(nextxCoordinate, nextyCoordinate, areaLength / 2, areaWidth / 2))
        {
            directionAngle = changeDirection(nextxCoordinate, nextyCoordinate, areaLength/2, areaWidth/2, directionAngle);
            //printf("[PROCESS %d] Changing direction. New direction: %f\n", rank, directionAngle);
            initialxCoordinate = actualxCoordinate;
            initialyCoordinate = actualyCoordinate;
            movement = 1;
            xVelocity = velocity * cos(directionAngle);
            yVelocity = velocity * sin(directionAngle); 
        }
        actualxCoordinate = initialxCoordinate + xVelocity * simulationSeconds * movement;
        actualyCoordinate = initialyCoordinate + yVelocity * simulationSeconds * movement;
        //printf("[PROCESS %d] x: %f y: %f\n", rank, actualxCoordinate, actualyCoordinate);
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
                MPI_Recv(receiveMessage, 1, mpi_message, MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &status);
                //printf("[PROCESS %d] Message received from %d XCoordinate: %f YCoordinate: %f state: %c\n",rank, i, receiveMessage->xCoordinate,receiveMessage->yCoordinate, receiveMessage->processState);
                if(rank == 0){
                    updateRegions(firstRegion, receiveMessage->xCoordinate, receiveMessage->yCoordinate, receiveMessage->processState);
    
                }
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