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
 * See the License  for the specific language governing permissions and
 * limitations under the License.
 */


package io.dvlopt.linux.i2c ;




/**
 * Enum representing the functionalities an I2C bus can have.
 */
public enum I2CFunctionality {


    /**
     * Can do transactions.
     * <p>
     * Sometimes a master device can do transaction containing only one message but not several, beware.
     *
     * @see I2CBus#doTransaction( I2CTransaction )
     */
    TRANSACTIONS       ( 0x00000001 ) ,


    /**
     * Can use 10-bit slave addresses.
     *
     * @see I2CBus#selectSlave( int, boolean, boolean )
     */
    TEN_BIT_ADDRESSING ( 0x00000002 ) ,


    /**
     * Can use special flags in transactions.
     *
     * @see I2CFlag
     */
    PROTOCOL_MANGLING  ( 0x00000004 ) ,


    /**
     * Can use packet error checking in SMBUS operations.
     *
     * @see I2CBus#usePEC( boolean )
     */
    SMBUS_PEC          ( 0x00000008 ) ,


    /**
     * Can do SMBUS quick operation.
     *
     * @see I2CBus#quick( boolean )
     */
    QUICK              ( 0x00010000 ) ,


    /**
     * Can do SMBUS read byte directly.
     *
     * @see I2CBus#readByteDirectly()
     */
    READ_BYTE_DIRECTLY ( 0x00020000 ) ,


    /**
     * Can do SMBUS read byte.
     *
     * @see I2CBus#readByte( int )
     */
    READ_BYTE          ( 0x00080000 ) ,


    /**
     * Can do SMBUS read word.
     *
     * @see I2CBus#readWord( int )
     */
    READ_WORD          ( 0x00200000 ) ,


    /**
     * Can do SMBUS read block.
     *
     * @see I2CBus#readBlock( int, I2CBlock )
     */
    READ_BLOCK         ( 0x01000000 ) ,


    /**
     * Can do SMBUS read I2C block.
     *
     * @see I2CBus#readI2CBlock( int, I2CBlock, int )
     */
    READ_I2C_BLOCK     ( 0x04000000 ) ,


    /**
     * Can do SMBUS write byte directly.
     *
     * @see I2CBus#writeByteDirectly( int )
     */
    WRITE_BYTE_DIRECTLY( 0x00040000 ) ,


    /**
     * Can do SMBUS write byte.
     *
     * @see I2CBus#writeByte( int, int )
     */
    WRITE_BYTE         ( 0x00100000 ) ,


    /**
     * Can do SMBUS write word.
     *
     * @see I2CBus#writeWord( int, int )
     */
    WRITE_WORD         ( 0x00400000 ) ,


    /**
     * Can do SMBUS write block.
     *
     * @see I2CBus#writeBlock( int, I2CBlock )
     */
    WRITE_BLOCK        ( 0x08000000 ) ,


    /**
     * Can do SMBUS I2C write block.
     *
     * @see I2CBus#writeI2CBlock( int, I2CBlock )
     */
    WRITE_I2C_BLOCK    ( 0x02000000 ) ,


    /**
     * Can do SMBUS process call.
     *
     * @see I2CBus#processCall( int, int )
     */
    PROCESS_CALL       ( 0x00800000 ) ,


    /**
     * Can do SMBUS block process call.
     *
     * @see I2CBus#blockProcessCall( int, I2CBlock, I2CBlock )
     */
    BLOCK_PROCESS_CALL ( 0x00008000 ) ;




    final int value ;




    private I2CFunctionality( int value ) {
    
        this.value = value ;
    }
}
