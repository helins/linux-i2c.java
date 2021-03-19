/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c ;


import io.helins.linux.i2c.I2CFunctionality ;




/**
 * Class holding the functionalities of an I2C bus.
 */
public class I2CFunctionalities {


    // Internal value refering to functionalities.
    //
    private final int flags ;




    // Package private constructor.
    //
    I2CFunctionalities( int flags ) {
    
        this.flags = flags ;
    }




    /**
     * Can the I2C bus do this ?
     *
     * @param functionality  The tested functionality.
     *
     * @return  A boolean.
     */
    public boolean can( I2CFunctionality functionality ) {
    
        return ( this.flags & functionality.value ) != 0 ;
    }
}
