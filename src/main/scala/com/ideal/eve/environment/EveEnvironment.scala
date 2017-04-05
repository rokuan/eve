package com.ideal.eve.environment

import com.ideal.eve.interpret.EveDatabaseContext
import com.ideal.evecore.interpreter.Environment


/**
  * Created by Christophe on 28/12/2016.
  */
class EveEnvironment extends Environment {
  addContext(EveDatabaseContext())
}

object EveEnvironment extends Environment {

}
