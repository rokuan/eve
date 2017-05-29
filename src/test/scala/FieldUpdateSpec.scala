import com.ideal.eve.db.EveEvaluator
import com.ideal.eve.interpret.EveContext
import com.ideal.eve.server.EveSession
import com.ideal.evecore.interpreter.EObject
import com.ideal.evecore.universe.MinimalWorld
import com.ideal.evecore.util.Result
import com.rokuan.calliopecore.sentence.IAction.{ActionType, Form, Tense}
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{NameObject, PersonObject, PronounSubject}
import com.rokuan.calliopecore.sentence.structure.{AffirmationObject, QuestionObject}
import org.scalatest.{FlatSpec, Matchers}
import com.ideal.evecore.interpreter.EObject._
import com.ideal.evecore.interpreter.EveObjectDSL._

/**
  * Created by Christophe on 25/10/2015.
  */
class FieldUpdateSpec extends FlatSpec with Matchers {
  "The database" should "change my name to Christophe" in {
    val myNameIsChristophe = new AffirmationObject()
    val i: PronounSubject = new PronounSubject(new IPronoun {
      override def getValue: String = "je"
      override def getSource: PronounSource = PronounSource.I
    })
    val named: IAction = new IAction {
      override def getValue: String = "m'appelle"
      override def isFieldBound: Boolean = true
      override def getAction: ActionType = ActionType.BE_NAMED
      override def getBoundField: String = "name"
      override def getForm: Form = Form.INDICATIVE
      override def getTense: Tense = Tense.PRESENT
      override def getState: String = null
      override def isStateBound: Boolean = false
      override def getBoundState: String = null
      override def isTargetAction: Boolean = false
    }

    myNameIsChristophe.setSubject(i)
    myNameIsChristophe.setAction(new ActionObject(Tense.PRESENT, named))
    myNameIsChristophe.setDirectObject(new PersonObject("Toto"))

    val whatIsMyName = new QuestionObject

    val is = new IAction {
      override def getValue: String = "est"
      override def isFieldBound: Boolean = false
      override def getAction: ActionType = ActionType.BE
      override def getBoundField: String = null
      override def getForm: Form = Form.INDICATIVE
      override def getTense: Tense = Tense.PRESENT
      override def getState: String = null
      override def isStateBound: Boolean = false
      override def getBoundState: String = null
      override def isTargetAction: Boolean = false
    }
    val myName = new NameObject()
    myName.count.definition = ArticleType.POSSESSIVE
    myName.count.possessiveTarget = new IPronoun {
      override def getValue: String = "mon"
      override def getSource: PronounSource = PronounSource.I
    }
    myName.`object` = new INameInfo {
      override def getValue: String = "nom"
      override def getNameTag: String = "name"
    }

    whatIsMyName.questionType = QuestionType.WHAT
    whatIsMyName.setAction(new ActionObject(Tense.PRESENT, is))
    whatIsMyName.setDirectObject(myName)

    // TODO:
    val world = new MinimalWorld
    val context = EveContext()
    val mySession = new EveSession("chris")
    val evaluator = new EveEvaluator(context, world)(mySession)
    evaluator.eval(myNameIsChristophe)
    val result = evaluator.eval(whatIsMyName)
    if (result.isSuccess) {
      val o = implicitly[EObject](result.get())
      val s = o.toText
      s shouldBe "Christophe"
    } else {
      throw new Exception("Value did not match")
    }
  }
}
