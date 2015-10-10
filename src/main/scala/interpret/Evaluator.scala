package interpret

import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.{OrderObject, AffirmationObject, QuestionObject, InterpretationObject}

/**
 * Created by Christophe on 10/10/2015.
 */
class Evaluator {
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

  }

  protected def evalOrder(order: OrderObject) = {

  }
}
