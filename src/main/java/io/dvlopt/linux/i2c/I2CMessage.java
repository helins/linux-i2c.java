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


import com.sun.jna.Pointer                           ;
import io.dvlopt.linux.NativeMemory                  ;
import io.dvlopt.linux.i2c.I2CBuffer                 ;
import io.dvlopt.linux.i2c.I2CFlags                  ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage ;




public class I2CMessage {


    Pointer ptr ;

    private I2CBuffer buffer ;





    I2CMessage( Pointer ptr ) {
    
        this.ptr = ptr ;
    }




    public int getAddress() {

        return NativeMemory.getUnsignedShort( this.ptr                        ,
                                              NativeI2CMessage.OFFSET_ADDRESS ) ;
    }




    public I2CMessage setAddress( int address ) {
    
        NativeMemory.setUnsignedShort( this.ptr                        ,
                                       NativeI2CMessage.OFFSET_ADDRESS ,
                                       address                         ) ;

        return this ;
    }




    public I2CFlags getFlags() {
    
        return new I2CFlags( NativeMemory.getUnsignedShort( this.ptr                      ,
                                                            NativeI2CMessage.OFFSET_FLAGS ) ) ;
    }




    public I2CMessage setFlags( I2CFlags flags ) {

        NativeMemory.setUnsignedShort( this.ptr                      ,
                                       NativeI2CMessage.OFFSET_FLAGS ,
                                       flags.value                   ) ;

        return this ;
    }




    public I2CBuffer getBuffer() {

        return this.buffer ;
    }




    public I2CMessage setBuffer( I2CBuffer buffer ) {

        this.buffer = buffer ;
    
        this.ptr.setPointer( NativeI2CMessage.OFFSET_BUFFER ,
                             buffer.memory                  ) ;

        NativeMemory.setUnsignedShort( this.ptr                       ,
                                       NativeI2CMessage.OFFSET_LENGTH ,
                                       buffer.length                  ) ;

        return this ;
    }
}
