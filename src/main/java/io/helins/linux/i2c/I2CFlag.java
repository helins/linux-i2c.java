/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;




/**
 * Enum representing a flag for describing an I2C message.
 *
 * @see I2CMessage
 */
public enum I2CFlag {


    /**
     * Uses 10-bit addressing scheme instead of 7-bit.
     */
    TEN_BIT_ADDRESSING( 0x00000010 ) ,


    /**
     * Is a read operation, not a write.
     */
    READ              ( 0x00000001 ) ,


    /**
     * Does not issue any more START/address after the initial START/address in a combined
     * message.
     * <p>
     * This is a workaround for broken I2C slave devices.
     */
    NO_START          ( 0x00004000 ) ,


    /**
     * Sends a read flag for writes and a write flag for reads.
     * <p>
     * This is a workaround for broken I2C slave devices.
     * <p>
     * Check for the <strong>{@link I2CFunctionality#PROTOCOL_MANGLING protocol mangling functionality}</strong>.
     */
    REVISE_RW_BIT     ( 0x00002000 ) ,

    /**
     * Ignores not acknowledge.
     * <p>
     * Check for the <strong>{@link I2CFunctionality#PROTOCOL_MANGLING protocol mangling functionality}</strong>.
     */
    IGNORE_NAK        ( 0x00001000 ) ,


    /**
     * Ignores read acknowledge.
     * <p>
     * Check for the <strong>{@link I2CFunctionality#PROTOCOL_MANGLING protocol mangling functionality}</strong>.
     */
    NO_READ_ACK       ( 0x00000800 ) ;




    // Internal value of the flag.
    //
    final int value ;




    // Private constructor.
    //
    private I2CFlag( int value ) {
    
        this.value = value ;
    }
}
