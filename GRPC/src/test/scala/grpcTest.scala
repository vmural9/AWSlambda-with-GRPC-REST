import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers._
import java.time.LocalTime

class grpcTest extends AnyFlatSpec with Matchers {

  val clientConfig = ConfigFactory.load("client.conf")
  val serverConfig = ConfigFactory.load("server.conf")

  it should "match endpoint in GRPC-server" in {
    val grpcEndpoint: String = serverConfig.getString("serverConfig.endPoint")
    assert(grpcEndpoint.equals("https://053keq5f5f.execute-api.us-east-1.amazonaws.com/test/logsearch"))
  }
  it should "match port in GRPC-Client" in {
    val gprcPort: Int = clientConfig.getInt("clientConfig.port")
    assert(grpcPort.equals(800))
  }
  it should "match port in GRPC-Server" in {
    val gprcPort: Int = serverConfig.getInt("serverConfig.port")
    assert(grpcPort.equals(800))
  }
  it should "match timestamp" in {
    val inputTime: String = clientConfig.getString("clientConfig.timestamp")
    assert(inputTime.equals("13:04:31.023"))
  }
}