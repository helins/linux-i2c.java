# linux-i2c

The Linux kernel provides a standard API for acting as an
[I2C](https://en.wikipedia.org/wiki/I%C2%B2C) master. Typically, a bus is
available at `/dev/i2c-X` where `X` is the number of the bus. This Java library
proposes an idiomatic interface while trying to remain semantically close to
what Linux offers.

## License

Licensed under the [Apache License, Version
2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2018 Adam Helinski
