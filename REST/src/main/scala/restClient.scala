/*
Scala client to invoke the lambda function using rest API calls.
The calls are made with the help of Amazon's API-Gateway that connects the client and the Lambda
The bucket details and the logfile name is hardcoded in the lambda.
The return message consists of a HTTP level code: 200 or 400 where the result is "not found" or the hash code of the message respectively.
*/


import HelperUtils.CreateLogger
import com.typesafe.config.{Config, ConfigFactory}

import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object restClient {

  val logger = CreateLogger(classOf[restClient.type])

  def main(args: Array[String]): Unit = {

    logger.info("Client Started ... ")
    
    /*
    1. loading parameters from client.conf.
    2. sending HTTP get request using scala.io.Source.fromURL().
    3. printing the response from the lambda function.
    */

    val conf: Config = ConfigFactory.load("client.conf")
    val endpoint: String = conf.getString("clientConfig.endpoint")
    val timestamp = conf.getString("clientConfig.timestamp")
    val interval = conf.getString("clientConfig.interval")

    logger.info("Timestamp: " + timestamp + "; Time Interval: " + interval)


    val url = endpoint+"?timestamp="+URLEncoder.encode(timestamp,"UTF-8")+"&interval="+URLEncoder.encode(interval,"UTF-8")
    logger.info(s"Lambda Function called ... at url $url ")
    val lambda_out = scala.io.Source.fromURL(url).mkString

    logger.info(s"Response received from lambda. Printing ...$lambda_out")
  }

}
