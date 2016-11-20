import com.ideal.eve.interpret.{EveContext, EveEvaluator}
import com.ideal.eve.server.EveSession
import com.ideal.evecore.interpreter.{EveStringObject, EveStructuredObject}
import com.ideal.evecore.io.CommonKey
import com.ideal.evecore.universe.World
import com.ideal.evecore.universe.receiver.base.UnitConverterController
import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.IAction.{ActionType, Form, Tense}
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence.structure.{AffirmationObject, OrderObject}
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal.UnitObject.UnitType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{NameObject, PronounSubject, QuantityObject, UnitObject}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

/**
  * Created by Christophe on 22/05/2016.
  */
class ConvertSpec extends FlatSpec with Matchers {
  "The conversion" should "return 3600" in {
    val converterReceiver = new UnitConverterController

    World.registerReceiver(converterReceiver)

    val i = new PronounSubject(new IPronoun {
      override def getSource: PronounSource = PronounSource.I
      override def getValue: String = "moi je"
    })

    val years = new QuantityObject()
    years.amount = 26
    years.unitType = UnitType.YEAR

    val convert = new IAction {
      override def getForm: Form = Form.IMPERATIVE
      override def getAction: ActionType = ActionType.CONVERT
      override def getTense: Tense = Tense.PRESENT
      override def isFieldBound: Boolean = false
      override def getBoundField: String = null
      override def getValue: String = "convertis"
      override def getState: String = null
      override def isStateBound: Boolean = false
      override def getBoundState: String = null
    }
    val action = new ActionObject(Tense.PRESENT, convert)
    val myAge = new NameObject()
    myAge.count.definition = ArticleType.POSSESSIVE
    myAge.count.possessiveTarget = new IPronoun {
      override def getSource: PronounSource = PronounSource.I
      override def getValue: String = "mon"
    }
    myAge.`object` = new INameInfo {
      override def getNameTag: String = "AGE"
      override def getValue: String = "âge"
    }
    val inSeconds = new UnitObject
    inSeconds.unitType = UnitType.SECOND

    val convertMyAgeIntoSeconds = new OrderObject
    convertMyAgeIntoSeconds.action = action
    convertMyAgeIntoSeconds.what = myAge
    convertMyAgeIntoSeconds.how = inSeconds

    val mySession = new EveSession("chris")
    val evaluator = new EveEvaluator(EveContext())(mySession)
    //evaluator.eval(myAgeIs26Years)
    evaluator.storage.set(evaluator.context, i, "age", years)
    val result = evaluator.eval(convertMyAgeIntoSeconds)

    World.unregisterReceiver(converterReceiver)

    result match {
      case Success(v) => println(v)
      case Failure(e) => e.printStackTrace()
    }
  }
}
