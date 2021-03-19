/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c.internal ;


import com.sun.jna.Union ;




/**
 * Internal class kept public for JNA to work, the user should not bother about this.
 */
public class NativeI2CSmbusData extends Union {


    // I2C_SMBUS_BLOCK_MAX( 32 ) + 2( 1 for length + 1 for PEC)
    //
    public static final int SIZE = 34 ;




    public byte   byt                      ;
    public short  word                     ;
    public byte[] block = new byte[ SIZE ] ;
}
