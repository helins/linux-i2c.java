/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.I2CMessage     ;
import io.helins.linux.i2c.I2CTransaction ;
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
