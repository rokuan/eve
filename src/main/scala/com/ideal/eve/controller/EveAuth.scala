package com.ideal.eve.controller

import com.ideal.eve.config.PropertyManager
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.ldap.client.api.LdapNetworkConnection

import scala.util.Try
import resource.managed

/**
 * Created by Christophe on 31/01/2016.
 */
object EveAuth {
  val HostProperty = classOf[EveAuth].getName + ".host"
  val PortProperty = classOf[EveAuth].getName + ".port"

  def login(login: String, password: String) = Try {
    managed(new LdapNetworkConnection(PropertyManager.get(HostProperty, "localhost"),
      PropertyManager.get(PortProperty, 389))) acquireAndGet { connection =>

      connection.bind(new Dn("ou=users,dc=ideal,dc=com").add("uid=" + login), password)
      connection.unBind()

      login
    }
  }
}

class EveAuth
