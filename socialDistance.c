#include <stdio.h>
#include <time.h>
#include <mpi.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <stdbool.h>

#define PI 3.1415
#define simulationTimeProportion 0.01
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
struct individual{
    int number;
    float actualXPosition;
    float actualYPosition;
    float initialxPosition;
    float initialYPosition;
    float xVelocity;
    float yVelocity;
    float directionAngle;
    char state;
    bool possibleInfected;
    int contactWithInfect;
    int timeAsInfect;
    int timeAsImmune;
    int movement;
    struct individual *next;
};
struct message
{
    float xCoordinate;
    float yCoordinate;
    char state;
};

MPI_Datatype createMessageType(){
    struct message msg;
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
    displacements[2] = (size_t) &(msg.state) - (size_t) &msg;
    MPI_Type_create_struct(struct_len, block_lens, displacements, types, &mpi_message);
    MPI_Type_commit(&mpi_message);
    return mpi_message;
}
struct region* createRegions(float areaWidth, float areaLength, float regionWidth, float regionLength, int rank, int localRegions, int size, int rest){

    struct region *firstRegion = NULL;
    float xRegion = areaLength / regionLength;
    float yRegion = areaWidth / regionWidth;
    float startX = -areaLength/2;
    float startY = -areaWidth/2;
    int lowerBound = (localRegions * rank) + 1;
    int higherBound = (rank + 1) * localRegions;
    if(rank + 1 == size && rest != 0){
        higherBound += rest; 
    }
    int count = 0;
    for (int i = 0; i < yRegion; i++)
    {
        for (int j = 0; j < xRegion; j++)
        {   
            count++;
            if(count >= lowerBound && count <= higherBound){
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
                printf("[PROCESS %d] REGION %d: from: %f, %f to: %f, %f\n",rank, newRegion->regionNumber, newRegion->xCoordinateStart, newRegion->yCoordinateStart, newRegion->xCoordinateEnd, newRegion->yCoordinateEnd);
            }
        } 
    }
    return firstRegion;
    
}
struct individual* createIndividuals(int localInfected, int localIndividuals, int rank, float width, float length, float velocity){
    struct individual* firstIndividual = NULL;
    int random = 1 / MPI_Wtime();
    for (int i = 0; i < localIndividuals; i++)
    {
        struct individual* newIndividual = malloc(sizeof(struct individual));
        newIndividual->number = i;
        float randomX = (((float) rand()) / (float) RAND_MAX);
        float randomY = (((float) rand()) / (float) RAND_MAX);
        //printf("%f %f\n", randomX, randomY); 
        random = abs(1 / MPI_Wtime());
        //printf("%d\n", random); 
        newIndividual->initialxPosition = -length/2 + (random % (int)length) + randomX;
        random = abs(1 / MPI_Wtime());
        //printf("%f\n", newIndividual->initialxPosition); 
        newIndividual->initialYPosition = -width/2 + (random % (int)width) + randomY;
        newIndividual->actualXPosition = newIndividual->initialxPosition;
        newIndividual->actualYPosition = newIndividual->initialYPosition;
        random = abs(1 / MPI_Wtime());
        newIndividual->directionAngle = 2 * PI / 30 * (random % 30);
        //printf("[PROCESS %d, INDIVIDUAL %d] x: %f y: %f direction: %f\n", rank, i, newIndividual->actualXPosition, newIndividual->actualYPosition, newIndividual->directionAngle);
        newIndividual->xVelocity = velocity * cos(newIndividual->directionAngle);
        newIndividual->yVelocity = velocity * sin(newIndividual->directionAngle);
        newIndividual->movement = 0;
        if(localInfected != 0){
            newIndividual->state = 'i';
            localInfected --;
        }else{
            newIndividual->state = 's';
        }
        newIndividual->possibleInfected = false;
        newIndividual->contactWithInfect = 0;
        newIndividual->timeAsImmune = 0;
        newIndividual->timeAsInfect = 0;
        newIndividual->next = firstIndividual;
        firstIndividual = newIndividual;
    }
    
    return firstIndividual;  
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
void resetRegions(struct region* first){
    struct region*p;
    for(p = first; p!=NULL; p = p->next){    
        p->infected = 0;
        p->immune = 0;
        p->susceptible = 0;
        
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
void checkInfected(struct individual *individual, int numberOfProcesses, int individuals, struct message individualList[numberOfProcesses][individuals], int rank, float safeDistance){
    if(individual->state != 's'){
    }else{
        for(int j = 0; j < numberOfProcesses; j++){
            for(int i = 0; i < individuals;  i++){
                struct message m = individualList[j][i];
                //printf("[PROCESS %d INDIVDUAL %d] x: %f y:%f state: %c\n", j, i, m.xCoordinate, m.yCoordinate, m.state);
                if(m.state == 'i' && calculateDistance(individual->actualXPosition, individual->actualYPosition, m.xCoordinate,m.yCoordinate) <= safeDistance){
                    individual->contactWithInfect++;
                    //printf("PROCESS %d INDIVIDUAL %d had contact with infect: %d\n", rank, individual->number, individual->contactWithInfect);
                    return;
                }
            }
        }
        individual->contactWithInfect = 0;
    }
    return;
    
}

int main(int argc, char** argv){
    MPI_Init(&argc, &argv);
    int individuals = atoi(argv[1]);
    int infected = atoi(argv[2]);
    float areaWidth = atof(argv[3]);
    float areaLength = atof(argv[4]);
    float regionWidth = atof(argv[5]);
    float regionLength = atof(argv[6]);
    float velocity = atof(argv[7]);
    float spreadingDistance = atof(argv[8]);
    float simulationSeconds = atof(argv[9]);

    int rank, size;
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    struct region * firstRegion;
    struct individual *firstIndividual;
    MPI_Datatype single_individual_message = createMessageType();
    MPI_Datatype all_individuals_message;

    int totalRegions = (areaWidth * areaLength) / (regionWidth * regionLength);
    int localRegions = totalRegions / size;
    int regionRest = totalRegions % size;
    int maxSize = individuals / size + individuals % size;
    int localIndividuals = individuals / size;
    int localInfected = infected / size;
    if(rank + 1 == size){
        localIndividuals += individuals % size;
        localInfected += infected % size;
    }

    firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength, rank, localRegions, size, regionRest);
    //printf("[PROCESS %d] Number of individuals: %d\n", rank, localIndividuals);
    //printf("[PROCESS %d] Number of infected: %d\n", rank, localInfected);
    firstIndividual = createIndividuals(localInfected, localIndividuals,rank, areaWidth, areaLength, velocity);
    MPI_Type_contiguous(maxSize, single_individual_message, &all_individuals_message);
    MPI_Type_commit(&all_individuals_message);

    unsigned long secondsInDay = 86400;
    unsigned long secondsInThreeMonths = 7776000;
    unsigned long secondsinTenDays = 864000;
    int iterations = 0;
    int day = 0;
    float iterationTime = simulationSeconds;
    int simulatedDay = secondsInDay / iterationTime;
    int simulatedTenDays = secondsinTenDays / iterationTime;
    int simulatedTenMinutes = secondsInTenMinutes / iterationTime;
    int simulatedThreeMonths = secondsInThreeMonths / iterationTime;
    
    while (1){
        iterations++;
        struct individual *p;
        struct message individualsMessage[size][maxSize];
        if(iterations % simulatedDay == 0){
                day++;
		if(rank == 0){
			printf("DAY %d\n", day);
		}
		MPI_Barrier(MPI_COMM_WORLD);
                printInfected(firstRegion);
        }
        
        resetRegions(firstRegion);
        
        for(p = firstIndividual; p!=NULL; p = p->next){
            p->movement++;
            if(p->state == 'i'){
                p->timeAsInfect++;
            }
            if(p->state == 'c'){
                p->timeAsImmune ++;
            }
            if(p->timeAsInfect != 0 && p->timeAsInfect % simulatedTenDays == 0){
                p->state = 'c';
                p->timeAsInfect = 0;
            }
            if(p->timeAsImmune != 0 && p->timeAsImmune % simulatedThreeMonths == 0){
                p->state = 's';
                p->timeAsImmune = 0;
            }
            if(p->contactWithInfect != 0 && p->contactWithInfect % simulatedTenMinutes == 0){
                p->state = 'i';
                p->contactWithInfect = 0;
            }
            
            float nextxCoordinate = p->initialxPosition + p->xVelocity * simulationSeconds * p->movement;
            float nextyCoordinate = p->initialYPosition + p->yVelocity * simulationSeconds * p->movement;
            while(checkOutOfBound(nextxCoordinate, nextyCoordinate, areaLength / 2, areaWidth / 2)){
                p->directionAngle = changeDirection(nextxCoordinate, nextyCoordinate, areaLength/2, areaWidth/2, p->directionAngle);
                p->initialxPosition = p->actualXPosition;
                p->initialYPosition = p->actualYPosition;
                p->movement = 1;
                p->xVelocity = velocity * cos(p->directionAngle);
                p->yVelocity = velocity * sin(p->directionAngle);
                nextxCoordinate = p->initialxPosition + p->xVelocity * simulationSeconds * p->movement;
                nextyCoordinate = p->initialYPosition + p->yVelocity * simulationSeconds * p->movement; 
            }

            p->actualXPosition = p->initialxPosition + p->xVelocity * simulationSeconds * p->movement;
            p->actualYPosition = p->initialYPosition + p->yVelocity * simulationSeconds * p->movement;
            //printf("[PROCESS %d, INDIVIDUAL %d] x: %f y: %f state: %c\n", rank, p->number, p->actualXPosition, p->actualYPosition, p->state);
            struct message newMessage;
            newMessage.xCoordinate = p->actualXPosition;
            newMessage.yCoordinate = p->actualYPosition;
            newMessage.state = p->state;
            individualsMessage[rank][p->number] = newMessage;
        }       
        
        MPI_Request request;
        for(int i = 0; i < size; i++){
            if(i != rank){
                MPI_Isend(&individualsMessage[rank], 1, all_individuals_message, i, 0, MPI_COMM_WORLD, &request);   
            }
        }
        MPI_Barrier(MPI_COMM_WORLD);
        for (int i = 0; i < size; i++){
            if(i != rank){
                MPI_Recv(&individualsMessage[i], 1, all_individuals_message, i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                //printf("[PROCESS %d] Received message from %d with %f %f %c\n",rank,i, individualsMessage[i][0].xCoordinate, individualsMessage[i][0].yCoordinate, individualsMessage[i][0].state);
            }
        }

        for(p = firstIndividual; p != NULL; p = p->next){
            checkInfected(p, size, maxSize, individualsMessage, rank, spreadingDistance);
        }
        for(int j = 0; j < size; j++){
            for(int i = 0; i < maxSize; i++){
                struct message m = individualsMessage[j][i];
                updateRegions(firstRegion, m.xCoordinate, m.yCoordinate, m.state);
            }
        }
                
        MPI_Barrier(MPI_COMM_WORLD);
        //printf("[PROCESS %d] Barrier reached\n", rank); 
    }
    
    MPI_Finalize();
}