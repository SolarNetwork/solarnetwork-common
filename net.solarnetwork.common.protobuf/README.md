# Protobuf Support

This project contains an OSGi bundle that provides support for working with the [Protobuf][protobuf] 
data serialization framework dynamically.

## Exported Packages

The bundle exports the following packages:

| Package | Description |
|:---------|:------------|
| `net.solarnetwork.common.protobuf`  | Defines the `ProtobufCompilerService` API and provides supporting classes like `ProtobufObjectCodec` to en/decode Protobuf messages. |
| `net.solarnetwork.common.protobuf.protoc`  | Provides a `ProtocProtobufCompilerService` implementation based on `protoc`. |

[protobuf]: https://developers.google.com/protocol-buffers
