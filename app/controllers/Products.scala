package controllers

import play.api.mvc.{Action, Controller}
import models.Product
import play.api.libs.json.{Reads, JsPath, Writes, Json}
import play.api.libs.functional.syntax._


object Products extends Controller {

  implicit object ProductWrites extends Writes[Product] {
    def writes(p: Product) = Json.obj(
      "ean" -> Json.toJson(p.ean),
      "name" -> Json.toJson(p.name),
      "description" -> Json.toJson(p.description)
    )
  }

  implicit val productReads: Reads[Product] = (
    (JsPath \ "ean").read[Long] and
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String]
  )(Product.apply _)

  def list = Action {
    val productCodes = Product.findAll.map(_.ean)

    Ok(Json.toJson(productCodes))
  }

  def details(ean: Long) = Action {
    Product.findByEan(ean).map { product =>
      Ok(Json.toJson(product))
    }.getOrElse(NotFound)
  }

  def save(ean: Long) = Action(parse.json) { request =>
    val productJson = request.body
    val product = productJson.as[Product]

    try {
      Product.save(product)
      Ok("Saved")
    }
    catch {
      case e:IllegalArgumentException =>
        BadRequest("Product not found")
    }
  }
}
