package com.ideal.eve.universe.route.implementation

import com.ideal.eve.universe.{EveReceiver, ValueMatcher}
import com.ideal.eve.universe.ValueMatcher.Mapping
import com.ideal.eve.universe.route.{Automaton, State}
import com.mongodb.DBObject

/**
  * Created by Christophe on 17/04/2016.
  */
class ReceiverAutomaton extends Automaton[DBObject, EveReceiver] {
  val finalState = new State[EveReceiver]("*")
  val states = collection.mutable.Map[String, State[EveReceiver]]()
  val initialStates = collection.mutable.Set[State[EveReceiver]]()

  override def add(o: EveReceiver): Unit = {
    val sortedMappings = o.getMappings().sortBy(_._1)
    initialStates.add(buildState(sortedMappings.toList))
    finalState.addTerminal(o)
  }

  protected def buildState(mappings: List[Mapping]): State[EveReceiver] = {
    mappings match {
      case Nil => finalState
      case head :: tail => {
        val state = getOrAddState(head._1)
        val nextState = buildState(tail)
        state.addNext(head._2, nextState)
        state
      }
    }
  }

  private def getOrAddState(n: String) = {
    states.get(n).getOrElse {
      val s = new State[EveReceiver](n)
      states.put(n, s)
      s
    }
  }

  override def remove(o: EveReceiver): Unit = unbuildState(o.getMappings().sortBy(_._1).toList)

  protected def unbuildState(mappings: List[Mapping]): Unit = {
    // TODO:
  }

  override def find(o: DBObject): Option[EveReceiver] = {
    val visited = collection.mutable.Map[String, Boolean]()

    def run(o: DBObject, s: State[EveReceiver]): Option[EveReceiver] = {
      if(visited.getOrElse(s.name, false)) {
        None
      } else if(s.isTerminal()){
        s.getResult()
      } else {
        visited.put(s.name, true)
        s.getNext().flatMap {
          // TODO: check that the value is in fact a String
          case (matcher, states) if Option(o.get(s.name)).map(v => matcher.matches(v.toString)).getOrElse(false) =>
            states.collect { case follower if !visited.getOrElse(follower.name, false) => follower }
              .flatMap(run(o, _))
              .collectFirst { case r => r }
          case _ => None
        }.collectFirst { case r => r }
      }
    }

    initialStates.collect { case s if (s.isTerminal() || o.get(s.name) != null) => s }
      .flatMap(run(o, _))
      .collectFirst { case r => r }
  }
}
