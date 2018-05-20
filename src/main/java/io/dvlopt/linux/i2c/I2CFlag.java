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




public enum I2CFlag {


    TEN         ( 0x00000010 ) ,
    READ        ( 0x00000001 ) ,
    NO_START    ( 0x00004000 ) ,
    REV_DIR_ADDR( 0x00002000 ) ,
    IGNORE_NAK  ( 0x00001000 ) ,
    NO_READ_ACK ( 0x00000800 ) ;




    final int value ;




    private I2CFlag( int value ) {
    
        this.value = value ;
    }
}
