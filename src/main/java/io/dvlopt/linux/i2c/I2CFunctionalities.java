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


import io.dvlopt.linux.i2c.I2CFunctionality ;




/**
 * Class holding the functionalities of an I2C bus.
 */
public class I2CFunctionalities {


    private final int flags ;




    I2CFunctionalities( int flags ) {
    
        this.flags = flags ;
    }




    /**
     * Can the I2C bus do this ?
     *
     * @param functionality  The tested functionality.
     *
     * @return  A boolean.
     */
    public boolean can( I2CFunctionality functionality ) {
    
        return ( this.flags & functionality.value ) != 0 ;
    }
}
