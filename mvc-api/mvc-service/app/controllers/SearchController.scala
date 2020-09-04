package controllers

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import com.google.inject.name.Named
import managers.SearchManager
import play.api.mvc.ControllerComponents
import utils.{ActorNames, ApiId}

import scala.concurrent.ExecutionContext
class SearchController @Inject()(@Named(ActorNames.SEARCH_ACTOR) searchActor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends SearchBaseController(cc) {

    val mgr: SearchManager = new SearchManager()

    def search(mode: Option[String]) = Action.async { implicit request =>
        val modeForVector = mode.getOrElse("")
        val internalReq = getRequest(ApiId.APPLICATION_MVCSEARCH,modeForVector)
        setHeaderContext(internalReq)
        getResult(mgr.search(internalReq, searchActor), ApiId.APPLICATION_MVCSEARCH)
    }

    def count() = Action.async { implicit request =>
        val internalReq = getRequest(ApiId.APPLICATION_COUNT)
        setHeaderContext(internalReq)
        getResult(mgr.count(internalReq, searchActor), ApiId.APPLICATION_COUNT)
    }
}
