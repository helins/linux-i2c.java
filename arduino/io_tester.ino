/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




/*
 * This program allows an Arduino to act as an I2C slave for testing IO operations.
 *
 * The first bytes designed a command :
 *
 *     - 0 RESET         Resets the state.
 *     - 1 SINGLE_READ   Next read will be the value set by SINGLE_WRITE.
 *     - 2 SINGLE_WRITE  The second byte is a value to save.
 *     - 3 MULTI_READ    Next read will be the values set by MULTI_WRITE.
 *     - 4 MULTI_WRITE   The next bytes (max. 12) will be saved.
 *
 * A read operation from the master when no command is set will send 42.
 *
 * Everytime something happens, it is printed to serial. The master should take this
 * into account as printing is slow, hence it should not rush the slave.
 */




#include <stdint.h>
#include <Wire.h>


// Values the user can change if needed (java tests hardcoded those).
//
#define ADDRESS              0x42
#define SERIAL_BAUD_RATE     9600
#define SIMPLE_ANSWER        42


// The maximum number of values that can be multi written/read.
// Usually, fails over 16.
//
#define VALUES_LENGTH        12


// The possible commands, besides UNSET.
//
#define COMMAND_UNSET        -1
#define COMMAND_RESET        0
#define COMMAND_SINGLE_READ  1
#define COMMAND_SINGLE_WRITE 2
#define COMMAND_MULTI_READ   3
#define COMMAND_MULTI_WRITE  4




// Global variables.
//
int8_t  command                         = COMMAND_UNSET ;
uint8_t command_value                   = 0             ;
uint8_t command_values[ VALUES_LENGTH ]                 ;
size_t  command_values_length           = 0             ;




// Handles read requests from master.
//
void handle_req() {

    switch ( command ) {

        case COMMAND_UNSET :

            Serial.println( "[ REQ ] COMMAND_UNSET" ) ;

            Wire.write( SIMPLE_ANSWER ) ;

            break ;


        case COMMAND_SINGLE_READ :

            Serial.print( "[ REQ ] COMMAND_SINGLE_READ : " ) ;
            Serial.println( command_value )                  ;

            Wire.write( command_value ) ;

            break ;


        case COMMAND_MULTI_READ :

            Wire.write( command_values        ,
                        command_values_length ) ;


            Serial.print( "[ REQ ] COMMAND_MULTI_READ :" ) ;

            for ( size_t i = 0              ;
                  i < command_values_length ;
                  
                  i += 1                    )  {

                Serial.print( " " )                 ;
                Serial.print( command_values[ i ] ) ;
            }

            Serial.println( "" ) ;

            break ;


        default :

            Serial.println( "[ REQ ] UNKNOWN" ) ;
    }

    command = COMMAND_UNSET ;
}




// Handles write requests from master.
//
void handle_recv( int n ) {

    if ( n <= 0 ) {
    
        Serial.println( "[ RECV ] No bytes received." ) ;

        command = COMMAND_UNSET ;

        return ;
    }

    char b = Wire.read() ;

    switch ( b ) {

        case COMMAND_RESET :

            Serial.println( "[ RECV ] COMMAND_RESET" ) ;

            command = COMMAND_UNSET ;

            break ;

    
        case COMMAND_SINGLE_READ :
            
            Serial.println( "[ RECV ] COMMAND_SINGLE_READ" ) ;

            command = COMMAND_SINGLE_READ ;

            break ;


        case COMMAND_MULTI_READ :

            Serial.println( "[ RECV ] COMMAND_MULTI_READ" ) ;

            command = COMMAND_MULTI_READ ;

            break ;


        case COMMAND_SINGLE_WRITE :

            command       = COMMAND_UNSET ;
            command_value = Wire.read()   ;

            Serial.print( "[ RECV ] COMMAND_SINGLE_WRITE : " ) ;
            Serial.println( command_value )                    ;

            break ;


        case COMMAND_MULTI_WRITE :

            Serial.print( "[ RECV ] COMMAND_MULTI_WRITE :" ) ;

            command               = COMMAND_UNSET ;
            command_values_length = 0             ;

            for ( int i = 0                             ;
                  Wire.available() && i < VALUES_LENGTH ;
                  i += 1                                )  {

                command_values[ i ]    = Wire.read()  ;
                command_values_length += 1            ;

                Serial.print( " " )                 ;
                Serial.print( command_values[ i ] ) ;
            }

            Serial.println( "" ) ;

            break ;


        default :

            Serial.print( "[ RECV ] UNKNOWN : " ) ;
            Serial.println( b   ,
                            DEC )                 ;
    }
}




// In the beginning, setups up serial and I2C.
//
void setup() {

    Serial.begin( SERIAL_BAUD_RATE ) ;

    Wire.begin( ADDRESS )        ;
    Wire.onRequest( handle_req ) ;
    Wire.onReceive( handle_recv) ;

    Serial.println( "Ready !" ) ;
}




void loop() {

    //delay( 10 ) ;
}
