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


import io.dvlopt.linux.i2c.I2CMessage     ;
import io.dvlopt.linux.i2c.I2CTransaction ;
import org.junit.jupiter.api.DisplayName  ;
import org.junit.jupiter.api.Test         ;




public class I2CTransactionTest {


    @Test
    @DisplayName( "Creating a transaction." )
    void create() {
    
        assertThrows( IllegalArgumentException.class                            ,
                      () -> new I2CTransaction( I2CTransaction.MAX_LENGTH + 1 ) ,
                      "Should not allocate more messages than supported."       ) ;

        I2CTransaction transaction = new I2CTransaction( I2CTransaction.MAX_LENGTH ) ;

        for ( int i = 0                     ;
              i < I2CTransaction.MAX_LENGTH ;
              i += 1                        ) {

            final int index = i ;

            assertDoesNotThrow( () -> transaction.getMessage( index )       ,
                                "Should get message at the requested index" ) ;
        }
    }
}
