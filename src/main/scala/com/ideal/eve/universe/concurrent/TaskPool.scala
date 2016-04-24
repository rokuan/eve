package com.ideal.eve.universe.concurrent

import java.util.{Date, Timer, TimerTask}

import com.ideal.eve.universe.{Message, ObjectValueSource, World}
import com.rokuan.calliopecore.sentence.structure.data.nominal.VerbalGroup
import com.rokuan.calliopecore.sentence.structure.data.time.TimeAdverbial
import com.ideal.eve.utils.TimeObjectConversions._

/**
  * Created by Christophe on 24/04/2016.
  */
object TaskPool {
  val timer = new Timer()

  def scheduleDelayedTask(values: List[ObjectValueSource], message: Message, time: TimeAdverbial) = {
    time match {
      case v: VerbalGroup =>
      case _ => scheduleDelayedTask(values, message, time: Date)
    }
  }

  def scheduleDelayedTask(values: List[ObjectValueSource], message: Message, date: Date) = {
    val task = new TimerTask {
      override def run(): Unit = values.foreach { v => World.findReceiver(v).map(_.handleMessage(message)) }
    }
    timer.schedule(task, date)
  }
}
