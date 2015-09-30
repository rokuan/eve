package interpret

/**
 * Created by Christophe on 27/09/2015.
 */

sealed trait EveObject

case class EveBooleanObject(b: Boolean) extends EveObject
case class EveIntObject(n: Int) extends EveObject
case class EveStringObject(s: String) extends EveObject


