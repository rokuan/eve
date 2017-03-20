package com.ideal.eve.controller.auth

import com.ideal.eve.config.PropertyManager
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import resource.managed

import scala.util.Try

/**
 * Created by chris on 20/03/2017.
 */
class LdapAuth extends EveAuth {
  import LdapAuth._

  override def login(username: String, password: String) = Try {
    managed(new LdapNetworkConnection(LdapHost, LdapPort)) acquireAndGet { connection =>
      connection.bind(new Dn("ou=users,dc=ideal,dc=com").add("uid=" + username), password)
      connection.unBind()
      username
    }
  }
}

object LdapAuth {
  val HostProperty = classOf[EveAuth].getName + ".host"
  val PortProperty = classOf[EveAuth].getName + ".port"
  lazy val LdapHost = PropertyManager.get(HostProperty, "localhost")
  lazy val LdapPort = PropertyManager.get(PortProperty, 389)
}
