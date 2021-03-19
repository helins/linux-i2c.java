/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.I2CBuffer      ;
import io.helins.linux.i2c.I2CFlag        ;
import io.helins.linux.i2c.I2CFlags       ;
import io.helins.linux.i2c.I2CMessage     ;
import io.helins.linux.i2c.I2CTransaction ;
import org.junit.jupiter.api.DisplayName  ;
import org.junit.jupiter.api.Test         ;




public class I2CMessageTest {


    @Test
    @DisplayName( "Setting and getting properties." )
    void getSet() {
    
        final I2CTransaction transaction = new I2CTransaction( 1 )     ;
        final I2CMessage     message     = transaction.getMessage( 0 ) ; 

        assertEquals( message.getAddress()             ,
                      0                                ,
                      "At first, address should be 0." ) ;

        message.setAddress( 42 ) ;

        assertEquals( message.getAddress()              ,
                      42                                ,
                      "Address should be properly set." ) ;

        assertNull( message.getBuffer()                ,
                    "At first, buffer should be null." ) ;

        I2CBuffer buffer = new I2CBuffer( 256 ) ;

        message.setBuffer( buffer ) ;

        assertEquals( message.getBuffer()              ,
                      buffer                           ,
                      "Buffer should be properly set." ) ;

        I2CFlags flags = new I2CFlags().set( I2CFlag.READ               )
                                       .set( I2CFlag.TEN_BIT_ADDRESSING )
                                       .set( I2CFlag.NO_START           ) ;

        message.setFlags( flags ) ;

        assertEquals( message.getFlags()               ,
                      flags                            ,
                      "Flags should be properly set. " ) ;
    }
}
