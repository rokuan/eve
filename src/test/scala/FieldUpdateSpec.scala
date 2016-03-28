import com.ideal.eve.server.EveSession
import com.rokuan.calliopecore.sentence.IAction.{Tense, Form, ActionType}
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{PersonObject, NameObject, PronounSubject}
import com.rokuan.calliopecore.sentence.structure.{QuestionObject, AffirmationObject}
import com.ideal.eve.interpret.{EveStringObject, Evaluator}
import org.scalatest.{Matchers, FlatSpec}

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
    }

    myNameIsChristophe.setSubject(i)
    myNameIsChristophe.setAction(new ActionObject(Tense.PRESENT, named))
    myNameIsChristophe.setDirectObject(new PersonObject("Christophe"))

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

    val evaluator = Evaluator()
    val mySession = new EveSession("chris")
    evaluator.eval(myNameIsChristophe)(mySession)
    val result = evaluator.eval(whatIsMyName)(mySession)
    assert(result.isInstanceOf[EveStringObject])
    assert(result.asInstanceOf[EveStringObject].s == "Christophe")
  }
}
