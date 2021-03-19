/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.I2CBuffer     ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class I2CBufferTest {


    @Test
    @DisplayName( "Getting and setting bytes." )
    void getSet() {
    
        I2CBuffer buffer = new I2CBuffer( 255 ) ;

        for ( int i = 0         ;
              i < buffer.length ;
              i += 1            ) {
              
            buffer.set( i ,
                        i ) ;

            assertEquals( buffer.get( i )                               ,
                          i                                             ,
                          "Byte get should equal to what has been set." ) ;
        }
    }
}
