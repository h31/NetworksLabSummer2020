package domain

import io.estatico.newtype.macros.newtype

object page {
  @newtype case class HtmlContent(value: String)
}
