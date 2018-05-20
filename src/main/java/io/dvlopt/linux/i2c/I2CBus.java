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


    public final static byte I2C_SMBUS_QUICK            = 0 ;
    public final static byte I2C_SMBUS_BYTE             = 1 ;
    public final static byte I2C_SMBUS_BYTE_DATA        = 2 ;
    public final static byte I2C_SMBUS_WORD_DATA        = 3 ;
    public final static byte I2C_SMBUS_PROC_CALL        = 4 ;
    public final static byte I2C_SMBUS_BLOCK_DATA       = 5 ;
    public final static byte I2C_SMBUS_I2C_BLOCK_BROKEN = 6 ;
    public final static byte I2C_SMBUS_BLOCK_PROC_CALL  = 7 ;
    public final static byte I2C_SMBUS_I2C_BLOCK_DATA   = 8 ;




    private final int    fd                ;
    private final Memory i2cSmbusIoctlData ;
    private final Memory i2cSmbusData      ;

    private boolean isTenBit = false ;




    public I2CBus( int busNumber ) throws LinuxException {
    
        this( "/dev/i2c-" + busNumber ) ;
    }




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




    public void close() throws LinuxException {
    
        if ( LinuxIO.close( this.fd ) != 0 ) {
        
            throw new LinuxException( "Unable to close this I2C bus" ) ;
        }
    }




    public I2CBus doTransaction( I2CTransaction transaction ) throws LinuxException {

        if ( LinuxIO.ioctl( this.fd            ,
                            I2C_RDWR           ,
                            transaction.memory ) < 0 ) {
        
            throw new LinuxException( "Unable to fully perform requested I2C transaction" ) ;
        }

        return this ;
    }




    public I2CFunctionalities getFunctionalities() throws LinuxException {
    
        LongByReference longRef = new LongByReference() ;

        if ( LinuxIO.ioctl( this.fd              ,
                            I2C_FUNCS            ,
                            longRef.getPointer() ) < 0 ) {
        
            throw new LinuxException( "Unable to get the I2C functionalities of this bus" ) ;
        }

        return new I2CFunctionalities( (int)( longRef.getValue() ) ) ;
    }




    public void selectSlave( int address ) throws LinuxException {

        this.selectSlave( address ,
                          false   ,
                          false   ) ;
    }




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




    public void setRetries( int nRetries ) throws LinuxException {

        if ( LinuxIO.ioctl( this.fd       ,
                            I2C_RETRIES   ,
                              nRetries
                            & 0xffffffffL ) < 0 ) {

            throw new LinuxException( "Unable to set the number of retries" ) ;
        }
    }




    public void setTimeout( int milliseconds ) throws LinuxException {
    
        if ( LinuxIO.ioctl( this.fd                 ,
                            I2C_TIMEOUT             ,
                              ( milliseconds / 10 )
                            & 0xffffffL             ) < 0 ) {

            throw new LinuxException( "Unable to set timeout" ) ;
        }
    }




    public void usePEC( boolean usePEC ) throws LinuxException {
    
        if ( LinuxIO.ioctl( this.fd     ,
                            I2C_PEC     ,
                            usePEC ? 1L
                                   : 0L ) < 0 ) {

            throw new LinuxException( "Unable to set or unset PEC for SMBUS operations" ) ;
        }
    }




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




    public void quick( boolean isWrite ) throws LinuxException {

        this.i2cSmbusAccess( isWrite ? I2C_SMBUS_WRITE
                                     : I2C_SMBUS_READ  ,
                             0                         ,
                             I2C_SMBUS_QUICK           ,
                             null                      ) ;
    }




    public int readByteDirectly() throws LinuxException {

        this.i2cSmbusAccess( I2C_SMBUS_READ    ,
                             0                 ,
                             I2C_SMBUS_BYTE    ,
                             this.i2cSmbusData ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    public int readByte( int command ) throws LinuxException {
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_BYTE_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedByte( this.i2cSmbusData ,
                                             0                 ) ;
    }




    public int readWord( int command ) throws LinuxException {
    
        this.i2cSmbusAccess( I2C_SMBUS_READ      ,
                             command             ,
                             I2C_SMBUS_WORD_DATA ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedShort( this.i2cSmbusData ,
                                              0                 ) ;
    }




    public int readBlock( int      command ,
                          I2CBlock block   ) throws LinuxException {
    
       this.i2cSmbusAccess( I2C_SMBUS_READ       ,
                            command              ,
                            I2C_SMBUS_BLOCK_DATA ,
                            block.memory         ) ;

       return block.readLength() ;
    }




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




    public void read( I2CBuffer buffer ) throws LinuxException {
    
        this.read( buffer        ,
                   buffer.length ) ;
    }




    public void read( I2CBuffer buffer ,
                      int       length ) throws LinuxException {
    
        if ( LinuxIO.read( this.fd             ,
                           buffer.memory       ,
                           new SizeT( length ) ).intValue() < 0 ) {
        
            throw new LinuxException( "Unable to read to I2C buffer" ) ;
        }
    }




    public void writeByteDirectly( int b ) throws LinuxException {

        this.i2cSmbusAccess( I2C_SMBUS_WRITE ,
                             b               ,
                             I2C_SMBUS_BYTE  ,
                             null            ) ;
    }




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




    public void writeBlock( int      command ,
                            I2CBlock block   ) throws LinuxException {
    
        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE          ,
                             command                  ,
                             I2C_SMBUS_I2C_BLOCK_DATA ,
                             block.memory             ) ;
    }




    public void writeI2CBlock( int      command   ,
                               I2CBlock block     ) throws LinuxException {
    
        block.writeLength() ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE            ,
                             command                    ,
                             I2C_SMBUS_I2C_BLOCK_BROKEN ,
                             block.memory               ) ;
    }




    public void write( I2CBuffer buffer ) throws LinuxException {
    
        if ( LinuxIO.write( this.fd                    ,
                            buffer.memory              ,
                            new SizeT( buffer.length ) ).intValue() < 0 ) {
        
            throw new LinuxException( "Unable to write I2C buffer" ) ;
        }
    }




    public int processCall( int command ,
                            int word    ) throws LinuxException {
    
        this.i2cSmbusData.setShort( 0           ,
                                    (short)word ) ;

        this.i2cSmbusAccess( I2C_SMBUS_WRITE     ,
                             command             ,
                             I2C_SMBUS_PROC_CALL ,
                             this.i2cSmbusData   ) ;

        return NativeMemory.getUnsignedShort( this.i2cSmbusData ,
                                              0                 ) ;
    }




    public int blockProcessCall( int      command ,
                                 I2CBlock block   ) throws LinuxException {
    
        return this.blockProcessCall( command ,
                                      block   ,
                                      block   ) ;
    }




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
