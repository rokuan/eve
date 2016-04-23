package com.ideal.eve.universe.route.implementation

import com.ideal.eve.universe.{EveReceiver, ObjectValueSource}
import com.ideal.eve.universe.ValueMatcher.Mapping
import com.ideal.eve.universe.route.{Automaton, State}

/**
  * Created by Christophe on 17/04/2016.
  */
class ReceiverAutomaton extends Automaton[ObjectValueSource, EveReceiver] {
  val finalState = new State[EveReceiver](State.FinalStateName)
  val states = collection.mutable.Map[String, State[EveReceiver]]()
  val initialStates = collection.mutable.Set[State[EveReceiver]]()

  override def add(o: EveReceiver): Unit = {
    val sortedMappings = o.getMappings().sortBy(_._1)

    if(sortedMappings.isEmpty) {
      initialStates.add(finalState)
      finalState.addTerminal(o)
    } else {
      initialStates.add(buildReceiver(o, sortedMappings.toList))
    }
  }

  protected def buildReceiver(o: EveReceiver, mappings: List[Mapping]): State[EveReceiver] = {
    mappings match {
      case last :: Nil => {
        val state = getOrAddState(last._1)
        val terminalState = new State[EveReceiver](State.TerminalStateName)
        state.addNext(last._2, terminalState)
        state.getTerminalNext(last._2).map(_.addTerminal(o))
        state
      }
      case head :: tail => {
        val state = getOrAddState(head._1)
        val nextState = buildReceiver(o, tail)
        state.addNext(head._2, nextState)
        state
      }
      case Nil => finalState
    }
  }

  private def getOrAddState(n: String) = states.getOrElseUpdate(n, new State[EveReceiver](n))

  override def remove(o: EveReceiver): Unit = {
    if(o.getMappings().isEmpty){
      finalState.removeTerminal(o)
    } else {
      val initial = removeReceiver(o, o.getMappings().sortBy(_._1).toList)
      if(initial.isEmpty()) {
        initialStates.remove(initial)
      }
    }
  }

  protected def removeReceiver(o: EveReceiver, mappings: List[Mapping]): State[EveReceiver] = {
    mappings match {
      case last :: Nil => {
        val state = states(last._1)
        state.getTerminalNext(last._2).map { terminal =>
          terminal.removeTerminal(o)
          state.removeNext(last._2, terminal)
        }
        if(state.isEmpty()){
          states.remove(state.name)
        }
        state
      }
      case head :: tail => {
        val state = states(head._1)
        state.removeNext(head._2, removeReceiver(o, tail))
        if(state.isEmpty()){
          states.remove(state.name)
        }
        state
      }
      case Nil =>
        finalState.removeTerminal(o)
        finalState
    }
  }

  override def find(o: ObjectValueSource): Option[EveReceiver] = {
    val visited = collection.mutable.Map[String, Boolean]()

    def run(o: ObjectValueSource, s: State[EveReceiver]): Option[EveReceiver] = {
      if(visited.getOrElse(s.name, false)) {
        None
      } else if(s.isTerminal()){
        s.getResult()
      } else {
        visited.put(s.name, true)
        s.getNext().flatMap {
          case (matcher, states) if(o.getObject().get(s.name).map(matcher.matches(_)).getOrElse(false)) =>
            states.collect { case follower if !visited.getOrElse(follower.name, false) => follower }
              .flatMap(run(o, _))
              .collectFirst { case r => r }
          case _ => None
        }.collectFirst { case r => r }
      }
    }

    initialStates.collect { case s if (s.isTerminal() || o.getObject().get(s.name).isDefined) => s }
      .flatMap(run(o, _))
      .collectFirst { case r => r }
  }

  override def toString(): String = {
    val builder = new StringBuilder()
    val visited = collection.mutable.Map[String, Boolean]()

    def stateToString(s: State[EveReceiver], level: Int): Unit = {
      if(!visited.getOrElse(s.name, false)){
        visited.put(s.name, true)
        s.getNext().foreach { p =>
          builder.append("--" * level + " " + s.name + " => " + p._1.toString + "\n")
          p._2.foreach { stateToString(_, level + 1) }
        }
      }
    }

    initialStates.foreach { stateToString(_, 0) }
    builder.toString()
  }
}
