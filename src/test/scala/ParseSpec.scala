import com.ideal.eve.db.WordDatabase
import com.rokuan.calliopecore.fr.autoroute.parser.SentenceParser
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by chris on 13/04/2017.
 */
class ParseSpec extends FlatSpec with Matchers {
  "This sentence " should "be parsed into a QuestionObject" in {
    val sentence = "quelle est la position"
    val parser = new SentenceParser(WordDatabase)
    val obj = parser.parseText(sentence)
    println(obj.getDirectObject.getClass)
  }
}
