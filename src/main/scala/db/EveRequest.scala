package db

/**
 * Created by Christophe on 04/10/2015.
 */

sealed trait EveRequest

class GetRequest extends EveRequest
class SetRequest extends EveRequest
