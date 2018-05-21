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


import io.dvlopt.linux.i2c.I2CBuffer      ;
import io.dvlopt.linux.i2c.I2CFlag        ;
import io.dvlopt.linux.i2c.I2CFlags       ;
import io.dvlopt.linux.i2c.I2CMessage     ;
import io.dvlopt.linux.i2c.I2CTransaction ;
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

        I2CFlags flags = new I2CFlags().set( I2CFlag.READ     )
                                       .set( I2CFlag.TEN      )
                                       .set( I2CFlag.NO_START ) ;

        message.setFlags( flags ) ;

        assertEquals( message.getFlags()               ,
                      flags                            ,
                      "Flags should be properly set. " ) ;
    }
}
