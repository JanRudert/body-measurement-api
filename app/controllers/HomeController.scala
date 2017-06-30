package controllers

import javax.inject.Inject

import play.api.mvc._

class HomeController @Inject()(cc: ControllerComponents, assets: Assets) extends AbstractController(cc) {

  def index: Action[AnyContent] = assets.at( path = "/public", file = "README.html" )

}

