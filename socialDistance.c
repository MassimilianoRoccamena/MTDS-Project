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
    float xCoordinateStart;             //starting x coordinate of the region
    float yCoordinateStart;             //starting y coordinate of the region
    float xCoordinateEnd;               //ending x coordinate of the region
    float yCoordinateEnd;               //ending y coordinate of the region
    int infected;                       //number of infected individuals
    int immune;                         //number of immune individuals
    int susceptible;                    //number of susceptible individuals
    int regionNumber;                   //number of the region
    struct region *next;                //next region in the list
    struct individual *firstIndividual; //list of individuals in the region
};
struct individual
{
    int number;              //number of the individual
    float actualXPosition;   //actual x coordinate of the individual
    float actualYPosition;   //actual y coordinate of the individual
    float initialxPosition;  // initial x coordinate of the individual
    float initialYPosition;  // initial y coordinate of the individual
    float xVelocity;         //velocity of the individual along the x axis
    float yVelocity;         //velocity of the individual along the y axis
    float directionAngle;    //direction of the individual
    char state;              //state of the individual: infected(i), susceptible(s) or immune(c)
    bool move;               //boolean to decide if an individual has to be moved in a specific iteration
    int contactWithInfect;   //number of iterations the individual has been in contact with an infected
    int timeAsInfect;        //number of iterations the individual has been infected
    int timeAsImmune;        //number of iterations the individual has been immune
    int movement;            //number of iterations passed from the last change of direction
    struct individual *next; //next individual in the list
};
struct message
{
    float actualXPosition;
    float actualYPosition;
    float initialxPosition;
    float initialYPosition;
    float directionAngle;
    char state;
    int contactWithInfect;
    int timeAsInfect;
    int timeAsImmune;
    int movement;
    int number;
};

MPI_Datatype createMessageType()
{
    struct message msg;
    MPI_Datatype mpi_message;
    int struct_len = 11;
    int block_lens[struct_len];
    MPI_Datatype types[struct_len];
    MPI_Aint displacements[struct_len];
    MPI_Aint current_displacement = 0;
    block_lens[0] = 1;
    types[0] = MPI_FLOAT;
    displacements[0] = (size_t) & (msg.actualXPosition) - (size_t)&msg;
    block_lens[1] = 1;
    types[1] = MPI_FLOAT;
    displacements[1] = (size_t) & (msg.actualYPosition) - (size_t)&msg;
    block_lens[2] = 1;
    types[2] = MPI_FLOAT;
    displacements[2] = (size_t) & (msg.initialxPosition) - (size_t)&msg;
    block_lens[3] = 1;
    types[3] = MPI_FLOAT;
    displacements[3] = (size_t) & (msg.initialYPosition) - (size_t)&msg;
    block_lens[4] = 1;
    types[4] = MPI_FLOAT;
    displacements[4] = (size_t) & (msg.directionAngle) - (size_t)&msg;
    block_lens[5] = 1;
    types[5] = MPI_CHAR;
    displacements[5] = (size_t) & (msg.state) - (size_t)&msg;
    block_lens[6] = 1;
    types[6] = MPI_INT;
    displacements[6] = (size_t) & (msg.contactWithInfect) - (size_t)&msg;
    block_lens[7] = 1;
    types[7] = MPI_INT;
    displacements[7] = (size_t) & (msg.timeAsInfect) - (size_t)&msg;
    block_lens[8] = 1;
    types[8] = MPI_INT;
    displacements[8] = (size_t) & (msg.timeAsImmune) - (size_t)&msg;
    block_lens[9] = 1;
    types[9] = MPI_INT;
    displacements[9] = (size_t) & (msg.movement) - (size_t)&msg;
    block_lens[10] = 1;
    types[10] = MPI_INT;
    displacements[10] = (size_t) & (msg.number) - (size_t)&msg;

    MPI_Type_create_struct(struct_len, block_lens, displacements, types, &mpi_message);
    MPI_Type_commit(&mpi_message);
    return mpi_message;
}
//create the list of regions
struct region *createRegions(float areaWidth, float areaLength, float regionWidth, float regionLength, int rank, int localRegions, int size, int rest)
{

    struct region *firstRegion = NULL;
    float xRegion = areaLength / regionLength;
    float yRegion = areaWidth / regionWidth;
    float startX = -areaLength / 2;
    float startY = -areaWidth / 2;
    int lowerBound = (localRegions * rank) + 1;
    int higherBound = (rank + 1) * localRegions;
    if (rank + 1 == size && rest != 0)
    {
        higherBound += rest;
    }
    int count = 0;
    for (int i = 0; i < yRegion; i++)
    {
        for (int j = 0; j < xRegion; j++)
        {
            count++;
            if (count >= lowerBound && count <= higherBound)
            {
                struct region *newRegion;
                newRegion = malloc(sizeof(struct region));
                newRegion->xCoordinateStart = startX + j * regionLength;
                newRegion->xCoordinateEnd = startX + (j + 1) * regionLength;
                newRegion->yCoordinateStart = startY + i * regionWidth;
                newRegion->yCoordinateEnd = startY + (i + 1) * regionWidth;
                newRegion->infected = 0;
                newRegion->susceptible = 0;
                newRegion->immune = 0;
                newRegion->regionNumber = count;
                newRegion->next = firstRegion;
                newRegion->firstIndividual = NULL;
                firstRegion = newRegion;
                //printf("[PROCESS %d] REGION %d: from: %f, %f to: %f, %f\n", rank, newRegion->regionNumber, newRegion->xCoordinateStart, newRegion->yCoordinateStart, newRegion->xCoordinateEnd, newRegion->yCoordinateEnd);
            }
        }
    }
    return firstRegion;
}
//create the list of individuals
void createIndividuals(int ind, int inf, int indr, int infr, int rank, float length, float width, float velocity, struct region *firstRegion)
{
    int tind = ind;
    int tinf = inf;
    bool first = false;
    int random = 1 / MPI_Wtime();
    struct region *j;
    for (j = firstRegion; j != NULL; j = j->next)
    {
        if (j->regionNumber == 1)
        {
            ind = tind + indr;
            inf = tinf + infr;
        }
        else
        {
            ind = tind;
            inf = tinf;
        }

        for (int i = 0; i < ind; i++)
        {
            struct individual *newIndividual = malloc(sizeof(struct individual));
            float randomX = (((float)rand()) / (float)RAND_MAX);
            float randomY = (((float)rand()) / (float)RAND_MAX);
            //printf("%f %f\n", randomX, randomY);
            random = abs(1 / MPI_Wtime());
            //printf("%d\n", random);
            newIndividual->initialxPosition = j->xCoordinateStart + (random % (int)length) + randomX;
            random = abs(1 / MPI_Wtime());
            //printf("%f\n", newIndividual->initialxPosition);
            newIndividual->initialYPosition = j->yCoordinateStart + (random % (int)width) + randomY;
            newIndividual->actualXPosition = newIndividual->initialxPosition;
            newIndividual->actualYPosition = newIndividual->initialYPosition;
            random = abs(1 / MPI_Wtime());
            newIndividual->directionAngle = 2 * PI / 30 * (random % 30);
            newIndividual->xVelocity = velocity * cos(newIndividual->directionAngle);
            newIndividual->yVelocity = velocity * sin(newIndividual->directionAngle);
            newIndividual->movement = 0;
            if (j->regionNumber == 1)
            {
                newIndividual->number = (j->regionNumber - 1) * tind + i + 1;
            }
            else
            {
                newIndividual->number = (j->regionNumber - 1) * ind + i + 1 + indr;
            }
            newIndividual->move = true;
            if (inf != 0)
            {
                newIndividual->state = 'i';
                inf--;
            }
            else
            {
                newIndividual->state = 's';
            }
            newIndividual->contactWithInfect = 0;
            newIndividual->timeAsImmune = 0;
            newIndividual->timeAsInfect = 0;
            newIndividual->next = j->firstIndividual;
            j->firstIndividual = newIndividual;
            //printf("[PROCESS %d, REGION: %d INDIVIDUAL %d] x: %f y: %f direction: %f state: %c\n", rank,j->regionNumber, newIndividual->number, newIndividual->actualXPosition, newIndividual->actualYPosition, newIndividual->directionAngle, newIndividual->state);
        }
    }

    return;
}
//calculate the distance between two points
float calculateDistance(float firstx, float firsty, float secondx, float secondy)
{
    return sqrt(pow(firstx - secondx, 2) + pow(firsty - secondy, 2));
}
//check if an individual is out of the area limits
bool checkOutOfBound(float x, float y, float xMax, float yMax)
{
    bool oob = false;
    if (x <= -xMax || x >= xMax || y <= -yMax || y >= yMax)
    {
        oob = true;
    }
    return oob;
}
//change the direction of an individual that is going to go out of the bounds
float changeDirection(float x, float y, float xMax, float yMax, float actualAngle, int iteration, int randomFactor)
{
    float startAngle;
    float finishAngle;
    unsigned long int temp = abs(randomFactor) * iteration;
    int random = temp % 10 + 1;
    if (x >= xMax)
    {
        startAngle = PI / 2;
        finishAngle = PI * 3 / 2;
        if (y >= yMax)
        {
            startAngle += PI / 2;
        }
        if (y <= -yMax)
        {
            finishAngle -= PI / 2;
        }
    }
    else if (x <= -xMax)
    {
        startAngle = -PI / 2;
        finishAngle = PI / 2;
        if (y >= yMax)
        {
            finishAngle -= PI / 2;
        }
        if (y <= yMax)
        {
            startAngle += PI / 2;
        }
    }
    else if (y >= yMax)
    {
        startAngle = PI;
        finishAngle = PI * 2;
    }
    else if (y <= -yMax)
    {
        startAngle = 0;
        finishAngle = PI;
    }
    else
    {
        return actualAngle;
    }

    return (startAngle + (finishAngle - startAngle) / 10 * random);
}
//print the list of individuals for each region
void printIndividuals(struct region *first, int day)
{
    struct region *p;
    for (p = first; p != NULL; p = p->next)
    {
        printf("DAY: %d REGION %d: %d infected, %d susceptible, %d immune\n", day, p->regionNumber, p->infected, p->susceptible, p->immune);
    }
    printf("\n");
    return;
}
//check if an individual is in the list of individuals for a region
bool individualAlreadyInRegion(struct region *region, int num)
{
    bool found = false;
    struct individual *p;
    for (p = region->firstIndividual; p != NULL; p = p->next)
    {
        if (p->number == num)
        {
            found = true;
            break;
        }
    }
    return found;
}
//update the informations abount an individual
void updateIndividual(struct region *region, char s, int taim, int tain, int cwi, int num)
{
    struct individual *p;
    for (p = region->firstIndividual; p != NULL; p = p->next)
    {
        if (p->number == num)
        {
            p->timeAsImmune = taim;
            p->timeAsInfect = tain;
            p->state = s;
            p->contactWithInfect = cwi;
        }
    }
    return;
}
//add an individual to a region in the same process
void addIndividual(struct region *region, struct individual *individual, float nextX, float nextY)
{
    struct individual *newIndividual = malloc(sizeof(struct individual));
    newIndividual->initialxPosition = individual->initialxPosition;
    newIndividual->initialYPosition = individual->initialYPosition;
    newIndividual->actualXPosition = nextX;
    newIndividual->actualYPosition = nextY;
    newIndividual->xVelocity = individual->xVelocity;
    newIndividual->yVelocity = individual->yVelocity;
    newIndividual->directionAngle = individual->directionAngle;
    newIndividual->contactWithInfect = individual->contactWithInfect;
    newIndividual->timeAsImmune = individual->timeAsImmune;
    newIndividual->timeAsInfect = individual->timeAsInfect;
    newIndividual->movement = individual->movement;
    newIndividual->move = false;
    newIndividual->number = individual->number;
    newIndividual->state = individual->state;
    newIndividual->next = region->firstIndividual;
    region->firstIndividual = newIndividual;
    return;
}
//check if some coordinates are in a region
bool insideRegion(float actualX, float actualY, struct region *reg, float xMove, float yMove)
{
    bool xBound = reg->xCoordinateStart - xMove <= actualX && reg->xCoordinateEnd + xMove >= actualX;
    bool yBound = reg->yCoordinateStart - yMove <= actualY && reg->yCoordinateEnd + yMove >= actualY;
    return xBound && yBound;
}
//check if an invidual is in the borders of a region
bool checkRegionBorders(struct region *reg, struct region *firstRegion, struct individual *ind, float infectDistance, float length, float width, float nextX, float nextY)
{
    if (!insideRegion(ind->actualXPosition, ind->actualYPosition, reg, 0, 0))
    {
        return false;
    }
    bool xCheckBefore = ind->actualXPosition <= reg->xCoordinateStart + infectDistance || ind->actualXPosition >= reg->xCoordinateEnd - infectDistance;
    bool yCheckBefore = ind->actualYPosition <= reg->yCoordinateStart + infectDistance || ind->actualYPosition >= reg->yCoordinateEnd - infectDistance;
    bool xCheckAfter = nextX <= reg->xCoordinateStart + infectDistance || nextX >= reg->xCoordinateEnd - infectDistance;
    bool yCheckAfter = nextY <= reg->yCoordinateStart + infectDistance || nextY >= reg->yCoordinateEnd - infectDistance;
    bool checkXBound = nextX - infectDistance >= -length / 2 && nextX + infectDistance <= length / 2;
    bool checkYBound = nextY - infectDistance >= -width / 2 && nextY + infectDistance <= width / 2;
    int regions = 0;
    struct region *p;
    //get the number of regions the individual is near (max number is 3)
    if (xCheckAfter && checkXBound)
    {
        regions++;
    }
    if (yCheckAfter && checkYBound)
    {
        regions++;
    }
    if ((xCheckAfter && yCheckAfter) && (checkYBound && checkXBound))
    {
        regions++;
    }
    //check if the regions the individual is near are controlled by this process. If this is the case add the individual or update it
    for (p = firstRegion; p != NULL && regions > 0; p = p->next)
    {
        if (p->regionNumber != reg->regionNumber && insideRegion(nextX, nextY, p, infectDistance, infectDistance))
        {
            if (!individualAlreadyInRegion(p, ind->number))
            {
                //printf("Individual added to region %d. Coordinates: %f %f\n", p->regionNumber, nextX, nextY);
                addIndividual(p, ind, nextX, nextY);
            }
            else
            {
                updateIndividual(p, ind->state, ind->timeAsImmune, ind->timeAsInfect, ind->contactWithInfect, ind->number);
            }

            regions--;
        }
    }
    //return the number of regions not controlled by this process, that have to be notified
    return (regions > 0);
}
//reset the number of infected, susceptible and immune individuals
void resetRegions(struct region *first)
{
    struct region *p;
    for (p = first; p != NULL; p = p->next)
    {
        p->infected = 0;
        p->immune = 0;
        p->susceptible = 0;
    }
    return;
    //printIndividuals(first);
}
//update the number of infected, susceptible and immune individuals
void updateRegions(struct region *first)
{
    struct region *p;
    struct individual *q;
    for (p = first; p != NULL; p = p->next)
    {
        for (q = p->firstIndividual; q != NULL; q = q->next)
        {
            if (insideRegion(q->actualXPosition, q->actualYPosition, p, 0, 0))
            {
                switch (q->state)
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
    }
    return;
    //printIndividuals(first);
}
//add an individual to a region from another process
void addMessageIndividual(struct message message, struct region *region, float velocity)
{
    struct individual *newIndividual = malloc(sizeof(struct individual));
    newIndividual->initialxPosition = message.initialxPosition;
    newIndividual->initialYPosition = message.initialYPosition;
    newIndividual->actualXPosition = message.actualXPosition;
    newIndividual->actualYPosition = message.actualYPosition;
    newIndividual->xVelocity = velocity * cos(message.directionAngle);
    newIndividual->yVelocity = velocity * sin(message.directionAngle);
    newIndividual->directionAngle = message.directionAngle;
    newIndividual->contactWithInfect = message.contactWithInfect;
    newIndividual->timeAsImmune = message.timeAsImmune;
    newIndividual->timeAsInfect = message.timeAsInfect;
    newIndividual->movement = message.movement;
    newIndividual->move = true;
    newIndividual->state = message.state;
    newIndividual->number = message.number;
    newIndividual->next = region->firstIndividual;
    region->firstIndividual = newIndividual;
    return;
}
//remove an individual from a region
void removeIndividual(struct region *region, struct individual *individual, struct region *firstRegion)
{
    struct region *p;
    struct individual *q;
    for (p = firstRegion; p != NULL; p = p->next)
    {
        if (p->regionNumber == region->regionNumber)
        {
            q = p->firstIndividual;
            if (q->number == individual->number)
            {
                p->firstIndividual = q->next;
                free(q);
                return;
            }
            else
            {
                struct individual *previous = q;
                for (q = q->next; q != NULL; q = q->next)
                {
                    if (q->number == individual->number)
                    {
                        previous->next = q->next;
                        free(q);
                        return;
                    }
                    previous = previous->next;
                }
            }
        }
    }
    return;
}
//reset the send buffer
void resetBuffer(int individuals, struct message buffer[])
{
    for (int i = 0; i < individuals; i++)
    {
        struct message msg = buffer[i];
        if (msg.state == 'i' || msg.state == 'c' || msg.state == 's')
        {
            buffer[i].state = 'x';
        }
        else
        {
            break;
        }
    }
}
//check if the individuals received from other processes are inside aregion controlled by this process
void manageReceivedIndividuals(int individuals, struct message buffer[], struct region *firstRegion, float spreadingDistance, float velocity)
{
    struct region *q;
    for (int j = 0; j < individuals; j++)
    {
        struct message msg = buffer[j];
        if (msg.state == 'i' || msg.state == 's' || msg.state == 'c')
        {
            //printf("[PROCESS %d] Found a message from process %d\n", rank, i);
            for (q = firstRegion; q != NULL; q = q->next)
            {
                if (insideRegion(msg.actualXPosition, msg.actualYPosition, q, spreadingDistance, spreadingDistance))
                {
                    if (!individualAlreadyInRegion(q, msg.number))
                    {
                        //printf("[PROCESS %d ITERATION: %d] Added individual to region %d from message. Coordinates: %f %f\n", rank, iterations, q->regionNumber, msg.actualXPosition, msg.actualYPosition);
                        addMessageIndividual(msg, q, velocity);
                    }
                    else
                    {
                        updateIndividual(q, msg.state, msg.timeAsImmune, msg.timeAsInfect, msg.contactWithInfect, msg.number);
                    }
                }
            }
        }
        else
        {
            //printf("[PROCESS %d] No message found from process %d\n", rank, i);
            break;
        }
    }
}
//check if a susceptible individual is near an infected one
void checkInfected(struct individual *individual, struct region *region, float safeDistance)
{
    struct individual *p;
    if (individual->state != 's')
    {
    }
    else
    {
        for (p = region->firstIndividual; p != NULL; p = p->next)
        {
            if (p->state == 'i' && calculateDistance(individual->actualXPosition, individual->actualYPosition, p->actualXPosition, p->actualYPosition) <= safeDistance)
            {
                individual->contactWithInfect++;
                return;
            }
        }
        individual->contactWithInfect = 0;
    }
    return;
}

int main(int argc, char **argv)
{
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

    struct region *firstRegion;
    struct individual *firstIndividual;
    MPI_Datatype single_individual_message = createMessageType();
    MPI_Datatype all_individuals_message;

    int totalRegions = (areaWidth * areaLength) / (regionWidth * regionLength);
    int localRegions = totalRegions / size;
    int regionRest = totalRegions % size;
    int individualsforRegion = individuals / totalRegions;
    int infectedforRegion = infected / totalRegions;
    int individualRest = individuals % totalRegions;
    int infectedRest = infected % totalRegions;

    firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength, rank, localRegions, size, regionRest);
    //printf("[PROCESS %d] Number of individuals: %d\n", rank, localIndividuals);
    //printf("[PROCESS %d] Number of infected: %d\n", rank, localInfected);
    createIndividuals(individualsforRegion, infectedforRegion, individualRest, infectedRest, rank, regionLength, regionWidth, velocity, firstRegion);
    MPI_Type_contiguous(individuals, single_individual_message, &all_individuals_message);
    MPI_Type_commit(&all_individuals_message);

    unsigned long secondsInDay = 86400;
    unsigned long secondsInThreeMonths = 7776000;
    unsigned long secondsinTenDays = 864000;
    int iterations = 0;
    int day = 0;
    float iterationTime = simulationSeconds;
    int simulatedDay = secondsInDay / iterationTime;                 //number of iterations for a day
    int simulatedTenDays = secondsinTenDays / iterationTime;         //number of iterations for 10 days
    int simulatedTenMinutes = secondsInTenMinutes / iterationTime;   //number of iterations for ten minutes
    int simulatedThreeMonths = secondsInThreeMonths / iterationTime; //number of iterations for three months
    struct message sendBuffer[individuals];                          //array to save the individuals to send to other processes
    struct message receiveBuffer[individuals];                       //array to save the individuals coming from other processes

    while (1)
    {
        iterations++;
        struct individual *p;
        struct region *q;
        int border = 0;
        if (iterations % simulatedDay == 0) //if a day is finished print the individuals for each region
        {
            day++;
            printIndividuals(firstRegion, day);
            MPI_Barrier(MPI_COMM_WORLD);
        }
        resetRegions(firstRegion);
        //iteration to move each individual of each region
        for (q = firstRegion; q != NULL; q = q->next)
        {
            p = q->firstIndividual;
            while (p != NULL)
            {
                if (p->move)
                {
                    p->movement++;
                    if (p->state == 'i')
                    {
                        p->timeAsInfect++;
                    }
                    if (p->state == 'c')
                    {
                        p->timeAsImmune++;
                    }
                    if (p->timeAsInfect != 0 && p->timeAsInfect % simulatedTenDays == 0)
                    {
                        p->state = 'c';
                        p->timeAsInfect = 0;
                    }
                    if (p->timeAsImmune != 0 && p->timeAsImmune % simulatedThreeMonths == 0)
                    {
                        p->state = 's';
                        p->timeAsImmune = 0;
                    }
                    if (p->contactWithInfect != 0 && p->contactWithInfect % simulatedTenMinutes == 0)
                    {
                        p->state = 'i';
                        p->contactWithInfect = 0;
                    }

                    float nextxCoordinate = p->initialxPosition + p->xVelocity * simulationSeconds * p->movement;
                    float nextyCoordinate = p->initialYPosition + p->yVelocity * simulationSeconds * p->movement;
                    int iterate = 1;
                    //if the next position of the individual is out of bound change direction until the next position is inside the area
                    while (checkOutOfBound(nextxCoordinate, nextyCoordinate, areaLength / 2, areaWidth / 2))
                    {
                        p->directionAngle = changeDirection(nextxCoordinate, nextyCoordinate, areaLength / 2, areaWidth / 2, p->directionAngle, iterate, p->actualXPosition * 1234);
                        p->initialxPosition = p->actualXPosition;
                        p->initialYPosition = p->actualYPosition;
                        p->movement = 1;
                        p->xVelocity = velocity * cos(p->directionAngle);
                        p->yVelocity = velocity * sin(p->directionAngle);
                        nextxCoordinate = p->initialxPosition + p->xVelocity * simulationSeconds * p->movement;
                        nextyCoordinate = p->initialYPosition + p->yVelocity * simulationSeconds * p->movement;
                        iterate = iterate + 4;
                    }
                    //printf("[PROCESS %d, REGION %d] x: %f y: %f state: %c\n", rank, q->regionNumber, nextxCoordinate, nextyCoordinate, p->state);
                    bool xBound = nextxCoordinate < q->xCoordinateStart - spreadingDistance || nextxCoordinate > q->xCoordinateEnd + spreadingDistance;
                    bool yBound = nextyCoordinate < q->yCoordinateStart - spreadingDistance || nextyCoordinate > q->yCoordinateEnd + spreadingDistance;
                    if (xBound || yBound) //check if an individual is not in the area of influence of a region and in that case remove it from the list
                    {
                        //printf("[PROCESS %d ITERATION %d] Removing individual from region %d coordinates: %f %f\n", rank, iterations, q->regionNumber, nextxCoordinate, nextyCoordinate);
                        struct individual *temp = p;
                        p = p->next;
                        removeIndividual(q, temp, firstRegion);
                        continue;
                    }
                    if (checkRegionBorders(q, firstRegion, p, spreadingDistance, areaLength, areaWidth, nextxCoordinate, nextyCoordinate))
                    {
                        //printf("[PROCESS %d REGION %d] individual near the border: %f %f\n", rank, q->regionNumber, nextxCoordinate, nextyCoordinate);
                        struct message newMessage;
                        newMessage.initialxPosition = p->initialxPosition;
                        newMessage.initialYPosition = p->initialYPosition;
                        newMessage.actualXPosition = nextxCoordinate;
                        newMessage.actualYPosition = nextyCoordinate;
                        newMessage.contactWithInfect = p->contactWithInfect;
                        newMessage.directionAngle = p->directionAngle;
                        newMessage.state = p->state;
                        newMessage.number = p->number;
                        newMessage.timeAsImmune = p->timeAsImmune;
                        newMessage.timeAsInfect = p->timeAsInfect;
                        newMessage.movement = p->movement;
                        sendBuffer[border] = newMessage;
                        border++;
                    }

                    p->actualXPosition = nextxCoordinate;
                    p->actualYPosition = nextyCoordinate;
                }
                else
                {
                    p->move = true;
                }
                p = p->next;
            }
        }

        MPI_Request request;
        struct message msg = sendBuffer[0];
        bool send = (msg.state == 'i' || msg.state == 'c' || msg.state == 's'); //check if there are individuals to send to other processes

        //printf("[PROCESS %d] Check if a send is needed...\n", rank);
        if (send)
        {
            //printf("[PROCESS %d] Sending...\n", rank);
            for (int i = 0; i < size; i++)
            {
                if (i != rank)
                {
                    MPI_Isend(&sendBuffer, 1, all_individuals_message, i, 0, MPI_COMM_WORLD, &request);
                }
            }
        }
        resetBuffer(individuals, sendBuffer);

        MPI_Barrier(MPI_COMM_WORLD);
        //check if there are messages to receive and process them
        for (int i = 0; i < size; i++)
        {
            if (i != rank)
            {
                int check = 0;
                MPI_Iprobe(i, 0, MPI_COMM_WORLD, &check, MPI_STATUS_IGNORE);
                if (check == 1)
                {
                    //printf("[PROCESS %d] Receiving from %d\n", rank, i);
                    MPI_Recv(&receiveBuffer, 1, all_individuals_message, i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                    manageReceivedIndividuals(individuals, receiveBuffer, firstRegion, spreadingDistance, velocity);
                }
            }
        }

        //check if any susceptible individual is near in infected one
        for (q = firstRegion; q != NULL; q = q->next)
        {
            for (p = q->firstIndividual; p != NULL; p = p->next)
            {
                checkInfected(p, q, spreadingDistance);
            }
        }

        updateRegions(firstRegion);

        MPI_Barrier(MPI_COMM_WORLD);
        //printf("[PROCESS %d] Barrier reached\n", rank);
    }

    MPI_Finalize();
}