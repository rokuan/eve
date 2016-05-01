package com.ideal.eve.interpret

import java.util.Locale

import com.ideal.eve.db.{EveDatabase, Writer}
import com.ideal.eve.server.EveSession
import com.ideal.eve.universe.concurrent.TaskPool
import com.ideal.eve.universe.receivers.Translator
import com.rokuan.calliopecore.sentence.{ActionObject, IAction}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.data.nominal.{LanguageObject, NameObject, UnitObject, VerbalGroup}
import com.rokuan.calliopecore.sentence.structure.{AffirmationObject, InterpretationObject, OrderObject, QuestionObject}
import com.ideal.eve.universe.{ActionEveMessage, DBObjectValueSource, World}

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

        case _ => {
          // TODO:
          if (order.getDirectObject == null && order.getTarget == null) {
            // TODO: interpretation interne d'Eve
          } else if (order.getTarget == null) {
            val dest = database.findObject(context, order.getDirectObject, true)
            dest.map(target => target match {
              case EveStructuredObject(o) => TaskPool.scheduleDelayedTask(actionType, List(DBObjectValueSource(o)), order.when)
              case EveStructuredObjectList(os) => {
                val objects = os.collect { case EveStructuredObject(o) => DBObjectValueSource(o) }.toList
                TaskPool.scheduleDelayedTask(actionType, objects, order.when)
              }
            })
          } else {
            val what = database.findObject(context, order.getDirectObject, true)
            val to = database.findObject(context, order.getTarget)

            what.map { src =>
              to.map(target => target)
            }
          }
        }
      }
    }
  }
}
