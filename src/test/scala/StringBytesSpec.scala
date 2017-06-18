import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.ideal.evecore.io.command.user.{EvaluateCommand, UserCommand}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Christophe on 16/06/2017.
  */
class StringBytesSpec extends FlatSpec with Matchers {
  val mapper = new ObjectMapper()
  val module = new SimpleModule()
  //module.addSerializer(classOf[UserCommand], new )

  "The JSON" should "end with a closing curly" in {
    val sentence = "redémarre"
    val command = new EvaluateCommand(sentence)
    //val json = mapper.writerFor(classOf[UserCommand]).writeValueAsString(command)
    val json = mapper.writeValueAsString(command)
    val bytes = json.getBytes()
    println(bytes.length + "/" + json.length)
    println(new String(bytes, 0, json.length))
  }
}
