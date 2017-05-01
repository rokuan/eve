import com.ideal.eve.db.WordDatabase
import com.rokuan.calliopecore.fr.autoroute.parser.SentenceParser
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by chris on 13/04/2017.
 */
class ParseSpec extends FlatSpec with Matchers {
  val parser = new SentenceParser(WordDatabase)

  "This sentence" should "be parsed into a QuestionObject" in {
    val sentence = "quelle sera leur position"
    val obj = parser.parseText(sentence)
    println(obj.getDirectObject.getClass)
  }

  "This sentence" should "be parsed into another QuestionObject" in {
    val sentence = "quel arbre es-tu"
    val obj = parser.parseText(sentence)
    println(obj.getDirectObject.getClass)
  }

  "1" should "2" in {

  }
}
