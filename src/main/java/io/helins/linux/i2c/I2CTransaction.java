/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import com.sun.jna.Memory                              ;
import com.sun.jna.Pointer                             ;
import io.helins.linux.i2c.I2CMessage                  ;
import io.helins.linux.i2c.internal.NativeI2CIoctlData ;
import io.helins.linux.i2c.internal.NativeI2CMessage   ;




/**
 * Class representing an I2C transaction for carrying out several uninterrupted IO operations.
 * <p>
 * Sometimes, only one message per transaction is supported, which defeat the purpose of having
 * transactions.
 */
public class I2CTransaction {


    /**
     * The maximum number of messages a single transaction can carry out in theory.
     */
    public final static int MAX_LENGTH = 42 ;


    // Pointer to native structure.
    //
    Memory memory         ;

    // Pointer to the native array of messages.
    //
    Memory memoryMessages ;

    // Messages as java objects.
    //
    private I2CMessage[] messages ;


    /**
     * How many messages this transaction holds.
     */
    public final int length ;




    /**
     * Creates a new transaction.
     *
     * @param  length
     *           Number of messages.
     *
     * @throws IllegalArgumentException
     *           When the request number of messages is higher than what is supported.
     *
     * @see    #MAX_LENGTH
     */
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




    /**
     * Retrieves a message from this transaction for inspection of modification.
     *
     * @param  index
     *           Which one.
     *
     * @return The relevant message.
     */
    public I2CMessage getMessage( int index ) {
    
        return this.messages[ index ] ;
    }
}
