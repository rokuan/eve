package interpret

import com.rokuan.calliopecore.sentence.Action.ActionType
import com.rokuan.calliopecore.sentence.{INameInfo, ActionObject}
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.nominal.NameObject
import com.rokuan.calliopecore.sentence.structure.{OrderObject, AffirmationObject, QuestionObject, InterpretationObject}
import db.EveDatabase

/**
 * Created by Christophe on 10/10/2015.
 */
class Evaluator {
  protected val context: EveContext = new EveContext(EveDatabase.db)

  def eval(obj: InterpretationObject) = {
    obj match {
      case question: QuestionObject => evalQuestion(question)
      case affirmation: AffirmationObject => evalAffirmation(affirmation)
      case order: OrderObject => evalOrder(order)
    }
  }

  protected def evalQuestion(question: QuestionObject) = {
    import EveObject._

    val expectedType = question.questionType match {
      case QuestionType.HOW_MANY => NumberResultType
      case QuestionType.WHAT | QuestionType.WHO =>
      case QuestionType.WHEN => DateResultType
      case QuestionType.YES_NO => BooleanResultType
      case QuestionType.WHERE => PlaceResultType
    }
  }

  protected def evalAffirmation(affirmation: AffirmationObject) = {
    val action: ActionObject = affirmation.getAction

    if(action.does(ActionType.BE)){

    } else if(action.does(ActionType.HAVE)){

    } else if(action.isAFieldAction) {
      val field = action.getBoundField
      val fieldObject: NameObject = new NameObject()

      fieldObject.`object` = new INameInfo {
        override def getValue: String = field

        override def getNameTag: String = field
      }
    }
  }

  protected def evalOrder(order: OrderObject) = {

  }
}
