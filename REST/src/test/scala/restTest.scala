import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers._
import java.time.LocalTime

class grpcTest extends AnyFlatSpec with Matchers {

  val clientConfig = ConfigFactory.load("client.conf")

  it should "match endpoint in REST-client" in {
    val restEndpoint: String = clientConfig.getString("clientConfig.endpoint")
    assert(restEndpoint.equals("https://053keq5f5f.execute-api.us-east-1.amazonaws.com/test/logsearch"))
  }
  it should "match timestamp" in {
    val inputTime: String = clientConfig.getString("clientConfig.timestamp")
    assert(inputTime.equals("13:04:31.023"))
  }
}