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


import io.dvlopt.linux.i2c.I2CBuffer     ;
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
