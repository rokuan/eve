package interpret

import java.util.Locale

import com.rokuan.calliopecore.sentence.Action.ActionType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.WayType
import com.rokuan.calliopecore.sentence.{INameInfo, ActionObject}
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{UnitObject, LanguageObject, NameObject}
import com.rokuan.calliopecore.sentence.structure.{OrderObject, AffirmationObject, QuestionObject, InterpretationObject}
import controller.Translator
import db.{ObjectWriter, EveDatabase}

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
    val action: ActionObject = order.getAction

    if(action.does(ActionType.CONVERT)){
      val unit = order.getWayAdverbial match {
        case u: UnitObject => u.unitType
        case _ => throw new RuntimeException("Cannot convert to unspecified unit")
      }

      val quantityToTranslate = database.findObject(context, order.getDirectObject)
      quantityToTranslate.map(result => {
        result match {
          case EveStructuredObject(o) if o.getAs[String](EveDatabase.ClassKey).getOrElse("") == ObjectWriter.UnitObjectType.getName =>
            // TODO:
            new EveStructuredObject(null)
          case _ => throw new RuntimeException("Only units can be converted")
        }
      })
    } else if(action.does(ActionType.TRANSLATE)){
      val language = order.getWayAdverbial match {
        case l: LanguageObject => l.language.getLanguageCode
        case _ => Locale.getDefault.getLanguage
      }

      val textToTranslate = database.findObject(context, order.getDirectObject)
      textToTranslate.map(result => {
        result match {
          case EveStringObject(text) => new EveStringObject(Translator.translate(text, language))
          case EveStructuredObjectList(objects) =>
            val translations = objects.collect { case EveStringObject(s) => s }.map(text => new EveStringObject(Translator.translate(text, language)))
            new EveStructuredObjectList(translations)
        }
      })
    }
  }
}
