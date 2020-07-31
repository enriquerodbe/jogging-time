package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject()(
    val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action { _ =>
    Ok("It works!")
  }
}
