import mill._
import scalalib._
import coursier.maven.MavenRepository

object copris extends ScalaModule {
  def scalaVersion = "2.12.10"

  //def unmanagedClasspath = T {
  //  Agg.from(ammonite.ops.ls(millSourcePath / "lib").map(PathRef(_)))
  //}

  def mainClass = Some("org.sat4j.BasicLauncher")

  def repositories = super.repositories ++ Seq(
    MavenRepository("http://dist.wso2.org/maven2/")
  )

  override def ivyDeps = Agg(
    ivy"org.eclipse:org.sat4j.core:latest.integration",
    ivy"org.eclipse:org.sat4j.pb:latest.integration",
    ivy"org.apache.logging.log4j:log4j-core:latest.integration",
  )
 
  override def moduleDeps = Seq(sugar)
}

object sugar extends JavaModule {
  def mainClass = Some("jp.kobe_u.sugar.SugarMain")
}
