package migrate

import scala.util.Try

import interfaces.Migrate
import org.scalatest.funsuite.AnyFunSuiteLike

class InterfacesSuite extends AnyFunSuiteLike {
  test("Fetch and load succesfully") {
    val api: Try[Migrate] = Try(Migrate.fetchAndClassloadInstance())
    assert(api.isSuccess)
  }

}
