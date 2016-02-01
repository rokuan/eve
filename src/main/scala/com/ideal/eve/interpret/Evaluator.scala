package com.ideal.eve.interpret

import java.util.Locale

import com.ideal.eve.controller.Translator
import com.ideal.eve.db.{EveDatabase, Writer}
import com.rokuan.calliopecore.sentence.Action.ActionType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.WayType
import com.rokuan.calliopecore.sentence.{INameInfo, ActionObject}
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{UnitObject, LanguageObject, NameObject}
import com.rokuan.calliopecore.sentence.structure.{OrderObject, AffirmationObject, QuestionObject, InterpretationObject}
import com.ideal.eve.universe.{ActionMessage, World}

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
    val actionType = action.getAction

    if(action == ActionType.BE){
      val result = database.findObject(context, question.getDirectObject)
      // TODO: decommenter et verifier qu'il y a bien un resultat
      //result.map(v => v.asInstanceOf[expectedType.type])
      //result.get.asInstanceOf[expectedType.type].toString
      result.get.toString
    }
  }

  protected def evalAffirmation(affirmation: AffirmationObject) = {
    val action: ActionObject = affirmation.getAction
    val actionType = action.getAction

    // TODO: prendre en compte le temps du verbe (uniquement present pour l'instant)

    if(actionType == ActionType.BE){
      //database.update(context, affirmation.getSubject, affirmation.affirmation)
      //database.set(context, affirmation.getSubject, affirmation)
    } else if(actionType == ActionType.HAVE){
      //database.set(context, )
    } else if(action.isAFieldAction) {
      val field = action.getBoundField
      database.set(context, affirmation.getSubject, field, affirmation.getDirectObject)
    }
  }

  protected def evalOrder(order: OrderObject) = {
    val action: ActionObject = order.getAction
    val actionType = action.getAction

    actionType match {
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
              o.getAs[String](EveDatabase.CodeKey).map(code =>
                World.getReceiver(code).map(r =>
                  r.handleMessage(ActionMessage(ActionType.TURN_OFF))
                )
              )
            }
            case EveStructuredObjectList(os) => {
              os.collect { case EveStructuredObject(o) if o.get(EveDatabase.CodeKey).isDefined => o }
                .flatMap(o => World.getReceiver(o.getAs[String](EveDatabase.CodeKey).get))
                .map(r => r.handleMessage(ActionMessage(ActionType.TURN_OFF))) // TODO: recuperer l'action principale
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
