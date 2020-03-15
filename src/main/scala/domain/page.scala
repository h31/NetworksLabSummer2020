package domain

import io.estatico.newtype.macros.newsubtype

object page {
  @newsubtype case class Content(value: String)
  @newsubtype case class Page(content: Content)
}
