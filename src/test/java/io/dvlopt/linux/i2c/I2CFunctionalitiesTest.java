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


import io.dvlopt.linux.i2c.I2CFunctionalities ;
import io.dvlopt.linux.i2c.I2CFunctionality   ;
import org.junit.jupiter.api.DisplayName ;
import org.junit.jupiter.api.Test        ;




public class I2CFunctionalitiesTest {


    private static I2CFunctionality[] FUNCTIONALITIES = I2CFunctionality.values() ;




    @Test
    @DisplayName( "" )
    void can() {
    
        int value = 0 ;

        for ( I2CFunctionality functionality : FUNCTIONALITIES )  value |= functionality.value ;

        I2CFunctionalities functionalities = new I2CFunctionalities( value ) ;

        for ( I2CFunctionality functionality : FUNCTIONALITIES )  assertTrue( functionalities.can( functionality ) ,
                                                                              "Functionality should be available." ) ;  
    }
}
