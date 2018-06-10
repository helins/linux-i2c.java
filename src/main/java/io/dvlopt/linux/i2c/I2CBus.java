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
import io.dvlopt.linux.Linux                                ;
import io.dvlopt.linux.NativeMemory                         ;
import io.dvlopt.linux.SizeT                                ;
import io.dvlopt.linux.errno.Errno                          ;
import io.dvlopt.linux.i2c.I2CBlock                         ;
import io.dvlopt.linux.i2c.I2CFunctionalities               ;
import io.dvlopt.linux.i2c.I2CTransaction                   ;
import io.dvlopt.linux.i2c.internal.NativeI2CSmbusData      ;
import io.dvlopt.linux.i2c.internal.NativeI2CSmbusIoctlData ;
import io.dvlopt.linux.io.LinuxIO                           ;
import java.io.FileNotFoundException                        ;
import java.io.IOException                                  ;




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
 * I2C is a standard but your master device and the slaves devices it talks to probably do not support everything properly.
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


    //
    // IOCTL requests
    //

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


    //
    // SMBUS operation values.
    //

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




    // File descriptor associated with the bus.
    //
    private final int fd ;

    // Reusable native structures needed for IOCTL calls.
    //
    private final Memory i2cSmbusIoctlData ;
    private final Memory i2cSmbusData      ;

    // Bookkeeping current addressing mode.
    //
    private boolean isTenBit = false ;

    // Bookkeeping current state.
    private boolean isClosed = false ;




    /**
     * Opens an I2C bus by number, `/dev/i2c-$busNumber`.
     *
     * @param busNumber
     *          Number of the bus.
     *
     * @throws IOException
     *           When an unplanned error occured.
     */
    public I2CBus( int busNumber ) throws IOException {
    
        this( "/dev/i2c-" + busNumber ) ;
    }




    /**
     * Opens an I2C bus on the given path.
     *
     * @param  path
     *          Path to the bus such as `/dev/i2c-1`.
     *
     * @throws FileNotFoundException
     *           When the bus is not found or the user does not have permissions.
     * @throws IOException
     *           When an unplanned error occured.
     */
    public I2CBus( String path ) throws IOException {
    
        this.i2cSmbusIoctlData = new Memory( NativeI2CSmbusIoctlData.SIZE ) ;
        this.i2cSmbusData      = new Memory( NativeI2CSmbusData.SIZE )      ;

        this.i2cSmbusIoctlData.clear() ;
        this.i2cSmbusData.clear()      ;

        this.fd = LinuxIO.open64( path           ,
                                  LinuxIO.O_RDWR ) ;

        if ( this.fd < 0 ) {

            final int errno = Linux.getErrno() ;

            switch ( errno ) {
            
                case Errno.EACCES : throw new FileNotFoundException( "I2C Bus not found : " + path ) ;

                case Errno.ENOENT : throw new FileNotFoundException( "Permission denied : " + path ) ;

                default           : throw new IOException( "Native error while opening I2C bus : errno " + errno ) ;
            }
        }
    }




    /**
     * Closes this bus.
     *
     * @throws IOException
     *           When an unplanned error occured.
     */
    public void close() throws IOException {
    
        if ( !( this.isClosed ) && LinuxIO.close( this.fd ) != 0 ) {

            throw new IOException( "Native error while closing I2C bus : errno " + Linux.getErrno() ) ;
        }

        this.isClosed = true ;
    }




    // Throws an IllegalStateException is the bus has been closed.
    //
    private void guardClosed() {
    
        if ( this.isClosed ) {
        
            throw new IllegalStateException( "Unable to perform IO on closed I2C bus" ) ;
        }
    }




    // Retrieves the current errno value and throws an exception if a generic error is recognized or simple returns the value.
    //
    private static int getErrno() throws IOException {
    
        final int errno = Linux.getErrno() ;

        switch ( errno ) {
        
            case Errno.ENOTTY    : throw new IOException( "Unable to use inappropriate I2C bus" ) ;

            case Errno.EREMOTEIO : throw new IOException( "Remote IO error during I2C operation" ) ;
        }

        return errno ;
    }




    /**
     * Do an uninterrupted transaction of several messages.
     * <p>
     * Not all devices support this feature and some only support one message at a time which
     * is not very interesting.
     *
     * @param  transaction
     *           Transaction that will be carried out.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void doTransaction( I2CTransaction transaction ) throws IOException {

        this.guardClosed() ;

        if ( LinuxIO.ioctl( this.fd            ,
                            I2C_RDWR           ,
                            transaction.memory ) < 0 ) {

            throw new IOException( "Native error while doing an I2C transaction : errno " + getErrno() ) ;
        }
    }




    /**
     * Finds out what this I2C bus can do.
     *
     * @return  Functionalities.
     *
     * @throws  IllegalStateException
     *            When the I2C bus has been closed.
     *
     * @throws  IOException
     *            When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public I2CFunctionalities getFunctionalities() throws IOException {

        this.guardClosed() ;
    
        LongByReference longRef = new LongByReference() ;

        if ( LinuxIO.ioctl( this.fd              ,
                            I2C_FUNCS            ,
                            longRef.getPointer() ) < 0 ) {

            throw new IOException( "Native error while getting the funcitonalities of an I2C bus : errno " + getErrno() ) ;
        }

        return new I2CFunctionalities( (int)( longRef.getValue() ) ) ;
    }




    /**
     * Selects an available slave device using a regular 7-bit address.
     *
     * @param  address
     *           1ddress of the needed slave.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     *
     * @see    #selectSlave( int, boolean, boolean )
     */
    public void selectSlave( int address ) throws IOException {

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
     * @param  address
     *           1ddress of the needed slave.
     *
     * @param  force
     *           Should the slave be selected even if it is already used somewhere else ?
     *
     * @param  isTenBit
     *           Is the given address in 10-bit resolution ?
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void selectSlave( int     address  ,
                             boolean force    ,
                             boolean isTenBit ) throws IOException {

        this.guardClosed() ;

        if ( isTenBit ) {
        
            if ( this.isTenBit == false ) {
            
                if ( LinuxIO.ioctl( this.fd    ,
                                    I2C_TENBIT ,
                                    1          ) < 0 ) {

                    throw new IOException( "Native error while switching to 10 bit I2C addressing scheme : errno " + getErrno() ) ;
                }

                this.isTenBit = true ;
            }
        }

        else if ( this.isTenBit ) {
        
            if ( LinuxIO.ioctl( this.fd    ,
                                I2C_TENBIT ,
                                0          ) < 0 ) {

                throw new IOException( "Native error while switching to 7 bit I2C addressing scheme : errno " + getErrno() ) ;
            }

            this.isTenBit = false ;
        }
        
        if ( LinuxIO.ioctl( this.fd                 ,
                            force ? I2C_SLAVE_FORCE
                                  : I2C_SLAVE       ,
                              address  
                            & 0xffffffffL           ) < 0 ) {
                            
            throw new IOException( "Native error while selecting I2C slave " + address + " : errno " + getErrno() ) ;
        }
    }




    /**
     * Sets the number of time a slave should be polled when not acknowledging.
     * <p>
     * Does not always have an effect depending on the underlying driver.
     *
     * @param  nRetries
     *           Number of retries.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void setRetries( int nRetries ) throws IOException {

        this.guardClosed() ;

        if ( LinuxIO.ioctl( this.fd       ,
                            I2C_RETRIES   ,
                              nRetries
                            & 0xffffffffL ) < 0 ) {

            throw new IOException( "Native error while setting I2C retries : errno " + getErrno() ) ;
        }
    }




    /**
     * Sets the timeout in milliseconds.
     * <p>
     * Does not always have an effect depending on the underlying driver.
     *
     * @param  milliseconds
     *           Timeout in milliseconds.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void setTimeout( int milliseconds ) throws IOException {

        this.guardClosed() ;
    
        if ( LinuxIO.ioctl( this.fd                 ,
                            I2C_TIMEOUT             ,
                              ( milliseconds / 10 )
                            & 0xffffffL             ) < 0 ) {

            throw new IOException( "Native error while setting I2C timeout : errno " + getErrno() ) ;
        }
    }




    /**
     * Enables or disables packet error checking for SMBUS commands.
     * <p>
     * Is ignored unless the underlying driver provides this <strong>{@link #getFunctionalities() functionality}</strong>.
     * <p>
     * Slave devices do not necessarely support this feature either.
     *
     * @param  usePEC
     *           Should PEC be enabled ?
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void usePEC( boolean usePEC ) throws IOException {
    
        this.guardClosed() ;

        if ( LinuxIO.ioctl( this.fd     ,
                            I2C_PEC     ,
                            usePEC ? 1L
                                   : 0L ) < 0 ) {

            throw new IOException( "Native error while setting PEC : errno " + getErrno() ) ;
        }
    }




    // Performs an SMBUS command.
    //
    private int i2cSmbusAccess( byte    readWrite ,
                                int     command   ,
                                int     size      ,
                                Pointer data      ) throws IOException {

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
        
            throw new IOException( "Native error during SMBUS operation : errno " + getErrno() ) ;
        }

        return result ;
    }




    /**
     * SMBUS operation sending only the READ or WRITE bit, no data is carried.
     *
     * @param  isWrite
     *           True if the WRITE bit must be set.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void quick( boolean isWrite ) throws IOException {

        this.guardClosed() ;

        this.i2cSmbusAccess( isWrite ? I2C_SMBUS_WRITE
                                     : I2C_SMBUS_READ  ,
                             0                         ,
                             I2C_SMBUS_QUICK           ,
                             null                      ) ;
    }




    /**
     * SMBUS operation reading a byte without a command.
     *
     * @return Unsigned byte received from the slave.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readByteDirectly() throws IOException {

        this.guardClosed() ;

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
     * @param command
     *          AKA "register".
     *
     * @return Unsigned byte received from the slave.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readByte( int command ) throws IOException {

        this.guardClosed() ;
    
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
     * @param  command
     *           AKA "register".
     *
     * @return Unsigned short received from the slave.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readWord( int command ) throws IOException {

        this.guardClosed() ;
    
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
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes the answer will be written to.
     *
     * @return Number of bytes read.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readBlock( int      command ,
                          I2CBlock block   ) throws IOException {

        this.guardClosed() ;
    
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
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes the answer will be written to.
     *
     * @param  length
     *           How many bytes should be read.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void readI2CBlock( int      command ,
                              I2CBlock block   ,
                              int      length  ) throws IOException {

        this.guardClosed() ;

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
     * @param  buffer
     *           Buffer the answer will be written to.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     *
     * @see    #read( I2CBuffer, int )
     */
    public void read( I2CBuffer buffer ) throws IOException {
    
        this.read( buffer        ,
                   buffer.length ) ;
    }




    /**
     * Directly reads <strong>length</strong> bytes from the slave device.
     *
     * @param  buffer
     *           Buffer the answer will be written to.
     *
     * @param  length
     *           Number of bytes requested.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void read( I2CBuffer buffer ,
                      int       length ) throws IOException {
    
        this.guardClosed() ;

        if ( LinuxIO.read( this.fd             ,
                           buffer.memory       ,
                           new SizeT( length ) ).intValue() < 0 ) {
        
            throw new IOException( "Native error while writing I2C buffer : errno " + getErrno() ) ;
        }
    }




    /**
     * SMBUS operation writing a byte without a command.
     *
     * @param  b
     *           Unsigned byte that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeByteDirectly( int b ) throws IOException {

        this.guardClosed() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE ,
                             b               ,
                             I2C_SMBUS_BYTE  ,
                             null            ) ;
    }




    /**
     * SMBUS operation writing a byte after specifying a command.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  b
     *           Unsigned byte that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeByte( int command ,
                           int b       ) throws IOException {

        this.guardClosed() ;

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
     * @param  command
     *           AKA "register".
     *
     * @param  word
     *           Unsigned short that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeWord( int command ,
                           int word    ) throws IOException {
    
        this.guardClosed() ;

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
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes to write.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeBlock( int      command ,
                            I2CBlock block   ) throws IOException {
    
        this.guardClosed() ;

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
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes to write.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeI2CBlock( int      command   ,
                               I2CBlock block     ) throws IOException {
    
        this.guardClosed() ;

        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE            ,
                             command                    ,
                             I2C_SMBUS_I2C_BLOCK_BROKEN ,
                             block.memory               ) ;
    }




    /**
     * Directly writes bytes to the slave device (length is specified by the buffer).
     *
     * @param  buffer
     *           Buffer to write.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     *
     * @see #write( I2CBuffer, int )
     */
    public void write( I2CBuffer buffer ) throws IOException {
    
        this.write( buffer        ,
                    buffer.length ) ;
    }




    /**
     * Directly writes <strong>length</strong> bytes to the slave device.
     *
     * @param  buffer
     *           Buffer to write.
     *
     * @param  length
     *           Number of bytes.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void write( I2CBuffer buffer ,
                       int       length ) throws IOException {

        this.guardClosed() ;
    
        if ( LinuxIO.write( this.fd             ,
                            buffer.memory       ,
                            new SizeT( length ) ).intValue() < 0 ) {
        
            throw new IOException( "Native error while writing I2C buffer : errno " + getErrno() ) ;
        }
    }




    /**
     * SMBUS RPC-like operation, writing a short after specifying a command and then
     * reading the answer.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  word
     *           Unsigned short to be sent.
     *
     * @return Unsigned short given back by the slave.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int processCall( int command ,
                            int word    ) throws IOException {

        this.guardClosed() ;

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
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes to write, also where the answer will be written to.
     *
     * @return Number of bytes read.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int blockProcessCall( int      command ,
                                 I2CBlock block   ) throws IOException {
    
        return this.blockProcessCall( command ,
                                      block   ,
                                      block   ) ;
    }




    /**
     * SMBUS RPC-like operation, writing several bytes after specifying a command and then
     * reading several bytes as an answer.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  blockWrite
     *           Block of bytes to write.
     *
     * @param  blockRead
     *           Block of bytes the answer will be written to (can be the same as <strong>blockWrite</strong>
     *           if it can be overwritten).
     *
     * @return Number of bytes read.
     *
     * @throws IllegalStateException
     *           When the I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int blockProcessCall( int      command    ,
                                 I2CBlock blockWrite ,
                                 I2CBlock blockRead  ) throws IOException {

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
