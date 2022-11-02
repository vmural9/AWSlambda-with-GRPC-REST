//GRPC imports
import buffer.{call, lambdaGrpc, response}
import io.grpc.{Server, ServerBuilder}
import HelperUtils.CreateLogger
import com.typesafe.config.{Config, ConfigFactory}

import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

object grpcServer {

  val logger = CreateLogger(classOf[grpcServer.type])

  val conf: Config = ConfigFactory.load("server.conf")
  val endpoint: String = conf.getString("serverConfig.endpoint")
  val port: Int = conf.getInt("serverConfig.port")

  def main(args: Array[String]): Unit = {

    logger.info("Server Started ...")
    val server = new grpcServer(ExecutionContext.global)
    server.begin()
    server.blockUntilTermination()
  }
}

class grpcServer(executionContext: ExecutionContext) {
  self =>
  private[this] var server: Server = null

  private[this] val logger = CreateLogger(classOf[grpcServer.type])


  private def begin(): Unit = {

    server = ServerBuilder.forPort(grpcServer.port).addService(lambdaGrpc.bindService(new lambdaImpl, executionContext)).build.start
    logger.info("Server started, listening on " + grpcServer.port)
    sys.addShutdownHook {
      System.err.println("shutting down gRPC server since JVM is shutting down")
      self.terminate()
      System.err.println("server shut down")
    }
  }

  private def terminate(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilTermination(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  class lambdaImpl extends lambdaGrpc.lambda {
    override def logsearch(req: call) = {
      try{
        val url = grpcServer.endpoint + "?timestamp=" + URLEncoder.encode(req.timestamp, "UTF-8") + "&interval=" + URLEncoder.encode(req.interval, "UTF-8")
        logger.info(s"Lambda Function called ... at url $url ")
        val lambda_out = scala.io.Source.fromURL(url).mkString
        logger.info(s"Response received from lambda. Printing ...$lambda_out")
        val temp = response(result = lambda_out)
        Future.successful(temp)
      }
      catch {
        case _: Throwable =>
          val message = "Failed to connect to AWS lambda function. Check inputs in config file."
          logger.error(message)
          val temp = response(result = message)
          Future.successful(temp)
      }
    }
  }
}
