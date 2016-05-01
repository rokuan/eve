package com.ideal.eve.universe.concurrent

import java.util.{Date, Timer, TimerTask}

import com.ideal.eve.universe._
import com.rokuan.calliopecore.sentence.structure.data.nominal.VerbalGroup
import com.ideal.eve.utils.TimeObjectConversions._
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.{ActionObject, IAction}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.content.{ITimeObject, IWayObject}

/**
  * Created by Christophe on 24/04/2016.
  */
object TaskPool {
  val timer = new Timer()

  def scheduleDelayedTask(action: ActionType, values: List[ObjectValueSource], time: ITimeObject): Unit = {
    time match {
      case v: VerbalGroup =>
      case _ => scheduleDelayedTask(action, values, time: Date)
    }
  }

  def scheduleDelayedTask(action: ActionType, values: List[ObjectValueSource], date: Date): Unit = {
    val task = new TimerTask {
      override def run(): Unit = values.foreach { v => World.findReceiver(v).map(_.handleMessage(ActionEveMessage(action, values))) }
    }
    timer.schedule(task, date)
  }

  def scheduleDelayedTask(action: IAction, values: List[ValueSource], when: ITimeObject, how: IWayObject, to: ValueSource = NullValueSource): Unit = {
    val actionValue = JsonValueSource(FullGsonBuilder.getSerializationGsonBuilder.create().toJsonTree(action, classOf[ActionObject]))
    val howValue = JsonValueSource(FullGsonBuilder.getSerializationGsonBuilder.create().toJsonTree(how, classOf[IWayObject]))
    val task = new TimerTask {
      override def run(): Unit = values.foreach { v =>
        val interpretationSource = new ObjectValueSource(
          ("action" -> actionValue),
          ("what" -> v),
          ("how" -> howValue)
        )
        World.findReceiver(interpretationSource).map(_.handleMessage(EveObjectMessage(action, v, howValue, to)))
      }
    }
    timer.schedule(task, when: Date)
  }
}
