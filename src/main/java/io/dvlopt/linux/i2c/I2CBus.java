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


import com.sun.jna.Memory                                   ;
import com.sun.jna.NativeLong                               ;
import com.sun.jna.Pointer                                  ;
import com.sun.jna.ptr.LongByReference                      ;
import io.dvlopt.linux.LinuxException                       ;
import io.dvlopt.linux.NativeMemory                         ;
import io.dvlopt.linux.SizeT                                ;
import io.dvlopt.linux.i2c.I2CBlock                         ;
import io.dvlopt.linux.i2c.I2CFunctionalities               ;
import io.dvlopt.linux.i2c.I2CTransaction                   ;
import io.dvlopt.linux.i2c.internal.NativeI2CSmbusData      ;
import io.dvlopt.linux.i2c.internal.NativeI2CSmbusIoctlData ;
import io.dvlopt.linux.io.LinuxIO                           ;




/**
 * Class representing an I2C bus.
 * <p>
 * It provides 3 sets of operations for doing IO :
 * <ul>
 *     <li>Directly reading and writing by using <strong>{@link #read( I2CBuffer ) read}</strong> and
 *         <strong>{@link #write( I2CBuffer ) write}</strong>.</li>
 *     <li>Doing <strong>{@link #doTransaction( I2CTransaction ) transactions}</strong> (ie. uninterrupted reads and writes)</li>
 *     <li>Using SMBUS operations as defined in the standard (all the other IO functions).</li>
 * </ul>
 * <p>
 * SMBUS operations are a subset of what I2C can achieve but propose common interactions. However, <strong>
 * {@link #getFunctionalities() getFunctionnalities}</strong> should be used in order to understand what operations the underlying
 * driver support.
 * <p>
 * I2C is a standard but your master device and the slaves devices it talks do probably do not support everything properly.
 * For instance, as is, the Raspberry Pi only supports a few SMBUS operations and do not support transactions involving more than
 * one message at a time (which defeats the purpose of having them in the first place). Furthermore, talking to an Arduino will result
 * in timing issues if it is too slow to answer. Communication between those two is very much possible but requires more preparation and
 * testing. This is an example of how things are not always that simple.
 * <p>
 * In this API, `command` is just another word for what is sometimes called `register`. Essentially, it means that before reading or
 * writing bytes, the master writes a byte specifying a command, something to do with the following byte(s). If an SMBUS functionality
 * is not available, this can often be emulated by explicitly sending the command byte and then reading or writing as needed.
 *
 * @see <a href="https://www.kernel.org/doc/Documentation/i2c/smbus-protocol">SMBUS operations</a>
 */
public class I2CBus implements AutoCloseable {


    private final static NativeLong I2C_RETRIES     = new NativeLong( 0x0701L ,
                                                                      true    ) ;

    private final static NativeLong I2C_TIMEOUT     = new NativeLong( 0x0702L ,
                                                                      true    ) ;

    private final static NativeLong I2C_SLAVE       = new NativeLong( 0x0703L ,
                                                                      true    ) ;

    private final static NativeLong I2C_SLAVE_FORCE = new NativeLong( 0x0706L ,
                                                                      true    ) ;

    private final static NativeLong I2C_TENBIT      = new NativeLong( 0x0704L ,
                                                                      true    ) ;

    private final static NativeLong I2C_FUNCS       = new NativeLong( 0x0705L ,
                                                                      true    ) ;

    private final static NativeLong I2C_RDWR        = new NativeLong( 0x0707L ,
                                                                      true    ) ;

    private final static NativeLong I2C_PEC         = new NativeLong( 0x0708L ,
                                                                      true    ) ;

    private final static NativeLong I2C_SMBUS       = new NativeLong( 0x0720L ,
                                                                      true    ) ;


    private final static byte I2C_SMBUS_READ  = 1 ; 
    private final static byte I2C_SMBUS_WRITE = 0 ;


    private final static byte I2C_SMBUS_QUICK            = 0 ;
    private final static byte I2C_SMBUS_BYTE             = 1 ;
    private final static byte I2C_SMBUS_BYTE_DATA        = 2 ;
    private final static byte I2C_SMBUS_WORD_DATA        = 3 ;
    private final static byte I2C_SMBUS_PROC_CALL        = 4 ;
    private final static byte I2C_SMBUS_BLOCK_DATA       = 5 ;
    private final static byte I2C_SMBUS_I2C_BLOCK_BROKEN = 6 ;
    private final static byte I2C_SMBUS_BLOCK_PROC_CALL  = 7 ;
    private final static byte I2C_SMBUS_I2C_BLOCK_DATA   = 8 ;




    private final int    fd                ;
    private final Memory i2cSmbusIoctlData ;
    private final Memory i2cSmbusData      ;

    private boolean isTenBit = false ;




    /**
     * Opens an I2C bus by number, `/dev/i2c-$busNumber`.
     *
     * @param busNumber  The number of the bus.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public I2CBus( int busNumber ) throws LinuxException {
    
        this( "/dev/i2c-" + busNumber ) ;
    }




    /**
     * Opens an I2C bus on the given path.
     *
     * @param path  A path to the bus such as `/dev/i2c-1`.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public I2CBus( String path ) throws LinuxException {
    

        this.fd = LinuxIO.open64( path           ,
                                  LinuxIO.O_RDWR ) ;

        if ( this.fd < 0 ) {
        
            throw new LinuxException( "Unable to open an I2C bus at the given path" ) ;
        }

        this.i2cSmbusIoctlData = new Memory( NativeI2CSmbusIoctlData.SIZE ) ;
        this.i2cSmbusData      = new Memory( NativeI2CSmbusData.SIZE )      ;

        this.i2cSmbusIoctlData.clear() ;
        this.i2cSmbusData.clear()      ;
    }




    /**
     * Closes this bus.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void close() throws LinuxException {
    
        if ( LinuxIO.close( this.fd ) != 0 ) {
        
            throw new LinuxException( "Unable to close this I2C bus" ) ;
        }
    }




    /**
     * Do an uninterrupted transaction of several messages.
     * <p>
     * Not all devices support this feature, or support only one message at a time which
     * is not very interesting.
     *
     * @param transaction  The transaction that will be carried out.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void doTransaction( I2CTransaction transaction ) throws LinuxException {

        if ( LinuxIO.ioctl( this.fd            ,
                            I2C_RDWR           ,
                            transaction.memory ) < 0 ) {
        
            throw new LinuxException( "Unable to fully perform requested I2C transaction" ) ;
        }
    }




    /**
     * Finds out what this I2C bus can do.
     *
     * @return  Functionalities.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public I2CFunctionalities getFunctionalities() throws LinuxException {
    
        LongByReference longRef = new LongByReference() ;

        if ( LinuxIO.ioctl( this.fd              ,
                            I2C_FUNCS            ,
                            longRef.getPointer() ) < 0 ) {
        
            throw new LinuxException( "Unable to get the I2C functionalities of this bus" ) ;
        }

        return new I2CFunctionalities( (int)( longRef.getValue() ) ) ;
    }




    /**
     * Selects an available slave device using a regular 7-bit address.
     *
     * @param address  The address of the needed slave.
     *
     * @throws LinuxException  When something fails on the native side.
     *
     * @see #selectSlave( int, boolean, boolean )
     */
    public void selectSlave( int address ) throws LinuxException {

        this.selectSlave( address ,
                          false   ,
                          false   ) ;
    }




    /**
     * Selects an available slave device.
     * <p>
     * This needs to be called only once if several IO operations are performed on the same
     * slave and has no effect on <strong>{@link #doTransaction(I2CTransaction) transactions}</strong>.
     *
     * @param address  The address of the needed slave.
     *
     * @param force  Should the slave be selected even if it is already used somewhere else ?
     *
     * @param isTenBit  Is the given address in 10-bit resolution ?
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void selectSlave( int     address  ,
                             boolean force    ,
                             boolean isTenBit ) throws LinuxException {

        if ( isTenBit ) {
        
            if ( this.isTenBit == false ) {
            
                if ( LinuxIO.ioctl( this.fd    ,
                                    I2C_TENBIT ,
                                    1          ) < 0 ) {
                
                    throw new LinuxException( "Unable to set 10 bit addressing" ) ;
                }

                this.isTenBit = true ;
            }
        }

        else if ( this.isTenBit ) {
        
            if ( LinuxIO.ioctl( this.fd    ,
                                I2C_TENBIT ,
                                0          ) < 0 ) {
            
                throw new LinuxException( "Unable to set 7 bit addressing" ) ;
            }

            this.isTenBit = false ;
        }
        
        if ( LinuxIO.ioctl( this.fd                 ,
                            force ? I2C_SLAVE_FORCE
                                  : I2C_SLAVE       ,
                              address  
                            & 0xffffffffL           ) < 0 ) {
                            
            throw new LinuxException( "Unable to use the given slave address" ) ;
        }
    }




    /**
     * Sets the number of time a slave should be polled when not acknowledging.
     * <p>
     * Does not always have an effect depending on the underlying driver.
     *
     * @param nRetries the number of retries.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void setRetries( int nRetries ) throws LinuxException {

        if ( LinuxIO.ioctl( this.fd       ,
                            I2C_RETRIES   ,
                              nRetries
                            & 0xffffffffL ) < 0 ) {

            throw new LinuxException( "Unable to set the number of retries" ) ;
        }
    }




    /**
     * Sets the timeout in milliseconds.
     * <p>
     * Does not always have an effect depending on the underlying driver.
     *
     * @param milliseconds  The timeout in milliseconds.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void setTimeout( int milliseconds ) throws LinuxException {
    
        if ( LinuxIO.ioctl( this.fd                 ,
                            I2C_TIMEOUT             ,
                              ( milliseconds / 10 )
                            & 0xffffffL             ) < 0 ) {

            throw new LinuxException( "Unable to set timeout" ) ;
        }
    }




    /**
     * Enables or disables packet error checking for SMBUS commands.
     * <p>
     * Is ignored unless the underlying driver provides this <strong>{@link #getFunctionalities() functionality}</strong>.
     * <p>
     * Slave devices do not necessarely support this feature either.
     *
     * @param usePEC  Should PEC be enabled ?
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void usePEC( boolean usePEC ) throws LinuxException {
    
        if ( LinuxIO.ioctl( this.fd     ,
                            I2C_PEC     ,
                            usePEC ? 1L
                                   : 0L ) < 0 ) {

            throw new LinuxException( "Unable to set or unset PEC for SMBUS operations" ) ;
        }
    }




    // Performs an SMBUS command.
    //
    private int i2cSmbusAccess( byte    readWrite ,
                                int     command   ,
                                int     size      ,
                                Pointer data      ) throws LinuxException {

        this.i2cSmbusIoctlData.setByte( NativeI2CSmbusIoctlData.OFFSET_READ_WRITE ,
                                        readWrite                                 ) ;

        this.i2cSmbusIoctlData.setByte( NativeI2CSmbusIoctlData.OFFSET_COMMAND ,
                                        (byte)command                          ) ;

        this.i2cSmbusIoctlData.setInt( NativeI2CSmbusIoctlData.OFFSET_SIZE ,
                                       size                                ) ;

        this.i2cSmbusIoctlData.setPointer( NativeI2CSmbusIoctlData.OFFSET_DATA ,
                                           data                                ) ;

        int result = LinuxIO.ioctl( this.fd                ,
                                    I2C_SMBUS              ,
                                    this.i2cSmbusIoctlData ) ;

        if ( result < 0 ) {
        
            throw new LinuxException( "Unable to perform I2C/SMBUS operation" ) ;
        }

        return result ;
    }




    /**
     * SMBUS operation sending only the READ or WRITE bit, no data is carried.
     *
     * @param isWrite  True if the WRITE bit must be set.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void quick( boolean isWrite ) throws LinuxException {

        this.i2cSmbusAccess( isWrite ? I2C_SMBUS_WRITE
                                     : I2C_SMBUS_READ  ,
                             0                         ,
                             I2C_SMBUS_QUICK           ,
                             null                      ) ;
    }




    /**
     * SMBUS operation reading a byte without a command.
     *
     * @return  The unsigned byte received from the slave.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int readByteDirectly() throws LinuxException {

        this.i2cSmbusAccess( I2C_SMBUS_READ    ,
                             0                 ,
                             I2C_SMBUS_BYTE    ,
                             this.i2cSmbusData ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    /**
     * SMBUS operation reading a byte after specifying a command.
     *
     * @param command  The command.
     *
     * @return  The unsigned byte received from the slave.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int readByte( int command ) throws LinuxException {
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_BYTE_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    /**
     * SMBUS operation reading a short after specifying a command.
     *
     * @param command  The command.
     *
     * @return  The unsigned short received from the slave.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int readWord( int command ) throws LinuxException {
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_WORD_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedShort( this.i2cSmbusData ,
                                              0                 ) ;
    }




    /**
     * SMBUS operation reading several bytes after specifying a command.
     *
     * @param command  The command.
     *
     * @param block  Block of bytes the answer will be written to.
     *
     * @return  The number of bytes read.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int readBlock( int      command ,
                          I2CBlock block   ) throws LinuxException {
    
       this.i2cSmbusAccess( I2C_SMBUS_READ       ,
                            command              ,
                            I2C_SMBUS_BLOCK_DATA ,
                            block.memory         ) ;

       return block.readLength() ;
    }




    /**
     * SMBUS-like operation reading several bytes after specifying a command where the length
     * is part of the message.
     * <p>
     * This operation is not in the SMBUS standard but is often supported nonetheless.
     *
     * @param command  The command.
     *
     * @param block  Block of bytes the answer will be written to.
     *
     * @param length  How many bytes should be read.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void readI2CBlock( int      command ,
                              I2CBlock block   ,
                              int      length  ) throws LinuxException {

        if ( length > I2CBlock.SIZE ) {
        
            throw new IllegalArgumentException( "Too many bytes requested." ) ;
        }
    
        block.writeLength( length ) ;

        this.i2cSmbusAccess( I2C_SMBUS_READ                            ,
                             command                                   ,
                             length == 32 ? I2C_SMBUS_I2C_BLOCK_BROKEN
                                          : I2C_SMBUS_I2C_BLOCK_DATA   ,
                             block.memory                              ) ;

        block.readLength() ;
    }




    /**
     * Directly reads bytes from the slave device (length is specified by the buffer).
     *
     * @param buffer  Buffer the answer will be written to.
     *
     * @throws LinuxException  When something fails on the native side.
     *
     * @see #read( I2CBuffer, int )
     */
    public void read( I2CBuffer buffer ) throws LinuxException {
    
        this.read( buffer        ,
                   buffer.length ) ;
    }




    /**
     * Directly reads <strong>length</strong> bytes from the slave device.
     *
     * @param buffer  Buffer the answer will be written to.
     *
     * @param length  The number of bytes requested.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void read( I2CBuffer buffer ,
                      int       length ) throws LinuxException {
    
        if ( LinuxIO.read( this.fd             ,
                           buffer.memory       ,
                           new SizeT( length ) ).intValue() < 0 ) {
        
            throw new LinuxException( "Unable to read to I2C buffer" ) ;
        }
    }




    /**
     * SMBUS operation writing a byte without a command.
     *
     * @param b  The unsigned byte that needs to be sent.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void writeByteDirectly( int b ) throws LinuxException {

        this.i2cSmbusAccess( I2C_SMBUS_WRITE ,
                             b               ,
                             I2C_SMBUS_BYTE  ,
                             null            ) ;
    }




    /**
     * SMBUS operation writing a byte after specifying a command.
     *
     * @param command  The command.
     *
     * @param b  The unsigned byte that need to be sent.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void writeByte( int command ,
                           int b       ) throws LinuxException {

        NativeMemory.setUnsignedByte( this.i2cSmbusData ,
                                      0                 ,
                                      b                 ) ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_BYTE_DATA ,
                             this.i2cSmbusData   ) ;
    }




    /**
     * SMBUS operation writing a short after specifying a command.
     *
     * @param command  The command.
     *
     * @param word  The unsigned short that needs to be sent.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void writeWord( int command ,
                           int word    ) throws LinuxException {
    
        NativeMemory.setUnsignedShort( this.i2cSmbusData ,
                                       0                 ,
                                       word              ) ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_WORD_DATA ,
                             this.i2cSmbusData   ) ;
    }




    /**
     * SMBUS operation writing several bytes after specifying a command.
     * <p>
     * After the command byte, the master also sends a byte count, how many bytes
     * will be written.
     *
     * @param command  The command.
     *
     * @param block  Block of bytes to write.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void writeBlock( int      command ,
                            I2CBlock block   ) throws LinuxException {
    
        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE          ,
                             command                  ,
                             I2C_SMBUS_I2C_BLOCK_DATA ,
                             block.memory             ) ;
    }




    /**
     * SMBUS-like operation writing several bytes after specifying a command where the length
     * is part of the message.
     * <p>
     * This operation is not in the SMBUS standard but is often supported nonetheless.
     * <p>
     * Unlike <strong>{@link #writeBlock( int, I2CBlock ) writeBlock}</strong>, this operation
     * does not send a byte count after the command.
     *
     * @param command  The command.
     *
     * @param block  Block of bytes to write.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void writeI2CBlock( int      command   ,
                               I2CBlock block     ) throws LinuxException {
    
        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE            ,
                             command                    ,
                             I2C_SMBUS_I2C_BLOCK_BROKEN ,
                             block.memory               ) ;
    }




    /**
     * Directly writes bytes to the slave device (length is specified by the buffer).
     *
     * @param buffer  Buffer to write.
     *
     * @throws LinuxException  When something fails on the native side.
     *
     * @see #write( I2CBuffer, int )
     */
    public void write( I2CBuffer buffer ) throws LinuxException {
    
        this.write( buffer        ,
                    buffer.length ) ;
    }




    /**
     * Directly writes <strong>length</strong> bytes to the slave device.
     *
     * @param buffer  Buffer to write.
     *
     * @param length  The number of bytes.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public void write( I2CBuffer buffer ,
                       int       length ) throws LinuxException {
    
        if ( LinuxIO.write( this.fd             ,
                            buffer.memory       ,
                            new SizeT( length ) ).intValue() < 0 ) {
        
            throw new LinuxException( "Unable to write I2C buffer" ) ;
        }
    }




    /**
     * SMBUS RPC-like operation, writing a short after specifying a command and then
     * reading the answer.
     *
     * @param command  The command.
     *
     * @param word  The unsigned short to be sent.
     *
     * @return  The unsigned short given back by the slave.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int processCall( int command ,
                            int word    ) throws LinuxException {

        NativeMemory.setUnsignedShort( this.i2cSmbusData ,
                                       0                 ,
                                       word              ) ;
    
        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_PROC_CALL ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedShort( this.i2cSmbusData ,
                                              0                 ) ;
    }




    /**
     * SMBUS RPC-like operation, writing several bytes after specifying a command and then
     * reading several bytes as an answer.
     *
     * @param command  The command.
     *
     * @param block  Block of bytes to write, also where the answer will be written to.
     *
     * @return  The number of bytes read.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int blockProcessCall( int      command ,
                                 I2CBlock block   ) throws LinuxException {
    
        return this.blockProcessCall( command ,
                                      block   ,
                                      block   ) ;
    }




    /**
     * SMBUS RPC-like operation, writing several bytes after specifying a command and then
     * reading several bytes as an answer.
     *
     * @param command  The command.
     *
     * @param blockWrite  Block of bytes to write.
     *
     * @param blockRead  Block of bytes the answer will be written to (can be the same as <strong>blockWrite</strong>
     *                   is it can be overwritten
     *
     * @return  The number of bytes read.
     *
     * @throws LinuxException  When something fails on the native side.
     */
    public int blockProcessCall( int      command    ,
                                 I2CBlock blockWrite ,
                                 I2CBlock blockRead  ) throws LinuxException {

        I2CBlock block ;

        blockWrite.writeLength() ;

        if ( blockWrite == blockRead ) {
        
            block = blockWrite ;
        }

        else {

            NativeMemory.copy( blockWrite.memory ,
                               blockRead.memory  ) ;

            block = blockRead ;
        }
    

        this.i2cSmbusAccess( I2C_SMBUS_WRITE           ,
                             command                   ,
                             I2C_SMBUS_BLOCK_PROC_CALL ,
                             block.memory              ) ;

        return block.readLength() ;
    }
}
