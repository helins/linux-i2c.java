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


import io.dvlopt.linux.i2c.I2CFlags      ;
import io.dvlopt.linux.i2c.I2CFlag       ;
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
