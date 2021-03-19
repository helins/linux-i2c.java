/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.*                                   ;
import java.io.IOException                                     ;
import org.junit.jupiter.api.Disabled                          ;
import org.junit.jupiter.api.DisplayName                       ;
import org.junit.jupiter.api.Test                              ;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty ;




// Meant to run on a Raspberry Pi connected to an Arduino ./arduino/io_tester.ino
//
@EnabledIfSystemProperty( named   = "raspduino" ,
                          matches = "true"      ) 
public class RaspberryArduinoTest {


    // Waits between IO operations so the arduino has time for printing to serial what is going on.
    //
    private static final int WAIT_MS = 100 ; 

    // The address of the arduino.
    //
    private static final int ADDRESS = 0x42 ;


    // Commands we will test.
    //
    private static final int COMMAND_RESET        = 0 ;
    private static final int COMMAND_SINGLE_READ  = 1 ;
    private static final int COMMAND_SINGLE_WRITE = 2 ;
    private static final int COMMAND_MULTI_READ   = 3 ;
    private static final int COMMAND_MULTI_WRITE  = 4 ;




    @Test
    @DisplayName( "Direct read using a transactions." )
    void transactionDirectRead() throws InterruptedException ,
                                        IOException          {

        // Opens bus and resets arduino.
        //
        I2CBus bus = new I2CBus( 1 ) ;

        bus.selectSlave( ADDRESS ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeByteDirectly( COMMAND_RESET ) ;

        // Direct read.
        //
        I2CTransaction trx = new I2CTransaction( 1 ) ;

        I2CBuffer buffer = new I2CBuffer( 1 ) ;

        trx.getMessage( 0 ).setAddress( ADDRESS )
                           .setFlags( new I2CFlags().set( I2CFlag.READ ) )
                           .setBuffer( buffer )                            ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trx ) ;

        assertEquals( 42                                           ,
                      buffer.get( 0 )                              ,
                      "Reading a byte directly should provide 42." ) ;
        
        bus.close() ;
    }




    @Test
    @DisplayName( "Single write and read command using a mono-message transactions." )
    void transactionSingle() throws InterruptedException ,
                                    IOException          {

        // Opens bus and resets arduino.
        //
        I2CBus bus = new I2CBus( 1 ) ;

        bus.selectSlave( ADDRESS ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeByteDirectly( COMMAND_RESET ) ;

        // Single write.
        //
        I2CTransaction trxWrite = new I2CTransaction( 1 ) ;

        trxWrite.getMessage( 0 ).setAddress( ADDRESS )
                                .setBuffer( new I2CBuffer( 2 ).set( 0                    ,
                                                                    COMMAND_SINGLE_WRITE )
                                                              .set( 1   ,
                                                                    246 )                  ) ;

        // Single read.
        //
        I2CTransaction trxRequestRead = new I2CTransaction( 1 ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trxWrite ) ;

        trxRequestRead.getMessage( 0 ).setAddress( ADDRESS )
                                      .setBuffer( new I2CBuffer( 1 ).set( 0                   ,
                                                                          COMMAND_SINGLE_READ ) ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trxRequestRead ) ;

        I2CTransaction trxRead = new I2CTransaction( 1 ) ;

        trxRead.getMessage( 0 ).setAddress( ADDRESS )
                               .setFlags( new I2CFlags().set( I2CFlag.READ ) )
                               .setBuffer( new I2CBuffer( 1 ) )                ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trxRead ) ;

        assertEquals( 246                                          ,
                      trxRead.getMessage( 0 ).getBuffer().get( 0 ) ) ;

        bus.close() ;
    }




    @Test
    @DisplayName( "Multi write and read command using mono-messages transactions." )
    void transactionMulti() throws InterruptedException ,
                                   IOException          {
    
        // Opens bus and resets arduino.
        //
        I2CBus bus = new I2CBus( 1 ) ;

        bus.selectSlave( ADDRESS ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeByteDirectly( COMMAND_RESET ) ;

        // Multi write.
        //
        I2CTransaction trxWrite = new I2CTransaction( 1 ) ;

        I2CBuffer bufferWrite = new I2CBuffer( 7 ) ;

        bufferWrite.set( 0                   ,
                         COMMAND_MULTI_WRITE ) ;

        for ( int i = 1 ;
              i < 7     ;
              i += 1    )  bufferWrite.set( i ,
                                            i ) ;

        trxWrite.getMessage( 0 ).setAddress( ADDRESS )
                                .setBuffer( bufferWrite ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trxWrite ) ;

        Thread.sleep( WAIT_MS ) ;

        // Multi read.
        //
        bus.smbus.writeByteDirectly( COMMAND_MULTI_READ ) ;

        I2CTransaction trxRead = new I2CTransaction( 1 ) ;

        I2CBuffer bufferRead = new I2CBuffer( 6 ) ;

        trxRead.getMessage( 0 ).setAddress( ADDRESS )
                               .setFlags( new I2CFlags().set( I2CFlag.READ ) )
                               .setBuffer( bufferRead )                        ;

        Thread.sleep( WAIT_MS ) ;

        bus.doTransaction( trxRead ) ;

        for ( int i = 0 ;
              i < 6     ;
              i += 1    )  assertEquals( i + 1               ,
                                         bufferRead.get( i ) ,
                                         "Bytes set should equal bytes get." ) ;

        bus.close() ;
    }




    @Test
    @DisplayName( "Using SMBUS operations." )
    void smbus() throws InterruptedException ,
                        IOException          {
    
        // Opens bus and resets arduino.
        //
        I2CBus bus = new I2CBus( 1 ) ;

        bus.selectSlave( ADDRESS ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeByteDirectly( COMMAND_RESET ) ;

        // Direct read.
        //
        Thread.sleep( WAIT_MS ) ;

        assertEquals( 42                                           ,
                      bus.smbus.readByteDirectly()                 ,
                      "Reading a byte directly should provide 42." ) ;

        // Single write.
        //
        SMBus.Block block = new SMBus.Block() ;

        block.set( 0   ,
                   246 ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeI2CBlock( COMMAND_SINGLE_WRITE ,
                                 block                ) ;

        // Single read.
        //
        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeByteDirectly( COMMAND_SINGLE_READ ) ;

        Thread.sleep( WAIT_MS ) ;

        assertEquals( 246                                   ,
                      bus.smbus.readByteDirectly()          ,
                      "Reading the byte that has been set." ) ;

        // Multi write.
        //
        int[] bytes = { 45, 46, 47, 48 } ;

        block.clear() ;

        for ( int i = 0        ;
              i < bytes.length ;
              i += 1           )  block.set( i          ,
                                             bytes[ i ] ) ;

        Thread.sleep( WAIT_MS ) ;

        bus.smbus.writeI2CBlock( COMMAND_MULTI_WRITE ,
                                 block               ) ;

        // Direct multi read.
        //
        I2CBuffer buffer = new I2CBuffer( bytes.length ) ;

        Thread.sleep( WAIT_MS ) ;
        bus.smbus.writeByteDirectly( COMMAND_MULTI_READ ) ;

        Thread.sleep( WAIT_MS ) ;
        bus.read( buffer ) ;

        for ( int i = 0         ;
              i < buffer.length ;
              i += 1            )  assertEquals( block.get( i )                    ,
                                                 buffer.get( i )                   ,
                                                 "Bytes read should be bytes set." ) ;

        bus.close() ;
    }
}
