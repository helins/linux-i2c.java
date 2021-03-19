/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import io.helins.linux.i2c.I2CFlag ;




/**
 * Class holding flags for describing an I2C message.
 *
 * @see I2CMessage
 */
public class I2CFlags {


    // Internal value holding flags.
    //
    int value ;



    /**
     * Basic constructor.
     */
    public I2CFlags() {
    
        clear() ;
    }




    // Private constructor.
    //
    I2CFlags( int value ) {
    
        this.value = value ;
    }




    /**
     * Is this flag set ?
     *
     * @param flag
     *          Tested flag.
     *
     * @return True if the flag is set.
     */
    public boolean isSet( I2CFlag flag ) {
    
        return ( this.value & flag.value ) > 0 ;
    }




    /**
     * Sets this flag.
     *
     * @param  flag
     *           Flag to be set.
     *
     * @return This instance.
     */
    public I2CFlags set( I2CFlag flag ) {

        this.value |= flag.value ;

        return this ;
    }




    /**
     * Unsets this flags.
     *
     * @param  flag
     *           Flag to be unset.
     *
     * @return  This instance.
     */
    public I2CFlags unset( I2CFlag flag ) {
    
        this.value &= ~( flag.value ) ;

        return this ;
    }




    /**
     * Unsets all flags.
     *
     * @return This instance.
     */
    public I2CFlags clear() {
    
        this.value = 0 ;

        return this ;
    }



    /**
     * Are those two sets of flags equal ?
     *
     * @param  flags
     *           Another set of flags.
     *
     * @return True of equal.
     */
    public boolean equals( I2CFlags flags ) {
    
        return this.value == flags.value ;
    }




    @Override
    public boolean equals( Object o ) {
    
        return o instanceof I2CFlags ? this.equals( (I2CFlags)o )
                                     : false                      ;
    }
}
