/*
 ============================================================================
 Name        : IoTGenerator.c
 Author      : Enas
 Version     :
 Copyright   : Your copyright notice
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<arpa/inet.h>
#include<sys/socket.h>

#define SERVER "10.0.0.3"
#define BUFLEN 1500  //Max length of buffer
#define PORT 9999   //The port on which to send data
#define PKTS_FILENAME "/home/enas/Client/OneDayMGRS.csv"
#define POISSON_FILENAME "/home/enas/Client/poisson.txt"
#define PKTNUM 5000/* change to accomodate other sizes, change ONCE here */

char ** pkts  = NULL;
char ** pnumbers  = NULL;
void die(char *s)
{
    perror(s);
    exit(1);
}
void readpackets(char *pkts_file)
{

 FILE *f;
 f = fopen(pkts_file,"r");



  fseek(f, 0, SEEK_END);
  long fsize = ftell(f);
  fseek(f, 0, SEEK_SET);  //same as rewind(f);

  char *string = malloc(fsize + 1);
  fread(string, fsize, 1, f);

  fclose(f);



  char *  p    = strtok (string, "\n");
  int n_spaces = 0, i;


  /* split string and append tokens to 'res' */

  while (p) {
    pkts = realloc (pkts, sizeof (char*) * ++n_spaces);

    if (pkts == NULL)
      exit (-1); /* memory allocation failed */

    pkts[n_spaces-1] = p;

    p = strtok (NULL, "\n");
  }

  /* realloc one extra element for the last NULL */

  pkts = realloc (pkts, sizeof (char*) * (n_spaces+1));
  pkts[n_spaces] = 0;

 

 
  }



}


void readPoissonNumbers(char *poisson_file)
{

 FILE *f;
 f = fopen(poisson_file,"r");



  fseek(f, 0, SEEK_END);
  long fsize = ftell(f);
  fseek(f, 0, SEEK_SET);  //same as rewind(f);

  char *string = malloc(fsize + 1);
  fread(string, fsize, 1, f);
  fclose(f);



  char *  p    = strtok (string, "\n");
  int n_spaces = 0, i;


  /* split string and append tokens to 'res' */

  while (p) {
	  pnumbers = realloc (pnumbers, sizeof (char*) * ++n_spaces);

    if (pnumbers == NULL)
      exit (-1); /* memory allocation failed */

    pnumbers[n_spaces-1] = p;

    p = strtok (NULL, "\n");
  }

  /* realloc one extra element for the last NULL */

  pnumbers = realloc (pnumbers, sizeof (char*) * (n_spaces+1));
  pnumbers[n_spaces] = 0;



}
int main( int argc, char *argv[])
{
	if ( argc != 4 )
	    {
	        printf( "Incorrect input. Correction: %s pkts_file poisson_file pktCount", argv[0] );
	    } else{

	  printf("hi\n");
	  int ctr;
	  //get file names

	   readpackets(argv[1]);
	   readPoissonNumbers(argv[2]);
	   int pktCount=atoi(argv[3]);
	   struct sockaddr_in si_other;
	   int s, i, slen=sizeof(si_other);
	   char buf[BUFLEN];


    if ( (s=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("socket");
    }

    memset((char *) &si_other, 0, sizeof(si_other));
    si_other.sin_family = AF_INET;
    si_other.sin_port = htons(PORT);

    if (inet_aton(SERVER , &si_other.sin_addr) == 0)
    {
        fprintf(stderr, "inet_aton() failed\n");
        exit(1);
    }
            // msg example: char message[BUFLEN]="18TXR0965645458,239,2015-03-08 08 06 08 835000,45.5540237427,-73.595085144";
    int index=0;
    char random[1455];
    int y;
    for ( y=0;y<=1455;y++)
    {
    	random[y]="f";
    }

    double x=0.0011*1000000;
    while(index <pktCount)
    {
  
        if (sendto(s, pkts[index], strlen(random) , 0 , (struct sockaddr *) &si_other, slen)==-1)
        {
            die("sendto()");
        }

        index++;
        int c=(strtod (pnumbers[index], NULL))*1000000;
        int prop_delay=0.00011*1000000;
	
         usleep(abs(c-prop_delay));
    }

    //sleep for a minute before sending termination string to make sure all transmitters are done
    printf("%d \n", index);   
    close(s);
    free (pkts);
    free (pnumbers);
	    }
    return 0;
}

