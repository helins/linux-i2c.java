# Linux-I2C

[![Maven
Central](https://maven-badges.herokuapp.com/maven-central/io.helins/linux-i2c/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.helins/linux-i2c)

[![Javadoc](https://javadoc.io/badge2/io.helins/linux-i2c/javadoc.svg)](https://javadoc.io/doc/io.helins/linux-i2c)

![CircleCI](https://circleci.com/gh/helins/linux-i2c.java.svg?style=shield)

The Linux kernel provides a standard API for acting as an
[I2C](https://en.wikipedia.org/wiki/I%C2%B2C) master. Typically, a bus is
available at `/dev/i2c-X` where `X` is the number of the bus. This Java library
proposes an idiomatic interface while trying to remain semantically close to
what Linux offers.

Here is a [Clojure wrapper](https://github.com/helins/linux.i2c.clj).


## Usage

This is an overview.

Read the
[javadoc](https://javadoc.io/doc/io.helins/linux-i2c) for more details.


### In short

Those examples are meant to be short (no error checking).

```java
import io.helins.linux.i2c.* ;

I2CBus bus = new I2CBus( "/dev/i2c-1" ) ;
```


#### Find out what a bus can do

```java
I2CFunctionalities functionalities = bus.getFunctionalities() ;

bus.can( I2CFunctionality.TRANSACTIONS ) ;

bus.can( I2CFunctionality.READ_BYTE ) ;
```


#### Directly read or write an arbitrary amount of bytes

```java
// Selects slave device with address 0x42.
//
bus.selectSlave( 0x42 ) ;

// Creates a buffer and sets the first 2 bytes.
// Then sends it to the slave.
//
I2CBuffer buffer = new I2CBuffer( 2 ).set( 0, 66 )
                                     .set( 1, 24 ) ;
bus.write( buffer ) ;

// Now, reads 1 byte from the slave.
//
bus.read( buffer ,
          1      ) ;

buffer.get( 0 ) ;
```


#### SMBUS operations

SMBUS is sort of a subset of I2C. Not every master of slave device support all
those features.

```java
// Selects slave device with address 0x42.
//
bus.selectSlave( 0x42 ) ;

// Just sends the RW bit without any data.
//
bus.quick( true ) ;

// Directly reads or writes a byte.
//
int b = bus.readByteDirectly() ;

bus.writeByteDirectly( 246 ) ;

// Reads or writes a byte or a short after specifying a command (aka register).
//
int b = bus.readByte( 66 ) ;

bus.writeByte( 66, 24 ) ;

int word = bus.readWord( 66 ) ;

bus.writeWord( 66, 746 ) ;

// Reads or writes a block of bytes (at most 32) after specifying a command.
//
I2CBlock blockWrite = new I2CBlock().set( 0, 12 )
                                    .set( 1, 24 )
                                    .set( 2, 48 ) ;

I2CBlock blockRead  = new I2CBlock() ;

bus.writeBlock( 66, blockWrite ) ;

int nBytes = bus.readBlock( 33, blockRead ) ;

// Executes a remote function call by sending a word after specifying a command
// and then reading one.
//
int word = bus.processCall( 66, 345 ) ;

// Similar but with several bytes.
//
int nBytes = bus.blockProcessCall( 66, blockWrite, blockRead ) ;
```


#### I2C transactions

Not every device support doing uninterrupted sequences of reads and/or writes,
and some only support 1 message per transaction which defeats the purpose of
having transactions in the first place.

The slave device is selected in each message.

```java
// Transaction of 2 messages.
//
I2CTransaction trx = new I2CTransaction( 2 ) ;

// First message consists of sending 3 bytes.
//
trx.getMessage( 0 ).setAddress( 0x42 )
                   .setBuffer( new I2CBuffer( 3 ).set( 0, 66 )
                                                 .set( 1, 132 )
                                                 .set( 2, 264 ) ) ;

// Second message will read 3 bytes in another buffer.
//
I2CBuffer bufferReponse = new I2CBuffer( 3 ) ;

trx.getMessage( 1 ).setAddress( 0x42 )
                   .setFlags( new I2CFlags().set( I2CFlag.READ ) )
                   .setBuffer( bufferResponse ) ;

bus.doTransaction( trx ) ;

// Does something with the received data.
//
bufferResponse.get( 0 ) ; 
```


#### Finally

```java
bus.close() ;
```


## Tests

This library has been currently tested with a Raspberry Pi 3. An [Arduino
sketch](../tree/main/arduino] is provided for either trying the library or
running IO tests.

For running non IO-tests :

```bash
$ ./bin/test
```

For running tests with a connected Arduino :
```bash
$ ./bin/test_with_io
```


## References

- [i2c-bus.org](https://www.i2c-bus.org/), a great website about the I2C protocol.
- [Short documentation about the native API](https://www.kernel.org/doc/Documentation/i2c/dev-interface).
- [Explaination of I2C
    transactions](http://www.st.com/content/ccc/resource/technical/document/application_note/78/ee/9e/cf/f4/94/46/ca/CD18149624.pdf/files/CD18149624.pdf/jcr:content/translations/en.CD18149624.pdf).
- [SMBUS
    protocol](http://www.smbus.org/specs/smbus20.pdf#%5B%7B%22num%22%3A208%2C%22gen%22%3A0%7D%2C%7B%22name%22%3A%22FitB%22%7D%5D),
    a more or less subset of I2C.
- [Summary of the SMBUS
    protocol](https://www.kernel.org/doc/Documentation/i2c/smbus-protocol).
- [Native source regarding flags and
    functionalities](https://code.woboq.org/linux/linux/include/uapi/linux/i2c.h.html).


## License

Copyright © 2018 Adam Helinski

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
