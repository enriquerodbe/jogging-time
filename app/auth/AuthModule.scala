package auth

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.codingwell.scalaguice.ScalaModule
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext.Implicits.global
import user.UserService
import user.password.PasswordDao

class AuthModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]

    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)

    bind[RequestProvider].to[BasicAuthProvider].asEagerSingleton()

    bind[AuthenticatorService[DummyAuthenticator]]
      .toInstance(new DummyAuthenticatorService)
  }

  @Provides
  def provideEnvironment(
      userService: UserService,
      authenticatorService: AuthenticatorService[DummyAuthenticator],
      requestProvider: RequestProvider,
      eventBus: EventBus): Environment[AuthEnv] = {
    Environment[AuthEnv](
      userService, authenticatorService, Seq(requestProvider), eventBus)
  }

  @Provides
  def provideAuthInfoRepository(
      passwordDao: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordDao)
  }

  @Provides
  def providePasswordHasherRegistry(
      passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    PasswordHasherRegistry(passwordHasher)
  }

  @Provides
  def providePasswordDao(
      db: DatabaseConfigProvider): DelegableAuthInfoDAO[PasswordInfo] = {
    new PasswordDao(db)
  }
}
