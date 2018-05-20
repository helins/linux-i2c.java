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


import com.sun.jna.Memory                              ;
import com.sun.jna.Pointer                             ;
import io.dvlopt.linux.i2c.I2CMessage                  ;
import io.dvlopt.linux.i2c.internal.NativeI2CIoctlData ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage   ;




public class I2CTransaction {


    public final static int MAX_LENGTH = 42 ;


    Memory memory         ;
    Memory memoryMessages ;

    private I2CMessage[] messages ;

    public final int length ;




    public I2CTransaction( int length ) {
    
        if ( length > MAX_LENGTH ) {
        
            throw new IllegalArgumentException( "The requested number of messages is higher than what is supported" ) ;
        }

        this.memoryMessages = new Memory( length * NativeI2CMessage.SIZE ) ;

        this.memoryMessages.clear() ;

        this.memory = new Memory( NativeI2CIoctlData.SIZE ) ;

        this.memory.setPointer( NativeI2CIoctlData.OFFSET_MESSAGES ,
                                this.memoryMessages                ) ;

        this.memory.setInt( NativeI2CIoctlData.OFFSET_LENGTH ,
                            length                           ) ;

        this.messages = new I2CMessage[ length ] ;

        for ( int i = 0  ;
              i < length ;
              i += 1     ) {

            this.messages[ i ] = new I2CMessage( this.memoryMessages.share( i * NativeI2CMessage.SIZE ) ) ;
        }

        this.length = length ;
    }




    public I2CMessage getMessage( int index ) {
    
        return this.messages[ index ] ;
    }
}
