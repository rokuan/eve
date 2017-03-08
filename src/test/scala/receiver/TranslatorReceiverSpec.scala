package receiver

import com.ideal.eve.universe.receivers.TranslationReceiver
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by chris on 08/03/17.
 */
class TranslatorReceiverSpec extends FlatSpec with Matchers {
  "The receiver" should "translate Bonjour to Hello" in {
    val receiver = new TranslationReceiver
    receiver.translate("Bonjour", "en").map { t =>
      //println(t)
    }
  }
}
