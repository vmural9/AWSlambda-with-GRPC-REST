# CS441: HW2

<aside>
⚙ Vishaal Karthik Muralidharan | NetID: vmural9 | AWS username: vmural9 |
YT link: https://youtu.be/mig-f_lfwzk

</aside>

---

# Description

Design a lambda function and a Scala client function that uses GRPC/REST methods (GET/POST) to communicate and host it on AWS.

The lambda function will determine if there’s a message available with the given timestamp. Additional 

- With the given timestamp and the time interval, find if there are any log messages that comes under the time interval starting with the timestamp.
    - return a MD5 generated hash code from these messages
    - or return a 400 level HTTP message stating that there are no messages in the log files in the time interval.

Also the log file generator is supposed to be hosted on EC2 and after running for some time, store the logs into a file in s3 storage.

---

# EC2 - `LogFileGenerator`

1. create EC2 instance - `**logcreator**`
2. install JDK8 and SBT, copy the logfile generator into the EC2.
3. Create an IAM role with `AmazonS3FullAccess` so that the EC2 can access the S3 bucket. Make sure you modify the IAM role of the 
4. Create a Cron job - automating and running a bash script periodically. The bash script `cp`s the log files from the EC2 instance to the S3 bucket. 

```bash
#!/bin/bash
cd LogFileGenerator
sbt run
aws s3 cp LogFileGenerator.2022-10-29 s3://cs453bucket/logs
```

The logfile is transferred to the S3 bucket - `**cs453bucket`** (I messed up the course code, sorry)

---

# Lambda - `logsearch`

Made with *python* and uses AWS API-gateway to communicate. Coded directly on the AWS lambda interface.

1. Reads the .log file from the S3 bucket using a *boto* client. 
2. The data is converted into a *pandas* data-frame (pandas added with layers)
3. We manipulate the data(the timestamp column) accordingly to binary search the log entries, find the log(s) in the time provided $[t-dt, t+dt]$ 
4. Return the *MD5-hashed* value of the first log entry if the binary search was a success or an error message if it was a failure.

---

# REST - `restClient`

1. loading parameters from `application.conf`.
2. sending HTTP get request using `scala.io.Source.fromURL()`.
3. printing the response from the lambda function.

```
[info] running restClient
17:58:48.797 [sbt-bg-threads-3] INFO  restClient$ - Client Started ...
17:58:48.831 [sbt-bg-threads-3] INFO  restClient$ - Timestamp: 13:04:31.023; Time Interval: 0:01:00.0
17:58:48.832 [sbt-bg-threads-3] INFO  restClient$ - Lambda Function called ... at url https://053keq5f5f.execute-api.us-east-1.amazonaws.com/test/logsearch?timestamp=13%3A04%3A31.023&interval=0%3A01%3A00.0
17:58:53.261 [sbt-bg-threads-3] INFO  restClient$ - Response received from lambda. Printing ...{"HTTP_code": "200", "result": "f426634ccdc08964071c44093a5799f3"}
[success] Total time: 7 s, completed 31 Oct, 2022 5:58:53 PM
```

**********Installation instructions:********** A simple `sbt clean compile run`  command starts the `restClient` that communicates with the lambda function and displays the final result.

---

# GRPC - `grpcClient, grpcServer`

1. The GRPC client sends the request with the timestamp and the time interval to the the GRPC server with the help of a protobuf file
2. The GRPC server communicates with the API gateway to send the lambda function the parameters. 
3. The response from the lambda is sent to the client using the protobuf. 

```
19:07:06.813 [sbt-bg-threads-1] INFO  grpcClient$ - Client Started ...
19:07:06.856 [sbt-bg-threads-1] INFO  grpcClient$ - Timestamp: 13:04:31.023; Time Interval: 0:01:00.0
19:07:06.893 [sbt-bg-threads-1] DEBUG i.n.u.i.l.InternalLoggerFactory - Using SLF4J as the default logging framework
19:07:06.893 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - Platform: Windows
19:07:06.897 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - -Dio.netty.noUnsafe: false
19:07:06.897 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - Java version: 8
19:07:06.898 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - sun.misc.Unsafe.theUnsafe: available
19:07:06.899 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - sun.misc.Unsafe.copyMemory: available
19:07:06.899 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.Buffer.address: available
19:07:06.900 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - direct buffer constructor: available
19:07:06.900 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.Bits.unaligned: available, true
19:07:06.901 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable prior to Java9
19:07:06.901 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.DirectByteBuffer.<init>(long, int): available
19:07:06.901 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - sun.misc.Unsafe: available
19:07:06.902 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.tmpdir: C:\Users\karrt\AppData\Local\Temp (java.io.tmpdir)
19:07:06.902 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.bitMode: 64 (sun.arch.data.model)
19:07:06.903 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.maxDirectMemory: 1029177344 bytes
19:07:06.903 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.uninitializedArrayAllocationThreshold: -1
19:07:06.905 [sbt-bg-threads-1] DEBUG io.netty.util.internal.CleanerJava6 - java.nio.ByteBuffer.cleaner(): available
19:07:06.906 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.noPreferDirect: false
19:07:06.976 [sbt-bg-threads-1] DEBUG i.n.c.MultithreadEventLoopGroup - -Dio.netty.eventLoopThreads: 32
19:07:06.988 [sbt-bg-threads-1] DEBUG i.n.u.i.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.initialSize: 1024
19:07:06.988 [sbt-bg-threads-1] DEBUG i.n.u.i.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.maxSize: 4096
19:07:06.992 [sbt-bg-threads-1] DEBUG io.netty.channel.nio.NioEventLoop - -Dio.netty.noKeySetOptimization: false
19:07:06.993 [sbt-bg-threads-1] DEBUG io.netty.channel.nio.NioEventLoop - -Dio.netty.selectorAutoRebuildThreshold: 512
19:07:06.999 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - org.jctools-core.MpscChunkedArrayQueue: available
19:07:07.245 [sbt-bg-threads-1] INFO  grpcClient$ - Client Calling Server ...
19:07:07.310 [grpc-default-executor-0] DEBUG io.netty.util.ResourceLeakDetector - -Dio.netty.leakDetection.level: simple
19:07:07.311 [grpc-default-executor-0] DEBUG io.netty.util.ResourceLeakDetector - -Dio.netty.leakDetection.targetRecords: 4
19:07:07.315 [grpc-default-executor-0] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkAccessible: true
19:07:07.315 [grpc-default-executor-0] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkBounds: true
19:07:07.316 [grpc-default-executor-0] DEBUG i.n.util.ResourceLeakDetectorFactory - Loaded default ResourceLeakDetector: io.netty.util.ResourceLeakDetector@7a60dad7
19:07:07.357 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.numHeapArenas: 10
19:07:07.358 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.numDirectArenas: 10
19:07:07.358 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.pageSize: 8192
19:07:07.358 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxOrder: 11
19:07:07.358 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.chunkSize: 16777216
19:07:07.358 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.smallCacheSize: 256
19:07:07.359 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.normalCacheSize: 64
19:07:07.359 [grpc-default-executor-0] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxCachedBufferCapacity: 32768
wSizeIncrement=983041
19:07:07.935 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND SETTINGS: ack=true
19:07:07.938 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] OUTBOUND HEADERS: streamId=3 headers=GrpcHttp2OutboundHeaders[:authority: localhost:800, :path: /lambda/logsearch, :method: POST, :scheme: http, content-type: application/grpc, te: trailers, user-agent: grpc-java-netty/1.37.0, grpc-accept-encoding: gzip] streamDependency=0 weight=16 exclusive=false padding=0 endStream=false
19:07:07.944 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] OUTBOUND DATA: streamId=3 padding=0 endStream=true length=30 bytes=00000000190a0c31333a30343a33312e3032331209303a30313a30302e30
19:07:07.970 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND PING: ack=false bytes=1234
19:07:07.970 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] OUTBOUND PING: ack=true bytes=1234
19:07:12.898 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND HEADERS: streamId=3 headers=GrpcHttp2ResponseHeaders[:status: 200, content-type: application/grpc, grpc-encoding: identity, grpc-accept-encoding: gzip] padding=0 endStream=false
19:07:12.902 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND DATA: streamId=3 padding=0 endStream=false length=73 bytes=00000000440a427b22485454505f636f6465223a2022323030222c2022726573756c74223a202266343236363334636364633038393634303731633434303933...
19:07:12.902 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] OUTBOUND PING: ack=false bytes=1234
19:07:12.905 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND HEADERS: streamId=3 headers=GrpcHttp2ResponseHeaders[grpc-status: 0] padding=0 endStream=true
***19:07:12.908 [sbt-bg-threads-1] INFO  grpcClient$ - Result is {"HTTP_code": "200", "result": "f426634ccdc08964071c44093a5799f3"}***
19:07:12.908 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] INBOUND PING: ack=true bytes=1234
19:07:12.910 [grpc-nio-worker-ELG-1-2] DEBUG io.grpc.netty.NettyClientHandler - [id: 0xbe173634, L:/127.0.0.1:51073 - R:localhost/127.0.0.1:800] OUTBOUND GO_AWAY: lastStreamId=0 errorCode=0 length=0 bytes=
[success] Total time: 33 s, completed 31 Oct, 2022 7:07:12 PM
19:07:13.926 [grpc-nio-worker-ELG-1-2] DEBUG io.netty.buffer.PoolThreadCache - Freed 7 thread-local buffer(s) from thread: grpc-nio-worker-ELG-1-2
```

```
***19:06:23.916 [sbt-bg-threads-1] INFO  grpcServer$ - Server Started ...***
19:06:23.955 [sbt-bg-threads-1] DEBUG i.n.u.i.l.InternalLoggerFactory - Using SLF4J as the default logging framework
19:06:23.956 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - Platform: Windows
19:06:23.959 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - -Dio.netty.noUnsafe: false
19:06:23.959 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - Java version: 8
19:06:23.959 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - sun.misc.Unsafe.theUnsafe: available
19:06:23.960 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - sun.misc.Unsafe.copyMemory: available
19:06:23.960 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.Buffer.address: available
19:06:23.961 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - direct buffer constructor: available
19:06:23.961 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.Bits.unaligned: available, true
19:06:23.962 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable prior to Java9
19:06:23.962 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent0 - java.nio.DirectByteBuffer.<init>(long, int): available
19:06:23.962 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - sun.misc.Unsafe: available
19:06:23.963 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.tmpdir: C:\Users\karrt\AppData\Local\Temp (java.io.tmpdir)
19:06:23.963 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.bitMode: 64 (sun.arch.data.model)
19:06:23.964 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.maxDirectMemory: 1029177344 bytes
19:06:23.964 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.uninitializedArrayAllocationThreshold: -1
19:06:23.964 [sbt-bg-threads-1] DEBUG io.netty.util.internal.CleanerJava6 - java.nio.ByteBuffer.cleaner(): available
19:06:23.965 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - -Dio.netty.noPreferDirect: false
19:06:24.211 [sbt-bg-threads-1] DEBUG i.n.c.MultithreadEventLoopGroup - -Dio.netty.eventLoopThreads: 32
19:06:24.224 [sbt-bg-threads-1] DEBUG i.n.u.i.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.initialSize: 1024
19:06:24.224 [sbt-bg-threads-1] DEBUG i.n.u.i.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.maxSize: 4096
19:06:24.230 [sbt-bg-threads-1] DEBUG io.netty.channel.nio.NioEventLoop - -Dio.netty.noKeySetOptimization: false
19:06:24.231 [sbt-bg-threads-1] DEBUG io.netty.channel.nio.NioEventLoop - -Dio.netty.selectorAutoRebuildThreshold: 512
19:06:24.236 [sbt-bg-threads-1] DEBUG i.n.util.internal.PlatformDependent - org.jctools-core.MpscChunkedArrayQueue: available
19:06:24.271 [sbt-bg-threads-1] DEBUG io.netty.util.ResourceLeakDetector - -Dio.netty.leakDetection.level: simple
19:06:24.271 [sbt-bg-threads-1] DEBUG io.netty.util.ResourceLeakDetector - -Dio.netty.leakDetection.targetRecords: 4
19:06:24.272 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.numHeapArenas: 10
19:06:24.272 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.numDirectArenas: 10
19:06:24.272 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.pageSize: 8192
19:06:24.272 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxOrder: 11
19:06:24.272 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.chunkSize: 16777216
19:06:24.273 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.smallCacheSize: 256
19:06:24.273 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.normalCacheSize: 64
19:06:24.273 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxCachedBufferCapacity: 32768
19:06:24.273 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.cacheTrimInterval: 8192
19:06:24.273 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.cacheTrimIntervalMillis: 0
19:06:24.274 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.useCacheForAllThreads: true
19:06:24.274 [sbt-bg-threads-1] DEBUG i.n.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxCachedByteBuffersPerChunk: 1023
19:06:24.296 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.channel.DefaultChannelId - -Dio.netty.processId: 38896 (auto-detected)
19:06:24.298 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.util.NetUtil - -Djava.net.preferIPv4Stack: false
19:06:24.298 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.util.NetUtil - -Djava.net.preferIPv6Addresses: false
19:06:24.479 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.util.NetUtil - Loopback interface: lo (Software Loopback Interface 1, 127.0.0.1)
19:06:24.480 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.util.NetUtil - Failed to get SOMAXCONN from sysctl and file \proc\sys\net\core\somaxconn. Default: 200
19:06:24.676 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.channel.DefaultChannelId - -Dio.netty.machineId: 00:50:56:ff:fe:c0:00:08 (auto-detected)
19:06:24.691 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.allocator.type: pooled
19:06:24.691 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.threadLocalDirectBufferSize: 0
19:06:24.691 [grpc-nio-boss-ELG-1-1] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.maxThreadLocalCharBufferSize: 16384
***19:06:24.700 [sbt-bg-threads-1] INFO  grpcServer$ - Server started, listening on 800***
19:07:07.838 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkAccessible: true
19:07:07.838 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkBounds: true
19:07:07.839 [grpc-nio-worker-ELG-3-1] DEBUG i.n.util.ResourceLeakDetectorFactory - Loaded default ResourceLeakDetector: io.netty.util.ResourceLeakDetector@4c5c46ca
19:07:07.907 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND SETTINGS: ack=false settings={MAX_CONCURRENT_STREAMS=2147483647, INITIAL_WINDOW_SIZE=1048576, MAX_HEADER_LIST_SIZE=8192}
19:07:07.910 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.maxCapacityPerThread: 4096
19:07:07.911 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.maxSharedCapacityFactor: 2
19:07:07.911 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.linkCapacity: 16
19:07:07.911 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.ratio: 8
19:07:07.911 [grpc-nio-worker-ELG-3-1] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.delayedQueue.ratio: 8
19:07:07.917 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND WINDOW_UPDATE: streamId=0 windowSizeIncrement=983041
19:07:07.925 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND SETTINGS: ack=false settings={ENABLE_PUSH=0, MAX_CONCURRENT_STREAMS=0, INITIAL_WINDOW_SIZE=1048576, MAX_HEADER_LIST_SIZE=8192}
19:07:07.926 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND SETTINGS: ack=true
19:07:07.928 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND WINDOW_UPDATE: streamId=0 windowSizeIncrement=983041
19:07:07.935 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND SETTINGS: ack=true
19:07:07.951 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND HEADERS: streamId=3 headers=GrpcHttp2Req
uestHeaders[:path: /lambda/logsearch, :authority: localhost:800, :method: POST, :scheme: http, te: trailers, content-type: application/grpc, user-agent: grpc-java-netty/1.37.0, grpc-accept-encoding: gzip] streamDependency=0 weight=16 exclusive=false padding=0 endStream=false
19:07:07.968 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND DATA: streamId=3 padding=0 endStream=true length=30 bytes=00000000190a0c31333a30343a33312e3032331209303a30313a30302e30
19:07:07.968 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND PING: ack=false bytes=1234
19:07:07.972 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] INBOUND PING: ack=true bytes=1234
***19:07:07.984 [grpc-default-executor-0] INFO  grpcServer$ - Lambda Function called ... at url https://053keq5f5f.execute-api.us-east-1.amazonaws.com/test/logsearch?timestamp=13%3A04%3A31.023&interval=0%3A01%3A00.0***
***19:07:12.869 [grpc-default-executor-0] INFO  grpcServer$ - Response received from lambda. Printing ...{"HTTP_code": "200", "result": "f426634ccdc08964071c44093a5799f3"}***
19:07:12.894 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND HEADERS: streamId=3 headers=GrpcHttp2OutboundHeaders[:status: 200, content-type: applicatboundHeaders[:status: 200, content-type: application/grpc, grpc-encoding: identity, grpc-accept-encoding: gzip] padding=tbtboundHeaders[:status: 200, content-type: application/grpc, grpc-encoding: identity, grpc-accept-encoding: gzip] padding=0 endStream=false
19:07:12.900 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND DATA: streamId=3 padding=0 endStream=false length=73 bytes=00000000440a427b22485454505f636f6465223a2022323030222c2022726573756c74223a202266343236363334636364633038393634303731633434303933...
19:07:12.901 [grpc-nio-worker-ELG-3-1] DEBUG io.grpc.netty.NettyServerHandler - [id: 0x43c2ba0a, L:/127.0.0.1:800 - R:/127.0.0.1:51073] OUTBOUND HEADERS: streamId=3 headers=GrpcHttp2OutboundHeaders[grpc-status: 0] padding=0 endStream=true
```

********Instructions:******** After an `sbt clean compile`, run both the `grpcServer`and the `grpcClient` (the server first and when it starts to listen, run the client) for the communication to happen.

---

# Resources

## Scala

[https://www.youtube.com/playlist?list=PLmtsMNDRU0BxryRX4wiwrTZ661xcp6VPM](https://www.youtube.com/playlist?list=PLmtsMNDRU0BxryRX4wiwrTZ661xcp6VPM)

[https://github.com/rockthejvm/scala-at-light-speed](https://github.com/rockthejvm/scala-at-light-speed)

## EC2

[https://www.youtube.com/watch?v=XzWyudb4N04](https://www.youtube.com/watch?v=XzWyudb4N04)

[https://stackoverflow.com/questions/70080505/how-to-pre-install-sbt-on-an-aws-ec2-instance-via-user-data](https://stackoverflow.com/questions/70080505/how-to-pre-install-sbt-on-an-aws-ec2-instance-via-user-data)

[https://docs.datastax.com/en/jdk-install/doc/jdk-install/installOpenJdkDeb.html](https://docs.datastax.com/en/jdk-install/doc/jdk-install/installOpenJdkDeb.html)

[https://www.scala-lang.org/download/](https://www.scala-lang.org/download/) - `curl -fL [https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz](https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz) | gzip -d > cs && chmod +x cs && ./cs setup`

[https://www.youtube.com/watch?v=v952m13p-b4](https://www.youtube.com/watch?v=v952m13p-b4)

## Lambda

[http://www.awslessons.com/2017/accessing-s3-with-lambda-functions/](http://www.awslessons.com/2017/accessing-s3-with-lambda-functions/)

[https://medium.datadriveninvestor.com/importing-log-files-with-ease-cb855982fa8c](https://medium.datadriveninvestor.com/importing-log-files-with-ease-cb855982fa8c)

[https://mkyong.com/python/python-md5-hashing-example/](https://mkyong.com/python/python-md5-hashing-example/)

## GRPC

[https://www.youtube.com/watch?v=778znDnjROg&t=17s](https://www.youtube.com/watch?v=778znDnjROg&t=17s)

[https://scalapb.github.io/docs/grpc/](https://scalapb.github.io/docs/grpc/)

[https://github.com/scalapb/ScalaPB](https://github.com/scalapb/ScalaPB)

## REST

[https://www.youtube.com/watch?v=uFsaiEhr1zs](https://www.youtube.com/watch?v=uFsaiEhr1zs)

[https://blog.sourcerer.io/full-guide-to-developing-rest-apis-with-aws-api-gateway-and-aws-lambda-d254729d6992](https://blog.sourcerer.io/full-guide-to-developing-rest-apis-with-aws-api-gateway-and-aws-lambda-d254729d6992)

[https://alvinalexander.com/scala/how-to-write-scala-http-get-request-client-source-fromurl/](https://alvinalexander.com/scala/how-to-write-scala-http-get-request-client-source-fromurl/) 

---