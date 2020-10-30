package migrate.internal.implementations

import java.nio.file.Path

import interfaces.MigrateService
import migrate.AbsolutePath
import migrate.Classpath
import migrate.Main

final class MigrateServiceImpl() extends MigrateService {

  override def previewMigration(
    sourceRoot: Path,
    source: Path,
    scala2Cp: Array[Path],
    scala2CompilerOptions: Array[String],
    toolCp: Array[Path],
    scala3Cp: Array[Path],
    scala3CompilerOptions: Array[String],
    scala3ClassDirectory: Path
  ): String = {

    val sourceRootAbs   = AbsolutePath.from(sourceRoot).get
    val sourceAbs       = AbsolutePath.from(source).get
    val scala2Classpath = Classpath(scala2Cp.map(AbsolutePath.from(_).get).toList: _*)
    val toolClasspath   = Classpath(toolCp.map(AbsolutePath.from(_).get).toList: _*)
    val scala3Classpath = Classpath(scala3Cp.map(AbsolutePath.from(_).get).toList: _*)

    val scala3ClassDirectoryAbs = AbsolutePath.from(scala3ClassDirectory).get

    Main
      .previewMigration(
        sourceRoot = sourceRootAbs,
        source = sourceAbs,
        scala2Classpath = scala2Classpath,
        scala2CompilerOptions = scala2CompilerOptions.toList,
        toolClasspath = toolClasspath,
        scala3Classpath = scala3Classpath,
        scala3CompilerOptions = scala3CompilerOptions.toList,
        scala3ClassDirectory = scala3ClassDirectoryAbs
      )
      .get

  }

}
