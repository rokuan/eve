import com.ideal.eve.db.EveEvaluator
import com.ideal.eve.interpret.EveContext
import com.ideal.eve.server.EveSession
import com.ideal.evecore.universe.MinimalWorld
import com.ideal.evecore.universe.receiver.base.UnitConverterController
import com.ideal.evecore.util.Result
import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.IAction.{ActionType, Form, Tense}
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence.structure.OrderObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal.UnitObject.UnitType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{NameObject, PronounSubject, QuantityObject, UnitObject}
import org.scalatest.{FlatSpec, Matchers}


/**
  * Created by Christophe on 22/05/2016.
  */
class ConvertSpec extends FlatSpec with Matchers {
  "The conversion" should "return 3600" in {
    val converterReceiver = new UnitConverterController

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

      override def isTargetAction: Boolean = false
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

      override def isTargetAction: Boolean = false
    })
    myAgeIs26Years.what = years

    val mySession = new EveSession("chris")
    val world = new MinimalWorld
    world.registerReceiver(converterReceiver)
    val context = EveContext()
    val evaluator = new EveEvaluator(context, world)(mySession)
    evaluator.eval(myAgeIs26Years)
    val result = evaluator.eval(convertMyAgeIntoSeconds)

    if (result.isSuccess) {
      println(result.get())
    } else {
      sys.error(result.getError.getMessage)
    }
  }
}
