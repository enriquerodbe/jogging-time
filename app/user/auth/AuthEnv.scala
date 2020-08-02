package user.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator
import domain.User

trait AuthEnv extends Env {
  type I = User
  type A = DummyAuthenticator
}
