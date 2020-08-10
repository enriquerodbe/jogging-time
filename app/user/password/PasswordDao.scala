package user.password

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import domain.Password
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}

class PasswordDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext)
  extends PasswordsTable with DelegableAuthInfoDAO[PasswordInfo] {

  import profile.api._

  override implicit val classTag = scala.reflect.classTag[PasswordInfo]

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query =
      passwords
        .filter(_.userEmail === loginInfo.providerKey)
        .result
        .headOption
        .map(_.map(p => PasswordInfo(p.hasher, p.hash, p.salt)))

    db.run(query)
  }

  def addAction(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo): DBIO[PasswordInfo] = {
    val password = Password(
      loginInfo.providerKey, authInfo.hasher, authInfo.password, authInfo.salt)

    (passwords += password).map(_ => authInfo)
  }

  override def add(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(addAction(loginInfo, authInfo))
  }

  override def update(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def save(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo): Future[PasswordInfo] = ???

  def removeAction(loginInfo: LoginInfo): DBIO[Unit] = {
    passwords
      .filter(_.userEmail === loginInfo.providerKey)
      .delete
      .map(_ => ())
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    db.run(removeAction(loginInfo))
  }
}
