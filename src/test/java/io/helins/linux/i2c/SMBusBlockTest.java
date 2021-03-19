/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.SMBus         ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class SMBusBlockTest {


    @Test
    @DisplayName( "Getting and setting bytes." )
    void getSet() {
    
        SMBus.Block block = new SMBus.Block() ;

        for ( int i = 0            ;
              i < SMBus.Block.SIZE ;
              i += 1               ) {

            block.set( i ,
                       i ) ;

            assertEquals( block.length()               ,
                          i + 1                        ,
                          "Length should be adjusted." ) ;

            assertEquals( block.get( i )                    ,
                          i                                 ,
                          "Byte get should equal byte set." ) ;
        }
    }




    @Test
    @DisplayName( "Testing that length is being adjusted properly." )
    void length() {
    
        final SMBus.Block block = new SMBus.Block()    ;
        final int         index = SMBus.Block.SIZE - 1 ;
        final int         size  = index + 1            ;

        assertEquals( block.length()                  ,
                      0                               ,
                      "At first, length should be 0." ) ;


        block.set( index ,
                   244   ) ;

        assertEquals( block.length()               ,
                      size                         ,
                      "Length should be adjusted." ) ;

        block.set( index - 1 ,
                   244       ) ;

        assertEquals( block.length()                   ,
                      size                             ,
                      "Length should remain the same." ) ;

        block.clear() ;

        assertEquals( block.length()                                      ,
                      0                                                   ,
                      "Length should become 0 after clearing the buffer." ) ;
    }
}
