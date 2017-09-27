package com.jensraaby.ddia.graphql

import sangria.schema._

package object graphql {

  val PictureType = ObjectType(
    "Picture",
    "The product picture",

    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType),
        description = Some("Picture CDN URL"),
        resolve = _.value.url
      )
    )
  )


  import sangria.macros.derive._
  implicit val PictureTypeFromMacro = deriveObjectType[Unit, Picture](
    ObjectTypeDescription("The product picture"),
    DocumentField("url", "Picture CDN URL")
  )


  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",

    fields[Unit, Identifiable](
      Field("id", StringType, resolve = _.value.id)
    )
  )

  val ProductType = deriveObjectType[Unit, Product](Interfaces(IdentifiableType), IncludeMethods("picture"))

  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[ProductRepo, Unit](
    Field("product", OptionType(ProductType),
      description = Some("Returns a product with specific `id`,"),
      arguments = Id :: Nil,
      resolve = c => c.ctx.product(c arg Id)
    ),

    Field("products", ListType(ProductType),
      description = Some("Returns a list of all available products."),
      resolve = _.ctx.products
    )
  ))

  val schema = Schema(QueryType)
}
