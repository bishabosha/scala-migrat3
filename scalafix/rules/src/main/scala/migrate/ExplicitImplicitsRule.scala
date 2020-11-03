package migrate

import scala.tools.nsc.reporters.StoreReporter
import scala.util.Failure
import scala.util.Success
import scala.util.control.NonFatal

import scala.meta.Tree
import scala.meta.internal.pc.ScalafixGlobal
import scala.meta.internal.proxy.GlobalProxy

import metaconfig.Configured
import scalafix.patch.Patch
import scalafix.v1._
import utils.CompilerService
import utils.ScalaExtensions._
import utils.SyntheticHelper

class ExplicitImplicitsRule(g: ScalafixGlobal) extends SemanticRule("ExplicitImplicits") {
  override def description: String = "Show implicit parameters and conversions"

  def this() = this(null)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    if (config.scalacClasspath.isEmpty) {
      Configured.error(s"config.scalacClasspath should not be empty")
    } else {
      val global = CompilerService.newGlobal(config.scalacClasspath, config.scalacOptions)
      global match {
        case Success(settings) =>
          Configured.ok(new ExplicitImplicitsRule(new ScalafixGlobal(settings, new StoreReporter, Map())))
        case Failure(exception) => Configured.error(exception.getMessage)
      }
    }

  override def afterComplete(): Unit =
    try {
      g.askShutdown()
      g.close()
    } catch {
      case NonFatal(_) =>
    }

  override def fix(implicit doc: SemanticDocument): Patch = {
    lazy implicit val unit: g.CompilationUnit = g.newCompilationUnit(doc.input.text, doc.input.syntax)

    doc.synthetics.flatMap {
      case syn @ ApplyTree(_, _) =>
        // case for implicit params
        if (syn.toString.startsWith("*")) {
          for {
            originalTree <- SyntheticHelper.getOriginalTree(syn)
            args         <- getImplicitParams(originalTree)
          } yield Patch.addRight(originalTree, "(" + args.mkString(", ") + ")")
        }
        // case for implicit conversions
        else if (syn.toString().contains("(*)")) {
          println(s"patch wrap from ${syn} at ${syn.symbol.get.normalized}")
          for {
            originalTree <- SyntheticHelper.getOriginalTree(syn)
            gt           <- getGlobalTree(originalTree)
          } yield Patch.replaceTree(originalTree, gt.toString)
        } else None
      case _ => None
    }.toList.asPatch
  }

  def getImplicitParams(originalTree: Tree)(implicit unit: g.CompilationUnit): Option[List[String]] =
    for {
      term       <- SyntheticHelper.getTermName(originalTree)
      context    <- CompilerService.getContext(term, g)
      globalTree <- getTreeFromContext(context)
      args <- globalTree match {
                case g.Apply(_, args) if globalTree.isInstanceOf[g.ApplyToImplicitArgs] => {
                  val listOfArgs = args.map(_.symbol.asInstanceOf[g.Symbol])
                  listOfArgs.map(printSymbol).sequence
                }
                case g.Apply(_, args) if args.nonEmpty && args.head.isInstanceOf[g.ApplyToImplicitArgs] => {
                  val newArgs    = args.head.asInstanceOf[g.Apply].args
                  val listOfArgs = newArgs.map(_.symbol.asInstanceOf[g.Symbol])
                  listOfArgs.map(printSymbol).sequence
                }
                case _ => None
              }
    } yield args

  private def getGlobalTree(originalTree: Tree)(implicit unit: g.CompilationUnit): Option[Any] =
    for {
      context    <- CompilerService.getContextDefault(originalTree, g)
      globalTree <- getTreeFromContext(context)
    } yield sanitizeGlobalTree(globalTree)

  private def sanitizeGlobalTree(globalTree: g.Tree): g.Tree = {
    globalTree
  }

  private def printSymbol(symbol: g.Symbol): Option[String] =
    if (symbol.isLocalToBlock && !symbol.name.startsWith("evidence$"))
      Some(symbol.name.toString())
    else if (symbol.isStatic) Some(symbol.fullName)
    else None

  private def getTreeFromContext(context: g.Context): Option[g.Tree] =
    context.tree match {
      case g.Apply(_, args) if args.nonEmpty =>
        Option(context.tree.asInstanceOf[g.Tree])
      case _ =>
        val gtree2 = GlobalProxy.typedTreeAt(g, context.tree.pos)
        Option(gtree2.asInstanceOf[g.Tree])
    }
}
