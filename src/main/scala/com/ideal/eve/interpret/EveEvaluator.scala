package com.ideal.eve.interpret

import com.ideal.eve.db.EveDatabase
import com.ideal.eve.server.EveSession
import com.ideal.eve.universe.concurrent.TaskPool
import com.rokuan.calliopecore.sentence.{ActionObject, IAction}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.QuestionObject.QuestionType
import com.rokuan.calliopecore.sentence.structure.{AffirmationObject, InterpretationObject, OrderObject, QuestionObject}
import com.ideal.eve.universe._
import com.ideal.evecore.interpreter._
import com.mongodb.casbah.commons.MongoDBObject

/**
  * Created by Christophe on 10/10/2015.
  */

class EveEvaluator(val context: EveContext)(implicit session: EveSession) extends Evaluator[MongoDBObject] {
  val storage: EveDatabase = new EveDatabase()
}
