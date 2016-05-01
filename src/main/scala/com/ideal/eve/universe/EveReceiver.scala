package com.ideal.eve.universe

import java.util.concurrent.ConcurrentLinkedQueue

import com.ideal.eve.universe.ValueMatcher.Mapping
import com.mongodb.DBObject

/**
  * Created by Christophe on 24/12/2015.
  */
trait EveReceiver {
  def initReceiver(): Unit
  def handleMessage(message: EveMessage)
  def destroyReceiver(): Unit
  def getMappings(): Seq[Mapping]
}

class EveDBReceiver(val mappings: Seq[Mapping]) extends Thread with EveReceiver {
  private val messagesQueue = new ConcurrentLinkedQueue[EveMessage]()
  private var running = true

  override def initReceiver(): Unit = {}

  override def run(): Unit = {
    while(running){

    }
  }

  override def handleMessage(message: EveMessage): Unit = {

  }

  override def destroyReceiver(): Unit = {

  }

  override def getMappings() = mappings
}
