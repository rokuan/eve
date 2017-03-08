import com.ideal.eve.db.EveEvaluator
import com.ideal.eve.environment.EveEnvironment
import com.ideal.eve.interpret.EveContext
import com.ideal.eve.server.EveSession
import com.ideal.eve.universe.Universe
import com.ideal.evecore.interpreter.{EveFailureObject, EveSuccessObject, EveStringObject, EveStructuredObject}
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

    Universe.registerReceiver(converterReceiver)

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

    val myAgeIs26Years = new OrderObject
    myAgeIs26Years.subject = i
    myAgeIs26Years.action = new ActionObject(Tense.PRESENT, new IAction {
      override def getForm: Form = Form.IMPERATIVE
      override def getAction: ActionType = ActionType.HAVE
      override def getTense: Tense = Tense.PRESENT
      override def isFieldBound: Boolean = true
      override def getBoundField: String = "age"
      override def getValue: String = "ai"
      override def getState: String = null
      override def isStateBound: Boolean = false
      override def getBoundState: String = null
    })
    myAgeIs26Years.what = years

    val mySession = new EveSession("chris")
    val evaluator = new EveEvaluator()(mySession)
    evaluator.eval(myAgeIs26Years)
    val result = evaluator.eval(convertMyAgeIntoSeconds)

    Universe.unregisterReceiver(converterReceiver)

    result match {
      case EveSuccessObject(v) => println(v)
      case EveFailureObject(e) => sys.error(e)
    }
  }
}
