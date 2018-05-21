/*
 * Copyright 2018 Adam Helinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.dvlopt.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.dvlopt.linux.i2c.I2CBlock      ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class I2CBlockTest {


    @Test
    @DisplayName( "Getting and setting bytes." )
    void getSet() {
    
        I2CBlock block = new I2CBlock() ;

        for ( int i = 0         ;
              i < I2CBlock.SIZE ;
              i += 1            ) {

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
    
        final I2CBlock block = new I2CBlock()    ;
        final int      index = I2CBlock.SIZE - 1 ;
        final int      size  = index + 1         ;

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
