/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.I2CFlags      ;
import io.helins.linux.i2c.I2CFlag       ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class I2CFlagsTest {


    private static I2CFlag[] FLAGS = I2CFlag.values() ;




    @Test
    @DisplayName( "Setting and unsetting flags." )
    void setUnset() {
    
        I2CFlags flags = new I2CFlags() ;

        for ( I2CFlag flag : FLAGS ) {
        
            flags.set( flag ) ;

            assertTrue( flags.isSet( flag )   ,
                        "Flag should be set." ) ;

            flags.unset( flag ) ;

            assertFalse( flags.isSet( flag )     ,
                         "Flag should be unset." ) ;
        }
    }




    @Test
    @DisplayName( "Clearing flags." )
    void clear() {
    
        I2CFlags flags = new I2CFlags() ;

        for ( I2CFlag flag : FLAGS )  flags.set( flag ) ;

        flags.clear() ;

        for (I2CFlag flag : FLAGS )  assertFalse( flags.isSet( flag )     ,
                                                  "Flag should be unset." ) ;
    }
}
