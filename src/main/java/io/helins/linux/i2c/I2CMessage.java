/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import com.sun.jna.Pointer                           ;
import io.helins.linux.NativeMemory                  ;
import io.helins.linux.i2c.I2CBuffer                 ;
import io.helins.linux.i2c.I2CFlags                  ;
import io.helins.linux.i2c.internal.NativeI2CMessage ;




/**
 * Class representing a message in an I2C transaction.
 *
 * @see I2CTransaction
 */
public class I2CMessage {


    // Pointer to the native structure.
    //
    Pointer ptr ;

    // Associated I2C buffer.
    //
    private I2CBuffer buffer ;





    // Private constructor.
    // I2C messages can only be obtained through a transaction.
    //
    I2CMessage( Pointer ptr ) {
    
        this.ptr = ptr ;
    }




    /**
     * Retrieves the address of the slave device this message is intented for.
     *
     * @return  The address.
     */
    public int getAddress() {

        return NativeMemory.getUnsignedShort( this.ptr                        ,
                                              NativeI2CMessage.OFFSET_ADDRESS ) ;
    }




    /**
     * Sets the address of the slave device this message is intended for.
     *
     * @param address The address of the slave.
     *
     * @return  This instance.
     */
    public I2CMessage setAddress( int address ) {
    
        NativeMemory.setUnsignedShort( this.ptr                        ,
                                       NativeI2CMessage.OFFSET_ADDRESS ,
                                       address                         ) ;

        return this ;
    }




    /**
     * Retrieves the flags describing this message.
     *
     * @return  The flags.
     */
    public I2CFlags getFlags() {
    
        return new I2CFlags( NativeMemory.getUnsignedShort( this.ptr                      ,
                                                            NativeI2CMessage.OFFSET_FLAGS ) ) ;
    }




    /**
     * Sets the flags for describing this message.
     *
     * @param flags  The flags.
     *
     * @return  This instance.
     */
    public I2CMessage setFlags( I2CFlags flags ) {

        NativeMemory.setUnsignedShort( this.ptr                      ,
                                       NativeI2CMessage.OFFSET_FLAGS ,
                                       flags.value                   ) ;

        return this ;
    }




    /**
     * Retrieves the buffer of this message.
     *
     * @return  The buffer.
     */
    public I2CBuffer getBuffer() {

        return this.buffer ;
    }




    /**
     * Sets the buffer of this message.
     *
     * @param buffer  The buffer.
     *
     * @return  This instance.
     */
    public I2CMessage setBuffer( I2CBuffer buffer ) {
    
        this.ptr.setPointer( NativeI2CMessage.OFFSET_BUFFER ,
                             buffer.memory                  ) ;

        NativeMemory.setUnsignedShort( this.ptr                       ,
                                       NativeI2CMessage.OFFSET_LENGTH ,
                                       buffer.length                  ) ;

        this.buffer = buffer ;

        return this ;
    }
}
