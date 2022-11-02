//GRPC imports
import buffer.lambdaGrpc.lambdaBlockingStub
import buffer.{lambdaGrpc, call}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}

import HelperUtils.CreateLogger
import com.typesafe.config.{Config, ConfigFactory}
import java.util.concurrent.TimeUnit

object grpcClient {

  val logger = CreateLogger(classOf[grpcClient.type])

  def apply(host: String, port: Int): grpcClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = lambdaGrpc.blockingStub(channel)
    new grpcClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {

    logger.info("Client Started ...")

    val conf: Config = ConfigFactory.load("client.conf")
    val timestamp = conf.getString("clientConfig.timestamp")
    val interval = conf.getString("clientConfig.interval")
    val port: Int = conf.getInt("clientConfig.port")

    logger.info("Timestamp: " + timestamp + "; Time Interval: " + interval)

    val client = grpcClient("localhost",port)

    logger.info("Client Calling Server ...")
    try {
      client.callServer(timestamp, interval)
    }
    finally {
      client.terminate()
    }
  }
}

class grpcClient private(private val channel: ManagedChannel, private val blockingStub: lambdaBlockingStub) {
  private[this] val logger = CreateLogger(classOf[grpcClient.type])

  def terminate(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def callServer(timestamp: String, interval: String): Unit = {
    val request = call(timestamp, interval)
    try{
      val response = blockingStub.logsearch(request)
      logger.info("Result is " + response.result)
    }
    catch {
      case e:StatusRuntimeException =>
        logger.error("Failed to connect with the Server")
    }
  }
}
