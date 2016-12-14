package com.ideal.eve.interpret

import com.ideal.eve.db.EveDatabase
import com.ideal.eve.server.EveSession
import com.ideal.evecore.interpreter._
import com.mongodb.casbah.commons.MongoDBObject

/**
  * Created by Christophe on 10/10/2015.
  */

class EveEvaluator(val context: EveContext)(implicit session: EveSession) extends Evaluator[MongoDBObject] {
  val storage: EveDatabase = new EveDatabase()
}
