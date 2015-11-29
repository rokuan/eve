import java.util

import com.rokuan.calliopecore.sentence.Action.ActionType
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence._
import com.rokuan.calliopecore.sentence.IVerbConjugation.{Form, Tense}
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.count.{AllItemsObject, CountObject}
import com.rokuan.calliopecore.sentence.structure.data.nominal.{NameObject, PronounSubject}
import com.rokuan.calliopecore.sentence.structure.{QuestionObject, AffirmationObject, InterpretationObject}
import interpret.{EveStringObject, Evaluator}
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
    val named: IVerbConjugation = new IVerbConjugation {
      override def getValue: String = "m'appelle"
      override def does(actionType: ActionType): Boolean = false

      override def getVerb: IVerb = new IVerb {
        override def getValue: String = "s'appeler"
        override def isAFieldAction: Boolean = true
        override def hasAction(actionType: ActionType): Boolean = false
        override def getActions: util.Set[ActionType] = null
        override def getBoundField: String = "name"
      }

      override def getForm: Form = Form.INDICATIVE
      override def getTense: Tense = Tense.PRESENT
    }
    val firstname = new NameObject()
    firstname.`object` = new INameInfo {
      override def getValue: String = "Christophe"
      override def getNameTag: String = null
    }

    myNameIsChristophe.setSubject(i)
    myNameIsChristophe.setAction(new ActionObject(Tense.PRESENT, named))
    myNameIsChristophe.setDirectObject(firstname)

    val whatIsMyName = new QuestionObject

    val is = new IVerbConjugation {
      override def getValue: String = "est"
      override def does(actionType: ActionType): Boolean = getVerb.hasAction(actionType)

      override def getVerb: IVerb = new IVerb {
        override def getValue: String = "être"
        override def isAFieldAction: Boolean = false
        override def hasAction(actionType: ActionType): Boolean = actionType == ActionType.BE
        override def getActions: util.Set[ActionType] = null
        override def getBoundField: String = null
      }

      override def getForm: Form = Form.INDICATIVE
      override def getTense: Tense = Tense.PRESENT
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

    val evaluator = new Evaluator()
    evaluator.eval(myNameIsChristophe)
    val result = evaluator.eval(whatIsMyName))
    assert(result.isInstanceOf[EveStringObject])
    assert(result.asInstanceOf[EveStringObject].s == "Christophe")
  }
}
