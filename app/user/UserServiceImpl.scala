package user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import domain.UserRole.UserRole
import domain.{Page, User, UserField}
import filter.{Eq, FilterOptions}
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import user.password.PasswordDao

private[user] class UserServiceImpl @Inject()(
    val dbConfigProvider: DatabaseConfigProvider,
    userDao: UserDao,
    passwordHasher: PasswordHasher,
    passwordDao: PasswordDao)(
    implicit ec: ExecutionContext)
  extends UserService with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  override def create(user: User, plainPassword: String): Future[User] = {
    val loginInfo = LoginInfo(BasicAuthProvider.ID, user.email)
    val hashedPassword = passwordHasher.hash(plainPassword)

    val query = for {
      user <- userDao.create(user)
      _ <- passwordDao.addAction(loginInfo, hashedPassword)
    } yield user

    db.run(query.transactionally)
  }

  override def retrieve(filter: FilterOptions): Future[Page[User]] = {
    val query = for {
      total <- userDao.count(filter.condition)
      results <- userDao.retrieve(filter)
    } yield Page(results, total, results.size, filter.offset)

    db.run(query)
  }

  override def update(user: User): Future[Unit] = {
    db.run(userDao.update(user)).map(_ => ())
  }

  override def updateRoles(
      userId: Long,
      add: Set[UserRole],
      remove: Set[UserRole]): Future[Unit] = {
    val query = for {
      user <- userDao.retrieve(userId)
      _ <- userDao.updateRoles(user.id, user.roles ++ add -- remove)
    } yield ()

    db.run(query.transactionally)
  }

  override def delete(id: Long): Future[Unit] = {
    val query = for {
      user <- userDao.retrieve(id)
      _ <- passwordDao.removeAction(LoginInfo(BasicAuthProvider.ID, user.email))
      _ <- userDao.delete(id)
    } yield ()

    db.run(query.transactionally)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val condition = Eq(UserField.Email, loginInfo.providerKey)
    retrieve(FilterOptions(condition)).map(_.results.headOption)
  }
}
