# linux-i2c

The Linux kernel provides a standard API for acting as an
[I2C](https://en.wikipedia.org/wiki/I%C2%B2C) master. Typically, a bus is
available at `/dev/i2c-X` where `X` is the number of the bus. This Java library
proposes an idiomatic interface while trying to remain semantically close to
what Linux offers.

# Usage

Read the [javadoc](https://dvlopt.github.io/doc/java/linux-i2c/index.html).

# References

- [i2c-bus.org](https://www.i2c-bus.org/), great website about the I2C protocol.
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

Licensed under the [Apache License, Version
2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2018 Adam Helinski
