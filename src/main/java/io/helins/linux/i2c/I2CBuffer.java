/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import com.sun.jna.Memory           ;
import io.helins.linux.NativeMemory ;




/**
 * Class representing a buffer of bytes for directly reading or writing to a slave device.
 */
public class I2CBuffer {


    // Pointer to native structure.
    //
    final Memory memory ;

    /**
     * The length of this buffer.
     */
    public final int length ;




    /**
     * Allocates a buffer.
     *
     * @param length  The number of bytes.
     */
    public I2CBuffer( int length ) {
    
        this.memory = new Memory( length ) ;
        this.length = length               ;

        this.memory.clear() ;
    }




    /**
     * Retrieves the byte at the given position.
     *
     * @param index  Which byte.
     *
     * @return  An unsigned byte.
     */
    public int get( int index ) {

        return NativeMemory.getUnsignedByte( this.memory ,
                                             index       ) ;
    }




    /**
     * Sets the byte at the given position.
     *
     * @param index  Where.
     *
     * @param b  An unsigned byte.
     *
     * @return  This instance.
     */
    public I2CBuffer set( int index ,
                          int b     ) {

        NativeMemory.setUnsignedByte( this.memory   ,
                                      index         ,
                                      b             ) ;

        return this ;
    }




    /**
     * Fills the buffer with 0.
     *
     * @return  This instance.
     */
    public I2CBuffer clear() {
    
        this.memory.clear() ;

        return this ;
    }
}
