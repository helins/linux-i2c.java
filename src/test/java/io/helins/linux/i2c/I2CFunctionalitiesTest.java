/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import static org.junit.jupiter.api.Assertions.* ;


import io.helins.linux.i2c.I2CFunctionalities ;
import io.helins.linux.i2c.I2CFunctionality   ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class I2CFunctionalitiesTest {


    private static I2CFunctionality[] FUNCTIONALITIES = I2CFunctionality.values() ;




    @Test
    @DisplayName( "Reading functionalities." )
    void can() {
    
        int value = 0 ;

        for ( I2CFunctionality functionality : FUNCTIONALITIES )  value |= functionality.value ;

        I2CFunctionalities functionalities = new I2CFunctionalities( value ) ;

        for ( I2CFunctionality functionality : FUNCTIONALITIES )  assertTrue( functionalities.can( functionality ) ,
                                                                              "Functionality should be available." ) ;  
    }
}
