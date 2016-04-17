package com.ideal.eve.interpret

import java.util.Locale

import com.ideal.eve.controller.Translator
import com.ideal.eve.db.{EveDatabase, Writer}
import com.ideal.eve.server.EveSession
import com.rokuan.calliopecore.sentence.{IAction, ActionObject}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{VerbalGroup, UnitObject, LanguageObject, NameObject}
import com.rokuan.calliopecore.sentence.structure.{OrderObject, AffirmationObject, QuestionObject, InterpretationObject}
import com.ideal.eve.universe.{ActionMessage, World}

/**
  * Created by Christophe on 10/10/2015.
  */
object Evaluator {
  val database: EveDatabase = new EveDatabase()

  def apply() = {
    new Evaluator(new EveContext(EveDatabase.db), database)
  }
}

class Evaluator(val context: EveContext, val database: EveDatabase) {
  def eval(obj: InterpretationObject)(implicit session: EveSession) = {
    obj match {
      case question: QuestionObject => evalQuestion(question)
      case affirmation: AffirmationObject => evalAffirmation(affirmation)
      case order: OrderObject => evalOrder(order)
    }
  }

  protected def evalQuestion(question: QuestionObject)(implicit session: EveSession) = {
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
    val actionType = action.getMainAction.getAction

    if(actionType == ActionType.BE){
      val result = database.findObject(context, question.getDirectObject)
      // TODO: decommenter et verifier qu'il y a bien un resultat
      //result.map(v => v.asInstanceOf[expectedType.type])
      //result.get.asInstanceOf[expectedType.type].toString
      result.get.toString
    }
  }

  protected def evalAffirmation(affirmation: AffirmationObject)(implicit session: EveSession) = {
    val action: IAction = affirmation.getAction.getMainAction
    val actionType = action.getAction

    // TODO: prendre en compte le temps du verbe (uniquement present pour l'instant)

    if(actionType == ActionType.BE){
      //database.update(context, affirmation.getSubject, affirmation.affirmation)
      //database.set(context, affirmation.getSubject, affirmation)
    } else if(actionType == ActionType.HAVE){
      database.update(context, affirmation.getSubject, affirmation.getDirectObject)
    } else if(action.isFieldBound) {
      val field = action.getBoundField
      database.set(context, affirmation.getSubject, field, affirmation.getDirectObject)
    }
  }

  protected def evalOrder(order: OrderObject)(implicit session: EveSession) = {
    val action: IAction = order.getAction.getMainAction
    val actionType = action.getAction

    if(action.isStateBound) {
      val stateKey = action.getBoundState
      val stateValue = action.getState
      database.updateState(context, order.getDirectObject, stateKey, stateValue)
    } else {
      actionType match {
        case ActionType.CHECK => {
          order.getDirectObject match {
            case verb: VerbalGroup =>
              database.check(context, verb)
          }
        }

        case ActionType.CONVERT => {
          val unit = order.getWayAdverbial match {
            case u: UnitObject => u.unitType
            case _ => throw new RuntimeException("Cannot convert to unspecified unit")
          }

          val quantityToConvert = database.findObject(context, order.getDirectObject)
          quantityToConvert.map(result => {
            result match {
              case EveStructuredObject(o) if o.getAs[String](EveDatabase.ClassKey).getOrElse("") == Writer.UnitObjectType.getName =>
                // TODO:
                new EveStructuredObject(null)
              case _ => throw new RuntimeException("Only units can be converted")
            }
          })
        }

        case ActionType.TRANSLATE => {
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

        case ActionType.SEND => {

        }

        case _ => {
          // TODO:
          if (order.getDirectObject == null && order.getTarget == null) {
            // TODO: interpretation interne d'Eve
          } else if (order.getTarget == null) {
            val dest = database.findObject(context, order.getDirectObject, true)
            dest.map(target => target match {
              case EveStructuredObject(o) => {
                World.findReceiver(o.underlying).map(r => r.handleMessage(ActionMessage(actionType)))
              }
              case EveStructuredObjectList(os) => {
                os.collect { case EveStructuredObject(o) => o }
                    .flatMap { o => World.findReceiver(o.underlying) }
                    .map(r => r.handleMessage(ActionMessage(actionType)))
              }
            })
          } else {
            val what = database.findObject(context, order.getDirectObject, true)
            val to = database.findObject(context, order.getTarget, true)

            what.map(src =>
              to.map(target => target)
            )
          }
        }
      }
    }
  }
}
