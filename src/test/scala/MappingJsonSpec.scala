import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.ideal.evecore.common.Mapping
import com.ideal.evecore.universe.matcher.ValueMatcher
import org.scalatest.{FlatSpec, Matchers}
import com.ideal.evecore.universe.EValueMatcher._
import com.ideal.evecore.common.Conversions._
import com.ideal.evecore.io.serialization.ValueMatcherSerialization.{ValueMatcherDeserializer, ValueMatcherSerializer}

/**
  * Created by Christophe on 01/05/2017.
  */
class MappingJsonSpec extends FlatSpec with Matchers {
  val mapper = new ObjectMapper()
  val module = new SimpleModule()
  module.addSerializer(classOf[ValueMatcher], new ValueMatcherSerializer)
  module.addDeserializer(classOf[ValueMatcher], new ValueMatcherDeserializer)
  mapper.registerModule(module)

  "1" should "2" in {
    val mappings: Mapping[ValueMatcher] = Map[String, ValueMatcher](
      "action" -> "DISPLAY",
      "what" -> Map[String, ValueMatcher]("eve_type" -> "location")
    )
    val json: String = mapper.writeValueAsString(mappings)
    println(json)
    val back = mapper.readValue(json, classOf[Mapping[ValueMatcher]])
    println(back)
  }
}
