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




/**
 * Class representing a block of bytes for SMBUS operations.
 * <p>
 * Such a buffer is limited to 32 bytes.
 */
public class I2CBlock {


    /**
     * The length of a buffer.
     */
    public static final int SIZE = NativeI2CSmbusData.SIZE - 2 ;


    private int length ;

    final Memory memory ;





    /**
     * Allocates a block.
     */
    public I2CBlock() {
    
        this.memory = new Memory( NativeI2CSmbusData.SIZE + 1 ) ;

        this.memory.clear() ;
    }



    
    /**
     * Fills the block with 0.
     *
     * @return  This instance.
     */
    public I2CBlock clear() {
    
        this.memory.clear() ;

        this.length = 0 ;

        return this ;
    }




    /**
     * Retrieves the current length of this block.
     * <p>
     * Setting a byte to an index adjusts the length of the block to the
     * highest index.
     *
     * @return The length.
     *
     * @see #set( int, int )
     */
    public int length() {
    
        return this.length ;
    }




    // Retrieves and syncs the length set by a native SMBUS call.
    //
    int readLength() {
    
        this.length = NativeMemory.getUnsignedByte( this.memory ,
                                                    0           ) ;

        return this.length ;
    }




    // Syncs the length for a native SMBUS call.
    //
    void writeLength() {

        this.writeLength( this.length ) ;
    }




    // Writes and syncs the length for a native SMBUS call.
    //
    void writeLength( int length ) {

        NativeMemory.setUnsignedByte( this.memory ,
                                      0           ,
                                      length      ) ;

        this.length = length ;
    }




    /**
     * Retrieves the byte at the given position.
     *
     * @param index  Where.
     *
     * @return  An unsigned byte.
     */
    public int get( int index ) {

        return NativeMemory.getUnsignedByte( this.memory ,
                                             index + 1   ) ;
    }




    /**
     * Sets the byte at the given position.
     * <p>
     * The length of this block will be adjusted to reflect the highest index
     * of a byte set by the user or by an SMBUS operation.
     *
     * @param index  Which one.
     *
     * @param b  An unsigned byte.
     *
     * @return  This instance.
     */
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
