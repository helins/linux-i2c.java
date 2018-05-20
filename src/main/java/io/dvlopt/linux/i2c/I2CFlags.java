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


import io.dvlopt.linux.i2c.I2CFlag ;




public class I2CFlags {


    int value ;



    public I2CFlags() {
    
        clear() ;
    }




    I2CFlags( int value ) {
    
        this.value = value ;
    }




    public boolean isSet( I2CFlag flag ) {
    
        return ( this.value & flag.value ) > 0 ;
    }




    public I2CFlags set( I2CFlag flag ) {

        this.value |= flag.value ;

        return this ;
    }




    public I2CFlags unset( I2CFlag flag ) {
    
        this.value &= ~( flag.value ) ;

        return this ;
    }




    public I2CFlags clear() {
    
        this.value = 0 ;

        return this ;
    }
}
