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
import io.dvlopt.linux.NativeMemory                    ;
import io.dvlopt.linux.i2c.internal.NativeI2CSmbusData ;




public class I2CBlock {


    public static final int SIZE = NativeI2CSmbusData.SIZE - 2 ;


    private int length ;

    final Memory memory ;





    public I2CBlock() {
    
        this.memory = new Memory( NativeI2CSmbusData.SIZE + 1 ) ;

        this.memory.clear() ;
    }



    
    public I2CBlock clear() {
    
        this.memory.clear() ;

        this.length = 0 ;

        return this ;
    }




    public int length() {
    
        return this.length ;
    }




    int readLength() {
    
        this.length = NativeMemory.getUnsignedByte( this.memory ,
                                                    0           ) ;

        return this.length ;
    }




    void writeLength() {

        this.writeLength( this.length ) ;
    }




    void writeLength( int length ) {

        NativeMemory.setUnsignedByte( this.memory ,
                                      0           ,
                                      length      ) ;

        this.length = length ;
    }




    public int get( int index ) {

        return NativeMemory.getUnsignedByte( this.memory ,
                                             index + 1   ) ;
    }




    public I2CBlock set( int index ,
                         int b     ) {

        index += 1 ;

        NativeMemory.setUnsignedByte( this.memory ,
                                      index       ,
                                      b           ) ;

        this.length = Math.max( this.length ,
                                index       ) ;
    
        return this ;
    }
}
