package io.dvlopt.linux.i2c ;


import com.sun.jna.NativeLong             ;
import io.dvlopt.linux.LinuxException     ;
import io.dvlopt.linux.i2c.I2CTransaction ;
import io.dvlopt.linux.io.LinuxIO         ;




public class I2CBus implements AutoCloseable {



    private final static NativeLong I2C_RETRIES      = new NativeLong( 0x0701L ,
                                                                       true    ) ;

    private final static NativeLong I2C_TIMEOUT      = new NativeLong( 0x0702L ,
                                                                       true    ) ;

    private final static NativeLong I2C_SLAVE        = new NativeLong( 0x0703L ,
                                                                       true    ) ;

    private final static NativeLong I2C_SLAVE_FORCE  = new NativeLong( 0x0706L ,
                                                                       true    ) ;

    private final static NativeLong I2C_TENBIT       = new NativeLong( 0x0704L ,
                                                                       true    ) ;

    private final static NativeLong I2C_FUNCS        = new NativeLong( 0x0705L ,
                                                                       true    ) ;

    private final static NativeLong I2C_RDWR         = new NativeLong( 0x0707L ,
                                                                       true    ) ;

    private final static NativeLong I2C_PEC          = new NativeLong( 0x0708L ,
                                                                       true    ) ;

    private final static NativeLong I2C_SMBUS        = new NativeLong( 0x0720L ,
                                                                       true    ) ;


    private final int fd ;




    public I2CBus( int busNumber ) throws LinuxException {
    
        this( "/dev/i2c-" + busNumber ) ;
    }




    public I2CBus( String path ) throws LinuxException {
    

        this.fd = LinuxIO.open64( path           ,
                                  LinuxIO.O_RDWR ) ;

        if ( this.fd < 0 ) {
        
            throw new LinuxException( "Unable to open an I2C bus at the given path" ) ;
        }
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
}
