#include <stdio.h>
#include <time.h>
#include <mpi.h>
#include <stdlib.h>
#include <math.h>

#define PI 3.1415
#define simulationTimeProportion 0.00001

struct region
    {
        int xCoordinateStart;
        int yCoordinateStart;
        int xCoordinateEnd;
        int yCoordinateEnd;
        struct country *next

    };

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
            clock_t difference = clock - before;
             msec = difference * 1000 / CLOCKS_PER_SEC;
        }while(msec < trigger)
}

int main(int argc, char** argv){
    int individuals = atoi(argv[1]);
    int infected = atoi(argv[2]);
    int areaWidth = atoi(argv[3]);
    int areaLength = atoi(argv[4]);
    int regionWidth = atoi(argv[5]);
    int regionLength = atoi(argv[6]);
    int velocity = atoi(argv[7]);
    int spreadingDistance = atoi(argv[8]);
    int simulationSeconds = atoi(argv[9]);

    int rank, size;
    float initialxCoordinate = areaWidth / 2;  
    float initialyCoordinate = areaLength / 2;
    float xCoordinate = initialxCoordinate;
    float yCoordinate = initialyCoordinate;
    float directionAngle;
    struct region * firstRegion = createRegions(areaWidth, areaLength, regionWidth, regionLength);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    directionAngle = 2 * PI / rank;

    double xVelocity = velocity * cos(directionAngle);
    double yVelocity = velocity * sin(directionAngle);
    while (1)
    {
        mainTimer(simulationSeconds);
        xCoordinate = initialxCoordinate + xVelocity * simulationSeconds;
        yCoordinate = initialyCoordinate + yVelocity * simulationSeconds;
    }
    
    





}