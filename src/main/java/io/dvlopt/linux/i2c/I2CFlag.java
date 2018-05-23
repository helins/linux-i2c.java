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




    final int value ;




    private I2CFlag( int value ) {
    
        this.value = value ;
    }
}
