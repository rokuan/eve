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
  protected val database: EveDatabase = new EveDatabase()

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
      case QuestionType.WHAT | QuestionType.WHO => classOf[EveObject]
      case QuestionType.WHEN => DateResultType
      case QuestionType.YES_NO => BooleanResultType
      case QuestionType.WHERE => PlaceResultType
      case _ => classOf[EveObject]
    }

    val action = question.getAction

    if(action.does(ActionType.BE)){
      val result = database.findObject(context, question.getDirectObject)
      // TODO: decommenter et verifier qu'il y a bien un resultat
      //result.map(v => v.asInstanceOf[expectedType.type])
      //result.get.asInstanceOf[expectedType.type].toString
      result.get.toString
    }
  }

  protected def evalAffirmation(affirmation: AffirmationObject) = {
    val action: ActionObject = affirmation.getAction

    // TODO: prendre en compte le temps du verbe (uniquement present pour l'instant)

    if(action.does(ActionType.BE)){
      //database.update(context, affirmation.getSubject, affirmation.affirmation)
      //database.set(context, affirmation.getSubject, affirmation)
    } else if(action.does(ActionType.HAVE)){
      //database.set(context, )
    } else if(action.isAFieldAction) {
      val field = action.getBoundField
      database.set(context, affirmation.getSubject, field, affirmation.getDirectObject)
    }
  }

  protected def evalOrder(order: OrderObject) = {

  }
}
