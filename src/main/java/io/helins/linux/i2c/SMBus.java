/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import com.sun.jna.Memory                                   ;
import com.sun.jna.NativeLong                               ;
import com.sun.jna.Pointer                                  ;
import io.helins.linux.NativeMemory                         ;
import io.helins.linux.i2c.I2CBus                           ;
import io.helins.linux.i2c.internal.NativeI2CSmbusData      ;
import io.helins.linux.i2c.internal.NativeI2CSmbusIoctlData ;
import io.helins.linux.io.LinuxIO                           ;
import java.io.IOException                                  ;




/**
 * SMBus is more or less a subset of I2C and has its own standard.
 * <p>
 * As such, the Linux kernel provides utilities for executing SMBus operations
 * on an I2C bus.
 */
public class SMBus {




    /**
     * Class representing a block of bytes for SMBus operations.
     * <p>
     * Such a buffer is limited to 32 bytes.
     */
    public static class Block {


        /**
         * The length of a buffer.
         */
        public static final int SIZE = NativeI2CSmbusData.SIZE - 2 ;


        private int length ;

        final Memory memory ;





        /**
         * Allocates a block.
         */
        public Block() {
        
            this.memory = new Memory( NativeI2CSmbusData.SIZE + 1 ) ;

            this.memory.clear() ;
        }



        
        /**
         * Fills the block with 0.
         *
         * @return  This instance.
         */
        public Block clear() {
        
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




        // Retrieves and syncs the length set by a native SMBus call.
        //
        int readLength() {
        
            this.length = NativeMemory.getUnsignedByte( this.memory ,
                                                        0           ) ;

            return this.length ;
        }




        // Syncs the length for a native SMBus call.
        //
        void writeLength() {

            this.writeLength( this.length ) ;
        }




        // Writes and syncs the length for a native SMBus call.
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
         * of a byte set by the user or by an SMBus operation.
         *
         * @param index  Which one.
         *
         * @param b  An unsigned byte.
         *
         * @return  This instance.
         */
        public Block set( int index ,
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






    //
    // Directly related to the SMBus class.
    //


    // IOCTL requests related to SMBus.
    //
    private final static NativeLong I2C_PEC   = new NativeLong( 0x0708L ,
                                                                true    ) ;

    private final static NativeLong I2C_SMBUS = new NativeLong( 0x0720L ,
                                                                true    ) ;

    //
    // SMBus operation values.
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



    // Underlying I2C bus.
    //
    private final I2CBus bus ;

    // Reusable native structures needed for IOCTL calls.
    //
    private final Memory i2cSmbusIoctlData ;
    private final Memory i2cSmbusData      ;




    // Package private constructor
    SMBus( I2CBus bus ) {
    
        this.bus = bus ;

        this.i2cSmbusIoctlData = new Memory( NativeI2CSmbusIoctlData.SIZE ) ;
        this.i2cSmbusData      = new Memory( NativeI2CSmbusData.SIZE )      ;

        this.i2cSmbusIoctlData.clear() ;
        this.i2cSmbusData.clear()      ;
    }




    /**
     * Enables or disables packet error checking for SMBus commands.
     * <p>
     * Is ignored unless the underlying driver provides this <strong>{@link I2CBus#getFunctionalities() functionality}</strong>.
     * <p>
     * Slave devices do not necessarely support this feature either.
     *
     * @param  usePEC
     *           Should PEC be enabled ?
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void usePEC( boolean usePEC ) throws IOException {
    
        this.bus.guardClosed() ;

        if ( LinuxIO.ioctl( this.bus.fd     ,
                            I2C_PEC         ,
                            usePEC ? 1L
                                   : 0L     ) < 0 ) {

            throw new IOException( "Native error while setting PEC : errno " + I2CBus.getErrno() ) ;
        }
    }




    // Performs an SMBus command.
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

        int result = LinuxIO.ioctl( this.bus.fd            ,
                                    I2C_SMBUS              ,
                                    this.i2cSmbusIoctlData ) ;

        if ( result < 0 ) {
        
            throw new IOException( "Native error during SMBus operation : errno " + I2CBus.getErrno() ) ;
        }

        return result ;
    }




    /**
     * SMBus operation sending only the READ or WRITE bit, no data is carried.
     *
     * @param  isWrite
     *           True if the WRITE bit must be set.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void quick( boolean isWrite ) throws IOException {

        this.bus.guardClosed() ;

        this.i2cSmbusAccess( isWrite ? I2C_SMBUS_WRITE
                                     : I2C_SMBUS_READ  ,
                             0                         ,
                             I2C_SMBUS_QUICK           ,
                             null                      ) ;
    }




    /**
     * SMBus operation reading a byte without a command.
     *
     * @return Unsigned byte received from the slave.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readByteDirectly() throws IOException {

        this.bus.guardClosed() ;

        this.i2cSmbusAccess( I2C_SMBUS_READ    ,
                             0                 ,
                             I2C_SMBUS_BYTE    ,
                             this.i2cSmbusData ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    /**
     * SMBus operation reading a byte after specifying a command.
     *
     * @param command
     *          AKA "register".
     *
     * @return Unsigned byte received from the slave.
     *
     * @throws IllegalStateException
     *           When thelbus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readByte( int command ) throws IOException {

        this.bus.guardClosed() ;
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_BYTE_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    /**
     * SMBus operation reading a short after specifying a command.
     *
     * @param  command
     *           AKA "register".
     *
     * @return Unsigned short received from the slave.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readWord( int command ) throws IOException {

        this.bus.guardClosed() ;
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_WORD_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedShort( this.i2cSmbusData ,
                                              0                 ) ;
    }




    /**
     * SMBus operation reading several bytes after specifying a command.
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int readBlock( int   command ,
                          Block block   ) throws IOException {

        this.bus.guardClosed() ;
    
        this.i2cSmbusAccess( I2C_SMBUS_READ       ,
                             command              ,
                             I2C_SMBUS_BLOCK_DATA ,
                             block.memory         ) ;

        return block.readLength() ;
    }




    /**
     * SMBus-like operation reading several bytes after specifying a command where the length
     * is part of the message.
     * <p>
     * This operation is not in the SMBus standard but is often supported nonetheless.
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void readI2CBlock( int   command ,
                              Block block   ,
                              int   length  ) throws IOException {

        this.bus.guardClosed() ;

        if ( length > Block.SIZE ) {
        
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
     * SMBus operation writing a byte without a command.
     *
     * @param  b
     *           Unsigned byte that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeByteDirectly( int b ) throws IOException {

        this.bus.guardClosed() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE ,
                             b               ,
                             I2C_SMBUS_BYTE  ,
                             null            ) ;
    }




    /**
     * SMBus operation writing a byte after specifying a command.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  b
     *           Unsigned byte that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeByte( int command ,
                           int b       ) throws IOException {

        this.bus.guardClosed() ;

        NativeMemory.setUnsignedByte( this.i2cSmbusData ,
                                      0                 ,
                                      b                 ) ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_BYTE_DATA ,
                             this.i2cSmbusData   ) ;
    }




    /**
     * SMBus operation writing a short after specifying a command.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  word
     *           Unsigned short that needs to be sent.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeWord( int command ,
                           int word    ) throws IOException {
    
        this.bus.guardClosed() ;

        NativeMemory.setUnsignedShort( this.i2cSmbusData ,
                                       0                 ,
                                       word              ) ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_WORD_DATA ,
                             this.i2cSmbusData   ) ;
    }




    /**
     * SMBus operation writing several bytes after specifying a command.
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeBlock( int   command ,
                            Block block   ) throws IOException {
    
        this.bus.guardClosed() ;

        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE          ,
                             command                  ,
                             I2C_SMBUS_I2C_BLOCK_DATA ,
                             block.memory             ) ;
    }




    /**
     * SMBus-like operation writing several bytes after specifying a command where the length
     * is part of the message.
     * <p>
     * This operation is not in the SMBus standard but is often supported nonetheless.
     * <p>
     * Unlike <strong>{@link #writeBlock( int, Block ) writeBlock}</strong>, this operation
     * does not send a byte count after the command.
     *
     * @param  command
     *           AKA "register".
     *
     * @param  block
     *           Block of bytes to write.
     *
     * @throws IllegalStateException
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public void writeI2CBlock( int   command   ,
                               Block block     ) throws IOException {
    
        this.bus.guardClosed() ;

        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE            ,
                             command                    ,
                             I2C_SMBUS_I2C_BLOCK_BROKEN ,
                             block.memory               ) ;
    }




    /**
     * SMBus RPC-like operation, writing a short after specifying a command and then
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int processCall( int command ,
                            int word    ) throws IOException {

        this.bus.guardClosed() ;

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
     * SMBus RPC-like operation, writing several bytes after specifying a command and then
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int blockProcessCall( int   command ,
                                 Block block   ) throws IOException {
    
        return this.blockProcessCall( command ,
                                      block   ,
                                      block   ) ;
    }




    /**
     * SMBus RPC-like operation, writing several bytes after specifying a command and then
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
     *           When the underlying I2C bus has been closed.
     *
     * @throws IOException
     *           When the bus is not a proper I2C bus or an unplanned error occured.
     */
    public int blockProcessCall( int   command    ,
                                 Block blockWrite ,
                                 Block blockRead  ) throws IOException {

        Block block ;

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
