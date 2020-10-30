package migrate.internal.implementations

import interfaces.Migrate
import interfaces.MigrateService

final class MigrateImpl extends Migrate {
  override def getService: MigrateService = new MigrateServiceImpl()
}
