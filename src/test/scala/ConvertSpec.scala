import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.IAction.{ActionType, Form, Tense}
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence.structure.OrderObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal.UnitObject.UnitType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{NameObject, UnitObject}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Christophe on 22/05/2016.
  */
class ConvertSpec extends FlatSpec with Matchers {
  "The conversion" should "return 3600" in {
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
  }
}
